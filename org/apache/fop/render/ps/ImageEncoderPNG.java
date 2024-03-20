package org.apache.fop.render.ps;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.image.loader.impl.ImageRawPNG;
import org.apache.xmlgraphics.ps.ImageEncoder;

public class ImageEncoderPNG implements ImageEncoder {
   private final ImageRawPNG image;
   private int numberOfInterleavedComponents;

   public ImageEncoderPNG(ImageRawPNG image) {
      this.image = image;
      ColorModel cm = this.image.getColorModel();
      if (cm instanceof IndexColorModel) {
         this.numberOfInterleavedComponents = 1;
      } else {
         this.numberOfInterleavedComponents = cm.getNumComponents();
      }

   }

   public void writeTo(OutputStream out) throws IOException {
      InputStream in = this.image.createInputStream();
      InflaterInputStream infStream = null;
      DataInputStream dataStream = null;
      ByteArrayOutputStream baos = null;
      DeflaterOutputStream dos = null;

      try {
         if (this.numberOfInterleavedComponents != 1 && this.numberOfInterleavedComponents != 3) {
            int numBytes = this.numberOfInterleavedComponents - 1;
            int numColumns = this.image.getSize().getWidthPx();
            infStream = new InflaterInputStream(in, new Inflater());
            dataStream = new DataInputStream(infStream);
            int offset = 0;
            int bytesPerRow = this.numberOfInterleavedComponents * numColumns;
            baos = new ByteArrayOutputStream();

            int filter;
            for(dos = new DeflaterOutputStream(baos, new Deflater()); (filter = dataStream.read()) != -1; offset = 0) {
               byte[] bytes = new byte[bytesPerRow];
               dataStream.readFully(bytes, 0, bytesPerRow);
               dos.write((byte)filter);

               for(int j = 0; j < numColumns; ++j) {
                  dos.write(bytes, offset, numBytes);
                  offset += this.numberOfInterleavedComponents;
               }
            }

            dos.close();
            IOUtils.copy((InputStream)(new ByteArrayInputStream(baos.toByteArray())), (OutputStream)out);
         } else {
            IOUtils.copy(in, out);
         }
      } finally {
         IOUtils.closeQuietly((OutputStream)dos);
         IOUtils.closeQuietly((OutputStream)baos);
         IOUtils.closeQuietly((InputStream)dataStream);
         IOUtils.closeQuietly((InputStream)infStream);
         IOUtils.closeQuietly(in);
      }

   }

   public String getImplicitFilter() {
      String filter = "<< /Predictor 15 /Columns " + this.image.getSize().getWidthPx();
      filter = filter + " /Colors " + (this.numberOfInterleavedComponents > 2 ? 3 : 1);
      filter = filter + " /BitsPerComponent " + this.image.getBitDepth() + " >> /FlateDecode";
      return filter;
   }
}
