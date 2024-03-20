package org.apache.batik.ext.awt.image.rendered;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiplyAlphaRed extends AbstractRed {
   public MultiplyAlphaRed(CachableRed src, CachableRed alpha) {
      super((List)makeList(src, alpha), makeBounds(src, alpha), fixColorModel(src), fixSampleModel(src), src.getTileGridXOffset(), src.getTileGridYOffset(), (Map)null);
   }

   public boolean is_INT_PACK_BYTE_COMP(SampleModel srcSM, SampleModel alpSM) {
      if (!(srcSM instanceof SinglePixelPackedSampleModel)) {
         return false;
      } else if (!(alpSM instanceof ComponentSampleModel)) {
         return false;
      } else if (srcSM.getDataType() != 3) {
         return false;
      } else if (alpSM.getDataType() != 0) {
         return false;
      } else {
         SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)srcSM;
         int[] masks = sppsm.getBitMasks();
         if (masks.length != 4) {
            return false;
         } else if (masks[0] != 16711680) {
            return false;
         } else if (masks[1] != 65280) {
            return false;
         } else if (masks[2] != 255) {
            return false;
         } else if (masks[3] != -16777216) {
            return false;
         } else {
            ComponentSampleModel csm = (ComponentSampleModel)alpSM;
            if (csm.getNumBands() != 1) {
               return false;
            } else {
               return csm.getPixelStride() == 1;
            }
         }
      }
   }

   public WritableRaster INT_PACK_BYTE_COMP_Impl(WritableRaster wr) {
      CachableRed srcRed = (CachableRed)this.getSources().get(0);
      CachableRed alphaRed = (CachableRed)this.getSources().get(1);
      srcRed.copyData(wr);
      Rectangle rgn = wr.getBounds();
      rgn = rgn.intersection(alphaRed.getBounds());
      Raster r = alphaRed.getData(rgn);
      ComponentSampleModel csm = (ComponentSampleModel)r.getSampleModel();
      int alpScanStride = csm.getScanlineStride();
      DataBufferByte alpDB = (DataBufferByte)r.getDataBuffer();
      int alpBase = alpDB.getOffset() + csm.getOffset(rgn.x - r.getSampleModelTranslateX(), rgn.y - r.getSampleModelTranslateY());
      byte[] alpPixels = alpDB.getBankData()[0];
      SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();
      int srcScanStride = sppsm.getScanlineStride();
      DataBufferInt srcDB = (DataBufferInt)wr.getDataBuffer();
      int srcBase = srcDB.getOffset() + sppsm.getOffset(rgn.x - wr.getSampleModelTranslateX(), rgn.y - wr.getSampleModelTranslateY());
      int[] srcPixels = srcDB.getBankData()[0];
      ColorModel cm = srcRed.getColorModel();
      int y;
      int sp;
      int ap;
      int end;
      int a;
      int pix;
      if (cm.isAlphaPremultiplied()) {
         for(y = 0; y < rgn.height; ++y) {
            sp = srcBase + y * srcScanStride;
            ap = alpBase + y * alpScanStride;

            for(end = sp + rgn.width; sp < end; ++sp) {
               a = alpPixels[ap++] & 255;
               pix = srcPixels[sp];
               srcPixels[sp] = ((pix >>> 24) * a & '\uff00') << 16 | ((pix >>> 16 & 255) * a & '\uff00') << 8 | (pix >>> 8 & 255) * a & '\uff00' | ((pix & 255) * a & '\uff00') >> 8;
            }
         }
      } else {
         for(y = 0; y < rgn.height; ++y) {
            sp = srcBase + y * srcScanStride;
            ap = alpBase + y * alpScanStride;

            for(end = sp + rgn.width; sp < end; ++sp) {
               a = alpPixels[ap++] & 255;
               pix = srcPixels[sp] >>> 24;
               srcPixels[sp] = (pix * a & '\uff00') << 16 | srcPixels[sp] & 16777215;
            }
         }
      }

      return wr;
   }

   public WritableRaster copyData(WritableRaster wr) {
      CachableRed srcRed = (CachableRed)this.getSources().get(0);
      CachableRed alphaRed = (CachableRed)this.getSources().get(1);
      if (this.is_INT_PACK_BYTE_COMP(srcRed.getSampleModel(), alphaRed.getSampleModel())) {
         return this.INT_PACK_BYTE_COMP_Impl(wr);
      } else {
         ColorModel cm = srcRed.getColorModel();
         if (cm.hasAlpha()) {
            srcRed.copyData(wr);
            Rectangle rgn = wr.getBounds();
            if (!rgn.intersects(alphaRed.getBounds())) {
               return wr;
            } else {
               rgn = rgn.intersection(alphaRed.getBounds());
               int[] wrData = null;
               int[] alphaData = null;
               Raster r = alphaRed.getData(rgn);
               int w = rgn.width;
               int bands = wr.getSampleModel().getNumBands();
               int y;
               int i;
               int a;
               if (cm.isAlphaPremultiplied()) {
                  for(y = rgn.y; y < rgn.y + rgn.height; ++y) {
                     label94: {
                        wrData = wr.getPixels(rgn.x, y, w, 1, wrData);
                        alphaData = r.getSamples(rgn.x, y, w, 1, 0, alphaData);
                        i = 0;
                        int[] var15;
                        int var16;
                        int var17;
                        int anAlphaData1;
                        switch (bands) {
                           case 2:
                              var15 = alphaData;
                              var16 = alphaData.length;
                              var17 = 0;

                              while(true) {
                                 if (var17 >= var16) {
                                    break label94;
                                 }

                                 anAlphaData1 = var15[var17];
                                 a = anAlphaData1 & 255;
                                 wrData[i] = (wrData[i] & 255) * a >> 8;
                                 ++i;
                                 wrData[i] = (wrData[i] & 255) * a >> 8;
                                 ++i;
                                 ++var17;
                              }
                           case 4:
                              var15 = alphaData;
                              var16 = alphaData.length;
                              var17 = 0;

                              while(true) {
                                 if (var17 >= var16) {
                                    break label94;
                                 }

                                 anAlphaData1 = var15[var17];
                                 a = anAlphaData1 & 255;
                                 wrData[i] = (wrData[i] & 255) * a >> 8;
                                 ++i;
                                 wrData[i] = (wrData[i] & 255) * a >> 8;
                                 ++i;
                                 wrData[i] = (wrData[i] & 255) * a >> 8;
                                 ++i;
                                 wrData[i] = (wrData[i] & 255) * a >> 8;
                                 ++i;
                                 ++var17;
                              }
                           default:
                              var15 = alphaData;
                              var16 = alphaData.length;
                              var17 = 0;
                        }

                        while(var17 < var16) {
                           anAlphaData1 = var15[var17];
                           a = anAlphaData1 & 255;

                           for(int b = 0; b < bands; ++b) {
                              wrData[i] = (wrData[i] & 255) * a >> 8;
                              ++i;
                           }

                           ++var17;
                        }
                     }

                     wr.setPixels(rgn.x, y, w, 1, wrData);
                  }
               } else {
                  y = srcRed.getSampleModel().getNumBands() - 1;

                  for(i = rgn.y; i < rgn.y + rgn.height; ++i) {
                     wrData = wr.getSamples(rgn.x, i, w, 1, y, wrData);
                     alphaData = r.getSamples(rgn.x, i, w, 1, 0, alphaData);

                     for(a = 0; a < wrData.length; ++a) {
                        wrData[a] = (wrData[a] & 255) * (alphaData[a] & 255) >> 8;
                     }

                     wr.setSamples(rgn.x, i, w, 1, y, wrData);
                  }
               }

               return wr;
            }
         } else {
            int[] bands = new int[wr.getNumBands() - 1];

            for(int i = 0; i < bands.length; bands[i] = i++) {
            }

            WritableRaster subWr = wr.createWritableChild(wr.getMinX(), wr.getMinY(), wr.getWidth(), wr.getHeight(), wr.getMinX(), wr.getMinY(), bands);
            srcRed.copyData(subWr);
            Rectangle rgn = wr.getBounds();
            rgn = rgn.intersection(alphaRed.getBounds());
            bands = new int[]{wr.getNumBands() - 1};
            subWr = wr.createWritableChild(rgn.x, rgn.y, rgn.width, rgn.height, rgn.x, rgn.y, bands);
            alphaRed.copyData(subWr);
            return wr;
         }
      }
   }

   public static List makeList(CachableRed src1, CachableRed src2) {
      List ret = new ArrayList(2);
      ret.add(src1);
      ret.add(src2);
      return ret;
   }

   public static Rectangle makeBounds(CachableRed src1, CachableRed src2) {
      Rectangle r1 = src1.getBounds();
      Rectangle r2 = src2.getBounds();
      return r1.intersection(r2);
   }

   public static SampleModel fixSampleModel(CachableRed src) {
      ColorModel cm = src.getColorModel();
      SampleModel srcSM = src.getSampleModel();
      if (cm.hasAlpha()) {
         return srcSM;
      } else {
         int w = srcSM.getWidth();
         int h = srcSM.getHeight();
         int b = srcSM.getNumBands() + 1;
         int[] offsets = new int[b];

         for(int i = 0; i < b; offsets[i] = i++) {
         }

         return new PixelInterleavedSampleModel(0, w, h, b, w * b, offsets);
      }
   }

   public static ColorModel fixColorModel(CachableRed src) {
      ColorModel cm = src.getColorModel();
      if (cm.hasAlpha()) {
         return cm;
      } else {
         int b = src.getSampleModel().getNumBands() + 1;
         int[] bits = new int[b];

         for(int i = 0; i < b; ++i) {
            bits[i] = 8;
         }

         ColorSpace cs = cm.getColorSpace();
         return new ComponentColorModel(cs, bits, true, false, 3, 0);
      }
   }
}
