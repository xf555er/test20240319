package org.apache.fop.pdf;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.Iterator;
import org.apache.fop.util.AbstractPaintingState;
import org.apache.xmlgraphics.java2d.color.ColorUtil;

public class PDFPaintingState extends AbstractPaintingState {
   private static final long serialVersionUID = 5384726143906371279L;

   public boolean setPaint(Paint p) {
      PDFData data = this.getPDFData();
      Paint currentPaint = data.paint;
      if (currentPaint == null) {
         if (p != null) {
            data.paint = p;
            return true;
         }
      } else if (p instanceof Color && currentPaint instanceof Color) {
         if (!ColorUtil.isSameColor((Color)p, (Color)currentPaint)) {
            data.paint = p;
            return true;
         }
      } else if (!currentPaint.equals(p)) {
         data.paint = p;
         return true;
      }

      return false;
   }

   public boolean checkClip(Shape cl) {
      Shape clip = this.getPDFData().clip;
      if (clip == null) {
         if (cl != null) {
            return true;
         }
      } else if (!(new Area(clip)).equals(new Area(cl))) {
         return true;
      }

      return false;
   }

   public void setClip(Shape cl) {
      PDFData data = this.getPDFData();
      Shape clip = data.clip;
      if (clip != null) {
         Area newClip = new Area(clip);
         newClip.intersect(new Area(cl));
         data.clip = new GeneralPath(newClip);
      } else {
         data.clip = cl;
      }

   }

   public boolean setCharacterSpacing(float value) {
      PDFData data = this.getPDFData();
      if (value != data.characterSpacing) {
         data.characterSpacing = value;
         return true;
      } else {
         return false;
      }
   }

   public float getCharacterSpacing() {
      return this.getPDFData().characterSpacing;
   }

   public int getStackLevel() {
      return this.getStateStack().size();
   }

   public PDFGState getGState() {
      PDFGState defaultState = PDFGState.DEFAULT;
      PDFGState newState = new PDFGState();
      newState.addValues(defaultState);
      Iterator var4 = this.getStateStack().iterator();

      while(var4.hasNext()) {
         AbstractPaintingState.AbstractData abstractData = (AbstractPaintingState.AbstractData)var4.next();
         PDFData data = (PDFData)abstractData;
         PDFGState state = data.gstate;
         if (state != null) {
            newState.addValues(state);
         }
      }

      if (this.getPDFData().gstate != null) {
         newState.addValues(this.getPDFData().gstate);
      }

      return newState;
   }

   public void setLayer(String layer) {
      this.getPDFData().setLayer(layer);
   }

   public String getLayer() {
      return this.getPDFData().getLayer();
   }

   public boolean getLayerChanged() {
      String layerCurrent = this.getLayer();
      if (layerCurrent == null) {
         return false;
      } else if (this.getStateStack().isEmpty()) {
         return true;
      } else {
         for(int i = this.getStackLevel(); i > 0; --i) {
            String layerPrev = ((PDFData)this.getStateStack().get(i - 1)).getLayer();
            if (layerPrev != null) {
               return !layerCurrent.equals(layerPrev);
            }
         }

         return true;
      }
   }

   protected AbstractPaintingState.AbstractData instantiateData() {
      return new PDFData();
   }

   protected AbstractPaintingState instantiate() {
      return new PDFPaintingState();
   }

   public void save() {
      AbstractPaintingState.AbstractData data = this.getData();
      AbstractPaintingState.AbstractData copy = (AbstractPaintingState.AbstractData)data.clone();
      data.clearTransform();
      this.getStateStack().push(copy);
   }

   private PDFData getPDFData() {
      return (PDFData)this.getData();
   }

   private class PDFData extends AbstractPaintingState.AbstractData {
      private static final long serialVersionUID = 3527950647293177764L;
      private Paint paint;
      private Paint backPaint;
      private Shape clip;
      private PDFGState gstate;
      private float characterSpacing;

      private PDFData() {
         super();
      }

      public Object clone() {
         PDFData obj = (PDFData)super.clone();
         obj.paint = this.paint;
         obj.backPaint = this.paint;
         obj.clip = this.clip;
         obj.gstate = this.gstate;
         obj.characterSpacing = this.characterSpacing;
         return obj;
      }

      public String toString() {
         return super.toString() + ", paint=" + this.paint + ", backPaint=" + this.backPaint + ", clip=" + this.clip + ", gstate=" + this.gstate;
      }

      protected AbstractPaintingState.AbstractData instantiate() {
         return PDFPaintingState.this.new PDFData();
      }

      // $FF: synthetic method
      PDFData(Object x1) {
         this();
      }
   }
}
