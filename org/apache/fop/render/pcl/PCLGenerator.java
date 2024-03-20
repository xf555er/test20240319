package org.apache.fop.render.pcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.pcl.fonts.PCLSoftFontManager;
import org.apache.fop.util.bitmap.BitmapImageUtil;
import org.apache.fop.util.bitmap.DitherUtil;
import org.apache.xmlgraphics.util.UnitConv;

public class PCLGenerator {
   private static final String US_ASCII = "US-ASCII";
   private static final String ISO_8859_1 = "ISO-8859-1";
   public static final char ESC = '\u001b';
   public static final int[] PCL_RESOLUTIONS = new int[]{75, 100, 150, 200, 300, 600};
   private final DecimalFormatSymbols symbols;
   private final DecimalFormat df2;
   private final DecimalFormat df4;
   private final CountingOutputStream out;
   protected Map fontReaderMap;
   protected Map fontManagerMap;
   private boolean currentSourceTransparency;
   private boolean currentPatternTransparency;
   private int maxBitmapResolution;
   private float ditheringQuality;
   private static final boolean USE_PCL_SHADES = false;
   private static int jaiAvailable = -1;
   private static final byte[] THRESHOLD_TABLE = new byte[256];

   public PCLGenerator(OutputStream out) {
      this.symbols = new DecimalFormatSymbols(Locale.US);
      this.df2 = new DecimalFormat("0.##", this.symbols);
      this.df4 = new DecimalFormat("0.####", this.symbols);
      this.fontReaderMap = new HashMap();
      this.fontManagerMap = new LinkedHashMap();
      this.currentSourceTransparency = true;
      this.currentPatternTransparency = true;
      this.maxBitmapResolution = PCL_RESOLUTIONS[PCL_RESOLUTIONS.length - 1];
      this.ditheringQuality = 0.5F;
      this.out = new CountingOutputStream(out);
   }

   public PCLGenerator(OutputStream out, int maxResolution) {
      this(out);
      boolean found = false;
      int[] var4 = PCL_RESOLUTIONS;
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         int pclResolutions = var4[var6];
         if (pclResolutions == maxResolution) {
            found = true;
            break;
         }
      }

