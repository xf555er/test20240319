package org.apache.fop.layoutmgr.inline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fo.FOText;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontSelector;
import org.apache.fop.fonts.GlyphMapping;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.text.linebreak.LineBreakStatus;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.ListUtil;

public class TextLayoutManager extends LeafNodeLayoutManager {
   private static final int SOFT_HYPHEN_PENALTY = 1;
   private static final Log LOG = LogFactory.getLog(TextLayoutManager.class);
   private final List mappings;
   private static final String BREAK_CHARS = "-/";
   private final FOText foText;
   private final MinOptMax[] letterSpaceAdjustArray;
   private Font spaceFont;
   private int nextStart;
   private int spaceCharIPD;
   private MinOptMax wordSpaceIPD;
   private MinOptMax letterSpaceIPD;
   private int hyphIPD;
   private boolean hasChanged;
   private int[] returnedIndices = new int[]{0, 0};
   private int changeOffset;
   private int thisStart;
   private int tempStart;
   private List changeList = new LinkedList();
   private AlignmentContext alignmentContext;
   private int lineStartBAP;
   private int lineEndBAP;
   private boolean keepTogether;
   private final Position auxiliaryPosition = new LeafPosition(this, -1);
   private FOUserAgent userAgent;

   public TextLayoutManager(FOText node, FOUserAgent userAgent) {
      this.foText = node;
      this.letterSpaceAdjustArray = new MinOptMax[node.length() + 1];
      this.mappings = new ArrayList();
      this.userAgent = userAgent;
   }

   private KnuthPenalty makeZeroWidthPenalty(int penaltyValue) {
      return new KnuthPenalty(0, penaltyValue, false, this.auxiliaryPosition, true);
   }

   private KnuthBox makeAuxiliaryZeroWidthBox() {
      return new KnuthInlineBox(0, (AlignmentContext)null, this.notifyPos(new LeafPosition(this, -1)), true);
   }

   public void initialize() {
      this.foText.resetBuffer();
      this.spaceFont = FontSelector.selectFontForCharacterInText(' ', this.foText, this);
      this.spaceCharIPD = this.spaceFont.getCharWidth(' ');
      this.hyphIPD = this.foText.getCommonHyphenation().getHyphIPD(this.spaceFont);
      SpaceVal letterSpacing = SpaceVal.makeLetterSpacing(this.foText.getLetterSpacing());
      SpaceVal wordSpacing = SpaceVal.makeWordSpacing(this.foText.getWordSpacing(), letterSpacing, this.spaceFont);
      this.letterSpaceIPD = letterSpacing.getSpace();
      this.wordSpaceIPD = MinOptMax.getInstance(this.spaceCharIPD).plus(wordSpacing.getSpace());
      this.keepTogether = this.foText.getKeepTogether().getWithinLine().getEnum() == 7;
   }

   public void addAreas(PositionIterator posIter, LayoutContext context) {
      int wordSpaceCount = 0;
      int letterSpaceCount = 0;
      int firstMappingIndex = -1;
      int lastMappingIndex = 0;
      MinOptMax realWidth = MinOptMax.ZERO;
      GlyphMapping lastMapping = null;

      while(true) {
         LeafPosition tbpNext;
         do {
            do {
               if (!posIter.hasNext()) {
                  if (lastMapping != null) {
                     this.addMappingAreas(lastMapping, wordSpaceCount, letterSpaceCount, firstMappingIndex, lastMappingIndex, realWidth, context);
                  }

                  return;
               }

               Position nextPos = posIter.next();

               assert nextPos instanceof LeafPosition;

               tbpNext = (LeafPosition)nextPos;
            } while(tbpNext == null);
         } while(tbpNext.getLeafPos() == -1);

         GlyphMapping mapping = (GlyphMapping)this.mappings.get(tbpNext.getLeafPos());
         if (lastMapping == null || mapping.font != lastMapping.font || mapping.level != lastMapping.level) {
            if (lastMapping != null) {
               this.addMappingAreas(lastMapping, wordSpaceCount, letterSpaceCount, firstMappingIndex, lastMappingIndex, realWidth, context);
            }

            firstMappingIndex = tbpNext.getLeafPos();
            wordSpaceCount = 0;
            letterSpaceCount = 0;
            realWidth = MinOptMax.ZERO;
         }

         wordSpaceCount += mapping.wordSpaceCount;
         letterSpaceCount += mapping.letterSpaceCount;
         realWidth = realWidth.plus(mapping.areaIPD);
         lastMappingIndex = tbpNext.getLeafPos();
         lastMapping = mapping;
      }
   }

