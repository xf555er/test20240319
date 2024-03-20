package org.apache.fop.render;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.Area;
import org.apache.fop.area.BeforeFloat;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockParent;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.CTM;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MainReference;
import org.apache.fop.area.NormalFlow;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageSequence;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Span;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineBlock;
import org.apache.fop.area.inline.InlineBlockParent;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.InlineViewport;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.ChangeBar;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.Visibility;
import org.w3c.dom.Document;

public abstract class AbstractRenderer implements Renderer, Constants {
   protected static final Log log = LogFactory.getLog("org.apache.fop.render");
   protected FOUserAgent userAgent;
   protected int currentBPPosition;
   protected int currentIPPosition;
   protected int containingBPPosition;
   protected int containingIPPosition;
   protected int columnStartIPPosition;
   protected int columnEndIPPosition;
   protected int columnLeftIPPosition;
   protected int columnRightIPPosition;
   protected int columnCount;
   protected int columnIndex;
   protected int columnWidth;
   protected int columnGap;
   protected Direction blockProgressionDirection;
   protected Direction inlineProgressionDirection;
   protected boolean bindingOnStartEdge;
   protected boolean bindingOnEndEdge;
   private int beginOffset;
   protected PageViewport currentPageViewport;
   private Set warnedXMLHandlers;
   private Stack layers;

   public abstract void setupFontInfo(FontInfo var1) throws FOPException;

   public AbstractRenderer(FOUserAgent userAgent) {
      this.userAgent = userAgent;
   }

   public FOUserAgent getUserAgent() {
      return this.userAgent;
   }

   public void startRenderer(OutputStream outputStream) throws IOException {
      if (this.userAgent == null) {
         throw new IllegalStateException("FOUserAgent has not been set on Renderer");
      }
   }

   public void stopRenderer() throws IOException {
   }

   public boolean supportsOutOfOrder() {
      return false;
   }

   public void setDocumentLocale(Locale locale) {
   }

   public void processOffDocumentItem(OffDocumentItem odi) {
   }

   public Graphics2DAdapter getGraphics2DAdapter() {
      return null;
   }

   public ImageAdapter getImageAdapter() {
      return null;
   }

   protected PageViewport getCurrentPageViewport() {
      return this.currentPageViewport;
   }

   public void preparePage(PageViewport page) {
   }

   protected String convertTitleToString(LineArea title) {
      List children = title.getInlineAreas();
      String str = this.convertToString(children);
      return str.trim();
   }

   private String convertToString(List children) {
      StringBuffer sb = new StringBuffer();
      Iterator var3 = children.iterator();

      while(var3.hasNext()) {
         Object aChildren = var3.next();
         InlineArea inline = (InlineArea)aChildren;
         if (inline instanceof TextArea) {
            sb.append(((TextArea)inline).getText());
         } else if (inline instanceof InlineParent) {
            sb.append(this.convertToString(((InlineParent)inline).getChildAreas()));
         } else {
            sb.append(" ");
         }
      }

      return sb.toString();
   }

   /** @deprecated */
   public void startPageSequence(LineArea seqTitle) {
   }

   public void startPageSequence(PageSequence pageSequence) {
   }

   public void renderPage(PageViewport page) throws IOException, FOPException {
      this.currentPageViewport = page;

      try {
         Page p = page.getPage();
         this.renderPageAreas(p);
      } finally {
         this.currentPageViewport = null;
      }

   }

   protected void renderPageAreas(Page page) {
      RegionViewport viewport = page.getRegionViewport(57);
      if (viewport != null) {
         this.renderRegionViewport(viewport);
      }

      viewport = page.getRegionViewport(61);
      if (viewport != null) {
         this.renderRegionViewport(viewport);
      }

      viewport = page.getRegionViewport(58);
      if (viewport != null) {
         this.renderRegionViewport(viewport);
      }

      viewport = page.getRegionViewport(59);
      if (viewport != null) {
         this.renderRegionViewport(viewport);
      }

      viewport = page.getRegionViewport(56);
      if (viewport != null) {
         this.renderRegionViewport(viewport);
      }

   }

