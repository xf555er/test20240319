package org.apache.fop.layoutmgr.inline;

import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.fo.flow.AbstractPageNumberCitation;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

public abstract class AbstractPageNumberCitationLayoutManager extends LeafNodeLayoutManager {
   protected AbstractPageNumberCitation citation;
   protected Font font;
   private boolean resolved;
   private String citationString;

   public AbstractPageNumberCitationLayoutManager(AbstractPageNumberCitation node) {
      super(node);
      this.citation = node;
   }

   public void initialize() {
      FontInfo fi = this.citation.getFOEventHandler().getFontInfo();
      FontTriplet[] fontkeys = this.citation.getCommonFont().getFontState(fi);
      this.font = fi.getFontInstance(fontkeys[0], this.citation.getCommonFont().fontSize.getValue(this));
      this.setCommonBorderPaddingBackground(this.citation.getCommonBorderPaddingBackground());
   }

   protected AlignmentContext makeAlignmentContext(LayoutContext context) {
      return new AlignmentContext(this.font, this.citation.getLineHeight().getOptimum(this).getLength().getValue(this), this.citation.getAlignmentAdjust(), this.citation.getAlignmentBaseline(), this.citation.getBaselineShift(), this.citation.getDominantBaseline(), context.getAlignmentContext());
   }

   protected MinOptMax getAllocationIPD(int refIPD) {
      this.determineCitationString();
      int ipd = this.getStringWidth(this.citationString);
      return MinOptMax.getInstance(ipd);
   }

   private void determineCitationString() {
      assert this.citationString == null;

      PageViewport page = this.getCitedPage();
      if (page != null) {
         this.resolved = true;
         this.citationString = page.getPageNumberString();
      } else {
         this.resolved = false;
         this.citationString = "MMM";
      }

   }

   private int getStringWidth(String str) {
      int width = 0;

      for(int count = 0; count < str.length(); ++count) {
         width += this.font.getCharWidth(str.charAt(count));
      }

      return width;
   }

   protected abstract PageViewport getCitedPage();

   protected InlineArea getEffectiveArea(LayoutContext layoutContext) {
      InlineArea area = this.getPageNumberCitationArea();
      if (!layoutContext.treatAsArtifact()) {
         TraitSetter.addStructureTreeElement(area, this.citation.getStructureTreeElement());
      }

      return area;
   }

   private InlineArea getPageNumberCitationArea() {
      Object text;
      if (this.resolved) {
         text = new TextArea();
         int bidiLevel = this.getBidiLevel();
         ((TextArea)text).setBidiLevel(bidiLevel);
         ((TextArea)text).addWord(this.citationString, this.getStringWidth(this.citationString), (int[])null, (int[])null, (int[][])null, 0);
      } else {
         UnresolvedPageNumber unresolved = new UnresolvedPageNumber(this.citation.getRefId(), this.font, this.getReferenceType());
         this.getPSLM().addUnresolvedArea(this.citation.getRefId(), unresolved);
         text = unresolved;
      }

      ((TextArea)text).setChangeBarList(this.getChangeBarList());
      this.setTraits((TextArea)text);
      return (InlineArea)text;
   }

   protected abstract boolean getReferenceType();

   private void setTraits(TextArea text) {
      TraitSetter.setProducerID(text, this.citation.getId());
      int bidiLevel = this.getBidiLevel();
      text.setBidiLevel(bidiLevel);
      int width = this.getStringWidth(this.citationString);
      text.setIPD(width);
      text.setBPD(this.font.getAscender() - this.font.getDescender());
      text.setBaselineOffset(this.font.getAscender());
      TraitSetter.addFontTraits(text, this.font);
      text.addTrait(Trait.COLOR, this.citation.getColor());
      TraitSetter.addTextDecoration(text, this.citation.getTextDecoration());
   }

   protected int getBidiLevel() {
      return this.citation.getBidiLevel();
   }
}