   private void addMappingAreas(GlyphMapping mapping, int wordSpaceCount, int letterSpaceCount, int firstMappingIndex, int lastMappingIndex, MinOptMax realWidth, LayoutContext context) {
      int textLength = mapping.getWordLength();
      if (mapping.letterSpaceCount == textLength && !mapping.isHyphenated && context.isLastArea()) {
         realWidth = realWidth.minus(this.letterSpaceIPD);
         --letterSpaceCount;
      }

      for(int i = mapping.startIndex; i < mapping.endIndex; ++i) {
         MinOptMax letterSpaceAdjustment = this.letterSpaceAdjustArray[i + 1];
         if (letterSpaceAdjustment != null && letterSpaceAdjustment.isElastic()) {
            ++letterSpaceCount;
         }
      }

      if (context.isLastArea() && mapping.isHyphenated) {
         realWidth = realWidth.plus(this.hyphIPD);
      }

      double ipdAdjust = context.getIPDAdjust();
      int difference;
      if (ipdAdjust > 0.0) {
         difference = (int)((double)realWidth.getStretch() * ipdAdjust);
      } else {
         difference = (int)((double)realWidth.getShrink() * ipdAdjust);
      }

      int letterSpaceDim = this.letterSpaceIPD.getOpt();
      if (ipdAdjust > 0.0) {
         letterSpaceDim += (int)((double)this.letterSpaceIPD.getStretch() * ipdAdjust);
      } else {
         letterSpaceDim += (int)((double)this.letterSpaceIPD.getShrink() * ipdAdjust);
      }

      int totalAdjust = (letterSpaceDim - this.letterSpaceIPD.getOpt()) * letterSpaceCount;
      int wordSpaceDim = this.wordSpaceIPD.getOpt();
      if (wordSpaceCount > 0) {
         wordSpaceDim += (difference - totalAdjust) / wordSpaceCount;
      }

      totalAdjust += (wordSpaceDim - this.wordSpaceIPD.getOpt()) * wordSpaceCount;
      if (totalAdjust != difference) {
         LOG.trace("TextLM.addAreas: error in word / letter space adjustment = " + (totalAdjust - difference));
         totalAdjust = difference;
      }

      TextArea textArea = (new TextAreaBuilder(realWidth, totalAdjust, context, firstMappingIndex, lastMappingIndex, context.isLastArea(), mapping.font)).build();
      textArea.setChangeBarList(this.getChangeBarList());
      textArea.setTextLetterSpaceAdjust(letterSpaceDim);
      textArea.setTextWordSpaceAdjust(wordSpaceDim - this.spaceCharIPD - 2 * textArea.getTextLetterSpaceAdjust());
      if (context.getIPDAdjust() != 0.0) {
         textArea.setSpaceDifference(this.wordSpaceIPD.getOpt() - this.spaceCharIPD - 2 * textArea.getTextLetterSpaceAdjust());
      }

      this.parentLayoutManager.addChildArea(textArea);
   }

   private void addGlyphMapping(GlyphMapping mapping) {
      this.addGlyphMapping(this.mappings.size(), mapping);
   }

   private void addGlyphMapping(int index, GlyphMapping mapping) {
      this.mappings.add(index, mapping);
   }

   private void removeGlyphMapping(int index) {
      this.mappings.remove(index);
   }

   private GlyphMapping getGlyphMapping(int index) {
      return (GlyphMapping)this.mappings.get(index);
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      this.lineStartBAP = context.getLineStartBorderAndPaddingWidth();
      this.lineEndBAP = context.getLineEndBorderAndPaddingWidth();
      this.alignmentContext = context.getAlignmentContext();
      List returnList = new LinkedList();
      KnuthSequence sequence = new InlineKnuthSequence();
      GlyphMapping mapping = null;
      GlyphMapping prevMapping = null;
      returnList.add(sequence);
      if (LOG.isDebugEnabled()) {
         LOG.debug("GK: [" + this.nextStart + "," + this.foText.length() + "]");
      }

      LineBreakStatus lineBreakStatus = new LineBreakStatus();
      this.thisStart = this.nextStart;
      boolean inWord = false;
      boolean inWhitespace = false;
      char ch = 0;
      int level = true;
      int prevLevel = -1;

      boolean retainControls;
      for(retainControls = false; this.nextStart < this.foText.length(); ++this.nextStart) {
         ch = this.foText.charAt(this.nextStart);
         int level = this.foText.bidiLevelAt(this.nextStart);
         boolean breakOpportunity = false;
         byte breakAction = this.keepTogether ? 4 : lineBreakStatus.nextChar(ch);
         switch (breakAction) {
            case 0:
            case 1:
            case 2:
               breakOpportunity = true;
            case 3:
            case 4:
            case 5:
               break;
            default:
               LOG.error("Unexpected breakAction: " + breakAction);
         }

         if (LOG.isDebugEnabled()) {
            LOG.debug("GK: { index = " + this.nextStart + ", char = " + CharUtilities.charToNCRef(ch) + ", level = " + level + ", levelPrev = " + prevLevel + ", inWord = " + inWord + ", inSpace = " + inWhitespace + "}");
         }

         if (inWord) {
            if (breakOpportunity || GlyphMapping.isSpace(ch) || CharUtilities.isExplicitBreak(ch) || prevLevel != -1 && level != prevLevel) {
               prevMapping = this.processWord(alignment, (KnuthSequence)sequence, prevMapping, ch, breakOpportunity, true, prevLevel, retainControls);
            }
         } else if (inWhitespace) {
            if (ch != ' ' || breakOpportunity) {
               prevMapping = this.processWhitespace(alignment, (KnuthSequence)sequence, breakOpportunity, prevLevel);
            }
         } else {
            if (mapping != null) {
               prevMapping = mapping;
               this.processLeftoverGlyphMapping(alignment, (KnuthSequence)sequence, mapping, ch == ' ' || breakOpportunity);
               mapping = null;
            }

            if (breakAction == 5) {
               sequence = this.processLinebreak(returnList, (KnuthSequence)sequence);
            }
         }

         Font font;
         MinOptMax areaIPD;
         if ((ch != ' ' || this.foText.getWhitespaceTreatment() != 108) && ch != 160) {
            if (!CharUtilities.isFixedWidthSpace(ch) && !CharUtilities.isZeroWidthSpace(ch)) {
               if (CharUtilities.isExplicitBreak(ch)) {
                  this.thisStart = this.nextStart + 1;
               }
            } else {
               font = FontSelector.selectFontForCharacterInText(ch, this.foText, this);
               areaIPD = MinOptMax.getInstance(font.getCharWidth(ch));
               mapping = new GlyphMapping(this.nextStart, this.nextStart + 1, 0, 0, areaIPD, false, true, breakOpportunity, font, level, (int[][])null);
               this.thisStart = this.nextStart + 1;
            }
         } else {
            font = FontSelector.selectFontForCharacterInText(ch, this.foText, this);
            font.mapChar(ch);
            if (prevMapping != null && prevMapping.isSpace) {
               areaIPD = this.wordSpaceIPD.minus(this.letterSpaceIPD);
            } else {
               areaIPD = this.wordSpaceIPD;
            }

            mapping = new GlyphMapping(this.nextStart, this.nextStart + 1, 1, 0, areaIPD, false, true, breakOpportunity, this.spaceFont, level, (int[][])null);
            this.thisStart = this.nextStart + 1;
         }

         inWord = !GlyphMapping.isSpace(ch) && !CharUtilities.isExplicitBreak(ch);
         inWhitespace = ch == ' ' && this.foText.getWhitespaceTreatment() != 108;
         prevLevel = level;
      }

      if (inWord) {
         this.processWord(alignment, (KnuthSequence)sequence, prevMapping, ch, false, false, prevLevel, retainControls);
      } else if (inWhitespace) {
         this.processWhitespace(alignment, (KnuthSequence)sequence, !this.keepTogether, prevLevel);
      } else if (mapping != null) {
         this.processLeftoverGlyphMapping(alignment, (KnuthSequence)sequence, mapping, ch == 8203);
      } else if (CharUtilities.isExplicitBreak(ch)) {
         this.processLinebreak(returnList, (KnuthSequence)sequence);
      }

      if (((List)ListUtil.getLast(returnList)).isEmpty()) {
         ListUtil.removeLast(returnList);
      }

      this.setFinished(true);
      if (returnList.isEmpty()) {
         return null;
      } else {
         return returnList;
      }
   }

