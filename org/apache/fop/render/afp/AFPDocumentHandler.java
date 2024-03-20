package org.apache.fop.render.afp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fop.afp.AFPDitheredRectanglePainter;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPRectanglePainter;
import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.afp.AbstractAFPPainter;
import org.apache.fop.afp.DataStream;
import org.apache.fop.afp.fonts.AFPFontCollection;
import org.apache.fop.afp.fonts.AFPPageFonts;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.render.afp.extensions.AFPIncludeFormMap;
import org.apache.fop.render.afp.extensions.AFPInvokeMediumMap;
import org.apache.fop.render.afp.extensions.AFPPageOverlay;
import org.apache.fop.render.afp.extensions.AFPPageSegmentElement;
import org.apache.fop.render.afp.extensions.AFPPageSetup;
import org.apache.fop.render.afp.extensions.ExtensionPlacement;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFDocumentHandler;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;

public class AFPDocumentHandler extends AbstractBinaryWritingIFDocumentHandler implements AFPCustomizable {
   private AFPResourceManager resourceManager;
   private final AFPPaintingState paintingState;
   private final AFPUnitConverter unitConv;
   private DataStream dataStream;
   private Map pageSegmentMap = new HashMap();
   private Map roundedCornerNameCache = new HashMap();
   private int roundedCornerCount;
   private Location location;
   private List deferredPageSequenceExtensions;
   private AFPShadingMode shadingMode;

   public AFPDocumentHandler(IFContext context) {
      super(context);
      this.location = AFPDocumentHandler.Location.ELSEWHERE;
      this.deferredPageSequenceExtensions = new LinkedList();
      this.shadingMode = AFPShadingMode.COLOR;
      this.resourceManager = new AFPResourceManager(context.getUserAgent().getResourceResolver());
      this.paintingState = new AFPPaintingState();
      this.unitConv = this.paintingState.getUnitConverter();
   }

   public boolean supportsPagesOutOfOrder() {
      return false;
   }

   public String getMimeType() {
      return "application/x-afp";
   }

   public IFDocumentHandlerConfigurator getConfigurator() {
      return new AFPRendererConfigurator(this.getUserAgent(), new AFPRendererConfig.AFPRendererConfigParser());
   }

   public void setDefaultFontInfo(FontInfo fontInfo) {
      FontManager fontManager = this.getUserAgent().getFontManager();
      FontCollection[] fontCollections = new FontCollection[]{new AFPFontCollection(this.getUserAgent().getEventBroadcaster(), (List)null)};
      FontInfo fi = fontInfo != null ? fontInfo : new FontInfo();
      fi.setEventListener(new FontEventAdapter(this.getUserAgent().getEventBroadcaster()));
      fontManager.setup(fi, fontCollections);
      this.setFontInfo(fi);
   }

   AFPPaintingState getPaintingState() {
      return this.paintingState;
   }

   DataStream getDataStream() {
      return this.dataStream;
   }

   AFPResourceManager getResourceManager() {
      return this.resourceManager;
   }

   AbstractAFPPainter createRectanglePainter() {
      return (AbstractAFPPainter)(AFPShadingMode.DITHERED.equals(this.shadingMode) ? new AFPDitheredRectanglePainter(this.getPaintingState(), this.getDataStream(), this.getResourceManager()) : new AFPRectanglePainter(this.getPaintingState(), this.getDataStream()));
   }

   public void startDocument() throws IFException {
      super.startDocument();

      try {
         this.paintingState.setColor(Color.WHITE);
         this.dataStream = this.resourceManager.createDataStream(this.paintingState, this.outputStream);
         this.dataStream.startDocument();
      } catch (IOException var2) {
         throw new IFException("I/O error in startDocument()", var2);
      }
   }

   public void startDocumentHeader() throws IFException {
      super.startDocumentHeader();
      this.location = AFPDocumentHandler.Location.IN_DOCUMENT_HEADER;
   }

   public void endDocumentHeader() throws IFException {
      super.endDocumentHeader();
      this.location = AFPDocumentHandler.Location.ELSEWHERE;
   }

   public void endDocument() throws IFException {
      try {
         this.dataStream.endDocument();
         this.dataStream = null;
         this.resourceManager.writeToStream();
         this.resourceManager = null;
      } catch (IOException var2) {
         throw new IFException("I/O error in endDocument()", var2);
      }

      super.endDocument();
   }

   public void startPageSequence(String id) throws IFException {
      try {
         this.dataStream.startPageGroup();
      } catch (IOException var3) {
         throw new IFException("I/O error in startPageSequence()", var3);
      }

      this.location = AFPDocumentHandler.Location.FOLLOWING_PAGE_SEQUENCE;
   }