   protected void renderRegionViewport(RegionViewport port) {
      this.currentBPPosition = 0;
      this.currentIPPosition = 0;
      RegionReference regionReference = port.getRegionReference();
      this.handleRegionTraits(port);
      this.startVParea(regionReference.getCTM(), port.getClipRectangle());
      if (regionReference.getRegionClass() == 58) {
         assert regionReference instanceof BodyRegion;

         this.renderBodyRegion((BodyRegion)regionReference);
      } else {
         this.renderRegion(regionReference);
      }

      this.endVParea();
   }

   protected abstract void startVParea(CTM var1, Rectangle var2);

   protected abstract void endVParea();

   protected void handleRegionTraits(RegionViewport rv) {
   }

   protected void renderRegion(RegionReference region) {
      this.renderBlocks((Block)null, region.getBlocks());
   }

   protected void renderBodyRegion(BodyRegion region) {
      BeforeFloat bf = region.getBeforeFloat();
      if (bf != null) {
         this.renderBeforeFloat(bf);
      }

      MainReference mr = region.getMainReference();
      if (mr != null) {
         this.renderMainReference(mr);
      }

      Footnote foot = region.getFootnote();
      if (foot != null) {
         this.renderFootnote(foot);
      }

   }

   protected void renderBeforeFloat(BeforeFloat bf) {
      List blocks = bf.getChildAreas();
      if (blocks != null) {
         this.renderBlocks((Block)null, blocks);
         Block sep = bf.getSeparator();
         if (sep != null) {
            this.renderBlock(sep);
         }
      }

   }

   protected void renderFootnote(Footnote footnote) {
      this.currentBPPosition += footnote.getTop();
      List blocks = footnote.getChildAreas();
      if (blocks != null) {
         Block sep = footnote.getSeparator();
         if (sep != null) {
            this.renderBlock(sep);
         }

         this.renderBlocks((Block)null, blocks);
      }

   }

   protected void renderMainReference(MainReference mainReference) {
      Span span = null;
      List spans = mainReference.getSpans();
      int saveBPPos = this.currentBPPosition;
      int saveIPPos = this.currentIPPosition;
      int saveSpanBPPos = saveBPPos;

      for(Iterator var7 = spans.iterator(); var7.hasNext(); saveSpanBPPos = this.currentBPPosition) {
         Object span1 = var7.next();
         span = (Span)span1;
         this.columnCount = span.getColumnCount();
         this.columnGap = span.getColumnGap();
         this.columnWidth = span.getColumnWidth();
         this.blockProgressionDirection = (Direction)span.getTrait(Trait.BLOCK_PROGRESSION_DIRECTION);
         this.inlineProgressionDirection = (Direction)span.getTrait(Trait.INLINE_PROGRESSION_DIRECTION);
         int level = span.getBidiLevel();
         if (level < 0) {
            level = 0;
         }

         if ((level & 1) == 1) {
            this.currentIPPosition += span.getIPD();
            this.currentIPPosition += this.columnGap;
         }

         for(this.columnIndex = 0; this.columnIndex < this.columnCount; ++this.columnIndex) {
            NormalFlow flow = span.getNormalFlow(this.columnIndex);
            boolean isLeftToRight = this.inlineProgressionDirection == null || this.inlineProgressionDirection.getEnumValue() == 199;
            if (flow != null) {
               int pageIndex = this.currentPageViewport.getPageIndex();
               this.bindingOnStartEdge = false;
               this.bindingOnEndEdge = false;
               if (isLeftToRight) {
                  this.columnStartIPPosition = 0;
                  this.columnEndIPPosition = this.columnWidth;
                  this.columnLeftIPPosition = 0;
                  this.columnRightIPPosition = this.columnWidth;
                  if (this.blockProgressionDirection == null || this.blockProgressionDirection.isVertical()) {
                     if (pageIndex % 2 == 0) {
                        this.bindingOnStartEdge = true;
                     } else {
                        this.bindingOnEndEdge = true;
                     }
                  }
               } else {
                  this.columnStartIPPosition = this.columnWidth;
                  this.columnEndIPPosition = 0;
                  this.columnLeftIPPosition = 0;
                  this.columnRightIPPosition = this.columnWidth;
                  if (this.blockProgressionDirection == null || this.blockProgressionDirection.isVertical()) {
                     if (pageIndex % 2 == 0) {
                        this.bindingOnEndEdge = true;
                     } else {
                        this.bindingOnStartEdge = true;
                     }
                  }
               }

               this.currentBPPosition = saveSpanBPPos;
               if ((level & 1) == 1) {
                  this.currentIPPosition -= flow.getIPD();
                  this.currentIPPosition -= this.columnGap;
               }

               this.renderFlow(flow);
               if ((level & 1) == 0) {
                  this.currentIPPosition += flow.getIPD();
                  this.currentIPPosition += this.columnGap;
               }
            }
         }

         this.currentIPPosition = saveIPPos;
         this.currentBPPosition = saveSpanBPPos + span.getHeight();
      }

      this.currentBPPosition = saveBPPos;
   }