   private KnuthSequence processLinebreak(List returnList, KnuthSequence sequence) {
      if (this.lineEndBAP != 0) {
         sequence.add(new KnuthGlue(this.lineEndBAP, 0, 0, this.auxiliaryPosition, true));
      }

      sequence.endSequence();
      KnuthSequence sequence = new InlineKnuthSequence();
      returnList.add(sequence);
      return sequence;
   }

   private void processLeftoverGlyphMapping(int alignment, KnuthSequence sequence, GlyphMapping mapping, boolean breakOpportunityAfter) {
      this.addGlyphMapping(mapping);
      mapping.breakOppAfter = breakOpportunityAfter;
      this.addElementsForASpace(sequence, alignment, mapping, this.mappings.size() - 1);
   }

   private GlyphMapping processWhitespace(int alignment, KnuthSequence sequence, boolean breakOpportunity, int level) {
      if (LOG.isDebugEnabled()) {
         LOG.debug("PS: [" + this.thisStart + "," + this.nextStart + "]");
      }

      assert this.nextStart >= this.thisStart;

      GlyphMapping mapping = new GlyphMapping(this.thisStart, this.nextStart, this.nextStart - this.thisStart, 0, this.wordSpaceIPD.mult(this.nextStart - this.thisStart), false, true, breakOpportunity, this.spaceFont, level, (int[][])null);
      this.addGlyphMapping(mapping);
      this.addElementsForASpace(sequence, alignment, mapping, this.mappings.size() - 1);
      this.thisStart = this.nextStart;
      return mapping;
   }

   private GlyphMapping processWord(int alignment, KnuthSequence sequence, GlyphMapping prevMapping, char ch, boolean breakOpportunity, boolean checkEndsWithHyphen, int level, boolean retainControls) {
      int lastIndex;
      for(lastIndex = this.nextStart; lastIndex > 0 && this.foText.charAt(lastIndex - 1) == 173; --lastIndex) {
      }

      boolean endsWithHyphen = checkEndsWithHyphen && this.foText.charAt(lastIndex) == 173;
      Font font = FontSelector.selectFontForCharactersInText(this.foText, this.thisStart, lastIndex, this.foText, this);
      char breakOpportunityChar = breakOpportunity ? ch : 0;
      char precedingChar = prevMapping != null && !prevMapping.isSpace && prevMapping.endIndex > 0 ? this.foText.charAt(prevMapping.endIndex - 1) : 0;
      GlyphMapping mapping = GlyphMapping.doGlyphMapping(this.foText, this.thisStart, lastIndex, font, this.letterSpaceIPD, this.letterSpaceAdjustArray, precedingChar, breakOpportunityChar, endsWithHyphen, level, false, false, retainControls);
      this.addGlyphMapping(mapping);
      this.tempStart = this.nextStart;
      this.addElementsForAWordFragment(sequence, alignment, mapping, this.mappings.size() - 1);
      this.thisStart = this.nextStart;
      return mapping;
   }

   public List addALetterSpaceTo(List oldList) {
      return this.addALetterSpaceTo(oldList, 0);
   }

   public List addALetterSpaceTo(List oldList, int depth) {
      ListIterator oldListIterator = oldList.listIterator();
      KnuthElement knuthElement = (KnuthElement)oldListIterator.next();
      Position pos = knuthElement.getPosition();
      Position innerPosition = pos.getPosition(depth);

      assert innerPosition instanceof LeafPosition;

      LeafPosition leafPos = (LeafPosition)innerPosition;
      int index = leafPos.getLeafPos();
      if (index > -1) {
         GlyphMapping mapping = this.getGlyphMapping(index);
         ++mapping.letterSpaceCount;
         mapping.addToAreaIPD(this.letterSpaceIPD);
         if ("-/".indexOf(this.foText.charAt(this.tempStart - 1)) >= 0) {
            oldListIterator = oldList.listIterator(oldList.size());
            oldListIterator.add(new KnuthPenalty(0, 50, true, this.auxiliaryPosition, false));
            oldListIterator.add(new KnuthGlue(this.letterSpaceIPD, this.auxiliaryPosition, false));
         } else if (this.letterSpaceIPD.isStiff()) {
            oldListIterator.set(new KnuthInlineBox(mapping.areaIPD.getOpt(), this.alignmentContext, pos, false));
         } else {
            oldListIterator.next();
            oldListIterator.next();
            oldListIterator.set(new KnuthGlue(this.letterSpaceIPD.mult(mapping.letterSpaceCount), this.auxiliaryPosition, true));
         }
      }

      return oldList;
   }