      if (!found) {
         throw new IllegalArgumentException("Illegal value for maximum resolution!");
      } else {
         this.maxBitmapResolution = maxResolution;
      }
   }

   public void addFont(PCLSoftFontManager sfManager, Typeface font) {
      if (!this.fontManagerMap.containsKey(sfManager)) {
         this.fontManagerMap.put(sfManager, new LinkedHashMap());
      }

      Map fonts = (Map)this.fontManagerMap.get(sfManager);
      if (!fonts.containsKey(font)) {
         fonts.put(font, this.out.getByteCount());
      }

   }

   public OutputStream getOutputStream() {
      return this.out;
   }

   public String getTextEncoding() {
      return "ISO-8859-1";
   }

   public int getMaximumBitmapResolution() {
      return this.maxBitmapResolution;
   }

   public void writeCommand(String cmd) throws IOException {
      this.out.write(27);
      this.out.write(cmd.getBytes("US-ASCII"));
   }

   public void writeText(String s) throws IOException {
      this.out.write(s.getBytes("ISO-8859-1"));
   }

   public void writeBytes(byte[] bytes) throws IOException {
      this.out.write(bytes);
   }

   public final String formatDouble2(double value) {
      return this.df2.format(value);
   }

   public final String formatDouble4(double value) {
      return this.df4.format(value);
   }

   public void universalEndOfLanguage() throws IOException {
      this.writeCommand("%-12345X");
   }

   public void resetPrinter() throws IOException {
      this.writeCommand("E");
   }

   public void separateJobs() throws IOException {
      this.writeCommand("&l1T");
   }

   public void formFeed() throws IOException {
      this.out.write(12);
   }

   public void setUnitOfMeasure(int value) throws IOException {
      this.writeCommand("&u" + value + "D");
   }

   public void setRasterGraphicsResolution(int value) throws IOException {
      this.writeCommand("*t" + value + "R");
   }

   public void selectPageSize(int selector) throws IOException {
      this.writeCommand("&l" + selector + "A");
   }

   public void selectPaperSource(int selector) throws IOException {
      this.writeCommand("&l" + selector + "H");
   }

   public void selectOutputBin(int selector) throws IOException {
      this.writeCommand("&l" + selector + "G");
   }

   public void selectDuplexMode(int selector) throws IOException {
      this.writeCommand("&l" + selector + "S");
   }

   public void clearHorizontalMargins() throws IOException {
      this.writeCommand("9");
   }

   public void setTopMargin(int numberOfLines) throws IOException {
      this.writeCommand("&l" + numberOfLines + "E");
   }

   public void setTextLength(int numberOfLines) throws IOException {
      this.writeCommand("&l" + numberOfLines + "F");
   }

   public void setVMI(double value) throws IOException {
      this.writeCommand("&l" + this.formatDouble4(value) + "C");
   }

   public void setCursorPos(double x, double y) throws IOException {
      if (x < 0.0) {
         this.writeCommand("&a0h" + this.formatDouble2(x / 100.0) + "h" + this.formatDouble2(y / 100.0) + "V");
      } else {
         this.writeCommand("&a" + this.formatDouble2(x / 100.0) + "h" + this.formatDouble2(y / 100.0) + "V");
      }

   }

   public void pushCursorPos() throws IOException {
      this.writeCommand("&f0S");
   }

   public void popCursorPos() throws IOException {
      this.writeCommand("&f1S");
   }

   public void changePrintDirection(int rotate) throws IOException {
      this.writeCommand("&a" + rotate + "P");
   }

   public void enterHPGL2Mode(boolean restorePreviousHPGL2Cursor) throws IOException {
      if (restorePreviousHPGL2Cursor) {
         this.writeCommand("%0B");
      } else {
         this.writeCommand("%1B");
      }

   }

   public void enterPCLMode(boolean restorePreviousPCLCursor) throws IOException {
      if (restorePreviousPCLCursor) {
         this.writeCommand("%0A");
      } else {
         this.writeCommand("%1A");
      }

   }

   protected void fillRect(int w, int h, Color col, boolean colorEnabled) throws IOException {
      if (w != 0 && h != 0) {
         if (h < 0) {
            h *= -1;
         }

         this.setPatternTransparencyMode(false);
         if (!Color.black.equals(col) && !Color.white.equals(col)) {
            if (colorEnabled) {
               this.selectColor(col);
               this.writeCommand("*c" + this.formatDouble4((double)w / 100.0) + "h" + this.formatDouble4((double)h / 100.0) + "V");
               this.writeCommand("*c0P");
            } else {
               this.defineGrayscalePattern(col, 32, 4);
               this.writeCommand("*c" + this.formatDouble4((double)w / 100.0) + "h" + this.formatDouble4((double)h / 100.0) + "V");
               this.writeCommand("*c32G");
               this.writeCommand("*c4P");
            }
         } else {
            this.writeCommand("*c" + this.formatDouble4((double)w / 100.0) + "h" + this.formatDouble4((double)h / 100.0) + "V");
            int lineshade = this.convertToPCLShade(col);
            this.writeCommand("*c" + lineshade + "G");
            this.writeCommand("*c2P");
         }

         this.setPatternTransparencyMode(true);
      }
   }

   public void defineGrayscalePattern(Color col, int patternID, int ditherMatrixSize) throws IOException {
      ByteArrayOutputStream baout = new ByteArrayOutputStream();
      DataOutputStream data = new DataOutputStream(baout);
      data.writeByte(0);
      data.writeByte(0);
      data.writeByte(1);
      data.writeByte(0);
      data.writeShort(8);
      data.writeShort(8);
      int gray255 = this.convertToGray(col.getRed(), col.getGreen(), col.getBlue());
      byte[] pattern;
      if (ditherMatrixSize == 8) {
         pattern = DitherUtil.getBayerDither(8, gray255, false);
      } else {
         pattern = DitherUtil.getBayerDither(4, gray255, true);
      }

      data.write(pattern);
      if (baout.size() % 2 > 0) {
         baout.write(0);
      }

      this.writeCommand("*c" + patternID + "G");
      this.writeCommand("*c" + baout.size() + "W");
      baout.writeTo(this.out);
      IOUtils.closeQuietly((OutputStream)data);
      IOUtils.closeQuietly((OutputStream)baout);
      this.writeCommand("*c4Q");
   }

   public void setSourceTransparencyMode(boolean transparent) throws IOException {
      this.setTransparencyMode(transparent, this.currentPatternTransparency);
   }

   public void setPatternTransparencyMode(boolean transparent) throws IOException {
      this.setTransparencyMode(this.currentSourceTransparency, transparent);
   }

   public void setTransparencyMode(boolean source, boolean pattern) throws IOException {
      if (source != this.currentSourceTransparency && pattern != this.currentPatternTransparency) {
         this.writeCommand("*v" + (source ? '0' : '1') + "n" + (pattern ? '0' : '1') + "O");
      } else if (source != this.currentSourceTransparency) {
         this.writeCommand("*v" + (source ? '0' : '1') + "N");
      } else if (pattern != this.currentPatternTransparency) {
         this.writeCommand("*v" + (pattern ? '0' : '1') + "O");
      }

      this.currentSourceTransparency = source;
      this.currentPatternTransparency = pattern;
   }

   public final int convertToGray(int r, int g, int b) {
      return BitmapImageUtil.convertToGray(r, g, b);
   }

   public final int convertToPCLShade(Color col) {
      float gray = (float)this.convertToGray(col.getRed(), col.getGreen(), col.getBlue()) / 255.0F;
      return (int)(100.0F - gray * 100.0F);
   }

   public void selectGrayscale(Color col) throws IOException {
      if (Color.black.equals(col)) {
         this.selectCurrentPattern(0, 0);
      } else if (Color.white.equals(col)) {
         this.selectCurrentPattern(0, 1);
      } else {
         this.defineGrayscalePattern(col, 32, 4);
         this.selectCurrentPattern(32, 4);
      }

   }

   public void selectColor(Color col) throws IOException {
      this.writeCommand("*v6W");
      this.writeBytes(new byte[]{0, 1, 1, 8, 8, 8});
      this.writeCommand(String.format("*v%da%db%dc0I", col.getRed(), col.getGreen(), col.getBlue()));
      this.writeCommand("*v0S");
   }

   public void selectCurrentPattern(int patternID, int pattern) throws IOException {
      if (pattern > 1) {
         this.writeCommand("*c" + patternID + "G");
      }

      this.writeCommand("*v" + pattern + "T");
   }

   public void setDitheringQuality(float quality) {
      quality = Math.min(Math.max(0.0F, quality), 1.0F);
      this.ditheringQuality = quality;
   }

   public float getDitheringQuality() {
      return this.ditheringQuality;
   }

   public static boolean isMonochromeImage(RenderedImage img) {
      return BitmapImageUtil.isMonochromeImage(img);
   }

   public static boolean isGrayscaleImage(RenderedImage img) {
      return BitmapImageUtil.isGrayscaleImage(img);
   }

   public static boolean isJAIAvailable() {
      if (jaiAvailable < 0) {
         try {
            String clName = "javax.media.jai.JAI";
            Class.forName(clName);
            jaiAvailable = 1;
         } catch (ClassNotFoundException var1) {
            jaiAvailable = 0;
         }
      }

      return jaiAvailable > 0;
   }

   private int calculatePCLResolution(int resolution) {
      return this.calculatePCLResolution(resolution, false);
   }

   private int calculatePCLResolution(int resolution, boolean increased) {
      int choice = -1;

      for(int i = PCL_RESOLUTIONS.length - 2; i >= 0; --i) {
         if (resolution > PCL_RESOLUTIONS[i]) {
            int idx = i + 1;
            if (idx < PCL_RESOLUTIONS.length - 2) {
               idx += increased ? 2 : 0;
            } else if (idx < PCL_RESOLUTIONS.length - 1) {
               idx += increased ? 1 : 0;
            }

            choice = idx;
            break;
         }
      }

      if (choice < 0) {
         choice = increased ? 2 : 0;
      }

      while(choice > 0 && PCL_RESOLUTIONS[choice] > this.getMaximumBitmapResolution()) {
         --choice;
      }

      return PCL_RESOLUTIONS[choice];
   }

   private boolean isValidPCLResolution(int resolution) {
      return resolution == this.calculatePCLResolution(resolution);
   }

   public void paintBitmap(RenderedImage img, Dimension targetDim, boolean sourceTransparency, PCLRenderingUtil pclUtil) throws IOException {
      boolean printerSupportsColor = pclUtil.isColorEnabled();
      boolean monochrome = isMonochromeImage(img);
      double targetHResolution = (double)img.getWidth() / UnitConv.mpt2in((double)targetDim.width);
      double targetVResolution = (double)img.getHeight() / UnitConv.mpt2in((double)targetDim.height);
      double targetResolution = Math.max(targetHResolution, targetVResolution);
      int resolution = (int)Math.round(targetResolution);
      int effResolution = this.calculatePCLResolution(resolution, !printerSupportsColor || monochrome);
      Dimension orgDim = new Dimension(img.getWidth(), img.getHeight());
      Dimension effDim;
      if (targetResolution == (double)effResolution) {
         effDim = orgDim;
      } else {
         effDim = new Dimension((int)Math.ceil(UnitConv.mpt2px((double)targetDim.width, effResolution)), (int)Math.ceil(UnitConv.mpt2px((double)targetDim.height, effResolution)));
      }

      boolean scaled = !orgDim.equals(effDim);
      Object effImg;
      if (!monochrome) {
         if (printerSupportsColor) {
            effImg = img;
            if (scaled) {
               effImg = BitmapImageUtil.convertTosRGB(img, effDim);
            }

            this.selectCurrentPattern(0, 0);
            this.renderImageAsColor((RenderedImage)effImg, effResolution);
         } else {
            RenderedImage red = BitmapImageUtil.convertToMonochrome(img, effDim, this.ditheringQuality);
            this.selectCurrentPattern(0, 0);
            this.setTransparencyMode(sourceTransparency, true);
            this.paintMonochromeBitmap(red, effResolution);
         }
      } else {
         effImg = img;
         if (scaled) {
            effImg = BitmapImageUtil.convertToMonochrome(img, effDim);
         }

         this.setSourceTransparencyMode(sourceTransparency);
         this.selectCurrentPattern(0, 0);
         this.paintMonochromeBitmap((RenderedImage)effImg, effResolution);
      }

   }

   private int toGray(int rgb) {
      double greyVal = 0.072169 * (double)(rgb & 255);
      rgb >>= 8;
      greyVal += 0.71516 * (double)(rgb & 255);
      rgb >>= 8;
      greyVal += 0.212671 * (double)(rgb & 255);
      return (int)greyVal;
   }

   private void renderImageAsColor(RenderedImage imgOrg, int dpi) throws IOException {
      BufferedImage img = new BufferedImage(imgOrg.getWidth(), imgOrg.getHeight(), 1);
      Graphics2D g = img.createGraphics();
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, imgOrg.getWidth(), imgOrg.getHeight());
      g.drawImage((Image)imgOrg, 0, 0, (ImageObserver)null);
      if (!this.isValidPCLResolution(dpi)) {
         throw new IllegalArgumentException("Invalid PCL resolution: " + dpi);
      } else {
         int w = img.getWidth();
         ColorModel cm = img.getColorModel();
         byte[] buf;
         if (cm instanceof DirectColorModel) {
            this.writeCommand("*v6W");
            this.out.write(new byte[]{0, 3, 0, 8, 8, 8});
         } else {
            IndexColorModel icm = (IndexColorModel)cm;
            this.writeCommand("*v6W");
            this.out.write(new byte[]{0, 1, (byte)icm.getMapSize(), 8, 8, 8});
            byte[] reds = new byte[256];
            byte[] greens = new byte[256];
            buf = new byte[256];
            icm.getReds(reds);
            icm.getGreens(greens);
            icm.getBlues(buf);

            for(int i = 0; i < icm.getMapSize(); ++i) {
               this.writeCommand("*v" + (reds[i] & 255) + "A");
               this.writeCommand("*v" + (greens[i] & 255) + "B");
               this.writeCommand("*v" + (buf[i] & 255) + "C");
               this.writeCommand("*v" + i + "I");
            }
         }

         this.setRasterGraphicsResolution(dpi);
         this.writeCommand("*r0f" + img.getHeight() + "t" + w + "S");
         this.writeCommand("*r1A");
         Raster raster = img.getData();
         ColorEncoder encoder = new ColorEncoder(img);
         int scanlineStride;
         int idx;
         int y;
         int maxy;
         int x;
         if (cm.getTransferType() == 0) {
            DataBufferByte dataBuffer = (DataBufferByte)raster.getDataBuffer();
            if (!(img.getSampleModel() instanceof MultiPixelPackedSampleModel) || dataBuffer.getNumBanks() != 1) {
               throw new IOException("Unsupported image");
            }

            buf = dataBuffer.getData();
            MultiPixelPackedSampleModel sampleModel = (MultiPixelPackedSampleModel)img.getSampleModel();
            scanlineStride = sampleModel.getScanlineStride();
            idx = 0;
            y = 0;

            for(maxy = img.getHeight(); y < maxy; ++y) {
               for(x = 0; x < scanlineStride; ++x) {
                  encoder.add8Bits(buf[idx]);
                  ++idx;
               }

               encoder.endLine();
            }
         } else {
            if (cm.getTransferType() != 3) {
               throw new IOException("Unsupported image");
            }

            DataBufferInt dataBuffer = (DataBufferInt)raster.getDataBuffer();
            if (!(img.getSampleModel() instanceof SinglePixelPackedSampleModel) || dataBuffer.getNumBanks() != 1) {
               throw new IOException("Unsupported image");
            }

            int[] buf = dataBuffer.getData();
            SinglePixelPackedSampleModel sampleModel = (SinglePixelPackedSampleModel)img.getSampleModel();
            scanlineStride = sampleModel.getScanlineStride();
            idx = 0;
            y = 0;

            for(maxy = img.getHeight(); y < maxy; ++y) {
               for(x = 0; x < scanlineStride; ++x) {
                  encoder.add8Bits((byte)(buf[idx] >> 16));
                  encoder.add8Bits((byte)(buf[idx] >> 8));
                  encoder.add8Bits((byte)(buf[idx] >> 0));
                  ++idx;
               }

               encoder.endLine();
            }
         }

         this.writeCommand("*rB");
      }
   }

   public void paintMonochromeBitmap(RenderedImage img, int resolution) throws IOException {
      if (!this.isValidPCLResolution(resolution)) {
         throw new IllegalArgumentException("Invalid PCL resolution: " + resolution);
      } else {
         boolean monochrome = isMonochromeImage(img);
         if (!monochrome) {
            throw new IllegalArgumentException("img must be a monochrome image");
         } else {
            this.setRasterGraphicsResolution(resolution);
            this.writeCommand("*r0f" + img.getHeight() + "t" + img.getWidth() + "s1A");
            Raster raster = img.getData();
            Encoder encoder = new Encoder(img);
            int imgw = img.getWidth();
            IndexColorModel cm = (IndexColorModel)img.getColorModel();
            int x;
            int scanlineStride;
            int idx;
            if (cm.getTransferType() == 0) {
               DataBufferByte dataBuffer = (DataBufferByte)raster.getDataBuffer();
               MultiPixelPackedSampleModel packedSampleModel = new MultiPixelPackedSampleModel(0, img.getWidth(), img.getHeight(), 1);
               int x;
               int maxx;
               if (img.getSampleModel().equals(packedSampleModel) && dataBuffer.getNumBanks() == 1) {
                  byte[] buf = dataBuffer.getData();
                  scanlineStride = packedSampleModel.getScanlineStride();
                  idx = 0;
                  x = this.toGray(cm.getRGB(0));
                  maxx = this.toGray(cm.getRGB(1));
                  boolean zeroIsWhite = x > maxx;
                  int y = 0;

                  for(int maxy = img.getHeight(); y < maxy; ++y) {
                     int x = 0;

                     for(int maxx = scanlineStride; x < maxx; ++x) {
                        if (zeroIsWhite) {
                           encoder.add8Bits(buf[idx]);
                        } else {
                           encoder.add8Bits((byte)(~buf[idx]));
                        }

                        ++idx;
                     }

                     encoder.endLine();
                  }
               } else {
                  x = 0;

                  for(scanlineStride = img.getHeight(); x < scanlineStride; ++x) {
                     byte[] line = (byte[])((byte[])raster.getDataElements(0, x, imgw, 1, (Object)null));
                     x = 0;

                     for(maxx = imgw; x < maxx; ++x) {
                        encoder.addBit(line[x] == 0);
                     }

                     encoder.endLine();
                  }
               }
            } else {
               int y = 0;

               for(int maxy = img.getHeight(); y < maxy; ++y) {
                  x = 0;

                  for(scanlineStride = imgw; x < scanlineStride; ++x) {
                     idx = raster.getSample(x, y, 0);
                     encoder.addBit(idx == 0);
                  }

                  encoder.endLine();
               }
            }

            this.writeCommand("*rB");
         }
      }
   }

   static {
      for(int i = 0; i < 256; ++i) {
         THRESHOLD_TABLE[i] = (byte)(i < 240 ? 255 : 0);
      }

   }

   private class ColorEncoder {
      private int imgw;
      private int bytewidth;
      private byte ib;
      private int currentIndex;
      private int len;
      private int shiftBit = 128;
      private int whiteLines;
      final byte[] zeros;
      final byte[] buff1;
      final byte[] buff2;
      final byte[] encodedRun;
      final byte[] encodedTagged;
      final byte[] encodedDelta;
      byte[] seed;
      byte[] current;
      int compression;
      int seedLen;

      public ColorEncoder(RenderedImage img) {
         this.imgw = img.getWidth();
         this.bytewidth = this.imgw * 3 + 1;
         this.zeros = new byte[this.bytewidth];
         this.buff1 = new byte[this.bytewidth];
         this.buff2 = new byte[this.bytewidth];
         this.encodedRun = new byte[this.bytewidth];
         this.encodedTagged = new byte[this.bytewidth];
         this.encodedDelta = new byte[this.bytewidth];
         this.seed = this.buff1;
         this.current = this.buff2;
         this.seedLen = 0;
         this.compression = -1;
         System.arraycopy(this.zeros, 0, this.seed, 0, this.zeros.length);
      }

      private int runCompression(byte[] buff, int len) {
         int bytes = 0;

         try {
            int i = 0;

            while(i < len) {
               byte seed = this.current[i++];

               int sameCount;
               for(sameCount = 1; i < len && this.current[i] == seed; ++i) {
                  ++sameCount;
               }

               while(sameCount > 256) {
                  buff[bytes++] = -1;
                  buff[bytes++] = seed;
                  sameCount -= 256;
               }

               if (sameCount > 0) {
                  buff[bytes++] = (byte)(sameCount - 1);
                  buff[bytes++] = seed;
               }
            }

            return bytes;
         } catch (ArrayIndexOutOfBoundsException var7) {
            return len + 1;
         }
      }

      private int deltaCompression(byte[] seed, byte[] buff, int len) {
         int bytes = 0;

         try {
            int i = 0;

            while(i < len) {
               int sameCount;
               for(sameCount = 0; i < len && this.current[i] == seed[i]; ++i) {
                  ++sameCount;
               }

               int diffCount;
               for(diffCount = 0; i < len && this.current[i] != seed[i]; ++i) {
                  ++diffCount;
               }

               while(diffCount != 0) {
                  int diffToWrite = diffCount > 8 ? 8 : diffCount;
                  int sameToWrite = sameCount > 31 ? 31 : sameCount;
                  buff[bytes++] = (byte)(diffToWrite - 1 << 5 | sameToWrite);
                  sameCount -= sameToWrite;
                  if (sameToWrite == 31) {
                     while(sameCount >= 255) {
                        buff[bytes++] = -1;
                        sameCount -= 255;
                     }

                     buff[bytes++] = (byte)sameCount;
                     sameCount = 0;
                  }

                  System.arraycopy(this.current, i - diffCount, buff, bytes, diffToWrite);
                  bytes += diffToWrite;
                  diffCount -= diffToWrite;
               }
            }

            return bytes;
         } catch (ArrayIndexOutOfBoundsException var10) {
            return len + 1;
         }
      }

      private int tiffCompression(byte[] encodedTagged, int len) {
         int literalCount = 0;
         int bytes = 0;

         try {
            int from = 0;

            while(from < len) {
               int repeatValue = this.current[from];

               int repeatLength;
               for(repeatLength = 1; repeatLength < 128 && from + repeatLength < len && this.current[from + repeatLength] == repeatValue; ++repeatLength) {
               }

               if (literalCount == 128 || repeatLength > 2 && literalCount > 0) {
                  encodedTagged[bytes++] = (byte)(literalCount - 1);
                  System.arraycopy(this.current, from - literalCount, encodedTagged, bytes, literalCount);
                  bytes += literalCount;
                  literalCount = 0;
               }

               if (repeatLength > 2) {
                  encodedTagged[bytes++] = (byte)(1 - repeatLength);
                  encodedTagged[bytes++] = this.current[from];
                  from += repeatLength;
               } else {
                  ++literalCount;
                  ++from;
               }
            }

            if (literalCount > 0) {
               encodedTagged[bytes++] = (byte)(literalCount - 1);
               System.arraycopy(this.current, 3 * len - literalCount, encodedTagged, bytes, literalCount);
               bytes += literalCount;
            }

            return bytes;
         } catch (ArrayIndexOutOfBoundsException var8) {
            return len + 1;
         }
      }

      public void addBit(boolean bit) {
         if (bit) {
            this.ib = (byte)(this.ib | this.shiftBit);
         }

         this.shiftBit >>= 1;
         if (this.shiftBit == 0) {
            this.add8Bits(this.ib);
            this.shiftBit = 128;
            this.ib = 0;
         }

      }

      public void add8Bits(byte b) {
         this.current[this.currentIndex++] = b;
         if (b != 0) {
            this.len = this.currentIndex;
         }

      }

      public void endLine() throws IOException {
         if (this.len == 0) {
            ++this.whiteLines;
         } else {
            if (this.whiteLines > 0) {
               PCLGenerator.this.writeCommand("*b" + this.whiteLines + "Y");
               this.whiteLines = 0;
            }

            int unencodedCount = this.len;
            int runCount = this.runCompression(this.encodedRun, this.len);
            int tiffCount = this.tiffCompression(this.encodedTagged, this.len);
            int deltaCount = this.deltaCompression(this.seed, this.encodedDelta, Math.max(this.len, this.seedLen));
            int bestCount = Math.min(unencodedCount, Math.min(runCount, Math.min(tiffCount, deltaCount)));
            byte bestCompression;
            if (bestCount == unencodedCount) {
               bestCompression = 0;
            } else if (bestCount == runCount) {
               bestCompression = 1;
            } else if (bestCount == tiffCount) {
               bestCompression = 2;
            } else {
               bestCompression = 3;
            }

            if (this.compression != bestCompression) {
               this.compression = bestCompression;
               PCLGenerator.this.writeCommand("*b" + this.compression + "M");
            }

            if (bestCompression == 0) {
               PCLGenerator.this.writeCommand("*b" + unencodedCount + "W");
               PCLGenerator.this.out.write(this.current, 0, unencodedCount);
            } else if (bestCompression == 1) {
               PCLGenerator.this.writeCommand("*b" + runCount + "W");
               PCLGenerator.this.out.write(this.encodedRun, 0, runCount);
            } else if (bestCompression == 2) {
               PCLGenerator.this.writeCommand("*b" + tiffCount + "W");
               PCLGenerator.this.out.write(this.encodedTagged, 0, tiffCount);
            } else if (bestCompression == 3) {
               PCLGenerator.this.writeCommand("*b" + deltaCount + "W");
               PCLGenerator.this.out.write(this.encodedDelta, 0, deltaCount);
            }

            if (this.current == this.buff1) {
               this.seed = this.buff1;
               this.current = this.buff2;
            } else {
               this.seed = this.buff2;
               this.current = this.buff1;
            }

            this.seedLen = this.len;
         }

         this.shiftBit = 128;
         this.ib = 0;
         this.len = 0;
         this.currentIndex = 0;
      }
   }

   private class Encoder {
      private int imgw;
      private int bytewidth;
      private byte[] rle;
      private byte[] uncompressed;
      private int lastcount = -1;
      private byte lastbyte;
      private int rlewidth;
      private byte ib;
      private int x;
      private boolean zeroRow = true;

      public Encoder(RenderedImage img) {
         this.imgw = img.getWidth();
         this.bytewidth = this.imgw / 8;
         if (this.imgw % 8 != 0) {
            ++this.bytewidth;
         }

         this.rle = new byte[this.bytewidth * 2];
         this.uncompressed = new byte[this.bytewidth];
      }

      public void addBit(boolean bit) {
         if (bit) {
            this.ib = (byte)(this.ib | 1);
         }

         if (this.x % 8 != 7 && this.x + 1 != this.imgw) {
            this.ib = (byte)(this.ib << 1);
         } else {
            this.finishedByte();
         }

         ++this.x;
      }

      public void add8Bits(byte b) {
         this.ib = b;
         this.finishedByte();
         this.x += 8;
      }

      private void finishedByte() {
         if (this.rlewidth < this.bytewidth) {
            if (this.lastcount >= 0) {
               if (this.ib == this.lastbyte) {
                  ++this.lastcount;
               } else {
                  this.rle[this.rlewidth++] = (byte)(this.lastcount & 255);
                  this.rle[this.rlewidth++] = this.lastbyte;
                  this.lastbyte = this.ib;
                  this.lastcount = 0;
               }
            } else {
               this.lastbyte = this.ib;
               this.lastcount = 0;
            }

            if (this.lastcount == 255 || this.x + 1 == this.imgw) {
               this.rle[this.rlewidth++] = (byte)(this.lastcount & 255);
               this.rle[this.rlewidth++] = this.lastbyte;
               this.lastbyte = 0;
               this.lastcount = -1;
            }
         }

         this.uncompressed[this.x / 8] = this.ib;
         if (this.ib != 0) {
            this.zeroRow = false;
         }

         this.ib = 0;
      }

      public void endLine() throws IOException {
         if (this.zeroRow && PCLGenerator.this.currentSourceTransparency) {
            PCLGenerator.this.writeCommand("*b1Y");
         } else if (this.rlewidth < this.bytewidth) {
            PCLGenerator.this.writeCommand("*b1m" + this.rlewidth + "W");
            PCLGenerator.this.out.write(this.rle, 0, this.rlewidth);
         } else {
            PCLGenerator.this.writeCommand("*b0m" + this.bytewidth + "W");
            PCLGenerator.this.out.write(this.uncompressed);
         }

         this.lastcount = -1;
         this.rlewidth = 0;
         this.ib = 0;
         this.x = 0;
         this.zeroRow = true;
      }
   }
}
