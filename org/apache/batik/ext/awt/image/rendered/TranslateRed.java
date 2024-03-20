package org.apache.batik.ext.awt.image.rendered;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Map;

public class TranslateRed extends AbstractRed {
   protected int deltaX;
   protected int deltaY;

   public TranslateRed(CachableRed cr, int xloc, int yloc) {
      super((CachableRed)cr, new Rectangle(xloc, yloc, cr.getWidth(), cr.getHeight()), cr.getColorModel(), cr.getSampleModel(), cr.getTileGridXOffset() + xloc - cr.getMinX(), cr.getTileGridYOffset() + yloc - cr.getMinY(), (Map)null);
      this.deltaX = xloc - cr.getMinX();
      this.deltaY = yloc - cr.getMinY();
   }

   public int getDeltaX() {
      return this.deltaX;
   }

   public int getDeltaY() {
      return this.deltaY;
   }

   public CachableRed getSource() {
      return (CachableRed)this.getSources().get(0);
   }

   public Object getProperty(String name) {
      return this.getSource().getProperty(name);
   }

   public String[] getPropertyNames() {
      return this.getSource().getPropertyNames();
   }

   public Raster getTile(int tileX, int tileY) {
      Raster r = this.getSource().getTile(tileX, tileY);
      return r.createTranslatedChild(r.getMinX() + this.deltaX, r.getMinY() + this.deltaY);
   }

   public Raster getData() {
      Raster r = this.getSource().getData();
      return r.createTranslatedChild(r.getMinX() + this.deltaX, r.getMinY() + this.deltaY);
   }

   public Raster getData(Rectangle rect) {
      Rectangle r = (Rectangle)rect.clone();
      r.translate(-this.deltaX, -this.deltaY);
      Raster ret = this.getSource().getData(r);
      return ret.createTranslatedChild(ret.getMinX() + this.deltaX, ret.getMinY() + this.deltaY);
   }

   public WritableRaster copyData(WritableRaster wr) {
      WritableRaster wr2 = wr.createWritableTranslatedChild(wr.getMinX() - this.deltaX, wr.getMinY() - this.deltaY);
      this.getSource().copyData(wr2);
      return wr;
   }
}
