package org.apache.xmlgraphics.image.writer.internal;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.xmlgraphics.image.codec.tiff.CompressionValue;
import org.apache.xmlgraphics.image.codec.tiff.TIFFEncodeParam;
import org.apache.xmlgraphics.image.codec.tiff.TIFFField;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageEncoder;
import org.apache.xmlgraphics.image.writer.AbstractImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.MultiImageWriter;
import org.apache.xmlgraphics.image.writer.ResolutionUnit;

public class TIFFImageWriter extends AbstractImageWriter {
   public void writeImage(RenderedImage image, OutputStream out) throws IOException {
      this.writeImage(image, out, (ImageWriterParams)null);
   }

   public void writeImage(RenderedImage image, OutputStream out, ImageWriterParams params) throws IOException {
      TIFFEncodeParam encodeParams = this.createTIFFEncodeParams(params);
      TIFFImageEncoder encoder = new TIFFImageEncoder(out, encodeParams);
      encoder.encode(image);
   }

   private TIFFEncodeParam createTIFFEncodeParams(ImageWriterParams params) {
      TIFFEncodeParam encodeParams = new TIFFEncodeParam();
      if (params == null) {
         encodeParams.setCompression(CompressionValue.NONE);
      } else {
         encodeParams.setCompression(CompressionValue.getValue(params.getCompressionMethod()));
         if (params.getResolution() != null) {
            int numPixX;
            int numPixY;
            short denom;
            if (ResolutionUnit.INCH == params.getResolutionUnit()) {
               numPixX = params.getXResolution();
               numPixY = params.getYResolution();
               denom = 1;
            } else {
               float pixXSzMM = 25.4F / params.getXResolution().floatValue();
               float pixYSzMM = 25.4F / params.getYResolution().floatValue();
               numPixX = (int)((double)(100000.0F / pixXSzMM) + 0.5);
               numPixY = (int)((double)(100000.0F / pixYSzMM) + 0.5);
               denom = 10000;
            }

            long[] xRational = new long[]{(long)numPixX, (long)denom};
            long[] yRational = new long[]{(long)numPixY, (long)denom};
            TIFFField[] fields = new TIFFField[]{new TIFFField(296, 3, 1, new char[]{(char)params.getResolutionUnit().getValue()}), new TIFFField(282, 5, 1, new long[][]{xRational}), new TIFFField(283, 5, 1, new long[][]{yRational})};
            encodeParams.setExtraFields(fields);
         }
      }

      return encodeParams;
   }

   public String getMIMEType() {
      return "image/tiff";
   }

   public MultiImageWriter createMultiImageWriter(OutputStream out) throws IOException {
      return new TIFFMultiImageWriter(out);
   }

   public boolean supportsMultiImageWriter() {
      return true;
   }

   private class TIFFMultiImageWriter implements MultiImageWriter {
      private OutputStream out;
      private TIFFEncodeParam encodeParams;
      private TIFFImageEncoder encoder;
      private Object context;

      public TIFFMultiImageWriter(OutputStream out) throws IOException {
         this.out = out;
      }

      public void writeImage(RenderedImage image, ImageWriterParams params) throws IOException {
         if (this.encoder == null) {
            this.encodeParams = TIFFImageWriter.this.createTIFFEncodeParams(params);
            this.encoder = new TIFFImageEncoder(this.out, this.encodeParams);
         }

         this.context = this.encoder.encodeMultiple(this.context, image);
      }

      public void close() throws IOException {
         if (this.encoder != null) {
            this.encoder.finishMultiple(this.context);
         }

         this.encoder = null;
         this.encodeParams = null;
         this.out.flush();
      }
   }
}
