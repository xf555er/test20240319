package org.apache.batik.svggen;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.w3c.dom.Document;

public class SVGGeneratorContext implements ErrorConstants {
   Document domFactory;
   ImageHandler imageHandler;
   GenericImageHandler genericImageHandler;
   ExtensionHandler extensionHandler;
   SVGIDGenerator idGenerator;
   StyleHandler styleHandler;
   String generatorComment;
   ErrorHandler errorHandler;
   boolean svgFont = false;
   GraphicContextDefaults gcDefaults;
   int precision = 4;
   protected DecimalFormat decimalFormat;
   protected static DecimalFormatSymbols dsf;
   protected static DecimalFormat[] decimalFormats;

   protected SVGGeneratorContext(Document domFactory) {
      this.decimalFormat = decimalFormats[this.precision];
      this.setDOMFactory(domFactory);
   }

   public static SVGGeneratorContext createDefault(Document domFactory) {
      SVGGeneratorContext ctx = new SVGGeneratorContext(domFactory);
      ctx.setIDGenerator(new SVGIDGenerator());
      ctx.setExtensionHandler(new DefaultExtensionHandler());
      ctx.setImageHandler(new ImageHandlerBase64Encoder());
      ctx.setStyleHandler(new DefaultStyleHandler());
      ctx.setComment("Generated by the Batik Graphics2D SVG Generator");
      ctx.setErrorHandler(new DefaultErrorHandler());
      return ctx;
   }

   public final GraphicContextDefaults getGraphicContextDefaults() {
      return this.gcDefaults;
   }

   public final void setGraphicContextDefaults(GraphicContextDefaults gcDefaults) {
      this.gcDefaults = gcDefaults;
   }

   public final SVGIDGenerator getIDGenerator() {
      return this.idGenerator;
   }

   public final void setIDGenerator(SVGIDGenerator idGenerator) {
      if (idGenerator == null) {
         throw new SVGGraphics2DRuntimeException("idGenerator should not be null");
      } else {
         this.idGenerator = idGenerator;
      }
   }

   public final Document getDOMFactory() {
      return this.domFactory;
   }

   public final void setDOMFactory(Document domFactory) {
      if (domFactory == null) {
         throw new SVGGraphics2DRuntimeException("domFactory should not be null");
      } else {
         this.domFactory = domFactory;
      }
   }

   public final ExtensionHandler getExtensionHandler() {
      return this.extensionHandler;
   }

   public final void setExtensionHandler(ExtensionHandler extensionHandler) {
      if (extensionHandler == null) {
         throw new SVGGraphics2DRuntimeException("extensionHandler should not be null");
      } else {
         this.extensionHandler = extensionHandler;
      }
   }

   public final ImageHandler getImageHandler() {
      return this.imageHandler;
   }

   public final void setImageHandler(ImageHandler imageHandler) {
      if (imageHandler == null) {
         throw new SVGGraphics2DRuntimeException("imageHandler should not be null");
      } else {
         this.imageHandler = imageHandler;
         this.genericImageHandler = new SimpleImageHandler(imageHandler);
      }
   }

   public final void setGenericImageHandler(GenericImageHandler genericImageHandler) {
      if (genericImageHandler == null) {
         throw new SVGGraphics2DRuntimeException("imageHandler should not be null");
      } else {
         this.imageHandler = null;
         this.genericImageHandler = genericImageHandler;
      }
   }

   public final StyleHandler getStyleHandler() {
      return this.styleHandler;
   }

   public final void setStyleHandler(StyleHandler styleHandler) {
      if (styleHandler == null) {
         throw new SVGGraphics2DRuntimeException("styleHandler should not be null");
      } else {
         this.styleHandler = styleHandler;
      }
   }

   public final String getComment() {
      return this.generatorComment;
   }

   public final void setComment(String generatorComment) {
      this.generatorComment = generatorComment;
   }

   public final ErrorHandler getErrorHandler() {
      return this.errorHandler;
   }

   public final void setErrorHandler(ErrorHandler errorHandler) {
      if (errorHandler == null) {
         throw new SVGGraphics2DRuntimeException("errorHandler should not be null");
      } else {
         this.errorHandler = errorHandler;
      }
   }

   public final boolean isEmbeddedFontsOn() {
      return this.svgFont;
   }

   public final void setEmbeddedFontsOn(boolean svgFont) {
      this.svgFont = svgFont;
   }

   public final int getPrecision() {
      return this.precision;
   }

   public final void setPrecision(int precision) {
      if (precision < 0) {
         this.precision = 0;
      } else if (precision > 12) {
         this.precision = 12;
      } else {
         this.precision = precision;
      }

      this.decimalFormat = decimalFormats[this.precision];
   }

   public final String doubleString(double value) {
      double absvalue = Math.abs(value);
      return !(absvalue >= 1.0E8) && (double)((int)value) != value ? this.decimalFormat.format(value) : Integer.toString((int)value);
   }

   static {
      dsf = new DecimalFormatSymbols(Locale.US);
      decimalFormats = new DecimalFormat[13];
      decimalFormats[0] = new DecimalFormat("#", dsf);
      String format = "#.";

      for(int i = 1; i < decimalFormats.length; ++i) {
         format = format + "#";
         decimalFormats[i] = new DecimalFormat(format, dsf);
      }

   }

   public static class GraphicContextDefaults {
      protected Paint paint;
      protected Stroke stroke;
      protected Composite composite;
      protected Shape clip;
      protected RenderingHints hints;
      protected Font font;
      protected Color background;

      public void setStroke(Stroke stroke) {
         this.stroke = stroke;
      }

      public Stroke getStroke() {
         return this.stroke;
      }

      public void setComposite(Composite composite) {
         this.composite = composite;
      }

      public Composite getComposite() {
         return this.composite;
      }

      public void setClip(Shape clip) {
         this.clip = clip;
      }

      public Shape getClip() {
         return this.clip;
      }

      public void setRenderingHints(RenderingHints hints) {
         this.hints = hints;
      }

      public RenderingHints getRenderingHints() {
         return this.hints;
      }

      public void setFont(Font font) {
         this.font = font;
      }

      public Font getFont() {
         return this.font;
      }

      public void setBackground(Color background) {
         this.background = background;
      }

      public Color getBackground() {
         return this.background;
      }

      public void setPaint(Paint paint) {
         this.paint = paint;
      }

      public Paint getPaint() {
         return this.paint;
      }
   }
}
