package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.image.loader.batik.BatikImageFlavors;
import org.apache.fop.image.loader.batik.BatikUtil;
import org.apache.fop.pdf.TransparencyDisallowedException;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.ps.PSImageHandlerSVG;
import org.apache.fop.svg.PDFAElementBridge;
import org.apache.fop.svg.PDFBridgeContext;
import org.apache.fop.svg.PDFGraphics2D;
import org.apache.fop.svg.SVGEventProducer;
import org.apache.fop.svg.SVGUserAgent;
import org.apache.fop.svg.font.FOPFontFamilyResolverImpl;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.w3c.dom.Document;

public class PDFImageHandlerSVG implements ImageHandler {
   private static Log log = LogFactory.getLog(PDFImageHandlerSVG.class);

   public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
      PDFRenderingContext pdfContext = (PDFRenderingContext)context;
      PDFContentGenerator generator = pdfContext.getGenerator();
      ImageXMLDOM imageSVG = (ImageXMLDOM)image;
      FOUserAgent userAgent = context.getUserAgent();
      float deviceResolution = userAgent.getTargetResolution();
      if (log.isDebugEnabled()) {
         log.debug("Generating SVG at " + deviceResolution + "dpi.");
      }

      float uaResolution = userAgent.getSourceResolution();
      SVGUserAgent ua = new SVGUserAgent(userAgent, new FOPFontFamilyResolverImpl(pdfContext.getFontInfo()), new AffineTransform());
      GVTBuilder builder = new GVTBuilder();
      boolean strokeText = PSImageHandlerSVG.shouldStrokeText(imageSVG.getDocument().getChildNodes());
      BridgeContext ctx = new PDFBridgeContext(ua, strokeText ? null : pdfContext.getFontInfo(), userAgent.getImageManager(), userAgent.getImageSessionContext(), new AffineTransform());
      Document clonedDoc = BatikUtil.cloneSVGDocument(imageSVG.getDocument());

      GraphicsNode root;
      try {
         root = builder.build(ctx, (Document)clonedDoc);
      } catch (Exception var33) {
         SVGEventProducer eventProducer = SVGEventProducer.Provider.get(context.getUserAgent().getEventBroadcaster());
         eventProducer.svgNotBuilt(this, var33, image.getInfo().getOriginalURI());
         return;
      }

      float w = (float)image.getSize().getWidthMpt();
      float h = (float)image.getSize().getHeightMpt();
      float sx = (float)pos.width / w;
      float sy = (float)pos.height / h;
      AffineTransform scaling = new AffineTransform(sx, 0.0F, 0.0F, sy, (float)pos.x / 1000.0F, (float)pos.y / 1000.0F);
      double sourceScale = (double)(72.0F / uaResolution);
      scaling.scale(sourceScale, sourceScale);
      AffineTransform resolutionScaling = new AffineTransform();
      double targetScale = (double)(uaResolution / deviceResolution);
      resolutionScaling.scale(targetScale, targetScale);
      resolutionScaling.scale(1.0 / (double)sx, 1.0 / (double)sy);
      AffineTransform imageTransform = new AffineTransform();
      imageTransform.concatenate(scaling);
      imageTransform.concatenate(resolutionScaling);
      if (log.isTraceEnabled()) {
         log.trace("nat size: " + w + "/" + h);
         log.trace("req size: " + pos.width + "/" + pos.height);
         log.trace("source res: " + uaResolution + ", targetRes: " + deviceResolution + " --> target scaling: " + targetScale);
         log.trace(image.getSize());
         log.trace("sx: " + sx + ", sy: " + sy);
         log.trace("scaling: " + scaling);
         log.trace("resolution scaling: " + resolutionScaling);
         log.trace("image transform: " + resolutionScaling);
      }

      if (log.isTraceEnabled()) {
         generator.comment("SVG setup");
      }

      generator.saveGraphicsState();
      if (context.getUserAgent().isAccessibilityEnabled()) {
         PDFLogicalStructureHandler.MarkedContentInfo mci = pdfContext.getMarkedContentInfo();
         generator.beginMarkedContentSequence(mci.tag, mci.mcid);
      }

