package org.apache.fop.render.pdf;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.pdf.BitmapImage;
import org.apache.fop.pdf.FlateFilter;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.pdf.PDFFilterException;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.xmlgraphics.image.loader.impl.ImageRawPNG;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;

public class ImageRawPNGAdapter extends AbstractImageAdapter {
   private static Log log = LogFactory.getLog(ImageRawPNGAdapter.class);
   private static final PDFName RI_PERCEPTUAL = new PDFName("Perceptual");
   private static final PDFName RI_RELATIVE_COLORIMETRIC = new PDFName("RelativeColorimetric");
   private static final PDFName RI_SATURATION = new PDFName("Saturation");
   private static final PDFName RI_ABSOLUTE_COLORIMETRIC = new PDFName("AbsoluteColorimetric");
   private PDFFilter pdfFilter;
   private String maskRef;
   private PDFReference softMask;
   private int numberOfInterleavedComponents;

   public ImageRawPNGAdapter(ImageRawPNG image, String key) {
      super(image, key);
   }

   public void setup(PDFDocument doc) {
      super.setup(doc);
      ColorModel cm = ((ImageRawPNG)this.image).getColorModel();
      if (cm instanceof IndexColorModel) {
         this.numberOfInterleavedComponents = 1;
      } else {
         this.numberOfInterleavedComponents = cm.getNumComponents();
      }

      FlateFilter flate;
      try {
         flate = new FlateFilter();
         flate.setApplied(true);
         flate.setPredictor(15);
         if (this.numberOfInterleavedComponents < 3) {
            flate.setColors(1);
         } else {
            flate.setColors(3);
         }

         flate.setColumns(this.image.getSize().getWidthPx());
         flate.setBitsPerComponent(this.getBitsPerComponent());
      } catch (PDFFilterException var21) {
         throw new RuntimeException("FlateFilter configuration error", var21);
      }

      this.pdfFilter = flate;
      this.disallowMultipleFilters();
      if (cm.hasAlpha() && cm.getTransparency() == 3) {
         doc.getProfile().verifyTransparencyAllowed(this.image.getInfo().getOriginalURI());
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         DeflaterOutputStream dos = new DeflaterOutputStream(baos, new Deflater());
         InputStream in = ((ImageRawStream)this.image).createInputStream();

         try {
            InflaterInputStream infStream = new InflaterInputStream(in, new Inflater());
            DataInputStream dataStream = new DataInputStream(infStream);
            int offset = this.numberOfInterleavedComponents - 1;
            int numColumns = this.image.getSize().getWidthPx();

            int filter;
            for(int bytesPerRow = this.numberOfInterleavedComponents * numColumns; (filter = dataStream.read()) != -1; offset = this.numberOfInterleavedComponents - 1) {
               byte[] bytes = new byte[bytesPerRow];
               dataStream.readFully(bytes, 0, bytesPerRow);
               dos.write((byte)filter);

               for(int j = 0; j < numColumns; ++j) {
                  dos.write(bytes, offset, 1);
                  offset += this.numberOfInterleavedComponents;
               }
            }

            dos.close();
         } catch (IOException var22) {
            throw new RuntimeException("Error processing transparency channel:", var22);
         } finally {
            IOUtils.closeQuietly(in);
         }

         FlateFilter transFlate;
         try {
            transFlate = new FlateFilter();
            transFlate.setApplied(true);
            transFlate.setPredictor(15);
            transFlate.setColors(1);
            transFlate.setColumns(this.image.getSize().getWidthPx());
            transFlate.setBitsPerComponent(this.getBitsPerComponent());
         } catch (PDFFilterException var20) {
            throw new RuntimeException("FlateFilter configuration error", var20);
         }

         BitmapImage alphaMask = new BitmapImage("Mask:" + this.getKey(), this.image.getSize().getWidthPx(), this.image.getSize().getHeightPx(), baos.toByteArray(), (PDFReference)null);
         alphaMask.setPDFFilter(transFlate);
         alphaMask.disallowMultipleFilters();
         alphaMask.setColorSpace(new PDFDeviceColorSpace(1));
         this.softMask = doc.addImage((PDFResourceContext)null, alphaMask).makeReference();
      }

   }

   public PDFDeviceColorSpace getColorSpace() {
      return toPDFColorSpace(this.image.getColorSpace());
   }

   public int getBitsPerComponent() {
      return ((ImageRawPNG)this.image).getBitDepth();
   }

   public boolean isTransparent() {
      return ((ImageRawPNG)this.image).isTransparent();
   }

   public PDFColor getTransparentColor() {
      return new PDFColor(((ImageRawPNG)this.image).getTransparentColor());
   }

   public String getMask() {
      return this.maskRef;
   }

   public String getSoftMask() {
      return this.softMask.toString();
   }

   public PDFReference getSoftMaskReference() {
      return this.softMask;
   }

   public PDFFilter getPDFFilter() {
      return this.pdfFilter;
   }

   public void outputContents(OutputStream out) throws IOException {
      InputStream in = ((ImageRawStream)this.image).createInputStream();

      try {
         if (this.numberOfInterleavedComponents != 1 && this.numberOfInterleavedComponents != 3) {
            int numBytes = this.numberOfInterleavedComponents - 1;
            int numColumns = this.image.getSize().getWidthPx();
            InflaterInputStream infStream = new InflaterInputStream(in, new Inflater());
            DataInputStream dataStream = new DataInputStream(infStream);
            int offset = 0;
            int bytesPerRow = this.numberOfInterleavedComponents * numColumns;

            int filter;
            DeflaterOutputStream dos;
            for(dos = new DeflaterOutputStream(out, new Deflater()); (filter = dataStream.read()) != -1; offset = 0) {
               byte[] bytes = new byte[bytesPerRow];
               dataStream.readFully(bytes, 0, bytesPerRow);
               dos.write((byte)filter);

               for(int j = 0; j < numColumns; ++j) {
                  dos.write(bytes, offset, numBytes);
                  offset += this.numberOfInterleavedComponents;
               }
            }

            dos.close();
         } else {
            IOUtils.copy(in, out);
         }
      } finally {
         IOUtils.closeQuietly(in);
      }

   }

   public String getFilterHint() {
      return "precompressed";
   }

   public void populateXObjectDictionary(PDFDictionary dict) {
      int renderingIntent = ((ImageRawPNG)this.image).getRenderingIntent();
      if (renderingIntent != -1) {
         switch (renderingIntent) {
            case 0:
               dict.put("Intent", RI_PERCEPTUAL);
               break;
            case 1:
               dict.put("Intent", RI_RELATIVE_COLORIMETRIC);
               break;
            case 2:
               dict.put("Intent", RI_SATURATION);
               break;
            case 3:
               dict.put("Intent", RI_ABSOLUTE_COLORIMETRIC);
         }
      }

      ColorModel cm = ((ImageRawPNG)this.image).getColorModel();
      if (cm instanceof IndexColorModel) {
         IndexColorModel icm = (IndexColorModel)cm;
         super.populateXObjectDictionaryForIndexColorModel(dict, icm);
      }

   }

   protected boolean issRGB() {
      return ((ImageRawPNG)this.image).getRenderingIntent() != -1;
   }
}
