package org.apache.fop.layoutmgr.inline;

import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.ResolvedPageNumber;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

public class PageNumberLayoutManager extends LeafNodeLayoutManager {
   private PageNumber fobj;
   private Font font;

   public PageNumberLayoutManager(PageNumber node) {
      super(node);
      this.fobj = node;
   }

   public void initialize() {
      FontInfo fi = this.fobj.getFOEventHandler().getFontInfo();
      FontTriplet[] fontkeys = this.fobj.getCommonFont().getFontState(fi);
      this.font = fi.getFontInstance(fontkeys[0], this.fobj.getCommonFont().fontSize.getValue(this));
      this.setCommonBorderPaddingBackground(this.fobj.getCommonBorderPaddingBackground());
   }

   protected AlignmentContext makeAlignmentContext(LayoutContext context) {
      return new AlignmentContext(this.font, this.fobj.getLineHeight().getOptimum(this).getLength().getValue(this), this.fobj.getAlignmentAdjust(), this.fobj.getAlignmentBaseline(), this.fobj.getBaselineShift(), this.fobj.getDominantBaseline(), context.getAlignmentContext());
   }

   public InlineArea get(LayoutContext context) {
      ResolvedPageNumber pn = new ResolvedPageNumber();
      String str = this.getCurrentPV().getPageNumberString();
      int width = this.getStringWidth(str);
      int level = this.getBidiLevel();
      pn.addWord(str, 0, level);
      pn.setBidiLevel(level);
      pn.setIPD(width);
      pn.setBPD(this.font.getAscender() - this.font.getDescender());
      pn.setBaselineOffset(this.font.getAscender());
      TraitSetter.addFontTraits(pn, this.font);
      pn.addTrait(Trait.COLOR, this.fobj.getColor());
      TraitSetter.addTextDecoration(pn, this.fobj.getTextDecoration());
      return pn;
   }

   protected InlineArea getEffectiveArea(LayoutContext layoutContext) {
      ResolvedPageNumber baseArea = (ResolvedPageNumber)this.curArea;
      ResolvedPageNumber pn = new ResolvedPageNumber();
      TraitSetter.setProducerID(pn, this.fobj.getId());
      pn.setIPD(baseArea.getIPD());
      pn.setBPD(baseArea.getBPD());
      pn.setBlockProgressionOffset(baseArea.getBlockProgressionOffset());
      pn.setBaselineOffset(baseArea.getBaselineOffset());
      pn.addTrait(Trait.COLOR, this.fobj.getColor());
      pn.getTraits().putAll(baseArea.getTraits());
      if (!layoutContext.treatAsArtifact()) {
         TraitSetter.addStructureTreeElement(pn, this.fobj.getStructureTreeElement());
      }

      this.updateContent(pn);
      return pn;
   }

   private void updateContent(ResolvedPageNumber pn) {
      pn.removeText();
      pn.addWord(this.getCurrentPV().getPageNumberString(), 0, this.getBidiLevel());
      pn.handleIPDVariation(this.getStringWidth(pn.getText()) - pn.getIPD());
      this.areaInfo.ipdArea = MinOptMax.getInstance(pn.getIPD());
   }

   private int getStringWidth(String str) {
      int width = 0;

      for(int count = 0; count < str.length(); ++count) {
         width += this.font.getCharWidth(str.charAt(count));
      }

      return width;
   }

   protected int getBidiLevel() {
      return this.fobj.getBidiLevel();
   }
}
