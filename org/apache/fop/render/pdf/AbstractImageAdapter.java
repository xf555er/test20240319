package org.apache.fop.render.pdf;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFICCBasedColorSpace;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.impl.ImageRawPNG;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil;

public abstract class AbstractImageAdapter implements PDFImage {
   private static Log log = LogFactory.getLog(AbstractImageAdapter.class);
   private String key;
   protected Image image;
   private PDFICCStream pdfICCStream;
   private static final int MAX_HIVAL = 255;
   private boolean multipleFiltersAllowed = true;

   public AbstractImageAdapter(Image image, String key) {
      this.image = image;
      this.key = key;
      if (log.isDebugEnabled()) {
         log.debug("New ImageAdapter created for key: " + key);
      }

   }

   public String getKey() {
      return this.key;
   }

   protected ColorSpace getImageColorSpace() {
      return this.image.getColorSpace();
   }

   public void setup(PDFDocument doc) {
      ICC_Profile prof = this.getEffectiveICCProfile();
      PDFDeviceColorSpace pdfCS = toPDFColorSpace(this.getImageColorSpace());
      if (prof != null) {
         this.pdfICCStream = setupColorProfile(doc, prof, pdfCS);
      } else if (this.issRGB()) {
         this.pdfICCStream = setupsRGBColorProfile(doc);
      }

      if (doc.getProfile().getPDFAMode().isPart1() && pdfCS != null && pdfCS.getColorSpace() != 2 && pdfCS.getColorSpace() != 1 && prof == null) {
         throw new PDFConformanceException("PDF/A-1 does not allow mixing DeviceRGB and DeviceCMYK: " + this.image.getInfo());
      }
   }

   protected ICC_Profile getEffectiveICCProfile() {
      return this.image.getICCProfile();
   }

   protected boolean issRGB() {
      return false;
   }

   private static PDFICCStream getDefaultsRGBICCStream(PDFICCBasedColorSpace cs, PDFDocument doc, String profileDesc) {
      if (cs == null) {
         if (profileDesc == null || !profileDesc.startsWith("sRGB")) {
            log.warn("The default sRGB profile was indicated, but the profile description does not match what was expected: " + profileDesc);
         }

         cs = (PDFICCBasedColorSpace)doc.getResources().getColorSpace(new PDFName("DefaultRGB"));
      }

      if (cs == null) {
         cs = PDFICCBasedColorSpace.setupsRGBColorSpace(doc);
      }

      return cs.getICCStream();
   }

   private static PDFICCStream setupsRGBColorProfile(PDFDocument doc) {
      PDFICCBasedColorSpace cs = doc.getResources().getICCColorSpaceByProfileName("sRGB");
      return getDefaultsRGBICCStream(cs, doc, "sRGB");
   }

   private static PDFICCStream setupColorProfile(PDFDocument doc, ICC_Profile prof, PDFDeviceColorSpace pdfCS) {
      boolean defaultsRGB = ColorProfileUtil.isDefaultsRGB(prof);
      String desc = ColorProfileUtil.getICCProfileDescription(prof);
      if (log.isDebugEnabled()) {
         log.debug("Image returns ICC profile: " + desc + ", default sRGB=" + defaultsRGB);
      }

      PDFICCBasedColorSpace cs = doc.getResources().getICCColorSpaceByProfileName(desc);
      PDFICCStream pdfICCStream;
      if (!defaultsRGB) {
         if (cs == null) {
            pdfICCStream = doc.getFactory().makePDFICCStream();
            pdfICCStream.setColorSpace(prof, pdfCS);
            cs = doc.getFactory().makeICCBasedColorSpace((PDFResourceContext)null, (String)null, pdfICCStream);
         } else {
            pdfICCStream = cs.getICCStream();
         }
      } else {
         pdfICCStream = getDefaultsRGBICCStream(cs, doc, desc);
      }

      return pdfICCStream;
   }

   public int getWidth() {
      return this.image.getSize().getWidthPx();
   }

   public int getHeight() {
      return this.image.getSize().getHeightPx();
   }

   public boolean isTransparent() {
      return false;
   }

