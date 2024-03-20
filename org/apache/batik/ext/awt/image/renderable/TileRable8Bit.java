package org.apache.batik.ext.awt.image.renderable;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Map;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.BufferedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.TileRed;

public class TileRable8Bit extends AbstractColorInterpolationRable implements TileRable {
   private Rectangle2D tileRegion;
   private Rectangle2D tiledRegion;
   private boolean overflow;

   public Rectangle2D getTileRegion() {
      return this.tileRegion;
   }

   public void setTileRegion(Rectangle2D tileRegion) {
      if (tileRegion == null) {
         throw new IllegalArgumentException();
      } else {
         this.touch();
         this.tileRegion = tileRegion;
      }
   }

   public Rectangle2D getTiledRegion() {
      return this.tiledRegion;
   }

   public void setTiledRegion(Rectangle2D tiledRegion) {
      if (tiledRegion == null) {
         throw new IllegalArgumentException();
      } else {
         this.touch();
         this.tiledRegion = tiledRegion;
      }
   }

   public boolean isOverflow() {
      return this.overflow;
   }

   public void setOverflow(boolean overflow) {
      this.touch();
      this.overflow = overflow;
   }

   public TileRable8Bit(Filter source, Rectangle2D tiledRegion, Rectangle2D tileRegion, boolean overflow) {
      super(source);
      this.setTileRegion(tileRegion);
      this.setTiledRegion(tiledRegion);
      this.setOverflow(overflow);
   }

   public void setSource(Filter src) {
      this.init(src);
   }

   public Filter getSource() {
      return (Filter)this.srcs.get(0);
   }

   public Rectangle2D getBounds2D() {
      return (Rectangle2D)this.tiledRegion.clone();
   }

   public RenderedImage createRendering(RenderContext rc) {
      RenderingHints rh = rc.getRenderingHints();
      if (rh == null) {
         rh = new RenderingHints((Map)null);
      }

      AffineTransform at = rc.getTransform();
      double sx = at.getScaleX();
      double sy = at.getScaleY();
      double shx = at.getShearX();
      double shy = at.getShearY();
      double tx = at.getTranslateX();
      double ty = at.getTranslateY();
      double scaleX = Math.sqrt(sx * sx + shy * shy);
      double scaleY = Math.sqrt(sy * sy + shx * shx);
      Rectangle2D tiledRect = this.getBounds2D();
      Shape aoiShape = rc.getAreaOfInterest();
      Rectangle2D aoiRect;
      if (aoiShape == null) {
         aoiRect = tiledRect;
      } else {
         aoiRect = aoiShape.getBounds2D();
         if (!tiledRect.intersects(aoiRect)) {
            return null;
         }

         Rectangle2D.intersect(tiledRect, aoiRect, tiledRect);
      }

      Rectangle2D tileRect = this.tileRegion;
      int dw = (int)Math.ceil(tileRect.getWidth() * scaleX);
      int dh = (int)Math.ceil(tileRect.getHeight() * scaleY);
      double tileScaleX = (double)dw / tileRect.getWidth();
      double tileScaleY = (double)dh / tileRect.getHeight();
      int dx = (int)Math.floor(tileRect.getX() * tileScaleX);
      int dy = (int)Math.floor(tileRect.getY() * tileScaleY);
      double ttx = (double)dx - tileRect.getX() * tileScaleX;
      double tty = (double)dy - tileRect.getY() * tileScaleY;
      AffineTransform tileAt = AffineTransform.getTranslateInstance(ttx, tty);
      tileAt.scale(tileScaleX, tileScaleY);
      Filter source = this.getSource();
      Rectangle2D srcRect;
      if (this.overflow) {
         srcRect = source.getBounds2D();
      } else {
         srcRect = tileRect;
      }

      RenderContext tileRc = new RenderContext(tileAt, srcRect, rh);
      RenderedImage tileRed = source.createRendering(tileRc);
      if (tileRed == null) {
         return null;
      } else {
         Rectangle tiledArea = tileAt.createTransformedShape(aoiRect).getBounds();
         if (tiledArea.width == Integer.MAX_VALUE || tiledArea.height == Integer.MAX_VALUE) {
            tiledArea = new Rectangle(-536870912, -536870912, 1073741823, 1073741823);
         }

         RenderedImage tileRed = this.convertSourceCS(tileRed);
         TileRed tiledRed = new TileRed(tileRed, tiledArea, dw, dh);
         AffineTransform shearAt = new AffineTransform(sx / scaleX, shy / scaleX, shx / scaleY, sy / scaleY, tx, ty);
         shearAt.scale(scaleX / tileScaleX, scaleY / tileScaleY);
         shearAt.translate(-ttx, -tty);
         CachableRed cr = tiledRed;
         if (!shearAt.isIdentity()) {
            cr = new AffineRed(tiledRed, shearAt, rh);
         }

         return (RenderedImage)cr;
      }
   }

