package org.apache.fop.afp;

public class AFPObjectAreaInfo {
   private final int x;
   private final int y;
   private final int width;
   private final int height;
   private int widthRes;
   private int heightRes;
   private final int rotation;

   public AFPObjectAreaInfo(int x, int y, int width, int height, int resolution, int rotation) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.rotation = rotation;
      this.widthRes = resolution;
      this.heightRes = resolution;
   }

   public void setResolution(int resolution) {
      this.widthRes = resolution;
      this.heightRes = resolution;
   }

   public void setWidthRes(int resolution) {
      this.widthRes = resolution;
   }

   public void setHeightRes(int resolution) {
      this.heightRes = resolution;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidthRes() {
      return this.widthRes;
   }

   public int getHeightRes() {
      return this.heightRes;
   }

   public int getRotation() {
      return this.rotation;
   }

   public String toString() {
      return "x=" + this.x + ", y=" + this.y + ", width=" + this.width + ", height=" + this.height + ", widthRes=" + this.widthRes + ", heigtRes=" + this.heightRes + ", rotation=" + this.rotation;
   }
}