   protected void renderFlow(NormalFlow flow) {
      List blocks = flow.getChildAreas();
      if (blocks != null) {
         this.renderBlocks((Block)null, blocks);
      }

   }

   protected void handleBlockTraits(Block block) {
   }

   protected void renderBlockViewport(BlockViewport bv, List children) {
      boolean inNewLayer = false;
      if (this.maybeStartLayer(bv)) {
         inNewLayer = true;
      }

      int saveIP;
      int saveBP;
      if (bv.getPositioning() == 2) {
         saveIP = this.currentIPPosition;
         saveBP = this.currentBPPosition;
         Rectangle clippingRect = null;
         if (bv.hasClip()) {
            clippingRect = new Rectangle(saveIP, saveBP, bv.getIPD(), bv.getBPD());
         }

         CTM ctm = bv.getCTM();
         this.currentIPPosition = 0;
         this.currentBPPosition = 0;
         this.startVParea(ctm, clippingRect);
         this.handleBlockTraits(bv);
         this.renderBlocks(bv, children);
         this.endVParea();
         this.currentIPPosition = saveIP;
         this.currentBPPosition = saveBP;
      } else {
         saveIP = this.currentIPPosition;
         saveBP = this.currentBPPosition;
         this.handleBlockTraits(bv);
         this.renderBlocks(bv, children);
         this.currentIPPosition = saveIP;
         this.currentBPPosition = saveBP + bv.getAllocBPD();
      }

      this.maybeEndLayer(bv, inNewLayer);
   }

   protected abstract void renderReferenceArea(Block var1);

   protected void renderBlocks(Block parent, List blocks) {
      int saveIP = this.currentIPPosition;
      if (parent != null && !parent.getTraitAsBoolean(Trait.IS_VIEWPORT_AREA)) {
         this.currentBPPosition += parent.getBorderAndPaddingWidthBefore();
      }

      int contBP = this.currentBPPosition;
      int contIP = this.currentIPPosition;
      this.containingBPPosition = this.currentBPPosition;
      this.containingIPPosition = this.currentIPPosition;

      for(Iterator var6 = blocks.iterator(); var6.hasNext(); this.currentIPPosition = saveIP) {
         Object obj = var6.next();
         if (obj instanceof Block) {
            this.currentIPPosition = contIP;
            this.containingBPPosition = contBP;
            this.containingIPPosition = contIP;
            this.renderBlock((Block)obj);
            this.containingBPPosition = contBP;
            this.containingIPPosition = contIP;
         } else if (obj instanceof LineArea) {
            LineArea line = (LineArea)obj;
            if (parent != null) {
               int level = parent.getBidiLevel();
               if (level != -1 && (level & 1) != 0) {
                  this.currentIPPosition += parent.getEndIndent();
               } else {
                  this.currentIPPosition += parent.getStartIndent();
               }
            }

            this.renderLineArea(line);
            this.currentBPPosition += line.getAllocBPD();
         }
      }

   }

