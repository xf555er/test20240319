package org.apache.batik.ext.awt.image;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.ext.awt.image.renderable.PaintRable;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.Any2LsRGBRed;
import org.apache.batik.ext.awt.image.rendered.Any2sRGBRed;
import org.apache.batik.ext.awt.image.rendered.BufferedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.FormatRed;
import org.apache.batik.ext.awt.image.rendered.RenderedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.TranslateRed;

public class GraphicsUtil {
   public static AffineTransform IDENTITY = new AffineTransform();
   public static final boolean WARN_DESTINATION;
   public static final ColorModel Linear_sRGB;
   public static final ColorModel Linear_sRGB_Pre;
   public static final ColorModel Linear_sRGB_Unpre;
   public static final ColorModel sRGB;
   public static final ColorModel sRGB_Pre;
   public static final ColorModel sRGB_Unpre;

   public static void drawImage(Graphics2D g2d, RenderedImage ri) {
      drawImage(g2d, wrap(ri));
   }

   public static void drawImage(Graphics2D g2d, CachableRed cr) {
      AffineTransform at = null;

      while(true) {
         TranslateRed tr;
         for(; !(cr instanceof AffineRed); cr = tr.getSource()) {
            if (!(cr instanceof TranslateRed)) {
               AffineTransform g2dAt = g2d.getTransform();
               if (at != null && !at.isIdentity()) {
                  at.preConcatenate(g2dAt);
               } else {
                  at = g2dAt;
               }

               ColorModel srcCM = ((CachableRed)cr).getColorModel();
               ColorModel g2dCM = getDestinationColorModel(g2d);
               ColorSpace g2dCS = null;
               if (g2dCM != null) {
                  g2dCS = g2dCM.getColorSpace();
               }

               if (g2dCS == null) {
                  g2dCS = ColorSpace.getInstance(1000);
               }

               ColorModel drawCM = g2dCM;
               if (g2dCM == null || !g2dCM.hasAlpha()) {
                  drawCM = sRGB_Unpre;
               }

               if (cr instanceof BufferedImageCachableRed && g2dCS.equals(srcCM.getColorSpace()) && drawCM.equals(srcCM)) {
                  g2d.setTransform(at);
                  BufferedImageCachableRed bicr = (BufferedImageCachableRed)cr;
                  g2d.drawImage(bicr.getBufferedImage(), bicr.getMinX(), bicr.getMinY(), (ImageObserver)null);
                  g2d.setTransform(g2dAt);
                  return;
               }

               double determinant = at.getDeterminant();
               if (!at.isIdentity() && determinant <= 1.0) {
                  if (at.getType() != 1) {
                     cr = new AffineRed((CachableRed)cr, at, g2d.getRenderingHints());
                  } else {
                     int xloc = ((CachableRed)cr).getMinX() + (int)at.getTranslateX();
                     int yloc = ((CachableRed)cr).getMinY() + (int)at.getTranslateY();
                     cr = new TranslateRed((CachableRed)cr, xloc, yloc);
                  }
               }

               if (g2dCS != srcCM.getColorSpace()) {
                  if (g2dCS == ColorSpace.getInstance(1000)) {
                     cr = convertTosRGB((CachableRed)cr);
                  } else if (g2dCS == ColorSpace.getInstance(1004)) {
                     cr = convertToLsRGB((CachableRed)cr);
                  }
               }

               srcCM = ((CachableRed)cr).getColorModel();
               if (!drawCM.equals(srcCM)) {
                  cr = FormatRed.construct((CachableRed)cr, drawCM);
               }

               if (!at.isIdentity() && determinant > 1.0) {
                  cr = new AffineRed((CachableRed)cr, at, g2d.getRenderingHints());
               }

               g2d.setTransform(IDENTITY);
               Composite g2dComposite = g2d.getComposite();
               if (g2d.getRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING) == "Printing" && SVGComposite.OVER.equals(g2dComposite)) {
                  g2d.setComposite(SVGComposite.OVER);
               }

               Rectangle crR = ((CachableRed)cr).getBounds();
               Shape clip = g2d.getClip();

               try {
                  Rectangle clipR;
                  if (clip == null) {
                     clipR = crR;
                  } else {
                     clipR = clip.getBounds();
                     if (!clipR.intersects(crR)) {
                        return;
                     }

                     clipR = clipR.intersection(crR);
                  }

                  Rectangle gcR = getDestinationBounds(g2d);
                  if (gcR != null) {
                     if (!clipR.intersects(gcR)) {
                        return;
                     }

                     clipR = clipR.intersection(gcR);
                  }

                  boolean useDrawRenderedImage = false;
                  srcCM = ((CachableRed)cr).getColorModel();
                  SampleModel srcSM = ((CachableRed)cr).getSampleModel();
                  if (srcSM.getWidth() * srcSM.getHeight() >= clipR.width * clipR.height) {
                     useDrawRenderedImage = true;
                  }

                  Object atpHint = g2d.getRenderingHint(RenderingHintsKeyExt.KEY_AVOID_TILE_PAINTING);
                  if (atpHint == RenderingHintsKeyExt.VALUE_AVOID_TILE_PAINTING_ON) {
                     useDrawRenderedImage = true;
                  }

                  if (atpHint == RenderingHintsKeyExt.VALUE_AVOID_TILE_PAINTING_OFF) {
                     useDrawRenderedImage = false;
                  }

                  WritableRaster wr;
                  if (useDrawRenderedImage) {
                     Raster r = ((CachableRed)cr).getData(clipR);
                     wr = ((WritableRaster)r).createWritableChild(clipR.x, clipR.y, clipR.width, clipR.height, 0, 0, (int[])null);
                     BufferedImage bi = new BufferedImage(srcCM, wr, srcCM.isAlphaPremultiplied(), (Hashtable)null);
                     g2d.drawImage(bi, clipR.x, clipR.y, (ImageObserver)null);
                     return;
                  } else {
                     wr = Raster.createWritableRaster(srcSM, new Point(0, 0));
                     BufferedImage bi = new BufferedImage(srcCM, wr, srcCM.isAlphaPremultiplied(), (Hashtable)null);
                     int xt0 = ((CachableRed)cr).getMinTileX();
                     int xt1 = xt0 + ((CachableRed)cr).getNumXTiles();
                     int yt0 = ((CachableRed)cr).getMinTileY();
                     int yt1 = yt0 + ((CachableRed)cr).getNumYTiles();
                     int tw = srcSM.getWidth();
                     int th = srcSM.getHeight();
                     Rectangle tR = new Rectangle(0, 0, tw, th);
                     Rectangle iR = new Rectangle(0, 0, 0, 0);
                     int yloc = yt0 * th + ((CachableRed)cr).getTileGridYOffset();
                     int skip = (clipR.y - yloc) / th;
                     if (skip < 0) {
                        skip = 0;
                     }

                     yt0 += skip;
                     int xloc = xt0 * tw + ((CachableRed)cr).getTileGridXOffset();
                     skip = (clipR.x - xloc) / tw;
                     if (skip < 0) {
                        skip = 0;
                     }

                     xt0 += skip;
                     int endX = clipR.x + clipR.width - 1;
                     int endY = clipR.y + clipR.height - 1;
                     yloc = yt0 * th + ((CachableRed)cr).getTileGridYOffset();
                     int minX = xt0 * tw + ((CachableRed)cr).getTileGridXOffset();
                     int xStep = tw;
                     xloc = minX;

                     for(int y = yt0; y < yt1 && yloc <= endY; yloc += th) {
                        for(int x = xt0; x < xt1 && xloc >= minX && xloc <= endX; xloc += xStep) {
                           tR.x = xloc;
                           tR.y = yloc;
                           Rectangle2D.intersect(crR, tR, iR);
                           WritableRaster twr = wr.createWritableChild(0, 0, iR.width, iR.height, iR.x, iR.y, (int[])null);
                           ((CachableRed)cr).copyData(twr);
                           BufferedImage subBI = bi.getSubimage(0, 0, iR.width, iR.height);
                           g2d.drawImage(subBI, iR.x, iR.y, (ImageObserver)null);
                           ++x;
                        }

                        xStep = -xStep;
                        xloc += xStep;
                        ++y;
                     }

                     return;
                  }
               } finally {
                  g2d.setTransform(g2dAt);
                  g2d.setComposite(g2dComposite);
               }
            }

            tr = (TranslateRed)cr;
            int dx = tr.getDeltaX();
            int dy = tr.getDeltaY();
            if (at == null) {
               at = AffineTransform.getTranslateInstance((double)dx, (double)dy);
            } else {
               at.translate((double)dx, (double)dy);
            }
         }

         AffineRed ar = (AffineRed)cr;
         if (at == null) {
            at = ar.getTransform();
         } else {
            at.concatenate(ar.getTransform());
         }

         cr = ar.getSource();
      }
   }

   public static void drawImage(Graphics2D g2d, RenderableImage filter, RenderContext rc) {
      AffineTransform origDev = g2d.getTransform();
      Shape origClip = g2d.getClip();
      RenderingHints origRH = g2d.getRenderingHints();
      Shape clip = rc.getAreaOfInterest();
      if (clip != null) {
         g2d.clip(clip);
      }

      g2d.transform(rc.getTransform());
      g2d.setRenderingHints(rc.getRenderingHints());
      drawImage(g2d, filter);
      g2d.setTransform(origDev);
      g2d.setClip(origClip);
      g2d.setRenderingHints(origRH);
   }

   public static void drawImage(Graphics2D g2d, RenderableImage filter) {
      if (filter instanceof PaintRable) {
         PaintRable pr = (PaintRable)filter;
         if (pr.paintRable(g2d)) {
            return;
         }
      }

      AffineTransform at = g2d.getTransform();
      RenderedImage ri = filter.createRendering(new RenderContext(at, g2d.getClip(), g2d.getRenderingHints()));
      if (ri != null) {
         g2d.setTransform(IDENTITY);
         drawImage(g2d, wrap(ri));
         g2d.setTransform(at);
      }
   }

   public static Graphics2D createGraphics(BufferedImage bi, RenderingHints hints) {
      Graphics2D g2d = bi.createGraphics();
      if (hints != null) {
         g2d.addRenderingHints(hints);
      }

      g2d.setRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE, new WeakReference(bi));
      g2d.clip(new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
      return g2d;
   }

   public static Graphics2D createGraphics(BufferedImage bi) {
      Graphics2D g2d = bi.createGraphics();
      g2d.setRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE, new WeakReference(bi));
      g2d.clip(new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
      return g2d;
   }

   public static BufferedImage getDestination(Graphics2D g2d) {
      Object o = g2d.getRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE);
      if (o != null) {
         return (BufferedImage)((BufferedImage)((Reference)o).get());
      } else {
         GraphicsConfiguration gc = g2d.getDeviceConfiguration();
         if (gc == null) {
            return null;
         } else {
            GraphicsDevice gd = gc.getDevice();
            if (WARN_DESTINATION && gd.getType() == 2 && g2d.getRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING) != "Printing") {
               System.err.println("Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint");
            }

            return null;
         }
      }
   }

   public static ColorModel getDestinationColorModel(Graphics2D g2d) {
      BufferedImage bi = getDestination(g2d);
      if (bi != null) {
         return bi.getColorModel();
      } else {
         GraphicsConfiguration gc = g2d.getDeviceConfiguration();
         if (gc == null) {
            return null;
         } else if (gc.getDevice().getType() == 2) {
            return g2d.getRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING) == "Printing" ? sRGB_Unpre : null;
         } else {
            return gc.getColorModel();
         }
      }
   }

   public static ColorSpace getDestinationColorSpace(Graphics2D g2d) {
      ColorModel cm = getDestinationColorModel(g2d);
      return cm != null ? cm.getColorSpace() : null;
   }

   public static Rectangle getDestinationBounds(Graphics2D g2d) {
      BufferedImage bi = getDestination(g2d);
      if (bi != null) {
         return new Rectangle(0, 0, bi.getWidth(), bi.getHeight());
      } else {
         GraphicsConfiguration gc = g2d.getDeviceConfiguration();
         if (gc == null) {
            return null;
         } else {
            return gc.getDevice().getType() == 2 ? null : null;
         }
      }
   }

   public static ColorModel makeLinear_sRGBCM(boolean premult) {
      return premult ? Linear_sRGB_Pre : Linear_sRGB_Unpre;
   }

   public static BufferedImage makeLinearBufferedImage(int width, int height, boolean premult) {
      ColorModel cm = makeLinear_sRGBCM(premult);
      WritableRaster wr = cm.createCompatibleWritableRaster(width, height);
      return new BufferedImage(cm, wr, premult, (Hashtable)null);
   }

   public static CachableRed convertToLsRGB(CachableRed src) {
      ColorModel cm = src.getColorModel();
      ColorSpace cs = cm.getColorSpace();
      return (CachableRed)(cs == ColorSpace.getInstance(1004) ? src : new Any2LsRGBRed(src));
   }

   public static CachableRed convertTosRGB(CachableRed src) {
      ColorModel cm = src.getColorModel();
      ColorSpace cs = cm.getColorSpace();
      return (CachableRed)(cs == ColorSpace.getInstance(1000) ? src : new Any2sRGBRed(src));
   }

   public static CachableRed wrap(RenderedImage ri) {
      if (ri instanceof CachableRed) {
         return (CachableRed)ri;
      } else {
         return (CachableRed)(ri instanceof BufferedImage ? new BufferedImageCachableRed((BufferedImage)ri) : new RenderedImageCachableRed(ri));
      }
   }

   public static void copyData_INT_PACK(Raster src, WritableRaster dst) {
      int x0 = dst.getMinX();
      if (x0 < src.getMinX()) {
         x0 = src.getMinX();
      }

      int y0 = dst.getMinY();
      if (y0 < src.getMinY()) {
         y0 = src.getMinY();
      }

      int x1 = dst.getMinX() + dst.getWidth() - 1;
      if (x1 > src.getMinX() + src.getWidth() - 1) {
         x1 = src.getMinX() + src.getWidth() - 1;
      }

      int y1 = dst.getMinY() + dst.getHeight() - 1;
      if (y1 > src.getMinY() + src.getHeight() - 1) {
         y1 = src.getMinY() + src.getHeight() - 1;
      }

      int width = x1 - x0 + 1;
      int height = y1 - y0 + 1;
      SinglePixelPackedSampleModel srcSPPSM = (SinglePixelPackedSampleModel)src.getSampleModel();
      int srcScanStride = srcSPPSM.getScanlineStride();
      DataBufferInt srcDB = (DataBufferInt)src.getDataBuffer();
      int[] srcPixels = srcDB.getBankData()[0];
      int srcBase = srcDB.getOffset() + srcSPPSM.getOffset(x0 - src.getSampleModelTranslateX(), y0 - src.getSampleModelTranslateY());
      SinglePixelPackedSampleModel dstSPPSM = (SinglePixelPackedSampleModel)dst.getSampleModel();
      int dstScanStride = dstSPPSM.getScanlineStride();
      DataBufferInt dstDB = (DataBufferInt)dst.getDataBuffer();
      int[] dstPixels = dstDB.getBankData()[0];
      int dstBase = dstDB.getOffset() + dstSPPSM.getOffset(x0 - dst.getSampleModelTranslateX(), y0 - dst.getSampleModelTranslateY());
      if (srcScanStride == dstScanStride && srcScanStride == width) {
         System.arraycopy(srcPixels, srcBase, dstPixels, dstBase, width * height);
      } else {
         int srcSP;
         int dstSP;
         int y;
         if (width > 128) {
            srcSP = srcBase;
            dstSP = dstBase;

            for(y = 0; y < height; ++y) {
               System.arraycopy(srcPixels, srcSP, dstPixels, dstSP, width);
               srcSP += srcScanStride;
               dstSP += dstScanStride;
            }
         } else {
            for(srcSP = 0; srcSP < height; ++srcSP) {
               dstSP = srcBase + srcSP * srcScanStride;
               y = dstBase + srcSP * dstScanStride;

               for(int x = 0; x < width; ++x) {
                  dstPixels[y++] = srcPixels[dstSP++];
               }
            }
         }
      }

   }

   public static void copyData_FALLBACK(Raster src, WritableRaster dst) {
      int x0 = dst.getMinX();
      if (x0 < src.getMinX()) {
         x0 = src.getMinX();
      }

      int y0 = dst.getMinY();
      if (y0 < src.getMinY()) {
         y0 = src.getMinY();
      }

      int x1 = dst.getMinX() + dst.getWidth() - 1;
      if (x1 > src.getMinX() + src.getWidth() - 1) {
         x1 = src.getMinX() + src.getWidth() - 1;
      }

      int y1 = dst.getMinY() + dst.getHeight() - 1;
      if (y1 > src.getMinY() + src.getHeight() - 1) {
         y1 = src.getMinY() + src.getHeight() - 1;
      }

      int width = x1 - x0 + 1;
      int[] data = null;

      for(int y = y0; y <= y1; ++y) {
         data = src.getPixels(x0, y, width, 1, data);
         dst.setPixels(x0, y, width, 1, data);
      }

   }

   public static void copyData(Raster src, WritableRaster dst) {
      if (is_INT_PACK_Data(src.getSampleModel(), false) && is_INT_PACK_Data(dst.getSampleModel(), false)) {
         copyData_INT_PACK(src, dst);
      } else {
         copyData_FALLBACK(src, dst);
      }
   }

   public static WritableRaster copyRaster(Raster ras) {
      return copyRaster(ras, ras.getMinX(), ras.getMinY());
   }

   public static WritableRaster copyRaster(Raster ras, int minX, int minY) {
      WritableRaster ret = Raster.createWritableRaster(ras.getSampleModel(), new Point(0, 0));
      ret = ret.createWritableChild(ras.getMinX() - ras.getSampleModelTranslateX(), ras.getMinY() - ras.getSampleModelTranslateY(), ras.getWidth(), ras.getHeight(), minX, minY, (int[])null);
      DataBuffer srcDB = ras.getDataBuffer();
      DataBuffer retDB = ret.getDataBuffer();
      if (srcDB.getDataType() != retDB.getDataType()) {
         throw new IllegalArgumentException("New DataBuffer doesn't match original");
      } else {
         int len = srcDB.getSize();
         int banks = srcDB.getNumBanks();
         int[] offsets = srcDB.getOffsets();

         for(int b = 0; b < banks; ++b) {
            switch (srcDB.getDataType()) {
               case 0:
                  DataBufferByte srcDBT = (DataBufferByte)srcDB;
                  DataBufferByte retDBT = (DataBufferByte)retDB;
                  System.arraycopy(srcDBT.getData(b), offsets[b], retDBT.getData(b), offsets[b], len);
                  break;
               case 1:
                  DataBufferUShort srcDBT = (DataBufferUShort)srcDB;
                  DataBufferUShort retDBT = (DataBufferUShort)retDB;
                  System.arraycopy(srcDBT.getData(b), offsets[b], retDBT.getData(b), offsets[b], len);
                  break;
               case 2:
                  DataBufferShort srcDBT = (DataBufferShort)srcDB;
                  DataBufferShort retDBT = (DataBufferShort)retDB;
                  System.arraycopy(srcDBT.getData(b), offsets[b], retDBT.getData(b), offsets[b], len);
                  break;
               case 3:
                  DataBufferInt srcDBT = (DataBufferInt)srcDB;
                  DataBufferInt retDBT = (DataBufferInt)retDB;
                  System.arraycopy(srcDBT.getData(b), offsets[b], retDBT.getData(b), offsets[b], len);
            }
         }

         return ret;
      }
   }

   public static WritableRaster makeRasterWritable(Raster ras) {
      return makeRasterWritable(ras, ras.getMinX(), ras.getMinY());
   }

   public static WritableRaster makeRasterWritable(Raster ras, int minX, int minY) {
      WritableRaster ret = Raster.createWritableRaster(ras.getSampleModel(), ras.getDataBuffer(), new Point(0, 0));
      ret = ret.createWritableChild(ras.getMinX() - ras.getSampleModelTranslateX(), ras.getMinY() - ras.getSampleModelTranslateY(), ras.getWidth(), ras.getHeight(), minX, minY, (int[])null);
      return ret;
   }

   public static ColorModel coerceColorModel(ColorModel cm, boolean newAlphaPreMult) {
      if (cm.isAlphaPremultiplied() == newAlphaPreMult) {
         return cm;
      } else {
         WritableRaster wr = cm.createCompatibleWritableRaster(1, 1);
         return cm.coerceData(wr, newAlphaPreMult);
      }
   }

   public static ColorModel coerceData(WritableRaster wr, ColorModel cm, boolean newAlphaPreMult) {
      if (!cm.hasAlpha()) {
         return cm;
      } else if (cm.isAlphaPremultiplied() == newAlphaPreMult) {
         return cm;
      } else {
         if (newAlphaPreMult) {
            multiplyAlpha(wr);
         } else {
            divideAlpha(wr);
         }

         return coerceColorModel(cm, newAlphaPreMult);
      }
   }

   public static void multiplyAlpha(WritableRaster wr) {
      if (is_BYTE_COMP_Data(wr.getSampleModel())) {
         mult_BYTE_COMP_Data(wr);
      } else if (is_INT_PACK_Data(wr.getSampleModel(), true)) {
         mult_INT_PACK_Data(wr);
      } else {
         int[] pixel = null;
         int bands = wr.getNumBands();
         float norm = 0.003921569F;
         int x0 = wr.getMinX();
         int x1 = x0 + wr.getWidth();
         int y0 = wr.getMinY();
         int y1 = y0 + wr.getHeight();

         for(int y = y0; y < y1; ++y) {
            for(int x = x0; x < x1; ++x) {
               pixel = wr.getPixel(x, y, pixel);
               int a = pixel[bands - 1];
               if (a >= 0 && a < 255) {
                  float alpha = (float)a * norm;

                  for(int b = 0; b < bands - 1; ++b) {
                     pixel[b] = (int)((float)pixel[b] * alpha + 0.5F);
                  }

                  wr.setPixel(x, y, pixel);
               }
            }
         }
      }

   }

   public static void divideAlpha(WritableRaster wr) {
      if (is_BYTE_COMP_Data(wr.getSampleModel())) {
         divide_BYTE_COMP_Data(wr);
      } else if (is_INT_PACK_Data(wr.getSampleModel(), true)) {
         divide_INT_PACK_Data(wr);
      } else {
         int bands = wr.getNumBands();
         int[] pixel = null;
         int x0 = wr.getMinX();
         int x1 = x0 + wr.getWidth();
         int y0 = wr.getMinY();
         int y1 = y0 + wr.getHeight();

         for(int y = y0; y < y1; ++y) {
            for(int x = x0; x < x1; ++x) {
               pixel = wr.getPixel(x, y, pixel);
               int a = pixel[bands - 1];
               if (a > 0 && a < 255) {
                  float ialpha = 255.0F / (float)a;

                  for(int b = 0; b < bands - 1; ++b) {
                     pixel[b] = (int)((float)pixel[b] * ialpha + 0.5F);
                  }

                  wr.setPixel(x, y, pixel);
               }
            }
         }
      }

   }

   public static void copyData(BufferedImage src, BufferedImage dst) {
      Rectangle srcRect = new Rectangle(0, 0, src.getWidth(), src.getHeight());
      copyData(src, srcRect, dst, new Point(0, 0));
   }

   public static void copyData(BufferedImage src, Rectangle srcRect, BufferedImage dst, Point destP) {
      boolean srcAlpha = src.getColorModel().hasAlpha();
      boolean dstAlpha = dst.getColorModel().hasAlpha();
      if (srcAlpha != dstAlpha || srcAlpha && src.isAlphaPremultiplied() != dst.isAlphaPremultiplied()) {
         int[] pixel = null;
         Raster srcR = src.getRaster();
         WritableRaster dstR = dst.getRaster();
         int bands = dstR.getNumBands();
         int dx = destP.x - srcRect.x;
         int dy = destP.y - srcRect.y;
         int w = srcRect.width;
         int x0 = srcRect.x;
         int y0 = srcRect.y;
         int y1 = y0 + srcRect.height - 1;
         int[] oPix;
         int a;
         int b;
         int ialpha;
         int in;
         if (!srcAlpha) {
            oPix = new int[bands * w];

            for(a = w * bands - 1; a >= 0; a -= bands) {
               oPix[a] = 255;
            }

            for(in = y0; in <= y1; ++in) {
               pixel = srcR.getPixels(x0, in, w, 1, pixel);
               ialpha = w * (bands - 1) - 1;
               a = w * bands - 2;
               label199:
               switch (bands) {
                  case 4:
                     while(true) {
                        if (ialpha < 0) {
                           break label199;
                        }

                        oPix[a--] = pixel[ialpha--];
                        oPix[a--] = pixel[ialpha--];
                        oPix[a--] = pixel[ialpha--];
                        --a;
                     }
                  default:
                     while(ialpha >= 0) {
                        for(b = 0; b < bands - 1; ++b) {
                           oPix[a--] = pixel[ialpha--];
                        }

                        --a;
                     }
               }

               dstR.setPixels(x0 + dx, in + dy, w, 1, oPix);
            }
         } else {
            int pt5;
            int fpNorm;
            int a;
            if (dstAlpha && dst.isAlphaPremultiplied()) {
               in = 65793;
               pt5 = 8388608;

               for(fpNorm = y0; fpNorm <= y1; ++fpNorm) {
                  pixel = srcR.getPixels(x0, fpNorm, w, 1, pixel);
                  ialpha = bands * w - 1;
                  label168:
                  switch (bands) {
                     case 4:
                        while(true) {
                           if (ialpha < 0) {
                              break label168;
                           }

                           a = pixel[ialpha];
                           if (a == 255) {
                              ialpha -= 4;
                           } else {
                              --ialpha;
                              b = in * a;
                              pixel[ialpha] = pixel[ialpha] * b + pt5 >>> 24;
                              --ialpha;
                              pixel[ialpha] = pixel[ialpha] * b + pt5 >>> 24;
                              --ialpha;
                              pixel[ialpha] = pixel[ialpha] * b + pt5 >>> 24;
                              --ialpha;
                           }
                        }
                     default:
                        label182:
                        while(true) {
                           while(true) {
                              if (ialpha < 0) {
                                 break label182;
                              }

                              a = pixel[ialpha];
                              if (a == 255) {
                                 ialpha -= bands;
                              } else {
                                 --ialpha;
                                 b = in * a;

                                 for(a = 0; a < bands - 1; ++a) {
                                    pixel[ialpha] = pixel[ialpha] * b + pt5 >>> 24;
                                    --ialpha;
                                 }
                              }
                           }
                        }
                  }

                  dstR.setPixels(x0 + dx, fpNorm + dy, w, 1, pixel);
               }
            } else if (dstAlpha && !dst.isAlphaPremultiplied()) {
               in = 16711680;
               int pt5 = '耀';

               for(fpNorm = y0; fpNorm <= y1; ++fpNorm) {
                  pixel = srcR.getPixels(x0, fpNorm, w, 1, pixel);
                  ialpha = bands * w - 1;
                  label136:
                  switch (bands) {
                     case 4:
                        while(true) {
                           while(true) {
                              if (ialpha < 0) {
                                 break label136;
                              }

                              a = pixel[ialpha];
                              if (a > 0 && a < 255) {
                                 --ialpha;
                                 b = in / a;
                                 pixel[ialpha] = pixel[ialpha] * b + pt5 >>> 16;
                                 --ialpha;
                                 pixel[ialpha] = pixel[ialpha] * b + pt5 >>> 16;
                                 --ialpha;
                                 pixel[ialpha] = pixel[ialpha] * b + pt5 >>> 16;
                                 --ialpha;
                              } else {
                                 ialpha -= 4;
                              }
                           }
                        }
                     default:
                        label152:
                        while(true) {
                           while(true) {
                              if (ialpha < 0) {
                                 break label152;
                              }

                              a = pixel[ialpha];
                              if (a > 0 && a < 255) {
                                 --ialpha;
                                 b = in / a;

                                 for(a = 0; a < bands - 1; ++a) {
                                    pixel[ialpha] = pixel[ialpha] * b + pt5 >>> 16;
                                    --ialpha;
                                 }
                              } else {
                                 ialpha -= bands;
                              }
                           }
                        }
                  }

                  dstR.setPixels(x0 + dx, fpNorm + dy, w, 1, pixel);
               }
            } else if (src.isAlphaPremultiplied()) {
               oPix = new int[bands * w];
               fpNorm = 16711680;
               int pt5 = '耀';

               label120:
               for(int y = y0; y <= y1; ++y) {
                  pixel = srcR.getPixels(x0, y, w, 1, pixel);
                  in = (bands + 1) * w - 1;
                  pt5 = bands * w - 1;

                  while(true) {
                     while(true) {
                        while(in >= 0) {
                           a = pixel[in];
                           --in;
                           if (a > 0) {
                              if (a < 255) {
                                 ialpha = fpNorm / a;

                                 for(b = 0; b < bands; ++b) {
                                    oPix[pt5--] = pixel[in--] * ialpha + pt5 >>> 16;
                                 }
                              } else {
                                 for(b = 0; b < bands; ++b) {
                                    oPix[pt5--] = pixel[in--];
                                 }
                              }
                           } else {
                              in -= bands;

                              for(b = 0; b < bands; ++b) {
                                 oPix[pt5--] = 255;
                              }
                           }
                        }

                        dstR.setPixels(x0 + dx, y + dy, w, 1, oPix);
                        continue label120;
                     }
                  }
               }
            } else {
               Rectangle dstRect = new Rectangle(destP.x, destP.y, srcRect.width, srcRect.height);

               for(a = 0; a < bands; ++a) {
                  copyBand(srcR, srcRect, a, dstR, dstRect, a);
               }
            }
         }

      } else {
         copyData((Raster)src.getRaster(), (WritableRaster)dst.getRaster());
      }
   }

   public static void copyBand(Raster src, int srcBand, WritableRaster dst, int dstBand) {
      Rectangle sR = src.getBounds();
      Rectangle dR = dst.getBounds();
      Rectangle cpR = sR.intersection(dR);
      copyBand(src, cpR, srcBand, dst, cpR, dstBand);
   }

   public static void copyBand(Raster src, Rectangle sR, int sBand, WritableRaster dst, Rectangle dR, int dBand) {
      int dy = dR.y - sR.y;
      int dx = dR.x - sR.x;
      sR = sR.intersection(src.getBounds());
      dR = dR.intersection(dst.getBounds());
      int width;
      if (dR.width < sR.width) {
         width = dR.width;
      } else {
         width = sR.width;
      }

      int height;
      if (dR.height < sR.height) {
         height = dR.height;
      } else {
         height = sR.height;
      }

      int x = sR.x + dx;
      int[] samples = null;

      for(int y = sR.y; y < sR.y + height; ++y) {
         samples = src.getSamples(sR.x, y, width, 1, sBand, samples);
         dst.setSamples(x, y + dy, width, 1, dBand, samples);
      }

   }

   public static boolean is_INT_PACK_Data(SampleModel sm, boolean requireAlpha) {
      if (!(sm instanceof SinglePixelPackedSampleModel)) {
         return false;
      } else if (sm.getDataType() != 3) {
         return false;
      } else {
         SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)sm;
         int[] masks = sppsm.getBitMasks();
         if (masks.length == 3) {
            if (requireAlpha) {
               return false;
            }
         } else if (masks.length != 4) {
            return false;
         }

         if (masks[0] != 16711680) {
            return false;
         } else if (masks[1] != 65280) {
            return false;
         } else if (masks[2] != 255) {
            return false;
         } else {
            return masks.length != 4 || masks[3] == -16777216;
         }
      }
   }

   public static boolean is_BYTE_COMP_Data(SampleModel sm) {
      if (!(sm instanceof ComponentSampleModel)) {
         return false;
      } else {
         return sm.getDataType() == 0;
      }
   }

   protected static void divide_INT_PACK_Data(WritableRaster wr) {
      SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();
      int width = wr.getWidth();
      int scanStride = sppsm.getScanlineStride();
      DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
      int base = db.getOffset() + sppsm.getOffset(wr.getMinX() - wr.getSampleModelTranslateX(), wr.getMinY() - wr.getSampleModelTranslateY());
      int[] pixels = db.getBankData()[0];

      for(int y = 0; y < wr.getHeight(); ++y) {
         int sp = base + y * scanStride;

         for(int end = sp + width; sp < end; ++sp) {
            int pixel = pixels[sp];
            int a = pixel >>> 24;
            if (a <= 0) {
               pixels[sp] = 16777215;
            } else if (a < 255) {
               int aFP = 16711680 / a;
               pixels[sp] = a << 24 | ((pixel & 16711680) >> 16) * aFP & 16711680 | (((pixel & '\uff00') >> 8) * aFP & 16711680) >> 8 | ((pixel & 255) * aFP & 16711680) >> 16;
            }
         }
      }

   }

   protected static void mult_INT_PACK_Data(WritableRaster wr) {
      SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();
      int width = wr.getWidth();
      int scanStride = sppsm.getScanlineStride();
      DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
      int base = db.getOffset() + sppsm.getOffset(wr.getMinX() - wr.getSampleModelTranslateX(), wr.getMinY() - wr.getSampleModelTranslateY());
      int[] pixels = db.getBankData()[0];

      for(int y = 0; y < wr.getHeight(); ++y) {
         int sp = base + y * scanStride;

         for(int end = sp + width; sp < end; ++sp) {
            int pixel = pixels[sp];
            int a = pixel >>> 24;
            if (a >= 0 && a < 255) {
               pixels[sp] = a << 24 | (pixel & 16711680) * a >> 8 & 16711680 | (pixel & '\uff00') * a >> 8 & '\uff00' | (pixel & 255) * a >> 8 & 255;
            }
         }
      }

   }

   protected static void divide_BYTE_COMP_Data(WritableRaster wr) {
      ComponentSampleModel csm = (ComponentSampleModel)wr.getSampleModel();
      int width = wr.getWidth();
      int scanStride = csm.getScanlineStride();
      int pixStride = csm.getPixelStride();
      int[] bandOff = csm.getBandOffsets();
      DataBufferByte db = (DataBufferByte)wr.getDataBuffer();
      int base = db.getOffset() + csm.getOffset(wr.getMinX() - wr.getSampleModelTranslateX(), wr.getMinY() - wr.getSampleModelTranslateY());
      int aOff = bandOff[bandOff.length - 1];
      int bands = bandOff.length - 1;
      byte[] pixels = db.getBankData()[0];

      for(int y = 0; y < wr.getHeight(); ++y) {
         int sp = base + y * scanStride;

         for(int end = sp + width * pixStride; sp < end; sp += pixStride) {
            int a = pixels[sp + aOff] & 255;
            int aFP;
            if (a == 0) {
               for(aFP = 0; aFP < bands; ++aFP) {
                  pixels[sp + bandOff[aFP]] = -1;
               }
            } else if (a < 255) {
               aFP = 16711680 / a;

               for(int b = 0; b < bands; ++b) {
                  int i = sp + bandOff[b];
                  pixels[i] = (byte)((pixels[i] & 255) * aFP >>> 16);
               }
            }
         }
      }

   }

   protected static void mult_BYTE_COMP_Data(WritableRaster wr) {
      ComponentSampleModel csm = (ComponentSampleModel)wr.getSampleModel();
      int width = wr.getWidth();
      int scanStride = csm.getScanlineStride();
      int pixStride = csm.getPixelStride();
      int[] bandOff = csm.getBandOffsets();
      DataBufferByte db = (DataBufferByte)wr.getDataBuffer();
      int base = db.getOffset() + csm.getOffset(wr.getMinX() - wr.getSampleModelTranslateX(), wr.getMinY() - wr.getSampleModelTranslateY());
      int aOff = bandOff[bandOff.length - 1];
      int bands = bandOff.length - 1;
      byte[] pixels = db.getBankData()[0];

      for(int y = 0; y < wr.getHeight(); ++y) {
         int sp = base + y * scanStride;

         for(int end = sp + width * pixStride; sp < end; sp += pixStride) {
            int a = pixels[sp + aOff] & 255;
            if (a != 255) {
               for(int b = 0; b < bands; ++b) {
                  int i = sp + bandOff[b];
                  pixels[i] = (byte)((pixels[i] & 255) * a >> 8);
               }
            }
         }
      }

   }

   static {
      boolean warn = true;

      try {
         String s = System.getProperty("org.apache.batik.warn_destination", "true");
         warn = Boolean.valueOf(s);
      } catch (SecurityException var6) {
      } catch (NumberFormatException var7) {
      } finally {
         WARN_DESTINATION = warn;
      }

      Linear_sRGB = new DirectColorModel(ColorSpace.getInstance(1004), 24, 16711680, 65280, 255, 0, false, 3);
      Linear_sRGB_Pre = new DirectColorModel(ColorSpace.getInstance(1004), 32, 16711680, 65280, 255, -16777216, true, 3);
      Linear_sRGB_Unpre = new DirectColorModel(ColorSpace.getInstance(1004), 32, 16711680, 65280, 255, -16777216, false, 3);
      sRGB = new DirectColorModel(ColorSpace.getInstance(1000), 24, 16711680, 65280, 255, 0, false, 3);
      sRGB_Pre = new DirectColorModel(ColorSpace.getInstance(1000), 32, 16711680, 65280, 255, -16777216, true, 3);
      sRGB_Unpre = new DirectColorModel(ColorSpace.getInstance(1000), 32, 16711680, 65280, 255, -16777216, false, 3);
   }
}