      generator.updateColor(Color.black, false, (StringBuffer)null);
      generator.updateColor(Color.black, true, (StringBuffer)null);
      if (!scaling.isIdentity()) {
         if (log.isTraceEnabled()) {
            generator.comment("viewbox");
         }

         generator.add(CTMHelper.toPDFString(scaling, false) + " cm\n");
      }

      PDFGraphics2D graphics = new PDFGraphics2D(true, pdfContext.getFontInfo(), generator.getDocument(), generator.getResourceContext(), pdfContext.getPage().makeReference(), "", 0.0F, new TransparencyIgnoredEventListener(pdfContext, imageSVG));
      graphics.setGraphicContext(new GraphicContext());
      if (!resolutionScaling.isIdentity()) {
         if (log.isTraceEnabled()) {
            generator.comment("resolution scaling for " + uaResolution + " -> " + deviceResolution);
         }

         generator.add(CTMHelper.toPDFString(resolutionScaling, false) + " cm\n");
         graphics.scale(1.0 / resolutionScaling.getScaleX(), 1.0 / resolutionScaling.getScaleY());
      }

      if (log.isTraceEnabled()) {
         generator.comment("SVG start");
      }

      generator.getState().save();
      generator.getState().concatenate(imageTransform);
      PDFAElementBridge aBridge = (PDFAElementBridge)ctx.getBridge("http://www.w3.org/2000/svg", "a");
      aBridge.getCurrentTransform().setTransform(generator.getState().getTransform());
      graphics.setPaintingState(generator.getState());
      graphics.setOutputStream(generator.getOutputStream());

      SVGEventProducer eventProducer;
      try {
         root.paint(graphics);
         ctx.dispose();
         generator.add(graphics.getString());
      } catch (TransparencyDisallowedException var31) {
         eventProducer = SVGEventProducer.Provider.get(context.getUserAgent().getEventBroadcaster());
         eventProducer.bitmapWithTransparency(this, var31.getProfile(), image.getInfo().getOriginalURI());
      } catch (Exception var32) {
         eventProducer = SVGEventProducer.Provider.get(context.getUserAgent().getEventBroadcaster());
         eventProducer.svgRenderingError(this, var32, image.getInfo().getOriginalURI());
      }

      generator.getState().restore();
      if (context.getUserAgent().isAccessibilityEnabled()) {
         generator.restoreGraphicsStateAccess();
      } else {
         generator.restoreGraphicsState();
      }

      if (log.isTraceEnabled()) {
         generator.comment("SVG end");
      }

   }

   public int getPriority() {
      return 400;
   }

   public Class getSupportedImageClass() {
      return ImageXMLDOM.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return new ImageFlavor[]{BatikImageFlavors.SVG_DOM};
   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      boolean supported = (image == null || image instanceof ImageXMLDOM && image.getFlavor().isCompatible(BatikImageFlavors.SVG_DOM)) && targetContext instanceof PDFRenderingContext;
      if (supported) {
         String mode = (String)targetContext.getHint(ImageHandlerUtil.CONVERSION_MODE);
         if (ImageHandlerUtil.isConversionModeBitmap(mode)) {
            return false;
         }
      }

      return supported;
   }

   private static class TransparencyIgnoredEventListener implements PDFGraphics2D.TransparencyIgnoredEventListener {
      private final RenderingContext context;
      private final Image image;
      private boolean warningIssued;

      public TransparencyIgnoredEventListener(RenderingContext context, Image image) {
         this.context = context;
         this.image = image;
      }

      public void transparencyIgnored(Object pdfProfile) {
         if (!this.warningIssued) {
            EventBroadcaster broadcaster = this.context.getUserAgent().getEventBroadcaster();
            SVGEventProducer producer = SVGEventProducer.Provider.get(broadcaster);
            producer.transparencyIgnored(this, pdfProfile, this.image.getInfo().getOriginalURI());
            this.warningIssued = true;
         }

      }
   }
}