   protected void renderBlock(Block block) {
      assert block != null;

      List changeBarList = block.getChangeBarList();
      if (changeBarList != null && !changeBarList.isEmpty()) {
         int saveIP = this.currentIPPosition;
         int saveBP = this.currentBPPosition;
         this.drawChangeBars(block, changeBarList);
         this.currentIPPosition = saveIP;
         this.currentBPPosition = saveBP;
      }

      List children = block.getChildAreas();
      boolean inNewLayer = false;
      if (this.maybeStartLayer(block)) {
         inNewLayer = true;
      }

      if (block instanceof BlockViewport) {
         if (children != null) {
            this.renderBlockViewport((BlockViewport)block, children);
         } else {
            this.handleBlockTraits(block);
            this.currentBPPosition += block.getAllocBPD();
         }
      } else if (block.getTraitAsBoolean(Trait.IS_REFERENCE_AREA)) {
         this.renderReferenceArea(block);
      } else {
         int saveIP = this.currentIPPosition;
         int saveBP = this.currentBPPosition;
         this.currentIPPosition += block.getXOffset();
         this.currentBPPosition += block.getYOffset();
         this.currentBPPosition += block.getSpaceBefore();
         this.handleBlockTraits(block);
         if (children != null && block.getTrait(Trait.VISIBILITY) != Visibility.HIDDEN) {
            this.renderBlocks(block, children);
         }

         if (block.getPositioning() == 2) {
            this.currentBPPosition = saveBP;
         } else {
            this.currentIPPosition = saveIP;
            this.currentBPPosition = saveBP + block.getAllocBPD();
         }
      }

      this.maybeEndLayer(block, inNewLayer);
   }

   protected void renderInlineBlock(InlineBlock inlineBlock) {
      this.renderBlock(inlineBlock.getBlock());
   }

   protected abstract void startLayer(String var1);

   protected abstract void endLayer();

   protected boolean maybeStartLayer(Area area) {
      String layer = (String)area.getTrait(Trait.LAYER);
      if (layer != null) {
         if (this.layers == null) {
            this.layers = new Stack();
         }

         if (this.layers.empty() || !((String)this.layers.peek()).equals(layer)) {
            this.layers.push(layer);
            this.startLayer(layer);
            return true;
         }
      }

      return false;
   }

   protected void maybeEndLayer(Area area, boolean inNewLayer) {
      if (inNewLayer) {
         assert this.layers != null;

         assert !this.layers.empty();

         String layer = (String)area.getTrait(Trait.LAYER);

         assert layer != null;

         assert ((String)this.layers.peek()).equals(layer);

         this.endLayer();
         this.layers.pop();
      }

   }

   protected void renderLineArea(LineArea line) {
      List children = line.getInlineAreas();
      int saveBP = this.currentBPPosition;
      this.currentBPPosition += line.getSpaceBefore();
      int bl = line.getBidiLevel();
      if (bl >= 0) {
         if ((bl & 1) == 0) {
            this.currentIPPosition += line.getStartIndent();
         } else {
            this.currentIPPosition += line.getEndIndent();
         }
      } else {
         this.currentIPPosition += line.getStartIndent();
      }

      Iterator var5 = children.iterator();

      while(var5.hasNext()) {
         Object aChildren = var5.next();
         InlineArea inline = (InlineArea)aChildren;
         this.renderInlineArea(inline);
      }

      this.currentBPPosition = saveBP;
   }

   protected void renderInlineArea(InlineArea inlineArea) {
      List changeBarList = inlineArea.getChangeBarList();
      if (changeBarList != null && !changeBarList.isEmpty()) {
         this.drawChangeBars(inlineArea, changeBarList);
      }

      if (inlineArea instanceof TextArea) {
         this.renderText((TextArea)inlineArea);
      } else if (inlineArea instanceof WordArea) {
         this.renderWord((WordArea)inlineArea);
      } else if (inlineArea instanceof SpaceArea) {
         this.renderSpace((SpaceArea)inlineArea);
      } else if (inlineArea instanceof InlineBlock) {
         this.renderInlineBlock((InlineBlock)inlineArea);
      } else if (inlineArea instanceof InlineParent) {
         this.renderInlineParent((InlineParent)inlineArea);
      } else if (inlineArea instanceof InlineBlockParent) {
         this.renderInlineBlockParent((InlineBlockParent)inlineArea);
      } else if (inlineArea instanceof Space) {
         this.renderInlineSpace((Space)inlineArea);
      } else if (inlineArea instanceof InlineViewport) {
         this.renderInlineViewport((InlineViewport)inlineArea);
      } else if (inlineArea instanceof Leader) {
         this.renderLeader((Leader)inlineArea);
      }

   }

