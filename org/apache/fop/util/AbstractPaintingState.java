package org.apache.fop.util;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public abstract class AbstractPaintingState implements Cloneable, Serializable {
   private static final long serialVersionUID = 5998356138437094188L;
   private AbstractData data;
   private StateStack stateStack = new StateStack();

   protected abstract AbstractData instantiateData();

   protected abstract AbstractPaintingState instantiate();

   public AbstractData getData() {
      if (this.data == null) {
         this.data = this.instantiateData();
      }

      return this.data;
   }

   public boolean setColor(Color col) {
      Color other = this.getData().color;
      if (!org.apache.xmlgraphics.java2d.color.ColorUtil.isSameColor(col, other)) {
         this.getData().color = col;
         return true;
      } else {
         return false;
      }
   }

   public Color getColor() {
      if (this.getData().color == null) {
         this.getData().color = Color.black;
      }

      return this.getData().color;
   }

   public Color getBackColor() {
      if (this.getData().backColor == null) {
         this.getData().backColor = Color.white;
      }

      return this.getData().backColor;
   }

   public boolean setBackColor(Color col) {
      Color other = this.getData().backColor;
      if (!org.apache.xmlgraphics.java2d.color.ColorUtil.isSameColor(col, other)) {
         this.getData().backColor = col;
         return true;
      } else {
         return false;
      }
   }

   public boolean setFontName(String internalFontName) {
      if (!internalFontName.equals(this.getData().fontName)) {
         this.getData().fontName = internalFontName;
         return true;
      } else {
         return false;
      }
   }

   public String getFontName() {
      return this.getData().fontName;
   }

   public int getFontSize() {
      return this.getData().fontSize;
   }

   public boolean setFontSize(int size) {
      if (size != this.getData().fontSize) {
         this.getData().fontSize = size;
         return true;
      } else {
         return false;
      }
   }

   public boolean setLineWidth(float width) {
      if (this.getData().lineWidth != width) {
         this.getData().lineWidth = width;
         return true;
      } else {
         return false;
      }
   }

   public float getLineWidth() {
      return this.getData().lineWidth;
   }

   public boolean setDashArray(float[] dash) {
      if (!Arrays.equals(dash, this.getData().dashArray)) {
         this.getData().dashArray = dash;
         return true;
      } else {
         return false;
      }
   }

   public AffineTransform getTransform() {
      AffineTransform at = new AffineTransform();
      Iterator var2 = this.stateStack.iterator();

      while(var2.hasNext()) {
         AbstractData data = (AbstractData)var2.next();
         AffineTransform stackTrans = data.getTransform();
         at.concatenate(stackTrans);
      }

      AffineTransform currentTrans = this.getData().getTransform();
      at.concatenate(currentTrans);
      return at;
   }

   public boolean checkTransform(AffineTransform tf) {
      return !tf.equals(this.getData().getTransform());
   }

   public AffineTransform getBaseTransform() {
      if (this.stateStack.isEmpty()) {
         return null;
      } else {
         AbstractData baseData = (AbstractData)this.stateStack.get(0);
         return (AffineTransform)baseData.getTransform().clone();
      }
   }

   public void concatenate(AffineTransform at) {
      this.getData().concatenate(at);
   }

   public void resetTransform() {
      this.getData().setTransform(this.getBaseTransform());
   }

   public void clearTransform() {
      this.getData().clearTransform();
   }

   public void save() {
      AbstractData copy = (AbstractData)this.getData().clone();
      this.stateStack.push(copy);
   }

   public AbstractData restore() {
      if (!this.stateStack.isEmpty()) {
         this.setData((AbstractData)this.stateStack.pop());
         return this.data;
      } else {
         return null;
      }
   }

   public void saveAll(List dataList) {
      Iterator var2 = dataList.iterator();

      while(var2.hasNext()) {
         AbstractData data = (AbstractData)var2.next();
         this.save();
         this.setData(data);
      }

   }

   public List restoreAll() {
      List dataList = new ArrayList();

      while(true) {
         AbstractData data = this.getData();
         if (this.restore() == null) {
            return dataList;
         }

         dataList.add(0, data);
      }
   }

   protected void setData(AbstractData data) {
      this.data = data;
   }

   public void clear() {
      this.stateStack.clear();
      this.setData((AbstractData)null);
   }

   protected Stack getStateStack() {
      return this.stateStack;
   }

   public Object clone() {
      AbstractPaintingState state = this.instantiate();
      state.stateStack = new StateStack(this.stateStack);
      if (this.data != null) {
         state.data = (AbstractData)this.data.clone();
      }

      return state;
   }

   public String toString() {
      return ", stateStack=" + this.stateStack + ", currentData=" + this.data;
   }

   public abstract class AbstractData implements Cloneable, Serializable {
      private static final long serialVersionUID = 5208418041189828624L;
      protected Color color;
      protected Color backColor;
      protected String fontName;
      protected int fontSize;
      protected float lineWidth;
      protected float[] dashArray;
      protected AffineTransform transform;
      protected String layer;

      protected abstract AbstractData instantiate();

      public void concatenate(AffineTransform at) {
         this.getTransform().concatenate(at);
      }

      public AffineTransform getTransform() {
         if (this.transform == null) {
            this.transform = new AffineTransform();
         }

         return this.transform;
      }

      public void setTransform(AffineTransform baseTransform) {
         this.transform = baseTransform;
      }

      public void clearTransform() {
         this.transform = new AffineTransform();
      }

      public void setLayer(String layer) {
         if (layer != null) {
            this.layer = layer;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public String getLayer() {
         return this.layer;
      }

      public int getDerivedRotation() {
         AffineTransform at = this.getTransform();
         double sx = at.getScaleX();
         double sy = at.getScaleY();
         double shx = at.getShearX();
         double shy = at.getShearY();
         int rotationx = false;
         short rotation;
         if (sx == 0.0 && sy == 0.0 && shx > 0.0 && shy < 0.0) {
            rotation = 270;
         } else if (sx < 0.0 && sy < 0.0 && shx == 0.0 && shy == 0.0) {
            rotation = 180;
         } else if (sx == 0.0 && sy == 0.0 && shx < 0.0 && shy > 0.0) {
            rotation = 90;
         } else {
            rotation = 0;
         }

         return rotation;
      }

      public Object clone() {
         AbstractData data = this.instantiate();
         data.color = this.color;
         data.backColor = this.backColor;
         data.fontName = this.fontName;
         data.fontSize = this.fontSize;
         data.lineWidth = this.lineWidth;
         data.dashArray = this.dashArray;
         if (this.transform == null) {
            this.transform = new AffineTransform();
         }

         data.transform = new AffineTransform(this.transform);
         data.layer = this.layer;
         return data;
      }

      public String toString() {
         return "color=" + this.color + ", backColor=" + this.backColor + ", fontName=" + this.fontName + ", fontSize=" + this.fontSize + ", lineWidth=" + this.lineWidth + ", dashArray=" + Arrays.toString(this.dashArray) + ", transform=" + this.transform + ", layer=" + this.layer;
      }
   }

   public class StateStack extends Stack {
      private static final long serialVersionUID = 4897178211223823041L;

      public StateStack() {
      }

      public StateStack(Collection c) {
         this.elementCount = c.size();
         this.elementData = new Object[(int)Math.min((long)this.elementCount * 110L / 100L, 2147483647L)];
         c.toArray(this.elementData);
      }
   }
}
