package org.apache.xmlgraphics.ps;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.apache.xmlgraphics.image.GraphicsUtil;

public class ImageEncodingHelper {
   private static final ColorModel DEFAULT_RGB_COLOR_MODEL = new ComponentColorModel(ColorSpace.getInstance(1000), false, false, 1, 0);
   private final RenderedImage image;
   private ColorModel encodedColorModel;
   private boolean firstTileDump;
   private boolean enableCMYK;
   private boolean isBGR;
   private boolean isKMYC;
   private boolean outputbw;
   private boolean bwinvert;

   public ImageEncodingHelper(RenderedImage image) {
      this(image, true);
      this.outputbw = true;
   }

   public ImageEncodingHelper(RenderedImage image, boolean enableCMYK) {
      this.image = image;
      this.enableCMYK = enableCMYK;
      this.determineEncodedColorModel();
   }

   public RenderedImage getImage() {
      return this.image;
   }

   public ColorModel getNativeColorModel() {
      return this.getImage().getColorModel();
   }

   public ColorModel getEncodedColorModel() {
      return this.encodedColorModel;
   }

   public boolean hasAlpha() {
      return this.image.getColorModel().hasAlpha();
   }

   public boolean isConverted() {
      return this.getNativeColorModel() != this.getEncodedColorModel();
   }

   private void writeRGBTo(OutputStream out) throws IOException {
      boolean encoded = encodeRenderedImageWithDirectColorModelAsRGB(this.image, out);
      if (!encoded) {
         encodeRenderedImageAsRGB(this.image, out, this.outputbw, this.bwinvert);
      }
   }

   public static void encodeRenderedImageAsRGB(RenderedImage image, OutputStream out) throws IOException {
      encodeRenderedImageAsRGB(image, out, false, false);
   }

   public static void encodeRenderedImageAsRGB(RenderedImage image, OutputStream out, boolean outputbw, boolean bwinvert) throws IOException {
      Raster raster = getRaster(image);
      int nbands = raster.getNumBands();
      int dataType = raster.getDataBuffer().getDataType();
      Object data;
      switch (dataType) {
         case 0:
            data = new byte[nbands];
            break;
         case 1:
            data = null;
            break;
         case 2:
         default:
            throw new IllegalArgumentException("Unknown data buffer type: " + dataType);
         case 3:
            data = new int[nbands];
            break;
         case 4:
            data = new float[nbands];
            break;
         case 5:
            data = new double[nbands];
      }

      ColorModel colorModel = image.getColorModel();
      int w = image.getWidth();
      int h = image.getHeight();
      int numDataElements = 3;
      if (colorModel.getPixelSize() == 1 && outputbw) {
         numDataElements = 1;
      }

      byte[] buf = new byte[w * numDataElements];

      for(int y = 0; y < h; ++y) {
         int idx = -1;

         for(int x = 0; x < w; ++x) {
            int rgb = colorModel.getRGB(raster.getDataElements(x, y, data));
            if (numDataElements > 1) {
               ++idx;
               buf[idx] = (byte)(rgb >> 16);
               ++idx;
               buf[idx] = (byte)(rgb >> 8);
            } else if (bwinvert && rgb == -1) {
               rgb = 1;
            }

            ++idx;
            buf[idx] = (byte)rgb;
         }

         out.write(buf);
      }

   }

   public static boolean encodeRenderedImageWithDirectColorModelAsRGB(RenderedImage image, OutputStream out) throws IOException {
      ColorModel cm = image.getColorModel();
      if (cm.getColorSpace() != ColorSpace.getInstance(1000)) {
         return false;
      } else if (!(cm instanceof DirectColorModel)) {
         return false;
      } else {
         DirectColorModel dcm = (DirectColorModel)cm;
         int[] templateMasks = new int[]{16711680, 65280, 255, -16777216};
         int[] masks = dcm.getMasks();
         if (!Arrays.equals(templateMasks, masks)) {
            return false;
         } else {
            Raster raster = getRaster(image);
            int dataType = raster.getDataBuffer().getDataType();
            if (dataType != 3) {
               return false;
            } else {
               int w = image.getWidth();
               int h = image.getHeight();
               int[] data = new int[w];
               byte[] buf = new byte[w * 3];

               for(int y = 0; y < h; ++y) {
                  int idx = -1;
                  raster.getDataElements(0, y, w, 1, data);

                  for(int x = 0; x < w; ++x) {
                     int rgb = data[x];
                     ++idx;
                     buf[idx] = (byte)(rgb >> 16);
                     ++idx;
                     buf[idx] = (byte)(rgb >> 8);
                     ++idx;
                     buf[idx] = (byte)rgb;
                  }

                  out.write(buf);
               }

               return true;
            }
         }
      }
   }

   private static Raster getRaster(RenderedImage image) {
      return (Raster)(image instanceof BufferedImage ? ((BufferedImage)image).getRaster() : image.getData());
   }

   public static void encodeRGBAsGrayScale(byte[] raw, int width, int height, int bitsPerPixel, OutputStream out) throws IOException {
      int pixelsPerByte = 8 / bitsPerPixel;
      int bytewidth = width / pixelsPerByte;
      if (width % pixelsPerByte != 0) {
         ++bytewidth;
      }

      byte[] linedata = new byte[bytewidth];

      for(int y = 0; y < height; ++y) {
         byte ib = 0;
         int i = 3 * y * width;

         for(int x = 0; x < width; i += 3) {
            double greyVal = 0.212671 * (double)(raw[i] & 255) + 0.71516 * (double)(raw[i + 1] & 255) + 0.072169 * (double)(raw[i + 2] & 255);
            switch (bitsPerPixel) {
               case 1:
                  if (greyVal < 128.0) {
                     ib |= (byte)(1 << 7 - x % 8);
                  }
                  break;
               case 4:
                  greyVal /= 16.0;
                  ib |= (byte)((byte)((int)greyVal) << (1 - x % 2) * 4);
                  break;
               case 8:
                  ib = (byte)((int)greyVal);
                  break;
               default:
                  throw new UnsupportedOperationException("Unsupported bits per pixel: " + bitsPerPixel);
            }

            if (x % pixelsPerByte == pixelsPerByte - 1 || x + 1 == width) {
               linedata[x / pixelsPerByte] = ib;
               ib = 0;
            }

            ++x;
         }

         out.write(linedata);
      }

   }