   public void endPageSequence() throws IFException {
      try {
         Iterator iter = this.deferredPageSequenceExtensions.iterator();

         while(true) {
            if (!iter.hasNext()) {
               this.dataStream.endPageGroup();
               break;
            }

            AFPPageSetup aps = (AFPPageSetup)iter.next();
            iter.remove();
            if (!"no-operation".equals(aps.getElementName())) {
               throw new UnsupportedOperationException("Don't know how to handle " + aps);
            }

            this.handleNOP(aps);
         }
      } catch (IOException var3) {
         throw new IFException("I/O error in endPageSequence()", var3);
      }

      this.location = AFPDocumentHandler.Location.ELSEWHERE;
   }

   private AffineTransform getBaseTransform() {
      AffineTransform baseTransform = new AffineTransform();
      double scale = (double)this.unitConv.mpt2units(1.0F);
      baseTransform.scale(scale, scale);
      return baseTransform;
   }

   public void startPage(int index, String name, String pageMasterName, Dimension size) throws IFException {
      this.location = AFPDocumentHandler.Location.ELSEWHERE;
      this.paintingState.clear();
      AffineTransform baseTransform = this.getBaseTransform();
      this.paintingState.concatenate(baseTransform);
      int pageWidth = Math.round(this.unitConv.mpt2units((float)size.width));
      this.paintingState.setPageWidth(pageWidth);
      int pageHeight = Math.round(this.unitConv.mpt2units((float)size.height));
      this.paintingState.setPageHeight(pageHeight);
      int pageRotation = this.paintingState.getPageRotation();
      int resolution = this.paintingState.getResolution();
      this.dataStream.startPage(pageWidth, pageHeight, pageRotation, resolution, resolution);
   }

   public void startPageHeader() throws IFException {
      super.startPageHeader();
      this.location = AFPDocumentHandler.Location.IN_PAGE_HEADER;
   }

   public void endPageHeader() throws IFException {
      this.location = AFPDocumentHandler.Location.ELSEWHERE;
      super.endPageHeader();
   }

   public IFPainter startPageContent() throws IFException {
      return new AFPPainter(this);
   }

   public void endPageContent() throws IFException {
   }

   public void endPage() throws IFException {
      try {
         AFPPageFonts pageFonts = this.paintingState.getPageFonts();
         if (pageFonts != null && !pageFonts.isEmpty()) {
            this.dataStream.addFontsToCurrentPage(pageFonts);
         }

         this.dataStream.endPage();
      } catch (IOException var2) {
         throw new IFException("I/O error in endPage()", var2);
      }
   }

   public void handleExtensionObject(Object extension) throws IFException {
      String mediumMap;
      if (extension instanceof AFPPageSetup) {
         AFPPageSetup aps = (AFPPageSetup)extension;
         mediumMap = aps.getElementName();
         String name;
         if ("tag-logical-element".equals(mediumMap)) {
            switch (this.location) {
               case FOLLOWING_PAGE_SEQUENCE:
               case IN_PAGE_HEADER:
                  String name = aps.getName();
                  name = aps.getValue();
                  int encoding = aps.getEncoding();
                  this.dataStream.createTagLogicalElement(name, name, encoding);
                  break;
               default:
                  throw new IFException("TLE extension must be in the page header or between page-sequence and the first page: " + aps, (Exception)null);
            }
         } else if ("no-operation".equals(mediumMap)) {
            switch (this.location) {
               case FOLLOWING_PAGE_SEQUENCE:
                  if (aps.getPlacement() == ExtensionPlacement.BEFORE_END) {
                     this.deferredPageSequenceExtensions.add(aps);
                     break;
                  }
               case IN_PAGE_HEADER:
               case IN_DOCUMENT_HEADER:
                  this.handleNOP(aps);
                  break;
               default:
                  throw new IFException("NOP extension must be in the document header, the page header or between page-sequence and the first page: " + aps, (Exception)null);
            }
         } else {
            if (this.location != AFPDocumentHandler.Location.IN_PAGE_HEADER) {
               throw new IFException("AFP page setup extension encountered outside the page header: " + aps, (Exception)null);
            }

            if ("include-page-segment".equals(mediumMap)) {
               AFPPageSegmentElement.AFPPageSegmentSetup apse = (AFPPageSegmentElement.AFPPageSegmentSetup)aps;
               name = apse.getName();
               String source = apse.getValue();
               String uri = apse.getResourceSrc();
               this.pageSegmentMap.put(source, new PageSegmentDescriptor(name, uri));
            }
         }
      } else if (extension instanceof AFPPageOverlay) {
         AFPPageOverlay ipo = (AFPPageOverlay)extension;
         if (this.location != AFPDocumentHandler.Location.IN_PAGE_HEADER) {
            throw new IFException("AFP page overlay extension encountered outside the page header: " + ipo, (Exception)null);
         }

         mediumMap = ipo.getName();
         if (mediumMap != null) {
            this.dataStream.createIncludePageOverlay(mediumMap, ipo.getX(), ipo.getY());
         }
      } else if (extension instanceof AFPInvokeMediumMap) {
         if (this.location != AFPDocumentHandler.Location.FOLLOWING_PAGE_SEQUENCE && this.location != AFPDocumentHandler.Location.IN_PAGE_HEADER) {
            throw new IFException("AFP IMM extension must be between page-sequence and the first page or child of page-header: " + extension, (Exception)null);
         }

         AFPInvokeMediumMap imm = (AFPInvokeMediumMap)extension;
         mediumMap = imm.getName();
         if (mediumMap != null) {
            this.dataStream.createInvokeMediumMap(mediumMap);
         }
      } else if (extension instanceof AFPIncludeFormMap) {
         AFPIncludeFormMap formMap = (AFPIncludeFormMap)extension;
         AFPResourceAccessor accessor = new AFPResourceAccessor(this.getUserAgent().getResourceResolver());

         try {
            this.getResourceManager().createIncludedResource(formMap.getName(), formMap.getSrc(), accessor, (byte)-2, false, (String)null);
         } catch (IOException var8) {
            throw new IFException("I/O error while embedding form map resource: " + formMap.getName(), var8);
         }
      }

   }

