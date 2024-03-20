package org.apache.fop.render.xml;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.Area;
import org.apache.fop.area.AreaTreeObject;
import org.apache.fop.area.BeforeFloat;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.BookmarkData;
import org.apache.fop.area.CTM;
import org.apache.fop.area.DestinationData;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MainReference;
import org.apache.fop.area.NormalFlow;
import org.apache.fop.area.OffDocumentExtensionAttachment;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.PageSequence;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Span;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Container;
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
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.util.ColorUtil;
import org.apache.fop.util.LanguageTags;
import org.apache.fop.util.XMLUtil;
import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.util.XMLizable;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLRenderer extends AbstractXMLRenderer {
   public static final String VERSION = "2.0";
   public static final String XML_MIME_TYPE = "application/X-fop-areatree";
   private boolean startedSequence;
   private boolean compactFormat;
   protected Renderer mimic;

   public XMLRenderer(FOUserAgent userAgent) {
      super(userAgent);
      this.context = new RendererContext(this, "application/X-fop-areatree");
      XMLHandler xmlHandler = new XMLXMLHandler();
      userAgent.getXMLHandlerRegistry().addXMLHandler((XMLHandler)xmlHandler);
      Boolean b = (Boolean)userAgent.getRendererOptions().get("compact-format");
      if (b != null) {
         this.setCompactFormat(b);
      }

   }

   public void mimicRenderer(Renderer renderer) {
      this.mimic = renderer;
   }

   public void setupFontInfo(FontInfo inFontInfo) throws FOPException {
      if (this.mimic != null) {
         this.mimic.setupFontInfo(inFontInfo);
      } else {
         super.setupFontInfo(inFontInfo);
      }

   }

   public void setCompactFormat(boolean compact) {
      this.compactFormat = compact;
   }

   private boolean isDetailedFormat() {
      return !this.compactFormat;
   }

   protected void addAreaAttributes(Area area) {
      this.addAttribute("ipd", area.getIPD());
      this.addAttribute("bpd", area.getBPD());
      this.maybeAddLevelAttribute(area);
      if (this.isDetailedFormat()) {
         if (area.getIPD() != 0) {
            this.addAttribute("ipda", area.getAllocIPD());
         }

         if (area.getBPD() != 0) {
            this.addAttribute("bpda", area.getAllocBPD());
         }

         this.addAttribute("bap", area.getBorderAndPaddingWidthStart() + " " + area.getBorderAndPaddingWidthEnd() + " " + area.getBorderAndPaddingWidthBefore() + " " + area.getBorderAndPaddingWidthAfter());
      }

   }

   protected void addTraitAttributes(Area area) {
      Map traitMap = area.getTraits();
      if (traitMap != null) {
         Iterator var3 = traitMap.entrySet().iterator();

         label66:
         while(true) {
            while(true) {
               Map.Entry traitEntry;
               Object key;
               String name;
               Class clazz;
               do {
                  do {
                     if (!var3.hasNext()) {
                        break label66;
                     }

                     Object o = var3.next();
                     traitEntry = (Map.Entry)o;
                     key = traitEntry.getKey();
                     name = Trait.getTraitName(key);
                     clazz = Trait.getTraitClass(key);
                  } while("break-before".equals(name));
               } while("break-after".equals(name));

               Object value = traitEntry.getValue();
               if ((Integer)key == Trait.FONT) {
                  FontTriplet triplet = (FontTriplet)value;
                  this.addAttribute("font-name", triplet.getName());
                  this.addAttribute("font-style", triplet.getStyle());
                  this.addAttribute("font-weight", triplet.getWeight());
               } else if (clazz.equals(Trait.InternalLink.class)) {
                  Trait.InternalLink iLink = (Trait.InternalLink)value;
                  this.addAttribute(name, iLink.xmlAttribute());
               } else if (clazz.equals(Trait.Background.class)) {
                  Trait.Background bkg = (Trait.Background)value;
                  this.addAttribute(name, bkg.toString());
                  if (bkg.getColor() != null) {
                     this.addAttribute("bkg-color", ColorUtil.colorToString(bkg.getColor()));
                  }

                  if (bkg.getURL() != null) {
                     this.addAttribute("bkg-img", bkg.getURL());
                     int repeat = bkg.getRepeat();
                     String repString;
                     switch (repeat) {
                        case 96:
                           repString = "no-repeat";
                           break;
                        case 112:
                           repString = "repeat";
                           break;
                        case 113:
                           repString = "repeat-x";
                           break;
                        case 114:
                           repString = "repeat-y";
                           break;
                        default:
                           throw new IllegalStateException("Illegal value for repeat encountered: " + repeat);
                     }

                     this.addAttribute("bkg-repeat", repString);
                     this.addAttribute("bkg-horz-offset", bkg.getHoriz());
                     this.addAttribute("bkg-vert-offset", bkg.getVertical());
                  }
               } else if (clazz.equals(Color.class)) {
                  Color c = (Color)value;
                  this.addAttribute(name, ColorUtil.colorToString(c));
               } else if ((Integer)key != Trait.START_INDENT && (Integer)key != Trait.END_INDENT) {
                  this.addAttribute(name, value.toString());
               } else if ((Integer)value != 0) {
                  this.addAttribute(name, value.toString());
               }
            }
         }
      }

      this.transferForeignObjects(area);
   }

   private void transferForeignObjects(AreaTreeObject ato) {
      Map prefixes = new HashMap();
      Iterator iter = ato.getForeignAttributes().entrySet().iterator();

      Map.Entry entry;
      while(iter.hasNext()) {
         entry = (Map.Entry)iter.next();
         QName qname = (QName)entry.getKey();
         prefixes.put(qname.getPrefix(), qname.getNamespaceURI());
         this.addAttribute(qname, (String)entry.getValue());
      }

      iter = prefixes.entrySet().iterator();

      while(iter.hasNext()) {
         entry = (Map.Entry)iter.next();
         String qn = "xmlns:" + (String)entry.getKey();
         this.atts.addAttribute("", (String)entry.getKey(), qn, "CDATA", (String)entry.getValue());
      }

   }

   public void processOffDocumentItem(OffDocumentItem oDI) {
      if (oDI instanceof BookmarkData) {
         this.renderBookmarkTree((BookmarkData)oDI);
      } else if (oDI instanceof DestinationData) {
         this.renderDestination((DestinationData)oDI);
      } else if (oDI instanceof OffDocumentExtensionAttachment) {
         ExtensionAttachment attachment = ((OffDocumentExtensionAttachment)oDI).getAttachment();
         if (this.extensionAttachments == null) {
            this.extensionAttachments = new ArrayList();
         }

         this.extensionAttachments.add(attachment);
      } else {
         String warn = "Ignoring OffDocumentItem: " + oDI;
         log.warn(warn);
      }

   }

   protected void renderBookmarkTree(BookmarkData bookmarkRoot) {
      if (bookmarkRoot.getWhenToProcess() == 2) {
         this.endPageSequence();
      }

      this.startElement("bookmarkTree");

      for(int i = 0; i < bookmarkRoot.getCount(); ++i) {
         this.renderBookmarkItem(bookmarkRoot.getSubData(i));
      }

      this.endElement("bookmarkTree");
   }

   private void renderBookmarkItem(BookmarkData bm) {
      this.atts.clear();
      this.addAttribute("title", bm.getBookmarkTitle());
      this.addAttribute("show-children", String.valueOf(bm.showChildItems()));
      PageViewport pv = bm.getPageViewport();
      String pvKey = pv == null ? null : pv.getKey();
      this.addAttribute("internal-link", Trait.InternalLink.makeXMLAttribute(pvKey, bm.getIDRef()));
      this.startElement("bookmark", this.atts);

      for(int i = 0; i < bm.getCount(); ++i) {
         this.renderBookmarkItem(bm.getSubData(i));
      }

      this.endElement("bookmark");
   }

   protected void renderDestination(DestinationData destination) {
      if (destination.getWhenToProcess() == 2) {
         this.endPageSequence();
      }

      this.atts.clear();
      PageViewport pv = destination.getPageViewport();
      String pvKey = pv == null ? null : pv.getKey();
      this.addAttribute("internal-link", Trait.InternalLink.makeXMLAttribute(pvKey, destination.getIDRef()));
      this.startElement("destination", this.atts);
      this.endElement("destination");
   }

   public void startRenderer(OutputStream outputStream) throws IOException {
      log.debug("Rendering areas to Area Tree XML");
      if (this.handler == null) {
         SAXTransformerFactory factory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

         try {
            TransformerHandler transformerHandler = factory.newTransformerHandler();
            this.handler = transformerHandler;
            StreamResult res = new StreamResult(outputStream);
            transformerHandler.setResult(res);
         } catch (TransformerConfigurationException var6) {
            throw new RuntimeException(var6.getMessage());
         }

         this.out = outputStream;
      }

      try {
         this.handler.startDocument();
      } catch (SAXException var5) {
         this.handleSAXException(var5);
      }

      if (this.userAgent.getProducer() != null) {
         this.comment("Produced by " + this.userAgent.getProducer());
      }

      this.atts.clear();
      this.addAttribute("version", "2.0");
      this.startElement("areaTree", this.atts);
   }

   public void stopRenderer() throws IOException {
      this.endPageSequence();
      this.endElement("areaTree");

      try {
         this.handler.endDocument();
      } catch (SAXException var2) {
         this.handleSAXException(var2);
      }

      if (this.out != null) {
         this.out.flush();
      }

      log.debug("Written out Area Tree XML");
   }

   public void renderPage(PageViewport page) throws IOException, FOPException {
      this.atts.clear();
      this.addAttribute("bounds", page.getViewArea());
      this.addAttribute("key", page.getKey());
      this.addAttribute("nr", page.getPageNumber());
      this.addAttribute("formatted-nr", page.getPageNumberString());
      if (page.getSimplePageMasterName() != null) {
         this.addAttribute("simple-page-master-name", page.getSimplePageMasterName());
      }

      if (page.isBlank()) {
         this.addAttribute("blank", "true");
      }

      this.transferForeignObjects(page);
      this.startElement("pageViewport", this.atts);
      this.startElement("page");
      this.handlePageExtensionAttachments(page);
      super.renderPage(page);
      this.endElement("page");
      this.endElement("pageViewport");
   }

   protected void handleExtensionAttachments(List attachments) {
      if (attachments != null && attachments.size() > 0) {
         this.startElement("extension-attachments");
         Iterator var2 = attachments.iterator();

         while(var2.hasNext()) {
            Object attachment1 = var2.next();
            ExtensionAttachment attachment = (ExtensionAttachment)attachment1;
            if (attachment instanceof XMLizable) {
               try {
                  ((XMLizable)attachment).toSAX(this.handler);
               } catch (SAXException var6) {
                  log.error("Error while serializing Extension Attachment", var6);
               }
            } else {
               String warn = "Ignoring non-XMLizable ExtensionAttachment: " + attachment;
               log.warn(warn);
            }
         }

         this.endElement("extension-attachments");
      }

   }

   public void startPageSequence(PageSequence pageSequence) {
      this.handleDocumentExtensionAttachments();
      this.endPageSequence();
      this.startedSequence = true;
      this.atts.clear();
      Locale locale = pageSequence.getLocale();
      if (locale != null) {
         this.addAttribute(new QName("http://www.w3.org/XML/1998/namespace", "xml:lang"), LanguageTags.toLanguageTag(locale));
      }

      this.transferForeignObjects(pageSequence);
      this.startElement("pageSequence", this.atts);
      this.handleExtensionAttachments(pageSequence.getExtensionAttachments());
      LineArea seqTitle = pageSequence.getTitle();
      if (seqTitle != null) {
         this.startElement("title");
         List children = seqTitle.getInlineAreas();
         Iterator var5 = children.iterator();

         while(var5.hasNext()) {
            Object aChildren = var5.next();
            InlineArea inline = (InlineArea)aChildren;
            this.renderInlineArea(inline);
         }

         this.endElement("title");
      }

   }

   public void endPageSequence() {
      if (this.startedSequence) {
         this.endElement("pageSequence");
      }

      this.startedSequence = false;
   }

   protected void renderRegionViewport(RegionViewport port) {
      if (port != null) {
         this.atts.clear();
         this.addAreaAttributes(port);
         this.addTraitAttributes(port);
         this.addAttribute("rect", port.getViewArea());
         if (port.hasClip()) {
            this.addAttribute("clipped", "true");
         }

         this.startElement("regionViewport", this.atts);
         RegionReference region = port.getRegionReference();
         this.atts.clear();
         this.addAreaAttributes(region);
         this.addTraitAttributes(region);
         this.addAttribute("name", region.getRegionName());
         this.addAttribute("ctm", region.getCTM().toString());
         if (region.getRegionClass() == 57) {
            this.startElement("regionBefore", this.atts);
            this.renderRegion(region);
            this.endElement("regionBefore");
         } else if (region.getRegionClass() == 61) {
            this.startElement("regionStart", this.atts);
            this.renderRegion(region);
            this.endElement("regionStart");
         } else if (region.getRegionClass() == 58) {
            assert region instanceof BodyRegion;

            BodyRegion body = (BodyRegion)region;
            if (body.getColumnCount() != 1) {
               this.addAttribute("columnGap", body.getColumnGap());
               this.addAttribute("columnCount", body.getColumnCount());
            }

            this.startElement("regionBody", this.atts);
            this.renderBodyRegion(body);
            this.endElement("regionBody");
         } else if (region.getRegionClass() == 59) {
            this.startElement("regionEnd", this.atts);
            this.renderRegion(region);
            this.endElement("regionEnd");
         } else if (region.getRegionClass() == 56) {
            this.startElement("regionAfter", this.atts);
            this.renderRegion(region);
            this.endElement("regionAfter");
         }

         this.endElement("regionViewport");
      }

   }

   protected void startVParea(CTM ctm, Rectangle clippingRect) {
   }

   protected void endVParea() {
   }

   protected void startLayer(String layer) {
   }

   protected void endLayer() {
   }

   protected void renderInlineAreaBackAndBorders(InlineArea area) {
   }

   protected void renderBeforeFloat(BeforeFloat bf) {
      this.startElement("beforeFloat");
      super.renderBeforeFloat(bf);
      this.endElement("beforeFloat");
   }

   protected void renderFootnote(Footnote footnote) {
      this.atts.clear();
      this.addAttribute("top-offset", footnote.getTop());
      this.startElement("footnote", this.atts);
      super.renderFootnote(footnote);
      this.endElement("footnote");
   }

   protected void renderMainReference(MainReference mr) {
      this.atts.clear();
      this.addAreaAttributes(mr);
      this.addTraitAttributes(mr);
      if (mr.getColumnCount() != 1) {
         this.addAttribute("columnGap", mr.getColumnGap());
      }

      this.startElement("mainReference", this.atts);
      Span span = null;
      List spans = mr.getSpans();
      Iterator var4 = spans.iterator();

      while(var4.hasNext()) {
         Object span1 = var4.next();
         span = (Span)span1;
         this.atts.clear();
         if (span.getColumnCount() != 1) {
            this.addAttribute("columnCount", span.getColumnCount());
         }

         this.addAreaAttributes(span);
         this.addTraitAttributes(span);
         this.startElement("span", this.atts);

         for(int c = 0; c < span.getColumnCount(); ++c) {
            NormalFlow flow = span.getNormalFlow(c);
            this.renderFlow(flow);
         }

         this.endElement("span");
      }

      this.endElement("mainReference");
   }

   protected void renderFlow(NormalFlow flow) {
      this.atts.clear();
      this.addAreaAttributes(flow);
      this.addTraitAttributes(flow);
      this.startElement("flow", this.atts);
      super.renderFlow(flow);
      this.endElement("flow");
   }

   protected void renderReferenceArea(Block block) {
      this.handleBlockTraits(block);
      List children = block.getChildAreas();
      if (children != null) {
         this.renderBlocks(block, children);
      }

   }

   protected void renderBlock(Block block) {
      this.atts.clear();
      this.addAreaAttributes(block);
      this.addTraitAttributes(block);
      int positioning = block.getPositioning();
      if (block instanceof BlockViewport) {
         BlockViewport bvp = (BlockViewport)block;
         boolean abspos = false;
         if (bvp.getPositioning() == 2 || bvp.getPositioning() == 3) {
            abspos = true;
         }

         if (abspos) {
            this.addAttribute("left-position", bvp.getXOffset());
            this.addAttribute("top-position", bvp.getYOffset());
         }

         this.addAttribute("ctm", bvp.getCTM().toString());
         if (bvp.hasClip()) {
            this.addAttribute("clipped", "true");
         }
      } else {
         if (block.getXOffset() != 0) {
            this.addAttribute("left-offset", block.getXOffset());
         }

         if (block.getYOffset() != 0) {
            this.addAttribute("top-offset", block.getYOffset());
         }
      }

      switch (positioning) {
         case 1:
            this.addAttribute("positioning", "relative");
            break;
         case 2:
            this.addAttribute("positioning", "absolute");
            break;
         case 3:
            this.addAttribute("positioning", "fixed");
      }

      this.startElement("block", this.atts);
      super.renderBlock(block);
      this.endElement("block");
   }

   protected void renderInlineBlock(InlineBlock inlineBlock) {
      Block block = inlineBlock.getBlock();
      this.atts.clear();
      this.addAreaAttributes(block);
      this.addTraitAttributes(block);
      if (block.getXOffset() != 0) {
         this.addAttribute("left-offset", block.getXOffset());
      }

      if (block.getYOffset() != 0) {
         this.addAttribute("top-offset", block.getYOffset());
      }

      this.startElement("inlineblock", this.atts);
      if (block.getChildAreas() != null) {
         this.renderBlocks((Block)null, block.getChildAreas());
      }

      this.endElement("inlineblock");
   }

   protected void renderLineArea(LineArea line) {
      this.atts.clear();
      this.addAreaAttributes(line);
      this.addTraitAttributes(line);
      this.startElement("lineArea", this.atts);
      super.renderLineArea(line);
      this.endElement("lineArea");
   }

   protected void renderInlineArea(InlineArea inlineArea) {
      this.atts.clear();
      if (inlineArea.getClass() == InlineArea.class) {
         this.addAreaAttributes(inlineArea);
         this.addTraitAttributes(inlineArea);
         this.startElement("inline", this.atts);
         this.endElement("inline");
      } else {
         super.renderInlineArea(inlineArea);
      }

   }

   protected void renderInlineViewport(InlineViewport viewport) {
      this.atts.clear();
      this.addAreaAttributes(viewport);
      this.addTraitAttributes(viewport);
      this.addAttribute("offset", viewport.getBlockProgressionOffset());
      this.addAttribute("pos", viewport.getContentPosition());
      if (viewport.hasClip()) {
         this.addAttribute("clip", "true");
      }

      this.startElement("viewport", this.atts);
      super.renderInlineViewport(viewport);
      this.endElement("viewport");
   }

   public void renderImage(Image image, Rectangle2D pos) {
      this.atts.clear();
      this.addAreaAttributes(image);
      this.addTraitAttributes(image);
      this.addAttribute("url", image.getURL());
      this.startElement("image", this.atts);
      this.endElement("image");
   }

   public void renderContainer(Container cont) {
      this.startElement("container");
      super.renderContainer(cont);
      this.endElement("container");
   }

   public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
      this.atts.clear();
      this.addAreaAttributes(fo);
      this.addTraitAttributes(fo);
      String ns = fo.getNameSpace();
      this.addAttribute("ns", ns);
      this.startElement("foreignObject", this.atts);
      Document doc = fo.getDocument();
      this.context.setProperty("handler", this.handler);
      this.renderXML(this.context, doc, ns);
      this.endElement("foreignObject");
   }

   protected void renderInlineSpace(Space space) {
      this.atts.clear();
      this.addAreaAttributes(space);
      this.addTraitAttributes(space);
      this.addAttribute("offset", space.getBlockProgressionOffset());
      this.startElement("space", this.atts);
      this.endElement("space");
   }

   protected void renderText(TextArea text) {
      this.atts.clear();
      if (text.getTextWordSpaceAdjust() != 0) {
         this.addAttribute("twsadjust", text.getTextWordSpaceAdjust());
      }

      if (text.getTextLetterSpaceAdjust() != 0) {
         this.addAttribute("tlsadjust", text.getTextLetterSpaceAdjust());
      }

      this.addAttribute("offset", text.getBlockProgressionOffset());
      this.addAttribute("baseline", text.getBaselineOffset());
      this.addAreaAttributes(text);
      this.addTraitAttributes(text);
      this.startElement("text", this.atts);
      super.renderText(text);
      this.endElement("text");
   }

   protected void renderWord(WordArea word) {
      this.atts.clear();
      int offset = word.getBlockProgressionOffset();
      if (offset != 0) {
         this.addAttribute("offset", offset);
      }

      int[] letterAdjust = word.getLetterAdjustArray();
      if (letterAdjust != null) {
         StringBuffer sb = new StringBuffer(64);
         boolean nonZeroFound = false;
         int i = 0;

         for(int c = letterAdjust.length; i < c; ++i) {
            if (i > 0) {
               sb.append(' ');
            }

            sb.append(letterAdjust[i]);
            nonZeroFound |= letterAdjust[i] != 0;
         }

         if (nonZeroFound) {
            this.addAttribute("letter-adjust", sb.toString());
         }
      }

      this.maybeAddLevelAttribute(word);
      this.maybeAddPositionAdjustAttribute(word);
      String text = word.getWord();
      this.maybeAddReversedAttribute(word, text);
      this.startElement("word", this.atts);
      this.characters(text);
      this.endElement("word");
      super.renderWord(word);
   }

   protected void renderSpace(SpaceArea space) {
      this.atts.clear();
      int offset = space.getBlockProgressionOffset();
      if (offset != 0) {
         this.addAttribute("offset", offset);
      }

      this.maybeAddLevelAttribute(space);
      if (!space.isAdjustable()) {
         this.addAttribute("adj", "false");
      }

      this.startElement("space", this.atts);
      this.characters(space.getSpace());
      this.endElement("space");
      super.renderSpace(space);
   }

   protected void renderInlineParent(InlineParent ip) {
      this.atts.clear();
      this.addAreaAttributes(ip);
      this.addTraitAttributes(ip);
      this.addAttribute("offset", ip.getBlockProgressionOffset());
      this.startElement("inlineparent", this.atts);
      super.renderInlineParent(ip);
      this.endElement("inlineparent");
   }

   protected void renderInlineBlockParent(InlineBlockParent ibp) {
      this.atts.clear();
      this.addAreaAttributes(ibp);
      this.addTraitAttributes(ibp);
      this.addAttribute("offset", ibp.getBlockProgressionOffset());
      this.startElement("inlineblockparent", this.atts);
      super.renderInlineBlockParent(ibp);
      this.endElement("inlineblockparent");
   }

   protected void renderLeader(Leader area) {
      this.atts.clear();
      this.addAreaAttributes(area);
      this.addTraitAttributes(area);
      this.addAttribute("offset", area.getBlockProgressionOffset());
      this.addAttribute("ruleStyle", area.getRuleStyleAsString());
      this.addAttribute("ruleThickness", area.getRuleThickness());
      this.startElement("leader", this.atts);
      this.endElement("leader");
      super.renderLeader(area);
   }

   public String getMimeType() {
      return "application/X-fop-areatree";
   }

   private void maybeAddLevelAttribute(Area a) {
      int level = a.getBidiLevel();
      if (level >= 0) {
         this.addAttribute("level", level);
      }

   }

   private void maybeAddPositionAdjustAttribute(WordArea w) {
      int[][] adjustments = w.getGlyphPositionAdjustments();
      if (adjustments != null) {
         this.addAttribute("position-adjust", XMLUtil.encodePositionAdjustments(adjustments));
      }

   }

   private void maybeAddReversedAttribute(WordArea w, String text) {
      if (w.isReversed() && text.length() > 1) {
         this.addAttribute("reversed", "true");
      }

   }
}
