package org.apache.batik.ext.awt.image.rendered;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.ext.awt.image.GraphicsUtil;

public class IndexImage {
   static byte[][] computeRGB(int nCubes, Cube[] cubes) {
      byte[] r = new byte[nCubes];
      byte[] g = new byte[nCubes];
      byte[] b = new byte[nCubes];
      byte[] rgb = new byte[3];

      for(int i = 0; i < nCubes; ++i) {
         rgb = cubes[i].averageColorRGB(rgb);
         r[i] = rgb[0];
         g[i] = rgb[1];
         b[i] = rgb[2];
      }

      byte[][] result = new byte[][]{r, g, b};
      return result;
   }

   static void logRGB(byte[] r, byte[] g, byte[] b) {
      StringBuffer buff = new StringBuffer(100);
      int nColors = r.length;

      for(int i = 0; i < nColors; ++i) {
         String rgbStr = "(" + (r[i] + 128) + ',' + (g[i] + 128) + ',' + (b[i] + 128) + "),";
         buff.append(rgbStr);
      }

      System.out.println("RGB:" + nColors + buff);
   }

   static List[] createColorList(BufferedImage bi) {
      int w = bi.getWidth();
      int h = bi.getHeight();
      List[] colors = new ArrayList[4096];

      for(int i_w = 0; i_w < w; ++i_w) {
         for(int i_h = 0; i_h < h; ++i_h) {
            int rgb = bi.getRGB(i_w, i_h) & 16777215;
            int idx = (rgb & 15728640) >>> 12 | (rgb & '\uf000') >>> 8 | (rgb & 240) >>> 4;
            List v = colors[idx];
            if (v == null) {
               v = new ArrayList();
               v.add(new Counter(rgb));
               colors[idx] = v;
            } else {
               Iterator i = v.iterator();

               do {
                  if (!i.hasNext()) {
                     v.add(new Counter(rgb));
                     break;
                  }
               } while(!((Counter)i.next()).add(rgb));
            }
         }
      }

      return colors;
   }

   static Counter[][] convertColorList(List[] colors) {
      Counter[] EMPTY_COUNTER = new Counter[0];
      Counter[][] colorTbl = new Counter[4096][];

      for(int i = 0; i < colors.length; ++i) {
         List cl = colors[i];
         if (cl == null) {
            colorTbl[i] = EMPTY_COUNTER;
         } else {
            int nSlots = cl.size();
            colorTbl[i] = (Counter[])((Counter[])cl.toArray(new Counter[nSlots]));
            colors[i] = null;
         }
      }

      return colorTbl;
   }

   public static BufferedImage getIndexedImage(BufferedImage bi, int nColors) {
      int w = bi.getWidth();
      int h = bi.getHeight();
      List[] colors = createColorList(bi);
      Counter[][] colorTbl = convertColorList(colors);
      colors = null;
      int nCubes = 1;
      int fCube = 0;
      Cube[] cubes = new Cube[nColors];
      cubes[0] = new Cube(colorTbl, w * h);

      int i;
      while(nCubes < nColors) {
         while(cubes[fCube].isDone()) {
            ++fCube;
            if (fCube == nCubes) {
               break;
            }
         }

         if (fCube == nCubes) {
            break;
         }

         Cube c = cubes[fCube];
         Cube nc = c.split();
         if (nc != null) {
            if (nc.count > c.count) {
               Cube tmp = c;
               c = nc;
               nc = tmp;
            }

            int j = fCube;
            int cnt = c.count;

            for(i = fCube + 1; i < nCubes && cubes[i].count >= cnt; ++i) {
               cubes[j++] = cubes[i];
            }

            cubes[j++] = c;

            for(cnt = nc.count; j < nCubes && cubes[j].count >= cnt; ++j) {
            }

            for(i = nCubes; i > j; --i) {
               cubes[i] = cubes[i - 1];
            }

            cubes[j++] = nc;
            ++nCubes;
         }
      }

      byte[][] rgbTbl = computeRGB(nCubes, cubes);
      IndexColorModel icm = new IndexColorModel(8, nCubes, rgbTbl[0], rgbTbl[1], rgbTbl[2]);
      BufferedImage indexed = new BufferedImage(w, h, 13, icm);
      Graphics2D g2d = indexed.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      g2d.drawImage(bi, 0, 0, (ImageObserver)null);
      g2d.dispose();

      for(i = 1; i <= 8 && 1 << i < nCubes; ++i) {
      }

      if (i > 4) {
         return indexed;
      } else {
         if (i == 3) {
            i = 4;
         }

         ColorModel cm = new IndexColorModel(i, nCubes, rgbTbl[0], rgbTbl[1], rgbTbl[2]);
         SampleModel sm = new MultiPixelPackedSampleModel(0, w, h, i);
         WritableRaster ras = Raster.createWritableRaster(sm, new Point(0, 0));
         bi = indexed;
         indexed = new BufferedImage(cm, ras, indexed.isAlphaPremultiplied(), (Hashtable)null);
         GraphicsUtil.copyData(bi, indexed);
         return indexed;
      }
   }