   public String cacheRoundedCorner(String cornerKey) {
      StringBuffer idBuilder = new StringBuffer("RC");
      String tmp = Integer.toHexString(this.roundedCornerCount).toUpperCase(Locale.ENGLISH);
      if (tmp.length() > 6) {
         this.roundedCornerCount = 0;
         tmp = "000000";
      } else if (tmp.length() < 6) {
         for(int i = 0; i < 6 - tmp.length(); ++i) {
            idBuilder.append("0");
         }

         idBuilder.append(tmp);
      }

      ++this.roundedCornerCount;
      String id = idBuilder.toString();
      this.roundedCornerNameCache.put(cornerKey, id);
      return id;
   }

   public String getCachedRoundedCorner(String cornerKey) {
      return (String)this.roundedCornerNameCache.get(cornerKey);
   }

   private void handleNOP(AFPPageSetup nop) {
      String content = nop.getContent();
      if (content != null) {
         this.dataStream.createNoOperation(content);
      }

   }

   public void setBitsPerPixel(int bitsPerPixel) {
      this.paintingState.setBitsPerPixel(bitsPerPixel);
   }

   public void setColorImages(boolean colorImages) {
      this.paintingState.setColorImages(colorImages);
   }

   public void setNativeImagesSupported(boolean nativeImages) {
      this.paintingState.setNativeImagesSupported(nativeImages);
   }

   public void setCMYKImagesSupported(boolean value) {
      this.paintingState.setCMYKImagesSupported(value);
   }

   public void setDitheringQuality(float quality) {
      this.paintingState.setDitheringQuality(quality);
   }

   public void setBitmapEncodingQuality(float quality) {
      this.paintingState.setBitmapEncodingQuality(quality);
   }

   public void setShadingMode(AFPShadingMode shadingMode) {
      this.shadingMode = shadingMode;
   }

   public void setResolution(int resolution) {
      this.paintingState.setResolution(resolution);
   }

   public void setLineWidthCorrection(float correction) {
      this.paintingState.setLineWidthCorrection(correction);
   }

   public int getResolution() {
      return this.paintingState.getResolution();
   }

   public void setGOCAEnabled(boolean enabled) {
      this.paintingState.setGOCAEnabled(enabled);
   }

   public boolean isGOCAEnabled() {
      return this.paintingState.isGOCAEnabled();
   }

   public void setStrokeGOCAText(boolean stroke) {
      this.paintingState.setStrokeGOCAText(stroke);
   }

   public boolean isStrokeGOCAText() {
      return this.paintingState.isStrokeGOCAText();
   }

   public void setWrapPSeg(boolean pSeg) {
      this.paintingState.setWrapPSeg(pSeg);
   }

   public void setWrapGocaPSeg(boolean pSeg) {
      this.paintingState.setWrapGocaPSeg(pSeg);
   }

   public void setFS45(boolean fs45) {
      this.paintingState.setFS45(fs45);
   }

   public boolean getWrapPSeg() {
      return this.paintingState.getWrapPSeg();
   }

   public boolean getFS45() {
      return this.paintingState.getFS45();
   }

   public void setDefaultResourceGroupUri(URI uri) {
      this.resourceManager.setDefaultResourceGroupUri(uri);
   }

   public void setResourceLevelDefaults(AFPResourceLevelDefaults defaults) {
      this.resourceManager.setResourceLevelDefaults(defaults);
   }

   PageSegmentDescriptor getPageSegmentNameFor(String uri) {
      return (PageSegmentDescriptor)this.pageSegmentMap.get(uri);
   }

   public void canEmbedJpeg(boolean canEmbed) {
      this.paintingState.setCanEmbedJpeg(canEmbed);
   }

   private static enum Location {
      ELSEWHERE,
      IN_DOCUMENT_HEADER,
      FOLLOWING_PAGE_SEQUENCE,
      IN_PAGE_HEADER;
   }
}
