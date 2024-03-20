package org.apache.fop.layoutmgr.inline;

import org.apache.fop.layoutmgr.FloatContentLayoutManager;
import org.apache.fop.layoutmgr.FootnoteBodyLayoutManager;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.Position;

public class KnuthInlineBox extends KnuthBox {
   private FootnoteBodyLayoutManager footnoteBodyLM;
   private AlignmentContext alignmentContext;
   private FloatContentLayoutManager floatContentLM;

   public KnuthInlineBox(int width, AlignmentContext alignmentContext, Position pos, boolean auxiliary) {
      super(width, pos, auxiliary);
      this.alignmentContext = alignmentContext;
   }

   public AlignmentContext getAlignmentContext() {
      return this.alignmentContext;
   }

   public void setFootnoteBodyLM(FootnoteBodyLayoutManager fblm) {
      this.footnoteBodyLM = fblm;
   }

   public FootnoteBodyLayoutManager getFootnoteBodyLM() {
      return this.footnoteBodyLM;
   }

   public boolean isAnchor() {
      return this.footnoteBodyLM != null;
   }

   public void setFloatContentLM(FloatContentLayoutManager fclm) {
      this.floatContentLM = fclm;
   }

   public FloatContentLayoutManager getFloatContentLM() {
      return this.floatContentLM;
   }

   public boolean isFloatAnchor() {
      return this.floatContentLM != null;
   }
}