   public void hyphenate(Position pos, HyphContext hyphContext) {
      int glyphIndex = ((LeafPosition)pos).getLeafPos() + this.changeOffset;
      GlyphMapping mapping = this.getGlyphMapping(glyphIndex);
      int startIndex = mapping.startIndex;
      boolean nothingChanged = true;
      Font font = mapping.font;
      int stopIndex;
      if (mapping.isHyphenated || glyphIndex > 0 && this.getGlyphMapping(glyphIndex - 1).isHyphenated) {
         stopIndex = mapping.endIndex;
         hyphContext.updateOffset(stopIndex - startIndex);
         startIndex = stopIndex;
      }

      for(; startIndex < mapping.endIndex; startIndex = stopIndex) {
         MinOptMax newIPD = MinOptMax.ZERO;
         stopIndex = startIndex + hyphContext.getNextHyphPoint();
         boolean hyphenFollows;
         if (hyphContext.hasMoreHyphPoints() && stopIndex <= mapping.endIndex) {
            hyphenFollows = true;
         } else {
            hyphenFollows = false;
            stopIndex = mapping.endIndex;
         }

         hyphContext.updateOffset(stopIndex - startIndex);

         int letterSpaceCount;
         for(int i = startIndex; i < stopIndex; ++i) {
            letterSpaceCount = Character.codePointAt(this.foText, i);
            i += Character.charCount(letterSpaceCount) - 1;
            newIPD = newIPD.plus(font.getCharWidth(letterSpaceCount));
            if (i < stopIndex) {
               MinOptMax letterSpaceAdjust = this.letterSpaceAdjustArray[i + 1];
               if (i == stopIndex - 1 && hyphenFollows) {
                  letterSpaceAdjust = null;
               }

               if (letterSpaceAdjust != null) {
                  newIPD = newIPD.plus(letterSpaceAdjust);
               }
            }
         }

         boolean isWordEnd = stopIndex == mapping.endIndex && mapping.letterSpaceCount < mapping.getWordLength();
         letterSpaceCount = isWordEnd ? stopIndex - startIndex - 1 : stopIndex - startIndex;

         assert letterSpaceCount >= 0;

         newIPD = newIPD.plus(this.letterSpaceIPD.mult(letterSpaceCount));
         if (!nothingChanged || stopIndex != mapping.endIndex || hyphenFollows) {
            this.changeList.add(new PendingChange(new GlyphMapping(startIndex, stopIndex, 0, letterSpaceCount, newIPD, hyphenFollows, false, false, font, -1, (int[][])null), glyphIndex));
            nothingChanged = false;
         }
      }

      this.hasChanged |= !nothingChanged;
   }

   public boolean applyChanges(List oldList) {
      return this.applyChanges(oldList, 0);
   }

   public boolean applyChanges(List oldList, int depth) {
      this.setFinished(false);
      if (oldList.isEmpty()) {
         return false;
      } else {
         LeafPosition startPos = null;
         LeafPosition endPos = null;
         ListIterator oldListIter = oldList.listIterator();

         Position pos;
         Position innerPosition;
         while(oldListIter.hasNext()) {
            pos = ((KnuthElement)oldListIter.next()).getPosition();
            innerPosition = pos.getPosition(depth);

            assert innerPosition == null || innerPosition instanceof LeafPosition;

            startPos = (LeafPosition)innerPosition;
            if (startPos != null && startPos.getLeafPos() != -1) {
               break;
            }
         }

         oldListIter = oldList.listIterator(oldList.size());

         while(oldListIter.hasPrevious()) {
            pos = ((KnuthElement)oldListIter.previous()).getPosition();
            innerPosition = pos.getPosition(depth);

            assert innerPosition instanceof LeafPosition;

            endPos = (LeafPosition)innerPosition;
            if (endPos != null && endPos.getLeafPos() != -1) {
               break;
            }
         }

         this.returnedIndices[0] = (startPos != null ? startPos.getLeafPos() : -1) + this.changeOffset;
         this.returnedIndices[1] = (endPos != null ? endPos.getLeafPos() : -1) + this.changeOffset;
         int mappingsAdded = 0;
         int mappingsRemoved = 0;
         if (!this.changeList.isEmpty()) {
            int oldIndex = -1;

            int changeIndex;
            PendingChange currChange;
            for(Iterator var11 = this.changeList.iterator(); var11.hasNext(); this.addGlyphMapping(changeIndex, currChange.mapping)) {
               Object aChangeList = var11.next();
               currChange = (PendingChange)aChangeList;
               if (currChange.index == oldIndex) {
                  ++mappingsAdded;
                  changeIndex = currChange.index + mappingsAdded - mappingsRemoved;
               } else {
                  ++mappingsRemoved;
                  ++mappingsAdded;
                  oldIndex = currChange.index;
                  changeIndex = currChange.index + mappingsAdded - mappingsRemoved;
                  this.removeGlyphMapping(changeIndex);
               }
            }

            this.changeList.clear();
         }

         int[] var10000 = this.returnedIndices;
         var10000[1] += mappingsAdded - mappingsRemoved;
         this.changeOffset += mappingsAdded - mappingsRemoved;
         return this.hasChanged;
      }
   }

   public List getChangedKnuthElements(List oldList, int alignment) {
      if (this.isFinished()) {
         return null;
      } else {
         int var10002;
         LinkedList returnList;
         for(returnList = new LinkedList(); this.returnedIndices[0] <= this.returnedIndices[1]; var10002 = this.returnedIndices[0]++) {
            GlyphMapping mapping = this.getGlyphMapping(this.returnedIndices[0]);
            if (mapping.wordSpaceCount == 0) {
               this.addElementsForAWordFragment(returnList, alignment, mapping, this.returnedIndices[0]);
            } else {
               this.addElementsForASpace(returnList, alignment, mapping, this.returnedIndices[0]);
            }
         }

         this.setFinished(this.returnedIndices[0] == this.mappings.size() - 1);
         return returnList;
      }
   }

   public String getWordChars(Position pos) {
      int leafValue = ((LeafPosition)pos).getLeafPos() + this.changeOffset;
      if (leafValue == -1) {
         return "";
      } else {
         GlyphMapping mapping = this.getGlyphMapping(leafValue);
         StringBuffer buffer = new StringBuffer(mapping.getWordLength());

         for(int i = mapping.startIndex; i < mapping.endIndex; ++i) {
            buffer.append(this.foText.charAt(i));
         }

         return buffer.toString();
      }
   }