   private static class Cube {
      static final byte[] RGB_BLACK = new byte[]{0, 0, 0};
      int[] min = new int[]{0, 0, 0};
      int[] max = new int[]{255, 255, 255};
      boolean done = false;
      final Counter[][] colors;
      int count = 0;
      static final int RED = 0;
      static final int GRN = 1;
      static final int BLU = 2;

      Cube(Counter[][] colors, int count) {
         this.colors = colors;
         this.count = count;
      }

      public boolean isDone() {
         return this.done;
      }

      private boolean contains(int[] val) {
         int vRed = val[0];
         int vGrn = val[1];
         int vBlu = val[2];
         return this.min[0] <= vRed && vRed <= this.max[0] && this.min[1] <= vGrn && vGrn <= this.max[1] && this.min[2] <= vBlu && vBlu <= this.max[2];
      }

      Cube split() {
         int dr = this.max[0] - this.min[0] + 1;
         int dg = this.max[1] - this.min[1] + 1;
         int db = this.max[2] - this.min[2] + 1;
         byte c0;
         byte c1;
         byte splitChannel;
         if (dr >= dg) {
            if (dr >= db) {
               splitChannel = 0;
               c0 = 1;
               c1 = 2;
            } else {
               splitChannel = 2;
               c0 = 0;
               c1 = 1;
            }
         } else if (dg >= db) {
            splitChannel = 1;
            c0 = 0;
            c1 = 2;
         } else {
            splitChannel = 2;
            c0 = 1;
            c1 = 0;
         }

         Cube ret = this.splitChannel(splitChannel, c0, c1);
         if (ret != null) {
            return ret;
         } else {
            ret = this.splitChannel(c0, splitChannel, c1);
            if (ret != null) {
               return ret;
            } else {
               ret = this.splitChannel(c1, splitChannel, c0);
               if (ret != null) {
                  return ret;
               } else {
                  this.done = true;
                  return null;
               }
            }
         }
      }

      private void normalize(int splitChannel, int[] counts) {
         if (this.count != 0) {
            int iMin = this.min[splitChannel];
            int iMax = this.max[splitChannel];
            int loBound = -1;
            int hiBound = -1;

            int i;
            for(i = iMin; i <= iMax; ++i) {
               if (counts[i] != 0) {
                  loBound = i;
                  break;
               }
            }

            for(i = iMax; i >= iMin; --i) {
               if (counts[i] != 0) {
                  hiBound = i;
                  break;
               }
            }

            boolean flagChangedLo = loBound != -1 && iMin != loBound;
            boolean flagChangedHi = hiBound != -1 && iMax != hiBound;
            if (flagChangedLo) {
               this.min[splitChannel] = loBound;
            }

            if (flagChangedHi) {
               this.max[splitChannel] = hiBound;
            }

         }
      }

      Cube splitChannel(int splitChannel, int c0, int c1) {
         if (this.min[splitChannel] == this.max[splitChannel]) {
            return null;
         } else if (this.count == 0) {
            return null;
         } else {
            int half = this.count / 2;
            int[] counts = this.computeCounts(splitChannel, c0, c1);
            int tcount = 0;
            int lastAdd = -1;
            int splitLo = this.min[splitChannel];
            int splitHi = this.max[splitChannel];

            for(int i = this.min[splitChannel]; i <= this.max[splitChannel]; ++i) {
               int c = counts[i];
               if (c == 0) {
                  if (tcount == 0 && i < this.max[splitChannel]) {
                     this.min[splitChannel] = i + 1;
                  }
               } else {
                  if (tcount + c >= half) {
                     if (half - tcount <= tcount + c - half) {
                        if (lastAdd == -1) {
                           if (c == this.count) {
                              this.max[splitChannel] = i;
                              return null;
                           }

                           splitLo = i;
                           splitHi = i + 1;
                           tcount += c;
                        } else {
                           splitLo = lastAdd;
                           splitHi = i;
                        }
                     } else if (i == this.max[splitChannel]) {
                        if (c == this.count) {
                           return null;
                        }

                        splitLo = lastAdd;
                        splitHi = i;
                     } else {
                        tcount += c;
                        splitLo = i;
                        splitHi = i + 1;
                     }
                     break;
                  }

                  lastAdd = i;
                  tcount += c;
               }
            }

            Cube ret = new Cube(this.colors, tcount);
            this.count -= tcount;
            ret.min[splitChannel] = this.min[splitChannel];
            ret.max[splitChannel] = splitLo;
            this.min[splitChannel] = splitHi;
            ret.min[c0] = this.min[c0];
            ret.max[c0] = this.max[c0];
            ret.min[c1] = this.min[c1];
            ret.max[c1] = this.max[c1];
            this.normalize(splitChannel, counts);
            ret.normalize(splitChannel, counts);
            return ret;
         }
      }