   private boolean optimizedWriteTo(OutputStream out) throws IOException {
      if (this.firstTileDump) {
         Raster raster = this.image.getTile(0, 0);
         DataBuffer buffer = raster.getDataBuffer();
         if (buffer instanceof DataBufferByte) {
            byte[] bytes = ((DataBufferByte)buffer).getData();
            byte[] bytesPermutated;
            int i;
            if (this.isBGR) {
               bytesPermutated = new byte[bytes.length];

               for(i = 0; i < bytes.length; i += 3) {
                  bytesPermutated[i] = bytes[i + 2];
                  bytesPermutated[i + 1] = bytes[i + 1];
                  bytesPermutated[i + 2] = bytes[i];
               }

               out.write(bytesPermutated);
            } else if (this.isKMYC) {
               bytesPermutated = new byte[bytes.length];

               for(i = 0; i < bytes.length; i += 4) {
                  bytesPermutated[i] = bytes[i + 3];
                  bytesPermutated[i + 1] = bytes[i + 2];
                  bytesPermutated[i + 2] = bytes[i + 1];
                  bytesPermutated[i + 3] = bytes[i];
               }

               out.write(bytesPermutated);
            } else {
               out.write(bytes);
            }

            return true;
         }
      }

      return false;
   }

   protected boolean isMultiTile() {
      int tilesX = this.image.getNumXTiles();
      int tilesY = this.image.getNumYTiles();
      return tilesX != 1 || tilesY != 1;
   }

   protected void determineEncodedColorModel() {
      this.firstTileDump = false;
      this.encodedColorModel = DEFAULT_RGB_COLOR_MODEL;
      ColorModel cm = this.image.getColorModel();
      ColorSpace cs = cm.getColorSpace();
      int numComponents = cm.getNumComponents();
      if (!this.isMultiTile()) {
         if (numComponents == 1 && cs.getType() == 6) {
            if (cm.getTransferType() == 0) {
               this.firstTileDump = true;
               this.encodedColorModel = cm;
            }
         } else if (cm instanceof IndexColorModel) {
            if (cm.getTransferType() == 0) {
               this.firstTileDump = true;
               this.encodedColorModel = cm;
            }
         } else if (cm instanceof ComponentColorModel && (numComponents == 3 || this.enableCMYK && numComponents == 4) && !cm.hasAlpha()) {
            Raster raster = this.image.getTile(0, 0);
            DataBuffer buffer = raster.getDataBuffer();
            SampleModel sampleModel = raster.getSampleModel();
            if (sampleModel instanceof PixelInterleavedSampleModel) {
               PixelInterleavedSampleModel piSampleModel = (PixelInterleavedSampleModel)sampleModel;
               int[] offsets = piSampleModel.getBandOffsets();

               for(int i = 0; i < offsets.length; ++i) {
                  if (offsets[i] != i && offsets[i] != offsets.length - 1 - i) {
                     return;
                  }
               }

               this.isBGR = false;
               if (offsets.length == 3 && offsets[0] == 2 && offsets[1] == 1 && offsets[2] == 0) {
                  this.isBGR = true;
               }

               if (offsets.length == 4 && offsets[0] == 3 && offsets[1] == 2 && offsets[2] == 1 && offsets[3] == 0) {
                  this.isKMYC = true;
               }
            }

            if (cm.getTransferType() == 0 && buffer.getOffset() == 0 && buffer.getNumBanks() == 1) {
               this.firstTileDump = true;
               this.encodedColorModel = cm;
            }
         }
      }

   }

   public void encode(OutputStream out) throws IOException {
      if (this.isConverted() || !this.optimizedWriteTo(out)) {
         this.writeRGBTo(out);
      }
   }

   public void encodeAlpha(OutputStream out) throws IOException {
      if (!this.hasAlpha()) {
         throw new IllegalStateException("Image doesn't have an alpha channel");
      } else {
         Raster alpha = GraphicsUtil.getAlphaRaster(this.image);
         DataBuffer buffer = alpha.getDataBuffer();
         if (buffer instanceof DataBufferByte) {
            out.write(((DataBufferByte)buffer).getData());
         } else {
            throw new UnsupportedOperationException("Alpha raster not supported: " + buffer.getClass().getName());
         }
      }
   }

   public static void encodePackedColorComponents(RenderedImage image, OutputStream out) throws IOException {
      ImageEncodingHelper helper = new ImageEncodingHelper(image);
      helper.encode(out);
   }

   public static ImageEncoder createRenderedImageEncoder(RenderedImage img) {
      return new RenderedImageEncoder(img);
   }

   public void setBWInvert(boolean v) {
      this.bwinvert = v;
   }

   private static class RenderedImageEncoder implements ImageEncoder {
      private final RenderedImage img;

      public RenderedImageEncoder(RenderedImage img) {
         this.img = img;
      }

      public void writeTo(OutputStream out) throws IOException {
         ImageEncodingHelper.encodePackedColorComponents(this.img, out);
      }

      public String getImplicitFilter() {
         return null;
      }
   }
}
