package org.apache.batik.gvt.flow;

import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTLineMetrics;

public class BlockInfo {
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
   protected List fontList;
   protected Map fontAttrs;
   protected float ascent = -1.0F;
   protected float descent = -1.0F;
   protected boolean flowRegionBreak;

   public BlockInfo(float top, float right, float bottom, float left, float indent, int alignment, float lineHeight, List fontList, Map fontAttrs, boolean flowRegionBreak) {
      this.top = top;
      this.right = right;
      this.bottom = bottom;
      this.left = left;
      this.indent = indent;
      this.alignment = alignment;
      this.lineHeight = lineHeight;
      this.fontList = fontList;
      this.fontAttrs = fontAttrs;
      this.flowRegionBreak = flowRegionBreak;
   }

   public BlockInfo(float margin, int alignment) {
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

   public void initLineInfo(FontRenderContext frc) {
      float fontSize = 12.0F;
      Float fsFloat = (Float)this.fontAttrs.get(TextAttribute.SIZE);
      if (fsFloat != null) {
         fontSize = fsFloat;
      }

      Iterator var4 = this.fontList.iterator();
      if (var4.hasNext()) {
         Object aFontList = var4.next();
         GVTFont font = (GVTFont)aFontList;
         GVTLineMetrics lm = font.getLineMetrics("", frc);
         this.ascent = lm.getAscent();
         this.descent = lm.getDescent();
      }

      if (this.ascent == -1.0F) {
         this.ascent = fontSize * 0.8F;
         this.descent = fontSize * 0.2F;
      }

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

   public List getFontList() {
      return this.fontList;
   }

   public Map getFontAttrs() {
      return this.fontAttrs;
   }

   public float getAscent() {
      return this.ascent;
   }

   public float getDescent() {
      return this.descent;
   }

   public boolean isFlowRegionBreak() {
      return this.flowRegionBreak;
   }
}