   public PDFColor getTransparentColor() {
      return null;
   }

   public String getMask() {
      return null;
   }

   public String getSoftMask() {
      return null;
   }

   public PDFReference getSoftMaskReference() {
      return null;
   }

   public boolean isInverted() {
      return false;
   }

   public boolean isPS() {
      return false;
   }

   public PDFICCStream getICCStream() {
      return this.pdfICCStream;
   }

   public void populateXObjectDictionary(PDFDictionary dict) {
   }

   protected void populateXObjectDictionaryForIndexColorModel(PDFDictionary dict, IndexColorModel icm) {
      PDFArray indexed = new PDFArray(dict);
      indexed.add(new PDFName("Indexed"));
      if (icm.getColorSpace().getType() != 5) {
         log.warn("Indexed color space is not using RGB as base color space. The image may not be handled correctly. Base color space: " + icm.getColorSpace() + " Image: " + this.image.getInfo());
      }

      int c = icm.getMapSize();
      int hival = c - 1;
      if (hival > 255) {
         throw new UnsupportedOperationException("hival must not go beyond 255");
      } else {
         ByteArrayOutputStream baout = new ByteArrayOutputStream();
         boolean isDeviceGray = false;
         int[] palette = new int[c];
         icm.getRGBs(palette);
         byte[] reds = new byte[c];
         byte[] greens = new byte[c];
         byte[] blues = new byte[c];
         icm.getReds(reds);
         icm.getGreens(greens);
         icm.getBlues(blues);
         isDeviceGray = Arrays.equals(reds, blues) && Arrays.equals(blues, greens);
         int bits;
         if (isDeviceGray) {
            indexed.add(new PDFName("DeviceGray"));

            try {
               baout.write(blues);
            } catch (IOException var15) {
               var15.printStackTrace();
            }
         } else {
            indexed.add(new PDFName(toPDFColorSpace(icm.getColorSpace()).getName()));

            for(bits = 0; bits < c; ++bits) {
               int entry = palette[bits];
               baout.write((entry & 16711680) >> 16);
               baout.write((entry & '\uff00') >> 8);
               baout.write(entry & 255);
            }
         }

         indexed.add((double)hival);
         indexed.add(baout.toByteArray());
         IOUtils.closeQuietly((OutputStream)baout);
         dict.put("ColorSpace", indexed);
         bits = 8;
         if (this.image instanceof ImageRawPNG) {
            bits = ((ImageRawPNG)this.image).getBitDepth();
         } else {
            Raster raster = ((ImageRendered)this.image).getRenderedImage().getTile(0, 0);
            if (raster.getDataBuffer() instanceof DataBufferByte) {
               bits = icm.getPixelSize();
            }
         }

         dict.put("BitsPerComponent", bits);
         Integer index = getIndexOfFirstTransparentColorInPalette(icm);
         if (index != null) {
            PDFArray mask = new PDFArray(dict);
            mask.add(index);
            mask.add(index);
            dict.put("Mask", mask);
         }

      }
   }

   private static Integer getIndexOfFirstTransparentColorInPalette(IndexColorModel icm) {
      byte[] alphas = new byte[icm.getMapSize()];
      icm.getAlphas(alphas);

      for(int i = 0; i < icm.getMapSize(); ++i) {
         if ((alphas[i] & 255) == 0) {
            return i;
         }
      }

      return null;
   }

   public static PDFDeviceColorSpace toPDFColorSpace(ColorSpace cs) {
      if (cs == null) {
         return null;
      } else {
         PDFDeviceColorSpace pdfCS = new PDFDeviceColorSpace(0);
         switch (cs.getType()) {
            case 6:
               pdfCS.setColorSpace(1);
               break;
            case 9:
               pdfCS.setColorSpace(3);
               break;
            default:
               pdfCS.setColorSpace(2);
         }

         return pdfCS;
      }
   }

   public boolean multipleFiltersAllowed() {
      return this.multipleFiltersAllowed;
   }

   public void disallowMultipleFilters() {
      this.multipleFiltersAllowed = false;
   }
}
