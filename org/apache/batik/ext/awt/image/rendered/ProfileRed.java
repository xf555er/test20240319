package org.apache.batik.ext.awt.image.rendered;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.Map;
import org.apache.xmlgraphics.java2d.color.ICCColorSpaceWithIntent;

public class ProfileRed extends AbstractRed {
   private static final ColorSpace sRGBCS = ColorSpace.getInstance(1000);
   private static final ColorModel sRGBCM;
   private ICCColorSpaceWithIntent colorSpace;

   public ProfileRed(CachableRed src, ICCColorSpaceWithIntent colorSpace) {
      this.colorSpace = colorSpace;
      this.init(src, src.getBounds(), sRGBCM, sRGBCM.createCompatibleSampleModel(src.getWidth(), src.getHeight()), src.getTileGridXOffset(), src.getTileGridYOffset(), (Map)null);
   }

   public CachableRed getSource() {
      return (CachableRed)this.getSources().get(0);
   }

   public WritableRaster copyData(WritableRaster argbWR) {
      try {
         RenderedImage img = this.getSource();
         ColorModel imgCM = img.getColorModel();
         ColorSpace imgCS = ((ColorModel)imgCM).getColorSpace();
         int nImageComponents = imgCS.getNumComponents();
         int nProfileComponents = this.colorSpace.getNumComponents();
         if (nImageComponents != nProfileComponents) {
            System.err.println("Input image and associated color profile have mismatching number of color components: conversion is not possible");
            return argbWR;
         } else {
            int w = argbWR.getWidth();
            int h = argbWR.getHeight();
            int minX = argbWR.getMinX();
            int minY = argbWR.getMinY();
            WritableRaster srcWR = ((ColorModel)imgCM).createCompatibleWritableRaster(w, h);
            srcWR = srcWR.createWritableTranslatedChild(minX, minY);
            img.copyData(srcWR);
            ComponentColorModel newCM;
            BufferedImage newImg;
            if (!(imgCM instanceof ComponentColorModel) || !(img.getSampleModel() instanceof BandedSampleModel) || ((ColorModel)imgCM).hasAlpha() && ((ColorModel)imgCM).isAlphaPremultiplied()) {
               newCM = new ComponentColorModel(imgCS, ((ColorModel)imgCM).getComponentSize(), ((ColorModel)imgCM).hasAlpha(), false, ((ColorModel)imgCM).getTransparency(), 0);
               WritableRaster wr = Raster.createBandedRaster(0, argbWR.getWidth(), argbWR.getHeight(), newCM.getNumComponents(), new Point(0, 0));
               newImg = new BufferedImage(newCM, wr, newCM.isAlphaPremultiplied(), (Hashtable)null);
               BufferedImage srcImg = new BufferedImage((ColorModel)imgCM, srcWR.createWritableTranslatedChild(0, 0), ((ColorModel)imgCM).isAlphaPremultiplied(), (Hashtable)null);
               Graphics2D g = newImg.createGraphics();
               g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
               g.drawImage(srcImg, 0, 0, (ImageObserver)null);
               imgCM = newCM;
               srcWR = wr.createWritableTranslatedChild(minX, minY);
            }

            newCM = new ComponentColorModel(this.colorSpace, ((ColorModel)imgCM).getComponentSize(), false, false, 1, 0);
            DataBufferByte data = (DataBufferByte)srcWR.getDataBuffer();
            srcWR = Raster.createBandedRaster(data, argbWR.getWidth(), argbWR.getHeight(), argbWR.getWidth(), new int[]{0, 1, 2}, new int[]{0, 0, 0}, new Point(0, 0));
            newImg = new BufferedImage(newCM, srcWR, newCM.isAlphaPremultiplied(), (Hashtable)null);
            ComponentColorModel sRGBCompCM = new ComponentColorModel(ColorSpace.getInstance(1000), new int[]{8, 8, 8}, false, false, 1, 0);
            WritableRaster wr = Raster.createBandedRaster(0, argbWR.getWidth(), argbWR.getHeight(), sRGBCompCM.getNumComponents(), new Point(0, 0));
            BufferedImage sRGBImage = new BufferedImage(sRGBCompCM, wr, false, (Hashtable)null);
            ColorConvertOp colorConvertOp = new ColorConvertOp((RenderingHints)null);
            colorConvertOp.filter(newImg, sRGBImage);
            if (((ColorModel)imgCM).hasAlpha()) {
               DataBufferByte rgbData = (DataBufferByte)wr.getDataBuffer();
               byte[][] imgBanks = data.getBankData();
               byte[][] rgbBanks = rgbData.getBankData();
               byte[][] argbBanks = new byte[][]{rgbBanks[0], rgbBanks[1], rgbBanks[2], imgBanks[3]};
               DataBufferByte argbData = new DataBufferByte(argbBanks, imgBanks[0].length);
               srcWR = Raster.createBandedRaster(argbData, argbWR.getWidth(), argbWR.getHeight(), argbWR.getWidth(), new int[]{0, 1, 2, 3}, new int[]{0, 0, 0, 0}, new Point(0, 0));
               sRGBCompCM = new ComponentColorModel(ColorSpace.getInstance(1000), new int[]{8, 8, 8, 8}, true, false, 3, 0);
               sRGBImage = new BufferedImage(sRGBCompCM, srcWR, false, (Hashtable)null);
            }

            BufferedImage result = new BufferedImage(sRGBCM, argbWR.createWritableTranslatedChild(0, 0), false, (Hashtable)null);
            Graphics2D g = result.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g.drawImage(sRGBImage, 0, 0, (ImageObserver)null);
            g.dispose();
            return argbWR;
         }
      } catch (Exception var24) {
         var24.printStackTrace();
         throw new RuntimeException(var24.getMessage());
      }
   }

   static {
      sRGBCM = new DirectColorModel(sRGBCS, 32, 16711680, 65280, 255, -16777216, false, 3);
   }
}
