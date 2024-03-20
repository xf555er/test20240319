package org.apache.batik.ext.awt.image.rendered;

import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Map;
import org.apache.batik.ext.awt.ColorSpaceHintKey;

public class FilterAsAlphaRed extends AbstractRed {
   public FilterAsAlphaRed(CachableRed src) {
      super((CachableRed)(new Any2LumRed(src)), src.getBounds(), new ComponentColorModel(ColorSpace.getInstance(1003), new int[]{8}, false, false, 1, 0), new PixelInterleavedSampleModel(0, src.getSampleModel().getWidth(), src.getSampleModel().getHeight(), 1, src.getSampleModel().getWidth(), new int[]{0}), src.getTileGridXOffset(), src.getTileGridYOffset(), (Map)null);
      this.props.put("org.apache.batik.gvt.filter.Colorspace", ColorSpaceHintKey.VALUE_COLORSPACE_ALPHA);
   }

   public WritableRaster copyData(WritableRaster wr) {
      CachableRed srcRed = (CachableRed)this.getSources().get(0);
      SampleModel sm = srcRed.getSampleModel();
      if (sm.getNumBands() == 1) {
         return srcRed.copyData(wr);
      } else {
         Raster srcRas = srcRed.getData(wr.getBounds());
         PixelInterleavedSampleModel srcSM = (PixelInterleavedSampleModel)srcRas.getSampleModel();
         DataBufferByte srcDB = (DataBufferByte)srcRas.getDataBuffer();
         byte[] src = srcDB.getData();
         PixelInterleavedSampleModel dstSM = (PixelInterleavedSampleModel)wr.getSampleModel();
         DataBufferByte dstDB = (DataBufferByte)wr.getDataBuffer();
         byte[] dst = dstDB.getData();
         int srcX0 = srcRas.getMinX() - srcRas.getSampleModelTranslateX();
         int srcY0 = srcRas.getMinY() - srcRas.getSampleModelTranslateY();
         int dstX0 = wr.getMinX() - wr.getSampleModelTranslateX();
         int dstX1 = dstX0 + wr.getWidth() - 1;
         int dstY0 = wr.getMinY() - wr.getSampleModelTranslateY();
         int srcStep = srcSM.getPixelStride();
         int[] offsets = srcSM.getBandOffsets();
         int srcLOff = offsets[0];
         int srcAOff = offsets[1];
         int y;
         int srcI;
         int dstI;
         int dstE;
         if (srcRed.getColorModel().isAlphaPremultiplied()) {
            for(y = 0; y < srcRas.getHeight(); ++y) {
               srcI = srcDB.getOffset() + srcSM.getOffset(srcX0, srcY0);
               dstI = dstDB.getOffset() + dstSM.getOffset(dstX0, dstY0);
               dstE = dstDB.getOffset() + dstSM.getOffset(dstX1 + 1, dstY0);

               for(srcI += srcLOff; dstI < dstE; srcI += srcStep) {
                  dst[dstI++] = src[srcI];
               }

               ++srcY0;
               ++dstY0;
            }
         } else {
            srcAOff -= srcLOff;

            for(y = 0; y < srcRas.getHeight(); ++y) {
               srcI = srcDB.getOffset() + srcSM.getOffset(srcX0, srcY0);
               dstI = dstDB.getOffset() + dstSM.getOffset(dstX0, dstY0);
               dstE = dstDB.getOffset() + dstSM.getOffset(dstX1 + 1, dstY0);

               for(srcI += srcLOff; dstI < dstE; srcI += srcStep) {
                  int sl = src[srcI] & 255;
                  int sa = src[srcI + srcAOff] & 255;
                  dst[dstI++] = (byte)(sl * sa + 128 >> 8);
               }

               ++srcY0;
               ++dstY0;
            }
         }

         return wr;
      }
   }
}