   private void addElementsForASpace(List baseList, int alignment, GlyphMapping mapping, int leafValue) {
      LeafPosition mainPosition = new LeafPosition(this, leafValue);
      if (!mapping.breakOppAfter) {
         if (alignment == 70) {
            baseList.add(this.makeAuxiliaryZeroWidthBox());
            baseList.add(this.makeZeroWidthPenalty(1000));
            baseList.add(new KnuthGlue(mapping.areaIPD, mainPosition, false));
         } else {
            baseList.add(new KnuthInlineBox(mapping.areaIPD.getOpt(), (AlignmentContext)null, mainPosition, true));
         }
      } else if (this.foText.charAt(mapping.startIndex) == ' ' && this.foText.getWhitespaceTreatment() != 108) {
         baseList.addAll(this.getElementsForBreakingSpace(alignment, mapping, mainPosition, mapping.areaIPD.getOpt(), this.auxiliaryPosition, 0, false));
      } else {
         baseList.addAll(this.getElementsForBreakingSpace(alignment, mapping, this.auxiliaryPosition, 0, mainPosition, mapping.areaIPD.getOpt(), true));
      }

   }

   private List getElementsForBreakingSpace(int alignment, GlyphMapping mapping, Position pos2, int p2WidthOffset, Position pos3, int p3WidthOffset, boolean skipZeroCheck) {
      List elements = new ArrayList();
      switch (alignment) {
         case 23:
            elements.add(new KnuthGlue(this.lineEndBAP, 10008, 0, this.auxiliaryPosition, false));
            elements.add(this.makeZeroWidthPenalty(0));
            elements.add(new KnuthGlue(p2WidthOffset - (this.lineStartBAP + this.lineEndBAP), -20016, 0, pos2, false));
            elements.add(this.makeAuxiliaryZeroWidthBox());
            elements.add(this.makeZeroWidthPenalty(1000));
            elements.add(new KnuthGlue(this.lineStartBAP + p3WidthOffset, 10008, 0, pos3, false));
            break;
         case 39:
         case 135:
            KnuthGlue g;
            if (!skipZeroCheck && this.lineStartBAP == 0 && this.lineEndBAP == 0) {
               g = new KnuthGlue(0, 10008, 0, this.auxiliaryPosition, false);
               elements.add(g);
               elements.add(this.makeZeroWidthPenalty(0));
               g = new KnuthGlue(mapping.areaIPD.getOpt(), -10008, 0, pos2, false);
               elements.add(g);
            } else {
               g = new KnuthGlue(this.lineEndBAP, 10008, 0, this.auxiliaryPosition, false);
               elements.add(g);
               elements.add(this.makeZeroWidthPenalty(0));
               g = new KnuthGlue(p2WidthOffset - (this.lineStartBAP + this.lineEndBAP), -10008, 0, pos2, false);
               elements.add(g);
               elements.add(this.makeAuxiliaryZeroWidthBox());
               elements.add(this.makeZeroWidthPenalty(1000));
               g = new KnuthGlue(this.lineStartBAP + p3WidthOffset, 0, 0, pos3, false);
               elements.add(g);
            }
            break;
         case 70:
            elements.addAll(this.getElementsForJustifiedText(mapping, pos2, p2WidthOffset, pos3, p3WidthOffset, skipZeroCheck, mapping.areaIPD.getShrink()));
            break;
         default:
            elements.addAll(this.getElementsForJustifiedText(mapping, pos2, p2WidthOffset, pos3, p3WidthOffset, skipZeroCheck, 0));
      }

      return elements;
   }

   private List getElementsForJustifiedText(GlyphMapping mapping, Position pos2, int p2WidthOffset, Position pos3, int p3WidthOffset, boolean skipZeroCheck, int shrinkability) {
      int stretchability = mapping.areaIPD.getStretch();
      List elements = new ArrayList();
      if (!skipZeroCheck && this.lineStartBAP == 0 && this.lineEndBAP == 0) {
         elements.add(new KnuthGlue(mapping.areaIPD.getOpt(), stretchability, shrinkability, pos2, false));
      } else {
         elements.add(new KnuthGlue(this.lineEndBAP, 0, 0, this.auxiliaryPosition, false));
         elements.add(this.makeZeroWidthPenalty(0));
         elements.add(new KnuthGlue(p2WidthOffset - (this.lineStartBAP + this.lineEndBAP), stretchability, shrinkability, pos2, false));
         elements.add(this.makeAuxiliaryZeroWidthBox());
         elements.add(this.makeZeroWidthPenalty(1000));
         elements.add(new KnuthGlue(this.lineStartBAP + p3WidthOffset, 0, 0, pos3, false));
      }

      return elements;
   }

   private void addElementsForAWordFragment(List baseList, int alignment, GlyphMapping mapping, int leafValue) {
      LeafPosition mainPosition = new LeafPosition(this, leafValue);
      boolean suppressibleLetterSpace = mapping.breakOppAfter && !mapping.isHyphenated;
      if (this.letterSpaceIPD.isStiff()) {
         baseList.add(new KnuthInlineBox(suppressibleLetterSpace ? mapping.areaIPD.getOpt() - this.letterSpaceIPD.getOpt() : mapping.areaIPD.getOpt(), this.alignmentContext, this.notifyPos(mainPosition), false));
      } else {
         int unsuppressibleLetterSpaces = suppressibleLetterSpace ? mapping.letterSpaceCount - 1 : mapping.letterSpaceCount;
         baseList.add(new KnuthInlineBox(mapping.areaIPD.getOpt() - mapping.letterSpaceCount * this.letterSpaceIPD.getOpt(), this.alignmentContext, this.notifyPos(mainPosition), false));
         baseList.add(this.makeZeroWidthPenalty(1000));
         baseList.add(new KnuthGlue(this.letterSpaceIPD.mult(unsuppressibleLetterSpaces), this.auxiliaryPosition, true));
         baseList.add(this.makeAuxiliaryZeroWidthBox());
      }

      if (mapping.isHyphenated) {
         MinOptMax widthIfNoBreakOccurs = null;
         if (mapping.endIndex < this.foText.length()) {
            widthIfNoBreakOccurs = this.letterSpaceAdjustArray[mapping.endIndex];
         }

         this.addElementsForAHyphen(baseList, alignment, this.hyphIPD, widthIfNoBreakOccurs, mapping.breakOppAfter);
      } else if (suppressibleLetterSpace) {
         this.addElementsForAHyphen(baseList, alignment, 0, this.letterSpaceIPD, true);
      }

   }

