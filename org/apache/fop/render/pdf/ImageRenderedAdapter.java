package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.pdf.AlphaRasterImage;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;

public class ImageRenderedAdapter extends AbstractImageAdapter {
   private static Log log = LogFactory.getLog(ImageRenderedAdapter.class);
   private ImageEncodingHelper encodingHelper;
   private PDFFilter pdfFilter;
   private String maskRef;
   private PDFReference softMask;

   public ImageRenderedAdapter(ImageRendered image, String key) {
      super(image, key);
      this.encodingHelper = new ImageEncodingHelper(image.getRenderedImage());
   }

   public ImageRendered getImage() {
      return (ImageRendered)this.image;
   }

   public int getWidth() {
      RenderedImage ri = this.getImage().getRenderedImage();
      return ri.getWidth();
   }

   public int getHeight() {
      RenderedImage ri = this.getImage().getRenderedImage();
      return ri.getHeight();
   }

   private ColorModel getEffectiveColorModel() {
      return this.encodingHelper.getEncodedColorModel();
   }

   protected ColorSpace getImageColorSpace() {
      return this.getEffectiveColorModel().getColorSpace();
   }

   protected ICC_Profile getEffectiveICCProfile() {
      ColorSpace cs = this.getImageColorSpace();
      if (cs instanceof ICC_ColorSpace) {
         ICC_ColorSpace iccSpace = (ICC_ColorSpace)cs;
         return iccSpace.getProfile();
      } else {
         return null;
      }
   }

   public void setup(PDFDocument doc) {
      RenderedImage ri = this.getImage().getRenderedImage();
      super.setup(doc);
      ColorModel orgcm = ri.getColorModel();
      if (orgcm.hasAlpha() && orgcm.getTransparency() == 3) {
         doc.getProfile().verifyTransparencyAllowed(this.image.getInfo().getOriginalURI());
         AlphaRasterImage alphaImage = new AlphaRasterImage("Mask:" + this.getKey(), ri);
         this.softMask = doc.addImage((PDFResourceContext)null, alphaImage).makeReference();
      }

   }

   public PDFDeviceColorSpace getColorSpace() {
      return toPDFColorSpace(this.getEffectiveColorModel().getColorSpace());
   }

   public int getBitsPerComponent() {
      ColorModel cm = this.getEffectiveColorModel();
      if (cm instanceof IndexColorModel) {
         IndexColorModel icm = (IndexColorModel)cm;
         return icm.getComponentSize(0);
      } else {
         return cm.getComponentSize(0);
      }
   }

   public boolean isTransparent() {
      return this.getImage().getTransparentColor() != null;
   }

   public PDFColor getTransparentColor() {
      ColorModel cm = this.getEffectiveColorModel();
      if (cm instanceof IndexColorModel) {
         IndexColorModel icm = (IndexColorModel)cm;
         if (cm.getTransparency() == 3 || cm.getTransparency() == 2) {
            int transPixel = icm.getTransparentPixel();
            if (transPixel != -1) {
               return new PDFColor(icm.getRed(transPixel), icm.getGreen(transPixel), icm.getBlue(transPixel));
            }
         }
      }

      Color transColor = this.getImage().getTransparentColor();
      return transColor != null ? new PDFColor(transColor) : null;
   }

   public String getMask() {
      return this.maskRef;
   }

   public PDFReference getSoftMaskReference() {
      return this.softMask;
   }

   public PDFFilter getPDFFilter() {
      return this.pdfFilter;
   }

   public void outputContents(OutputStream out) throws IOException {
      long start = System.currentTimeMillis();
      this.encodingHelper.setBWInvert(true);
      this.encodingHelper.encode(out);
      long duration = System.currentTimeMillis() - start;
      if (log.isDebugEnabled()) {
         log.debug("Image encoding took " + duration + "ms");
      }

   }

   public void populateXObjectDictionary(PDFDictionary dict) {
      ColorModel cm = this.getEffectiveColorModel();
      if (cm instanceof IndexColorModel) {
         IndexColorModel icm = (IndexColorModel)cm;
         super.populateXObjectDictionaryForIndexColorModel(dict, icm);
      }

   }

   public String getFilterHint() {
      return "image";
   }
}
