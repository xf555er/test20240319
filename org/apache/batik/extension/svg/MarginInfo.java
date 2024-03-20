package org.apache.batik.extension.svg;

public class MarginInfo {
   public static final int JUSTIFY_START = 0;
   public static final int JUSTIFY_MIDDLE = 1;
   public static final int JUSTIFY_END = 2;
   public static final int JUSTIFY_FULL = 3;
   protected float top;
   protected float right;
   protected float bottom;
   protected float left;
   protected float indent;
   protected int justification;
   protected boolean flowRegionBreak;

   public MarginInfo(float top, float right, float bottom, float left, float indent, int justification, boolean flowRegionBreak) {
      this.top = top;
      this.right = right;
      this.bottom = bottom;
      this.left = left;
      this.indent = indent;
      this.justification = justification;
      this.flowRegionBreak = flowRegionBreak;
   }

   public MarginInfo(float margin, int justification) {
      this.setMargin(margin);
      this.indent = 0.0F;
      this.justification = justification;
      this.flowRegionBreak = false;
   }

   public void setMargin(float margin) {
      this.top = margin;
      this.right = margin;
      this.bottom = margin;
      this.left = margin;
   }

   public float getTopMargin() {
      return this.top;
   }

   public float getRightMargin() {
      return this.right;
   }

   public float getBottomMargin() {
      return this.bottom;
   }

   public float getLeftMargin() {
      return this.left;
   }

   public float getIndent() {
      return this.indent;
   }

   public int getJustification() {
      return this.justification;
   }

   public boolean isFlowRegionBreak() {
      return this.flowRegionBreak;
   }
}