   private void addElementsForAHyphen(List baseList, int alignment, int widthIfBreakOccurs, MinOptMax widthIfNoBreakOccurs, boolean unflagged) {
      if (widthIfNoBreakOccurs == null) {
         widthIfNoBreakOccurs = MinOptMax.ZERO;
      }

      switch (alignment) {
         case 23:
            baseList.add(this.makeZeroWidthPenalty(1000));
            baseList.add(new KnuthGlue(this.lineEndBAP, 10008, 0, this.auxiliaryPosition, true));
            baseList.add(new KnuthPenalty(this.hyphIPD, unflagged ? 1 : 50, !unflagged, this.auxiliaryPosition, false));
            baseList.add(new KnuthGlue(-(this.lineEndBAP + this.lineStartBAP), -20016, 0, this.auxiliaryPosition, false));
            baseList.add(this.makeAuxiliaryZeroWidthBox());
            baseList.add(this.makeZeroWidthPenalty(1000));
            baseList.add(new KnuthGlue(this.lineStartBAP, 10008, 0, this.auxiliaryPosition, true));
            break;
         case 39:
         case 135:
            if (this.lineStartBAP == 0 && this.lineEndBAP == 0) {
               baseList.add(this.makeZeroWidthPenalty(1000));
               baseList.add(new KnuthGlue(0, 10008, 0, this.auxiliaryPosition, false));
               baseList.add(new KnuthPenalty(widthIfBreakOccurs, unflagged ? 1 : 50, !unflagged, this.auxiliaryPosition, false));
               baseList.add(new KnuthGlue(widthIfNoBreakOccurs.getOpt(), -10008, 0, this.auxiliaryPosition, false));
            } else {
               baseList.add(this.makeZeroWidthPenalty(1000));
               baseList.add(new KnuthGlue(this.lineEndBAP, 10008, 0, this.auxiliaryPosition, false));
               baseList.add(new KnuthPenalty(widthIfBreakOccurs, unflagged ? 1 : 50, !unflagged, this.auxiliaryPosition, false));
               baseList.add(new KnuthGlue(widthIfNoBreakOccurs.getOpt() - (this.lineStartBAP + this.lineEndBAP), -10008, 0, this.auxiliaryPosition, false));
               baseList.add(this.makeAuxiliaryZeroWidthBox());
               baseList.add(this.makeZeroWidthPenalty(1000));
               baseList.add(new KnuthGlue(this.lineStartBAP, 0, 0, this.auxiliaryPosition, false));
            }
            break;
         default:
            if (this.lineStartBAP == 0 && this.lineEndBAP == 0) {
               baseList.add(new KnuthPenalty(widthIfBreakOccurs, unflagged ? 1 : 50, !unflagged, this.auxiliaryPosition, false));
               if (widthIfNoBreakOccurs.isNonZero()) {
                  baseList.add(new KnuthGlue(widthIfNoBreakOccurs, this.auxiliaryPosition, false));
               }
            } else {
               baseList.add(this.makeZeroWidthPenalty(1000));
               baseList.add(new KnuthGlue(this.lineEndBAP, 0, 0, this.auxiliaryPosition, false));
               baseList.add(new KnuthPenalty(widthIfBreakOccurs, unflagged ? 1 : 50, !unflagged, this.auxiliaryPosition, false));
               if (widthIfNoBreakOccurs.isNonZero()) {
                  baseList.add(new KnuthGlue(widthIfNoBreakOccurs.getOpt() - (this.lineStartBAP + this.lineEndBAP), widthIfNoBreakOccurs.getStretch(), widthIfNoBreakOccurs.getShrink(), this.auxiliaryPosition, false));
               } else {
                  baseList.add(new KnuthGlue(-(this.lineStartBAP + this.lineEndBAP), 0, 0, this.auxiliaryPosition, false));
               }

               baseList.add(this.makeAuxiliaryZeroWidthBox());
               baseList.add(this.makeZeroWidthPenalty(1000));
               baseList.add(new KnuthGlue(this.lineStartBAP, 0, 0, this.auxiliaryPosition, false));
            }
      }

   }

   public List getChangeBarList() {
      return this.foText == null ? null : this.foText.getChangeBarList();
   }

   public String toString() {
      return super.toString() + "{chars = '" + CharUtilities.toNCRefs(this.foText.getCharSequence().toString()) + "', len = " + this.foText.length() + "}";
   }

   private final class TextAreaBuilder {
      private final MinOptMax width;
      private final int adjust;
      private final LayoutContext context;
      private final int firstIndex;
      private final int lastIndex;
      private final boolean isLastArea;
      private final Font font;
      private TextArea textArea;
      private int blockProgressionDimension;
      private GlyphMapping mapping;
      private StringBuffer wordChars;
      private int[] letterSpaceAdjust;
      private int letterSpaceAdjustIndex;
      private int[] wordLevels;
      private int wordLevelsIndex;
      private int wordIPD;
      private int[][] gposAdjustments;
      private int gposAdjustmentsIndex;