      private int[] computeCounts(int splitChannel, int c0, int c1) {
         int splitSh4 = (2 - splitChannel) * 4;
         int c0Sh4 = (2 - c0) * 4;
         int c1Sh4 = (2 - c1) * 4;
         int half = this.count / 2;
         int[] counts = new int[256];
         int tcount = 0;
         int minR = this.min[0];
         int minG = this.min[1];
         int minB = this.min[2];
         int maxR = this.max[0];
         int maxG = this.max[1];
         int maxB = this.max[2];
         int[] minIdx = new int[]{minR >> 4, minG >> 4, minB >> 4};
         int[] maxIdx = new int[]{maxR >> 4, maxG >> 4, maxB >> 4};
         int[] vals = new int[]{0, 0, 0};

         for(int i = minIdx[splitChannel]; i <= maxIdx[splitChannel]; ++i) {
            int idx1 = i << splitSh4;

            for(int j = minIdx[c0]; j <= maxIdx[c0]; ++j) {
               int idx2 = idx1 | j << c0Sh4;

               for(int k = minIdx[c1]; k <= maxIdx[c1]; ++k) {
                  int idx = idx2 | k << c1Sh4;
                  Counter[] v = this.colors[idx];
                  Counter[] var26 = v;
                  int var27 = v.length;

                  for(int var28 = 0; var28 < var27; ++var28) {
                     Counter c = var26[var28];
                     vals = c.getRgb(vals);
                     if (this.contains(vals)) {
                        counts[vals[splitChannel]] += c.count;
                        tcount += c.count;
                     }
                  }
               }
            }
         }

         return counts;
      }

      public String toString() {
         return "Cube: [" + this.min[0] + '-' + this.max[0] + "] [" + this.min[1] + '-' + this.max[1] + "] [" + this.min[2] + '-' + this.max[2] + "] n:" + this.count;
      }

      public int averageColor() {
         if (this.count == 0) {
            return 0;
         } else {
            byte[] rgb = this.averageColorRGB((byte[])null);
            return rgb[0] << 16 & 16711680 | rgb[1] << 8 & '\uff00' | rgb[2] & 255;
         }
      }

      public byte[] averageColorRGB(byte[] rgb) {
         if (this.count == 0) {
            return RGB_BLACK;
         } else {
            float red = 0.0F;
            float grn = 0.0F;
            float blu = 0.0F;
            int minR = this.min[0];
            int minG = this.min[1];
            int minB = this.min[2];
            int maxR = this.max[0];
            int maxG = this.max[1];
            int maxB = this.max[2];
            int[] minIdx = new int[]{minR >> 4, minG >> 4, minB >> 4};
            int[] maxIdx = new int[]{maxR >> 4, maxG >> 4, maxB >> 4};
            int[] vals = new int[3];

            for(int i = minIdx[0]; i <= maxIdx[0]; ++i) {
               int idx1 = i << 8;

               for(int j = minIdx[1]; j <= maxIdx[1]; ++j) {
                  int idx2 = idx1 | j << 4;

                  for(int k = minIdx[2]; k <= maxIdx[2]; ++k) {
                     int idx = idx2 | k;
                     Counter[] v = this.colors[idx];
                     Counter[] var21 = v;
                     int var22 = v.length;

                     for(int var23 = 0; var23 < var22; ++var23) {
                        Counter c = var21[var23];
                        vals = c.getRgb(vals);
                        if (this.contains(vals)) {
                           float weight = (float)c.count / (float)this.count;
                           red += (float)vals[0] * weight;
                           grn += (float)vals[1] * weight;
                           blu += (float)vals[2] * weight;
                        }
                     }
                  }
               }
            }

            byte[] result = rgb == null ? new byte[3] : rgb;
            result[0] = (byte)((int)(red + 0.5F));
            result[1] = (byte)((int)(grn + 0.5F));
            result[2] = (byte)((int)(blu + 0.5F));
            return result;
         }
      }
   }

   private static class Counter {
      final int val;
      int count = 1;

      Counter(int val) {
         this.val = val;
      }

      boolean add(int val) {
         if (this.val != val) {
            return false;
         } else {
            ++this.count;
            return true;
         }
      }

      int[] getRgb(int[] rgb) {
         rgb[0] = (this.val & 16711680) >> 16;
         rgb[1] = (this.val & '\uff00') >> 8;
         rgb[2] = this.val & 255;
         return rgb;
      }
   }
}