   protected abstract void renderInlineAreaBackAndBorders(InlineArea var1);

   protected void renderInlineSpace(Space space) {
      this.renderInlineAreaBackAndBorders(space);
      this.currentIPPosition += space.getAllocIPD();
   }

   protected void renderLeader(Leader area) {
      this.currentIPPosition += area.getAllocIPD();
   }

   protected void renderText(TextArea text) {
      List children = text.getChildAreas();
      int saveIP = this.currentIPPosition;
      int saveBP = this.currentBPPosition;
      List changeBarList = text.getChangeBarList();
      if (changeBarList != null && !changeBarList.isEmpty()) {
         this.drawChangeBars(text, changeBarList);
         this.currentIPPosition = saveIP;
         this.currentBPPosition = saveBP;
      }

      Iterator var6 = children.iterator();

      while(var6.hasNext()) {
         Object aChildren = var6.next();
         InlineArea inline = (InlineArea)aChildren;
         this.renderInlineArea(inline);
      }

      this.currentIPPosition = saveIP + text.getAllocIPD();
   }

   protected void renderWord(WordArea word) {
      this.currentIPPosition += word.getAllocIPD();
   }

   protected void renderSpace(SpaceArea space) {
      this.currentIPPosition += space.getAllocIPD();
   }

   protected void renderInlineParent(InlineParent ip) {
      boolean inNewLayer = false;
      if (this.maybeStartLayer(ip)) {
         inNewLayer = true;
      }

      int level = ip.getBidiLevel();
      List children = ip.getChildAreas();
      this.renderInlineAreaBackAndBorders(ip);
      int saveIP = this.currentIPPosition;
      int saveBP = this.currentBPPosition;
      int ipAdjust;
      if (ip instanceof FilledArea && (level & 1) != 0) {
         int ipdChildren = 0;

         InlineArea inline;
         for(Iterator var9 = children.iterator(); var9.hasNext(); ipdChildren += inline.getAllocIPD()) {
            Object aChildren = var9.next();
            inline = (InlineArea)aChildren;
         }

         ipAdjust = ip.getAllocIPD() - ipdChildren;
      } else {
         ipAdjust = 0;
      }

      if (level != -1 && (level & 1) != 0) {
         this.currentIPPosition += ip.getBorderAndPaddingWidthEnd();
         if (ipAdjust > 0) {
            this.currentIPPosition += ipAdjust;
         }
      } else {
         this.currentIPPosition += ip.getBorderAndPaddingWidthStart();
      }

      this.currentBPPosition += ip.getBlockProgressionOffset();
      Iterator var12 = children.iterator();

      while(var12.hasNext()) {
         Object aChildren = var12.next();
         InlineArea inline = (InlineArea)aChildren;
         this.renderInlineArea(inline);
      }

      this.currentIPPosition = saveIP + ip.getAllocIPD();
      this.currentBPPosition = saveBP;
      this.maybeEndLayer(ip, inNewLayer);
   }

   protected void renderInlineBlockParent(InlineBlockParent ibp) {
      int level = ibp.getBidiLevel();
      this.renderInlineAreaBackAndBorders(ibp);
      if (level != -1 && (level & 1) != 0) {
         this.currentIPPosition += ibp.getBorderAndPaddingWidthEnd();
      } else {
         this.currentIPPosition += ibp.getBorderAndPaddingWidthStart();
      }

      int saveBP = this.currentBPPosition;
      this.currentBPPosition += ibp.getBlockProgressionOffset();
      this.renderBlock(ibp.getChildArea());
      this.currentBPPosition = saveBP;
   }