   public Rectangle2D getActualTileBounds(Rectangle2D tiledRect) {
      Rectangle2D tileRect = (Rectangle2D)this.tileRegion.clone();
      if (!(tileRect.getWidth() <= 0.0) && !(tileRect.getHeight() <= 0.0) && !(tiledRect.getWidth() <= 0.0) && !(tiledRect.getHeight() <= 0.0)) {
         double tileWidth = tileRect.getWidth();
         double tileHeight = tileRect.getHeight();
         double tiledWidth = tiledRect.getWidth();
         double tiledHeight = tiledRect.getHeight();
         double w = Math.min(tileWidth, tiledWidth);
         double h = Math.min(tileHeight, tiledHeight);
         Rectangle2D realTileRect = new Rectangle2D.Double(tileRect.getX(), tileRect.getY(), w, h);
         return realTileRect;
      } else {
         return null;
      }
   }

   public RenderedImage createTile(RenderContext rc) {
      AffineTransform usr2dev = rc.getTransform();
      RenderingHints rcHints = rc.getRenderingHints();
      RenderingHints hints = new RenderingHints((Map)null);
      if (rcHints != null) {
         hints.add(rcHints);
      }

      Rectangle2D tiledRect = this.getBounds2D();
      Shape aoiShape = rc.getAreaOfInterest();
      Rectangle2D aoiRect = aoiShape.getBounds2D();
      if (!tiledRect.intersects(aoiRect)) {
         return null;
      } else {
         Rectangle2D.intersect(tiledRect, aoiRect, tiledRect);
         Rectangle2D tileRect = (Rectangle2D)this.tileRegion.clone();
         if (!(tileRect.getWidth() <= 0.0) && !(tileRect.getHeight() <= 0.0) && !(tiledRect.getWidth() <= 0.0) && !(tiledRect.getHeight() <= 0.0)) {
            double tileX = tileRect.getX();
            double tileY = tileRect.getY();
            double tileWidth = tileRect.getWidth();
            double tileHeight = tileRect.getHeight();
            double tiledX = tiledRect.getX();
            double tiledY = tiledRect.getY();
            double tiledWidth = tiledRect.getWidth();
            double tiledHeight = tiledRect.getHeight();
            double w = Math.min(tileWidth, tiledWidth);
            double h = Math.min(tileHeight, tiledHeight);
            double dx = (tiledX - tileX) % tileWidth;
            double dy = (tiledY - tileY) % tileHeight;
            if (dx > 0.0) {
               dx = tileWidth - dx;
            } else {
               dx *= -1.0;
            }

            if (dy > 0.0) {
               dy = tileHeight - dy;
            } else {
               dy *= -1.0;
            }

            double scaleX = usr2dev.getScaleX();
            double scaleY = usr2dev.getScaleY();
            double tdx = Math.floor(scaleX * dx);
            double tdy = Math.floor(scaleY * dy);
            dx = tdx / scaleX;
            dy = tdy / scaleY;
            Rectangle2D.Double A = new Rectangle2D.Double(tileX + tileWidth - dx, tileY + tileHeight - dy, dx, dy);
            Rectangle2D.Double B = new Rectangle2D.Double(tileX, tileY + tileHeight - dy, w - dx, dy);
            Rectangle2D.Double C = new Rectangle2D.Double(tileX + tileWidth - dx, tileY, dx, h - dy);
            Rectangle2D.Double D = new Rectangle2D.Double(tileX, tileY, w - dx, h - dy);
            Rectangle2D realTileRect = new Rectangle2D.Double(tiledRect.getX(), tiledRect.getY(), w, h);
            RenderedImage ARed = null;
            RenderedImage BRed = null;
            RenderedImage CRed = null;
            RenderedImage DRed = null;
            Filter source = this.getSource();
            Rectangle realTileRectDev;
            AffineTransform DTxf;
            Rectangle2D.Double aoi;
            RenderContext drc;
            if (A.getWidth() > 0.0 && A.getHeight() > 0.0) {
               realTileRectDev = usr2dev.createTransformedShape(A).getBounds();
               if (realTileRectDev.width > 0 && realTileRectDev.height > 0) {
                  DTxf = new AffineTransform(usr2dev);
                  DTxf.translate(-A.x + tiledX, -A.y + tiledY);
                  aoi = A;
                  if (this.overflow) {
                     aoi = new Rectangle2D.Double(A.x, A.y, tiledWidth, tiledHeight);
                  }

                  hints.put(RenderingHintsKeyExt.KEY_AREA_OF_INTEREST, aoi);
                  drc = new RenderContext(DTxf, aoi, hints);
                  ARed = source.createRendering(drc);
               }
            }

            if (B.getWidth() > 0.0 && B.getHeight() > 0.0) {
               realTileRectDev = usr2dev.createTransformedShape(B).getBounds();
               if (realTileRectDev.width > 0 && realTileRectDev.height > 0) {
                  DTxf = new AffineTransform(usr2dev);
                  DTxf.translate(-B.x + tiledX + dx, -B.y + tiledY);
                  aoi = B;
                  if (this.overflow) {
                     aoi = new Rectangle2D.Double(B.x - tiledWidth + w - dx, B.y, tiledWidth, tiledHeight);
                  }

                  hints.put(RenderingHintsKeyExt.KEY_AREA_OF_INTEREST, aoi);
                  drc = new RenderContext(DTxf, aoi, hints);
                  BRed = source.createRendering(drc);
               }
            }

            if (C.getWidth() > 0.0 && C.getHeight() > 0.0) {
               realTileRectDev = usr2dev.createTransformedShape(C).getBounds();
               if (realTileRectDev.width > 0 && realTileRectDev.height > 0) {
                  DTxf = new AffineTransform(usr2dev);
                  DTxf.translate(-C.x + tiledX, -C.y + tiledY + dy);
                  aoi = C;
                  if (this.overflow) {
                     aoi = new Rectangle2D.Double(C.x, C.y - tileHeight + h - dy, tiledWidth, tiledHeight);
                  }

                  hints.put(RenderingHintsKeyExt.KEY_AREA_OF_INTEREST, aoi);
                  drc = new RenderContext(DTxf, aoi, hints);
                  CRed = source.createRendering(drc);
               }
            }

            if (D.getWidth() > 0.0 && D.getHeight() > 0.0) {
               realTileRectDev = usr2dev.createTransformedShape(D).getBounds();
               if (realTileRectDev.width > 0 && realTileRectDev.height > 0) {
                  DTxf = new AffineTransform(usr2dev);
                  DTxf.translate(-D.x + tiledX + dx, -D.y + tiledY + dy);
                  aoi = D;
                  if (this.overflow) {
                     aoi = new Rectangle2D.Double(D.x - tileWidth + w - dx, D.y - tileHeight + h - dy, tiledWidth, tiledHeight);
                  }

                  hints.put(RenderingHintsKeyExt.KEY_AREA_OF_INTEREST, aoi);
                  drc = new RenderContext(DTxf, aoi, hints);
                  DRed = source.createRendering(drc);
               }
            }

            realTileRectDev = usr2dev.createTransformedShape(realTileRect).getBounds();
            if (realTileRectDev.width != 0 && realTileRectDev.height != 0) {
               BufferedImage realTileBI = new BufferedImage(realTileRectDev.width, realTileRectDev.height, 2);
               Graphics2D g = GraphicsUtil.createGraphics(realTileBI, rc.getRenderingHints());
               g.translate(-realTileRectDev.x, -realTileRectDev.y);
               AffineTransform redTxf = new AffineTransform();
               Point2D.Double redVec = new Point2D.Double();
               RenderedImage refRed = null;
               if (ARed != null) {
                  g.drawRenderedImage(ARed, redTxf);
                  refRed = ARed;
               }

               if (BRed != null) {
                  if (refRed == null) {
                     refRed = BRed;
                  }

                  redVec.x = dx;
                  redVec.y = 0.0;
                  usr2dev.deltaTransform(redVec, redVec);
                  redVec.x = Math.floor(redVec.x) - (double)(BRed.getMinX() - refRed.getMinX());
                  redVec.y = Math.floor(redVec.y) - (double)(BRed.getMinY() - refRed.getMinY());
                  g.drawRenderedImage(BRed, redTxf);
               }

               if (CRed != null) {
                  if (refRed == null) {
                     refRed = CRed;
                  }

                  redVec.x = 0.0;
                  redVec.y = dy;
                  usr2dev.deltaTransform(redVec, redVec);
                  redVec.x = Math.floor(redVec.x) - (double)(CRed.getMinX() - refRed.getMinX());
                  redVec.y = Math.floor(redVec.y) - (double)(CRed.getMinY() - refRed.getMinY());
                  g.drawRenderedImage(CRed, redTxf);
               }

               if (DRed != null) {
                  if (refRed == null) {
                     refRed = DRed;
                  }

                  redVec.x = dx;
                  redVec.y = dy;
                  usr2dev.deltaTransform(redVec, redVec);
                  redVec.x = Math.floor(redVec.x) - (double)(DRed.getMinX() - refRed.getMinX());
                  redVec.y = Math.floor(redVec.y) - (double)(DRed.getMinY() - refRed.getMinY());
                  g.drawRenderedImage(DRed, redTxf);
               }

               CachableRed realTile = new BufferedImageCachableRed(realTileBI, realTileRectDev.x, realTileRectDev.y);
               return realTile;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }
}
