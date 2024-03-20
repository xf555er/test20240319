package org.apache.batik.ext.awt.image.rendered;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.Map;
import org.apache.batik.ext.awt.ColorSpaceHintKey;
import org.apache.batik.ext.awt.image.GraphicsUtil;

public class Any2LumRed extends AbstractRed {
   boolean isColorConvertOpAplhaSupported = getColorConvertOpAplhaSupported();

   public Any2LumRed(CachableRed src) {
      super((CachableRed)src, src.getBounds(), fixColorModel(src), fixSampleModel(src), src.getTileGridXOffset(), src.getTileGridYOffset(), (Map)null);
      this.props.put("org.apache.batik.gvt.filter.Colorspace", ColorSpaceHintKey.VALUE_COLORSPACE_GREY);
   }

   public WritableRaster copyData(WritableRaster wr) {
      CachableRed src = (CachableRed)this.getSources().get(0);
      SampleModel sm = src.getSampleModel();
      ColorModel srcCM = src.getColorModel();
      Raster srcRas = src.getData(wr.getBounds());
      if (srcCM == null) {
         float[][] matrix = (float[][])null;
         if (sm.getNumBands() == 2) {
            matrix = new float[2][2];
            matrix[0][0] = 1.0F;
            matrix[1][1] = 1.0F;
         } else {
            matrix = new float[sm.getNumBands()][1];
            matrix[0][0] = 1.0F;
         }

         BandCombineOp op = new BandCombineOp(matrix, (RenderingHints)null);
         op.filter(srcRas, wr);
      } else {
         WritableRaster srcWr = (WritableRaster)srcRas;
         if (srcCM.hasAlpha()) {
            GraphicsUtil.coerceData(srcWr, srcCM, false);
         }

         BufferedImage srcBI = new BufferedImage(srcCM, srcWr.createWritableTranslatedChild(0, 0), false, (Hashtable)null);
         ColorModel dstCM = this.getColorModel();
         BufferedImage dstBI;
         if (dstCM.hasAlpha() && !this.isColorConvertOpAplhaSupported) {
            PixelInterleavedSampleModel dstSM = (PixelInterleavedSampleModel)wr.getSampleModel();
            SampleModel smna = new PixelInterleavedSampleModel(dstSM.getDataType(), dstSM.getWidth(), dstSM.getHeight(), dstSM.getPixelStride(), dstSM.getScanlineStride(), new int[]{0});
            WritableRaster dstWr = Raster.createWritableRaster(smna, wr.getDataBuffer(), new Point(0, 0));
            dstWr = dstWr.createWritableChild(wr.getMinX() - wr.getSampleModelTranslateX(), wr.getMinY() - wr.getSampleModelTranslateY(), wr.getWidth(), wr.getHeight(), 0, 0, (int[])null);
            ColorModel cmna = new ComponentColorModel(ColorSpace.getInstance(1003), new int[]{8}, false, false, 1, 0);
            dstBI = new BufferedImage(cmna, dstWr, false, (Hashtable)null);
         } else {
            dstBI = new BufferedImage(dstCM, wr.createWritableTranslatedChild(0, 0), dstCM.isAlphaPremultiplied(), (Hashtable)null);
         }

         ColorConvertOp op = new ColorConvertOp((RenderingHints)null);
         op.filter(srcBI, dstBI);
         if (dstCM.hasAlpha()) {
            copyBand(srcWr, sm.getNumBands() - 1, wr, this.getSampleModel().getNumBands() - 1);
            if (dstCM.isAlphaPremultiplied()) {
               GraphicsUtil.multiplyAlpha(wr);
            }
         }
      }

      return wr;
   }

   protected static ColorModel fixColorModel(CachableRed src) {
      ColorModel cm = src.getColorModel();
      if (cm != null) {
         return cm.hasAlpha() ? new ComponentColorModel(ColorSpace.getInstance(1003), new int[]{8, 8}, true, cm.isAlphaPremultiplied(), 3, 0) : new ComponentColorModel(ColorSpace.getInstance(1003), new int[]{8}, false, false, 1, 0);
      } else {
         SampleModel sm = src.getSampleModel();
         return sm.getNumBands() == 2 ? new ComponentColorModel(ColorSpace.getInstance(1003), new int[]{8, 8}, true, true, 3, 0) : new ComponentColorModel(ColorSpace.getInstance(1003), new int[]{8}, false, false, 1, 0);
      }
   }

   protected static SampleModel fixSampleModel(CachableRed src) {
      SampleModel sm = src.getSampleModel();
      int width = sm.getWidth();
      int height = sm.getHeight();
      ColorModel cm = src.getColorModel();
      if (cm != null) {
         return cm.hasAlpha() ? new PixelInterleavedSampleModel(0, width, height, 2, 2 * width, new int[]{0, 1}) : new PixelInterleavedSampleModel(0, width, height, 1, width, new int[]{0});
      } else {
         return sm.getNumBands() == 2 ? new PixelInterleavedSampleModel(0, width, height, 2, 2 * width, new int[]{0, 1}) : new PixelInterleavedSampleModel(0, width, height, 1, width, new int[]{0});
      }
   }

   protected static boolean getColorConvertOpAplhaSupported() {
      int size = 50;
      BufferedImage srcImage = new BufferedImage(size, size, 2);
      Graphics2D srcGraphics = srcImage.createGraphics();
      srcGraphics.setColor(Color.red);
      srcGraphics.fillRect(0, 0, size, size);
      srcGraphics.dispose();
      BufferedImage dstImage = new BufferedImage(size, size, 2);
      Graphics2D dstGraphics = dstImage.createGraphics();
      dstGraphics.setComposite(AlphaComposite.Clear);
      dstGraphics.fillRect(0, 0, size, size);
      dstGraphics.dispose();
      ColorSpace grayColorSpace = ColorSpace.getInstance(1003);
      ColorConvertOp op = new ColorConvertOp(grayColorSpace, (RenderingHints)null);
      op.filter(srcImage, dstImage);
      return getAlpha(srcImage) == getAlpha(dstImage);
   }

   protected static int getAlpha(BufferedImage bufferedImage) {
      int x = bufferedImage.getWidth() / 2;
      int y = bufferedImage.getHeight() / 2;
      return 255 & bufferedImage.getRGB(x, y) >> 24;
   }
}