   protected void renderInlineViewport(InlineViewport viewport) {
      Area content = viewport.getContent();
      int saveBP = this.currentBPPosition;
      this.currentBPPosition += viewport.getBlockProgressionOffset();
      Rectangle2D contpos = viewport.getContentPosition();
      if (content instanceof Image) {
         this.renderImage((Image)content, contpos);
      } else if (content instanceof Container) {
         this.renderContainer((Container)content);
      } else if (content instanceof ForeignObject) {
         this.renderForeignObject((ForeignObject)content, contpos);
      } else if (content instanceof InlineBlockParent) {
         this.renderInlineBlockParent((InlineBlockParent)content);
      }

      this.currentIPPosition += viewport.getAllocIPD();
      this.currentBPPosition = saveBP;
   }

   public void renderImage(Image image, Rectangle2D pos) {
      List changeBarList = image.getChangeBarList();
      if (changeBarList != null && !changeBarList.isEmpty()) {
         this.drawChangeBars(image, changeBarList);
      }

   }

   protected void renderContainer(Container cont) {
      int saveIP = this.currentIPPosition;
      int saveBP = this.currentBPPosition;
      List blocks = cont.getBlocks();
      this.renderBlocks((Block)null, blocks);
      this.currentIPPosition = saveIP;
      this.currentBPPosition = saveBP;
   }

