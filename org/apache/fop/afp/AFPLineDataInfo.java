package org.apache.fop.afp;

import java.awt.Color;

public class AFPLineDataInfo {
   private int x1;
   private int y1;
   private int x2;
   private int y2;
   private int thickness;
   private Color color;
   private int rotation;

   public AFPLineDataInfo() {
   }

   public AFPLineDataInfo(AFPLineDataInfo template) {
      this.x1 = template.x1;
      this.y1 = template.y1;
      this.x2 = template.x2;
      this.y2 = template.y2;
      this.thickness = template.thickness;
      this.color = template.color;
      this.rotation = template.rotation;
   }

   public int getX1() {
      return this.x1;
   }

   public void setX1(int x1) {
      this.x1 = x1;
   }

   public int getY1() {
      return this.y1;
   }

   public void setY1(int y1) {
      this.y1 = y1;
   }

   public int getX2() {
      return this.x2;
   }

   public void setX2(int x2) {
      this.x2 = x2;
   }

   public int getY2() {
      return this.y2;
   }

   public void setY2(int y2) {
      this.y2 = y2;
   }

   public int getThickness() {
      return this.thickness;
   }

   public void setThickness(int thickness) {
      this.thickness = thickness;
   }

   public Color getColor() {
      return this.color;
   }

   public void setColor(Color color) {
      this.color = color;
   }

   public int getRotation() {
      return this.rotation;
   }

   public void setRotation(int rotation) {
      this.rotation = rotation;
   }

   public String toString() {
      return "AFPLineDataInfo{x1=" + this.x1 + ", y1=" + this.y1 + ", x2=" + this.x2 + ", y2=" + this.y2 + ", thickness=" + this.thickness + ", color=" + this.color + ", rotation=" + this.rotation + "}";
   }
}