      private TextAreaBuilder(MinOptMax width, int adjust, LayoutContext context, int firstIndex, int lastIndex, boolean isLastArea, Font font) {
         this.width = width;
         this.adjust = adjust;
         this.context = context;
         this.firstIndex = firstIndex;
         this.lastIndex = lastIndex;
         this.isLastArea = isLastArea;
         this.font = font;
      }

      private TextArea build() {
         this.createTextArea();
         this.setInlineProgressionDimension();
         this.calcBlockProgressionDimension();
         this.setBlockProgressionDimension();
         this.setBaselineOffset();
         this.setBlockProgressionOffset();
         this.setText();
         TraitSetter.addFontTraits(this.textArea, this.font);
         this.textArea.addTrait(Trait.COLOR, TextLayoutManager.this.foText.getColor());
         TraitSetter.addTextDecoration(this.textArea, TextLayoutManager.this.foText.getTextDecoration());
         if (!this.context.treatAsArtifact()) {
            TraitSetter.addStructureTreeElement(this.textArea, TextLayoutManager.this.foText.getStructureTreeElement());
         }

         return this.textArea;
      }

      private void createTextArea() {
         if (this.context.getIPDAdjust() == 0.0) {
            this.textArea = new TextArea();
         } else {
            this.textArea = new TextArea(this.width.getStretch(), this.width.getShrink(), this.adjust);
         }

         this.textArea.setChangeBarList(TextLayoutManager.this.getChangeBarList());
      }

      private void setInlineProgressionDimension() {
         this.textArea.setIPD(this.width.getOpt() + this.adjust);
      }

      private void calcBlockProgressionDimension() {
         this.blockProgressionDimension = this.font.getAscender() - this.font.getDescender();
      }

      private void setBlockProgressionDimension() {
         this.textArea.setBPD(this.blockProgressionDimension);
      }

      private void setBaselineOffset() {
         this.textArea.setBaselineOffset(this.font.getAscender());
      }

      private void setBlockProgressionOffset() {
         if (this.blockProgressionDimension == TextLayoutManager.this.alignmentContext.getHeight()) {
            this.textArea.setBlockProgressionOffset(0);
         } else {
            this.textArea.setBlockProgressionOffset(TextLayoutManager.this.alignmentContext.getOffset());
         }

      }

      private void setText() {
         int mappingIndex = -1;
         int wordCharLength = 0;

         for(int wordIndex = this.firstIndex; wordIndex <= this.lastIndex; ++wordIndex) {
            this.mapping = TextLayoutManager.this.getGlyphMapping(wordIndex);
            this.textArea.updateLevel(this.mapping.level);
            if (this.mapping.isSpace) {
               this.addSpaces();
            } else {
               if (mappingIndex == -1) {
                  mappingIndex = wordIndex;
                  wordCharLength = 0;
               }

               wordCharLength += this.mapping.getWordLength();
               if (this.isWordEnd(wordIndex)) {
                  this.addWord(mappingIndex, wordIndex, wordCharLength);
                  mappingIndex = -1;
               }
            }
         }

      }

      private boolean isWordEnd(int mappingIndex) {
         return mappingIndex == this.lastIndex || TextLayoutManager.this.getGlyphMapping(mappingIndex + 1).isSpace;
      }

      private void addWord(int startIndex, int endIndex, int wordLength) {
         int blockProgressionOffset = 0;
         boolean gposAdjusted = false;
         if (this.isHyphenated(endIndex)) {
            ++wordLength;
         }

         this.initWord(wordLength);

         for(int i = startIndex; i <= endIndex; ++i) {
            GlyphMapping wordMapping = TextLayoutManager.this.getGlyphMapping(i);
            this.addWordChars(wordMapping);
            this.addLetterAdjust(wordMapping);
            if (this.addGlyphPositionAdjustments(wordMapping)) {
               gposAdjusted = true;
            }
         }

         if (this.isHyphenated(endIndex)) {
            this.addHyphenationChar();
         }

         if (!gposAdjusted) {
            this.gposAdjustments = (int[][])null;
         }

         this.textArea.addWord(this.wordChars.toString(), this.wordIPD, this.letterSpaceAdjust, this.getNonEmptyLevels(), this.gposAdjustments, blockProgressionOffset, this.isWordSpace(endIndex + 1));
      }

      private boolean isWordSpace(int mappingIndex) {
         return TextLayoutManager.this.userAgent.isAccessibilityEnabled() && mappingIndex < TextLayoutManager.this.mappings.size() - 1 && TextLayoutManager.this.getGlyphMapping(mappingIndex).isSpace;
      }

      private int[] getNonEmptyLevels() {
         if (this.wordLevels == null) {
            return null;
         } else {
            assert this.wordLevelsIndex <= this.wordLevels.length;

            boolean empty = true;
            int i = 0;

            for(int n = this.wordLevelsIndex; i < n; ++i) {
               if (this.wordLevels[i] >= 0) {
                  empty = false;
                  break;
               }
            }

            return empty ? null : this.wordLevels;
         }
      }

      private void initWord(int wordLength) {
         this.wordChars = new StringBuffer(wordLength);
         this.letterSpaceAdjust = new int[wordLength];
         this.letterSpaceAdjustIndex = 0;
         this.wordLevels = new int[wordLength];
         this.wordLevelsIndex = 0;
         Arrays.fill(this.wordLevels, -1);
         this.gposAdjustments = new int[wordLength][4];
         this.gposAdjustmentsIndex = 0;
         this.wordIPD = 0;
      }

      private boolean isHyphenated(int endIndex) {
         return this.isLastArea && endIndex == this.lastIndex && this.mapping.isHyphenated;
      }

      private void addHyphenationChar() {
         Character hyphChar = TextLayoutManager.this.foText.getCommonHyphenation().getHyphChar(this.font);
         if (hyphChar != null) {
            this.wordChars.append(hyphChar);
         }

         this.textArea.setHyphenated();
      }

