package org.apache.batik.gvt.flow;

public class MarginInfo {
   public static final int ALIGN_START = 0;
   public static final int ALIGN_MIDDLE = 1;
   public static final int ALIGN_END = 2;
   public static final int ALIGN_FULL = 3;
   protected float top;
   protected float right;
   protected float bottom;
   protected float left;
   protected float indent;
   protected int alignment;
   protected float lineHeight;
   protected boolean fontSizeRelative;
   protected boolean flowRegionBreak;

   public MarginInfo(float top, float right, float bottom, float left, float indent, int alignment, float lineHeight, boolean fontSizeRelative, boolean flowRegionBreak) {
      this.top = top;
      this.right = right;
      this.bottom = bottom;
      this.left = left;
      this.indent = indent;
      this.alignment = alignment;
      this.lineHeight = lineHeight;
      this.fontSizeRelative = fontSizeRelative;
      this.flowRegionBreak = flowRegionBreak;
   }

   public MarginInfo(float margin, int alignment) {
      this.setMargin(margin);
      this.indent = 0.0F;
      this.alignment = alignment;
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

   public int getTextAlignment() {
      return this.alignment;
   }

   public float getLineHeight() {
      return this.lineHeight;
   }

   public boolean isFontSizeRelative() {
      return this.fontSizeRelative;
   }

   public boolean isFlowRegionBreak() {
      return this.flowRegionBreak;
   }
}
