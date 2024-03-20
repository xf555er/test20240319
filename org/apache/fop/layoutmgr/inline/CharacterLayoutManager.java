package org.apache.fop.layoutmgr.inline;

import java.util.LinkedList;
import java.util.List;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontSelector;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.util.CharUtilities;

public class CharacterLayoutManager extends LeafNodeLayoutManager {
   private MinOptMax letterSpaceIPD;
   private int hyphIPD;
   private Font font;
   private CommonBorderPaddingBackground borderProps;

   public CharacterLayoutManager(Character node) {
      super(node);
   }

   public void initialize() {
      Character fobj = (Character)this.fobj;
      this.font = FontSelector.selectFontForCharacter(fobj, this);
      SpaceVal ls = SpaceVal.makeLetterSpacing(fobj.getLetterSpacing());
      this.letterSpaceIPD = ls.getSpace();
      this.hyphIPD = fobj.getCommonHyphenation().getHyphIPD(this.font);
      this.borderProps = fobj.getCommonBorderPaddingBackground();
      this.setCommonBorderPaddingBackground(this.borderProps);
   }

   private TextArea createCharacterArea() {
      Character fobj = (Character)this.fobj;
      TextArea text = new TextArea();
      text.setChangeBarList(this.getChangeBarList());
      char ch = fobj.getCharacter();
      int ipd = this.font.getCharWidth(ch);
      int blockProgressionOffset = 0;
      int level = fobj.getBidiLevel();
      if (CharUtilities.isAnySpace(ch)) {
         if (!CharUtilities.isZeroWidthSpace(ch)) {
            text.addSpace(ch, ipd, CharUtilities.isAdjustableSpace(ch), blockProgressionOffset, level);
         }
      } else {
         int[] levels = level >= 0 ? new int[]{level} : null;
         text.addWord(String.valueOf(ch), ipd, (int[])null, levels, (int[][])null, blockProgressionOffset);
      }

      TraitSetter.setProducerID(text, fobj.getId());
      TraitSetter.addTextDecoration(text, fobj.getTextDecoration());
      text.setIPD(this.font.getCharWidth(fobj.getCharacter()));
      text.setBPD(this.font.getAscender() - this.font.getDescender());
      text.setBaselineOffset(this.font.getAscender());
      TraitSetter.addFontTraits(text, this.font);
      text.addTrait(Trait.COLOR, fobj.getColor());
      return text;
   }

   protected InlineArea getEffectiveArea(LayoutContext layoutContext) {
      InlineArea area = this.createCharacterArea();
      if (!layoutContext.treatAsArtifact()) {
         TraitSetter.addStructureTreeElement(area, ((Character)this.fobj).getStructureTreeElement());
      }

      return area;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      Character fobj = (Character)this.fobj;
      this.alignmentContext = new AlignmentContext(this.font, this.font.getFontSize(), fobj.getAlignmentAdjust(), fobj.getAlignmentBaseline(), fobj.getBaselineShift(), fobj.getDominantBaseline(), context.getAlignmentContext());
      KnuthSequence seq = new InlineKnuthSequence();
      this.addKnuthElementsForBorderPaddingStart(seq);
      MinOptMax ipd = MinOptMax.getInstance(this.font.getCharWidth(fobj.getCharacter()));
      this.areaInfo = new LeafNodeLayoutManager.AreaInfo((short)0, ipd, false, this.alignmentContext);
      if (this.letterSpaceIPD.isStiff()) {
         seq.add(new KnuthInlineBox(this.areaInfo.ipdArea.getOpt(), this.areaInfo.alignmentContext, this.notifyPos(new LeafPosition(this, 0)), false));
      } else {
         seq.add(new KnuthInlineBox(this.areaInfo.ipdArea.getOpt(), this.areaInfo.alignmentContext, this.notifyPos(new LeafPosition(this, 0)), false));
         seq.add(new KnuthPenalty(0, 1000, false, new LeafPosition(this, -1), true));
         seq.add(new KnuthGlue(0, 0, 0, new LeafPosition(this, -1), true));
         seq.add(new KnuthInlineBox(0, (AlignmentContext)null, this.notifyPos(new LeafPosition(this, -1)), true));
      }

      this.addKnuthElementsForBorderPaddingEnd(seq);
      LinkedList returnList = new LinkedList();
      returnList.add(seq);
      this.setFinished(true);
      return returnList;
   }

   public String getWordChars(Position pos) {
      return String.valueOf(((Character)this.fobj).getCharacter());
   }

   public void hyphenate(Position pos, HyphContext hc) {
      if (hc.getNextHyphPoint() == 1) {
         this.areaInfo.isHyphenated = true;
         this.somethingChanged = true;
      }

      hc.updateOffset(1);
   }

   public boolean applyChanges(List oldList) {
      this.setFinished(false);
      return this.somethingChanged;
   }

   public List getChangedKnuthElements(List oldList, int alignment) {
      if (this.isFinished()) {
         return null;
      } else {
         LinkedList returnList = new LinkedList();
         this.addKnuthElementsForBorderPaddingStart(returnList);
         if (!this.letterSpaceIPD.isStiff() && this.areaInfo.letterSpaces != 0) {
            returnList.add(new KnuthInlineBox(this.areaInfo.ipdArea.getOpt() - this.areaInfo.letterSpaces * this.letterSpaceIPD.getOpt(), this.areaInfo.alignmentContext, this.notifyPos(new LeafPosition(this, 0)), false));
            returnList.add(new KnuthPenalty(0, 1000, false, new LeafPosition(this, -1), true));
            returnList.add(new KnuthGlue(this.letterSpaceIPD.mult(this.areaInfo.letterSpaces), new LeafPosition(this, -1), true));
            returnList.add(new KnuthInlineBox(0, (AlignmentContext)null, this.notifyPos(new LeafPosition(this, -1)), true));
            if (this.areaInfo.isHyphenated) {
               returnList.add(new KnuthPenalty(this.hyphIPD, 50, true, new LeafPosition(this, -1), false));
            }
         } else {
            returnList.add(new KnuthInlineBox(this.areaInfo.ipdArea.getOpt(), this.areaInfo.alignmentContext, this.notifyPos(new LeafPosition(this, 0)), false));
            if (this.areaInfo.isHyphenated) {
               returnList.add(new KnuthPenalty(this.hyphIPD, 50, true, new LeafPosition(this, -1), false));
            }
         }

         this.addKnuthElementsForBorderPaddingEnd(returnList);
         this.setFinished(true);
         return returnList;
      }
   }
}