      private void addWordChars(GlyphMapping wordMapping) {
         int s = wordMapping.startIndex;
         int e = wordMapping.endIndex;
         if (wordMapping.mapping != null) {
            this.wordChars.append(wordMapping.mapping);
            this.addWordLevels(this.getMappingBidiLevels(wordMapping));
         } else {
            for(int i = s; i < e; ++i) {
               this.wordChars.append(TextLayoutManager.this.foText.charAt(i));
            }

            this.addWordLevels(TextLayoutManager.this.foText.getBidiLevels(s, e));
         }

         this.wordIPD += wordMapping.areaIPD.getOpt();
      }

      private int[] getMappingBidiLevels(GlyphMapping mapping) {
         if (mapping.mapping == null) {
            return TextLayoutManager.this.foText.getBidiLevels(mapping.startIndex, mapping.endIndex);
         } else {
            int nc = mapping.endIndex - mapping.startIndex;
            int nm = mapping.mapping.length();
            int[] la = TextLayoutManager.this.foText.getBidiLevels(mapping.startIndex, mapping.endIndex);
            if (la == null) {
               return null;
            } else if (nm == nc) {
               return la;
            } else {
               int[] ma;
               if (nm <= nc) {
                  ma = new int[nm];
                  System.arraycopy(la, 0, ma, 0, ma.length);
                  return ma;
               } else {
                  ma = new int[nm];
                  System.arraycopy(la, 0, ma, 0, la.length);
                  int i = la.length;
                  int n = ma.length;

                  for(int l = i > 0 ? la[i - 1] : 0; i < n; ++i) {
                     ma[i] = l;
                  }

                  return ma;
               }
            }
         }
      }

      private void addWordLevels(int[] levels) {
         int numLevels = levels != null ? levels.length : 0;
         if (numLevels > 0) {
            int need = this.wordLevelsIndex + numLevels;
            if (need > this.wordLevels.length) {
               throw new IllegalStateException("word levels array too short: expect at least " + need + " entries, but has only " + this.wordLevels.length + " entries");
            }

            System.arraycopy(levels, 0, this.wordLevels, this.wordLevelsIndex, numLevels);
         }

         this.wordLevelsIndex += numLevels;
      }

      private void addLetterAdjust(GlyphMapping wordMapping) {
         int letterSpaceCount = wordMapping.letterSpaceCount;
         int wordLength = wordMapping.getWordLength();
         int taAdjust = this.textArea.getTextLetterSpaceAdjust();
         int i = 0;

         for(int n = wordLength; i < n; ++i) {
            int j = this.letterSpaceAdjustIndex + i;
            if (j > 0) {
               int k = wordMapping.startIndex + i;
               MinOptMax adj = k < TextLayoutManager.this.letterSpaceAdjustArray.length ? TextLayoutManager.this.letterSpaceAdjustArray[k] : null;
               this.letterSpaceAdjust[j] = adj == null ? 0 : adj.getOpt();
            }

            if (letterSpaceCount > 0) {
               int[] var10000 = this.letterSpaceAdjust;
               var10000[j] += taAdjust;
               --letterSpaceCount;
            }
         }

         this.letterSpaceAdjustIndex += wordLength;
      }

      private boolean addGlyphPositionAdjustments(GlyphMapping wordMapping) {
         boolean adjusted = false;
         int[][] gpa = wordMapping.gposAdjustments;
         int numAdjusts = gpa != null ? gpa.length : 0;
         int wordLength = wordMapping.getWordLength();
         if (numAdjusts > 0) {
            int need = this.gposAdjustmentsIndex + numAdjusts;
            if (need > this.gposAdjustments.length) {
               throw new IllegalStateException("gpos adjustments array too short: expect at least " + need + " entries, but has only " + this.gposAdjustments.length + " entries");
            }

            int i = 0;
            int n = wordLength;

            for(int j = 0; i < n; ++i) {
               if (i < numAdjusts) {
                  int[] wpa1 = this.gposAdjustments[this.gposAdjustmentsIndex + i];
                  int[] wpa2 = gpa[j++];

                  for(int k = 0; k < 4; ++k) {
                     int a = wpa2[k];
                     if (a != 0) {
                        wpa1[k] += a;
                        adjusted = true;
                     }
                  }
               }
            }
         }

         this.gposAdjustmentsIndex += wordLength;
         return adjusted;
      }

      private void addSpaces() {
         int blockProgressionOffset = 0;
         int numZeroWidthSpaces = 0;

         int numSpaces;
         int spaceIPD;
         for(numSpaces = this.mapping.startIndex; numSpaces < this.mapping.endIndex; ++numSpaces) {
            spaceIPD = TextLayoutManager.this.foText.charAt(numSpaces);
            if (CharUtilities.isZeroWidthSpace(spaceIPD)) {
               ++numZeroWidthSpaces;
            }
         }

         numSpaces = this.mapping.endIndex - this.mapping.startIndex - numZeroWidthSpaces;
         spaceIPD = this.mapping.areaIPD.getOpt() / (numSpaces > 0 ? numSpaces : 1);

         for(int i = this.mapping.startIndex; i < this.mapping.endIndex; ++i) {
            char spaceChar = TextLayoutManager.this.foText.charAt(i);
            int level = TextLayoutManager.this.foText.bidiLevelAt(i);
            if (!CharUtilities.isZeroWidthSpace(spaceChar)) {
               this.textArea.addSpace(spaceChar, spaceIPD, CharUtilities.isAdjustableSpace(spaceChar), blockProgressionOffset, level);
            }
         }

      }

      // $FF: synthetic method
      TextAreaBuilder(MinOptMax x1, int x2, LayoutContext x3, int x4, int x5, boolean x6, Font x7, Object x8) {
         this(x1, x2, x3, x4, x5, x6, x7);
      }
   }

   private final class PendingChange {
      private final GlyphMapping mapping;
      private final int index;

      private PendingChange(GlyphMapping mapping, int index) {
         this.mapping = mapping;
         this.index = index;
      }

      // $FF: synthetic method
      PendingChange(GlyphMapping x1, int x2, Object x3) {
         this(x1, x2);
      }
   }
}
