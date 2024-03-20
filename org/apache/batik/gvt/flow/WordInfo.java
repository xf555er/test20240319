package org.apache.batik.gvt.flow;

import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTLineMetrics;

public class WordInfo {
   int index = -1;
   float ascent = -1.0F;
   float descent = -1.0F;
   float lineHeight = -1.0F;
   GlyphGroupInfo[] glyphGroups = null;
   Object flowLine = null;

   public WordInfo(int index) {
      this.index = index;
   }

   WordInfo(int index, float ascent, float descent, float lineHeight, GlyphGroupInfo[] glyphGroups) {
      this.index = index;
      this.ascent = ascent;
      this.descent = descent;
      this.lineHeight = lineHeight;
      this.glyphGroups = glyphGroups;
   }

   public int getIndex() {
      return this.index;
   }

   public float getAscent() {
      return this.ascent;
   }

   public void setAscent(float ascent) {
      this.ascent = ascent;
   }

   public float getDescent() {
      return this.descent;
   }

   public void setDescent(float descent) {
      this.descent = descent;
   }

   public void addLineMetrics(GVTFont font, GVTLineMetrics lm) {
      if (this.ascent < lm.getAscent()) {
         this.ascent = lm.getAscent();
      }

      if (this.descent < lm.getDescent()) {
         this.descent = lm.getDescent();
      }

   }

   public float getLineHeight() {
      return this.lineHeight;
   }

   public void setLineHeight(float lineHeight) {
      this.lineHeight = lineHeight;
   }

   public void addLineHeight(float lineHeight) {
      if (this.lineHeight < lineHeight) {
         this.lineHeight = lineHeight;
      }

   }

   public Object getFlowLine() {
      return this.flowLine;
   }

   public void setFlowLine(Object fl) {
      this.flowLine = fl;
   }

   public int getNumGlyphGroups() {
      return this.glyphGroups == null ? -1 : this.glyphGroups.length;
   }

   public void setGlyphGroups(GlyphGroupInfo[] glyphGroups) {
      this.glyphGroups = glyphGroups;
   }

   public GlyphGroupInfo getGlyphGroup(int idx) {
      return this.glyphGroups == null ? null : this.glyphGroups[idx];
   }
}