   protected void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
      List changeBarList = fo.getChangeBarList();
      if (changeBarList != null && !changeBarList.isEmpty()) {
         this.drawChangeBars(fo, changeBarList);
      }

   }

   public void renderXML(RendererContext ctx, Document doc, String namespace) {
      XMLHandler handler = this.userAgent.getXMLHandlerRegistry().getXMLHandler(this, (String)namespace);
      if (handler != null) {
         try {
            XMLHandlerConfigurator configurator = new XMLHandlerConfigurator(this.userAgent);
            configurator.configure(ctx, namespace);
            handler.handleXML(ctx, doc, namespace);
         } catch (Exception var7) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(ctx.getUserAgent().getEventBroadcaster());
            eventProducer.foreignXMLProcessingError(this, doc, namespace, var7);
         }
      } else {
         if (this.warnedXMLHandlers == null) {
            this.warnedXMLHandlers = new HashSet();
         }

         if (!this.warnedXMLHandlers.contains(namespace)) {
            this.warnedXMLHandlers.add(namespace);
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(ctx.getUserAgent().getEventBroadcaster());
            eventProducer.foreignXMLNoHandler(this, doc, namespace);
         }
      }

   }

   protected AffineTransform mptToPt(AffineTransform at) {
      double[] matrix = new double[6];
      at.getMatrix(matrix);
      matrix[4] /= 1000.0;
      matrix[5] /= 1000.0;
      return new AffineTransform(matrix);
   }

   protected AffineTransform ptToMpt(AffineTransform at) {
      double[] matrix = new double[6];
      at.getMatrix(matrix);
      matrix[4] = (double)Math.round(matrix[4] * 1000.0);
      matrix[5] = (double)Math.round(matrix[5] * 1000.0);
      return new AffineTransform(matrix);
   }

   protected void drawChangeBars(Area area, List changeBarList) {
      if (!area.getTraitAsBoolean(Trait.IS_REFERENCE_AREA)) {
         int saveIP = this.currentIPPosition;
         int saveBP = this.currentBPPosition;
         int currentColumnStartIP = this.columnStartIPPosition;
         int currentColumnEndIP = this.columnEndIPPosition;
         int currentColumnLeftIP = this.columnLeftIPPosition;
         int currentColumnRightIP = this.columnRightIPPosition;

         for(Iterator var10 = changeBarList.iterator(); var10.hasNext(); this.currentBPPosition = saveBP) {
            ChangeBar changeBar = (ChangeBar)var10.next();
            boolean isLeftToRight = this.inlineProgressionDirection == null || this.inlineProgressionDirection.getEnumValue() == 199;
            Block changeBarArea = new Block();
            this.currentIPPosition = 0;
            this.currentBPPosition = saveBP;
            int changeBarWidth = changeBar.getWidth().getValue();
            int changeBarOffset = changeBar.getOffset().getValue();
            if (isLeftToRight) {
               currentColumnStartIP = this.columnStartIPPosition - changeBarWidth;
               currentColumnLeftIP = this.columnLeftIPPosition - changeBarWidth;
            } else {
               currentColumnEndIP = this.columnEndIPPosition - changeBarWidth;
               currentColumnLeftIP = this.columnLeftIPPosition - changeBarWidth;
            }

            int xOffset = currentColumnStartIP;
            int xScale = -1;
            switch (changeBar.getPlacement()) {
               case 39:
                  xOffset = currentColumnEndIP;
                  xScale = 1;
                  break;
               case 68:
                  if (this.bindingOnStartEdge) {
                     xOffset = currentColumnStartIP;
                     xScale = -1;
                  } else if (this.bindingOnEndEdge) {
                     xOffset = currentColumnEndIP;
                     xScale = 1;
                  } else {
                     xOffset = currentColumnStartIP;
                     xScale = -1;
                  }
                  break;
               case 73:
                  xOffset = currentColumnLeftIP;
                  xScale = isLeftToRight ? -1 : 1;
                  break;
               case 102:
                  if (this.bindingOnStartEdge) {
                     xOffset = this.columnEndIPPosition;
                     xScale = 1;
                  } else if (this.bindingOnEndEdge) {
                     xOffset = this.columnStartIPPosition;
                     xScale = -1;
                  } else {
                     xOffset = this.columnStartIPPosition;
                     xScale = -1;
                  }
                  break;
               case 120:
                  xOffset = currentColumnRightIP;
                  xScale = isLeftToRight ? 1 : -1;
                  break;
               case 135:
                  xOffset = currentColumnStartIP;
                  xScale = -1;
                  break;
               case 198:
                  if (this.columnCount == 2) {
                     if (this.columnIndex == 0) {
                        xOffset = this.columnStartIPPosition;
                        xScale = -1;
                     } else {
                        xOffset = this.columnEndIPPosition;
                        xScale = 1;
                     }
                  } else if (this.bindingOnStartEdge) {
                     xOffset = this.columnEndIPPosition;
                     xScale = 1;
                  } else if (this.bindingOnEndEdge) {
                     xOffset = this.columnStartIPPosition;
                     xScale = -1;
                  } else {
                     xOffset = this.columnStartIPPosition;
                     xScale = -1;
                  }
            }

            if (isLeftToRight) {
               xOffset += xScale * changeBarOffset;
            } else {
               xOffset -= xScale * changeBarOffset;
            }

            xOffset += this.getBeginOffset();
            changeBarArea.setAreaClass(2);
            changeBarArea.setIPD(0);
            BorderProps props = BorderProps.makeRectangular(changeBar.getStyle(), changeBarWidth, changeBar.getColor(), BorderProps.Mode.SEPARATE);
            changeBarArea.addTrait(Trait.BORDER_START, props);
            changeBarArea.addTrait(Trait.BORDER_END, props);
            changeBarArea.setXOffset(xOffset);
            int areaHeight = area.getAllocBPD();
            if (area instanceof BlockParent) {
               changeBarArea.setBPD(areaHeight);
               changeBarArea.setYOffset(((BlockParent)area).getYOffset());
               this.renderBlock(changeBarArea);
            } else {
               if (areaHeight > 0) {
                  Property p = changeBar.getLineHeight().getOptimum(DummyPercentBaseContext.getInstance());
                  int lineHeight = p.getLength().getValue();
                  changeBarArea.setBPD(lineHeight);
                  changeBarArea.setYOffset(areaHeight - lineHeight);
               }

               this.renderInlineBlock(new InlineBlock(changeBarArea));
            }

            this.currentIPPosition = saveIP;
         }

      }
   }

   protected int getBeginOffset() {
      return this.beginOffset;
   }

   protected void setBeginOffset(int offset) {
      this.beginOffset = offset;
   }
}
