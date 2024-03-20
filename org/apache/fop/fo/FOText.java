package org.apache.fop.fo;

import java.awt.Color;
import java.nio.CharBuffer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.NoSuchElementException;
import java.util.Stack;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOPException;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.fonts.TextFragment;
import org.xml.sax.Locator;

public class FOText extends FONode implements CharSequence, TextFragment {
   private CharBuffer charBuffer;
   private CharacterIterator charIterator;
   private CommonFont commonFont;
   private CommonHyphenation commonHyphenation;
   private Color color;
   private KeepProperty keepTogether;
   private Property letterSpacing;
   private SpaceProperty lineHeight;
   private int whiteSpaceTreatment;
   private int whiteSpaceCollapse;
   private int textTransform;
   private Property wordSpacing;
   private int wrapOption;
   private Length baselineShift;
   private String country;
   private String language;
   private String script;
   private FOText prevFOTextThisBlock;
   private FOText nextFOTextThisBlock;
   private Block ancestorBlock;
   private CommonTextDecoration textDecoration;
   private StructureTreeElement structureTreeElement;
   private int[] bidiLevels;
   private static final int IS_WORD_CHAR_FALSE = 0;
   private static final int IS_WORD_CHAR_TRUE = 1;
   private static final int IS_WORD_CHAR_MAYBE = 2;

   public FOText(FONode parent) {
      super(parent);
      PageSequence pageSequence = this.getRoot().getLastPageSequence();
      if (pageSequence != null && pageSequence.hasChangeBars()) {
         this.nodeChangeBarList = this.getRoot().getLastPageSequence().getClonedChangeBarList();
      }

   }

   protected void characters(char[] data, int start, int length, PropertyList list, Locator locator) throws FOPException {
      int requires;
      if (this.charBuffer == null) {
         requires = length < 16 ? 16 : length;
         this.charBuffer = CharBuffer.allocate(requires);
      } else {
         requires = this.charBuffer.position() + length;
         int capacity = this.charBuffer.capacity();
         if (requires > capacity) {
            int newCapacity = capacity * 2;
            if (requires > newCapacity) {
               newCapacity = requires;
            }

            CharBuffer newBuffer = CharBuffer.allocate(newCapacity);
            this.charBuffer.rewind();
            newBuffer.put(this.charBuffer);
            this.charBuffer = newBuffer;
         }
      }

      this.charBuffer.limit(this.charBuffer.capacity());
      this.charBuffer.put(data, start, length);
      this.charBuffer.limit(this.charBuffer.position());
   }

   public CharSequence getCharSequence() {
      if (this.charBuffer == null) {
         return null;
      } else {
         this.charBuffer.rewind();
         return this.charBuffer.asReadOnlyBuffer().subSequence(0, this.charBuffer.limit());
      }
   }

   public FONode clone(FONode parent, boolean removeChildren) throws FOPException {
      FOText ft = (FOText)super.clone(parent, removeChildren);
      if (removeChildren && this.charBuffer != null) {
         ft.charBuffer = CharBuffer.allocate(this.charBuffer.limit());
         this.charBuffer.rewind();
         ft.charBuffer.put(this.charBuffer);
         ft.charBuffer.rewind();
      }

      ft.prevFOTextThisBlock = null;
      ft.nextFOTextThisBlock = null;
      ft.ancestorBlock = null;
      return ft;
   }

   public void bind(PropertyList pList) throws FOPException {
      this.commonFont = pList.getFontProps();
      this.commonHyphenation = pList.getHyphenationProps();
      this.color = pList.get(72).getColor(this.getUserAgent());
      this.keepTogether = pList.get(131).getKeep();
      this.lineHeight = pList.get(144).getSpace();
      this.letterSpacing = pList.get(141);
      this.whiteSpaceCollapse = pList.get(261).getEnum();
      this.whiteSpaceTreatment = pList.get(262).getEnum();
      this.textTransform = pList.get(252).getEnum();
      this.wordSpacing = pList.get(265);
      this.wrapOption = pList.get(266).getEnum();
      this.textDecoration = pList.getTextDecorationProps();
      this.baselineShift = pList.get(15).getLength();
      this.country = pList.get(81).getString();
      this.language = pList.get(134).getString();
      this.script = pList.get(218).getString();
   }

   public void endOfNode() throws FOPException {
      if (this.charBuffer != null) {
         this.charBuffer.rewind();
      }

      super.endOfNode();
      this.getFOEventHandler().characters(this);
   }

   public void finalizeNode() {
      this.textTransform();
   }

   public boolean willCreateArea() {
      if (this.whiteSpaceCollapse == 48 && this.charBuffer.limit() > 0) {
         return true;
      } else {
         this.charBuffer.rewind();

         char ch;
         do {
            if (!this.charBuffer.hasRemaining()) {
               return false;
            }

            ch = this.charBuffer.get();
         } while(ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t');

         this.charBuffer.rewind();
         return true;
      }
   }

   public CharIterator charIterator() {
      return new TextCharIterator();
   }

   protected void createBlockPointers(Block ancestorBlock) {
      this.ancestorBlock = ancestorBlock;
      if (ancestorBlock.lastFOTextProcessed != null) {
         if (ancestorBlock.lastFOTextProcessed.ancestorBlock == this.ancestorBlock) {
            this.prevFOTextThisBlock = ancestorBlock.lastFOTextProcessed;
            this.prevFOTextThisBlock.nextFOTextThisBlock = this;
         } else {
            this.prevFOTextThisBlock = null;
         }
      }

   }

   private void textTransform() {
      if (!this.getBuilderContext().inMarker() && this.textTransform != 95) {
         this.charBuffer.rewind();
         CharBuffer tmp = this.charBuffer.slice();
         int lim = this.charBuffer.limit();
         int pos = -1;

         while(true) {
            ++pos;
            if (pos >= lim) {
               return;
            }

            char c = this.charBuffer.get();
            switch (this.textTransform) {
               case 22:
                  if (this.isStartOfWord(pos)) {
                     tmp.put(Character.toTitleCase(c));
                  } else {
                     tmp.put(c);
                  }
                  break;
               case 78:
                  tmp.put(Character.toLowerCase(c));
                  break;
               case 155:
                  tmp.put(Character.toUpperCase(c));
                  break;
               default:
                  assert false;
            }
         }
      }
   }

   private boolean isStartOfWord(int var1) {
      // $FF: Couldn't be decompiled
   }

   private char getRelativeCharInBlock(int i, int offset) {
      int charIndex = i + offset;
      if (charIndex >= 0 && charIndex < this.length()) {
         return this.charAt(i + offset);
      } else if (offset > 0) {
         return '\u0000';
      } else {
         boolean foundChar = false;
         char charToReturn = 0;
         FOText nodeToTest = this;
         int remainingOffset = offset + i;

         while(!foundChar && nodeToTest.prevFOTextThisBlock != null) {
            nodeToTest = nodeToTest.prevFOTextThisBlock;
            int diff = nodeToTest.length() + remainingOffset - 1;
            if (diff >= 0) {
               charToReturn = nodeToTest.charAt(diff);
               foundChar = true;
            } else {
               remainingOffset += diff;
            }
         }

         return charToReturn;
      }
   }

   private static int isWordChar(char var0) {
      // $FF: Couldn't be decompiled
   }

   public CommonFont getCommonFont() {
      return this.commonFont;
   }

   public CommonHyphenation getCommonHyphenation() {
      return this.commonHyphenation;
   }

   public Color getColor() {
      return this.color;
   }

   public KeepProperty getKeepTogether() {
      return this.keepTogether;
   }

   public Property getLetterSpacing() {
      return this.letterSpacing;
   }

   public SpaceProperty getLineHeight() {
      return this.lineHeight;
   }

   public int getWhitespaceTreatment() {
      return this.whiteSpaceTreatment;
   }

   public Property getWordSpacing() {
      return this.wordSpacing;
   }

   public int getWrapOption() {
      return this.wrapOption;
   }

   public CommonTextDecoration getTextDecoration() {
      return this.textDecoration;
   }

   public Length getBaseLineShift() {
      return this.baselineShift;
   }

   public String getCountry() {
      return this.country;
   }

   public synchronized CharacterIterator getIterator() {
      if (this.charIterator != null) {
         this.charIterator = new StringCharacterIterator(this.toString());
      }

      return this.charIterator;
   }

   public int getBeginIndex() {
      return 0;
   }

   public int getEndIndex() {
      return this.length();
   }

   public String getLanguage() {
      return this.language;
   }

   public String getScript() {
      return this.script;
   }

   public int getBidiLevel() {
      return this.length() > 0 ? this.bidiLevelAt(0) : -1;
   }

   public String toString() {
      if (this.charBuffer == null) {
         return "";
      } else {
         CharBuffer cb = this.charBuffer.duplicate();
         cb.rewind();
         return cb.toString();
      }
   }

   public String getLocalName() {
      return "#PCDATA";
   }

   public String getNormalNamespacePrefix() {
      return null;
   }

   protected String gatherContextInfo() {
      return this.locator != null ? super.gatherContextInfo() : this.toString();
   }

   public char charAt(int position) {
      return this.charBuffer.get(position);
   }

   public CharSequence subSequence(int start, int end) {
      return this.charBuffer.subSequence(start, end);
   }

   public int length() {
      return this.charBuffer.limit();
   }

   public void resetBuffer() {
      if (this.charBuffer != null) {
         this.charBuffer.rewind();
      }

   }

   public boolean isDelimitedTextRangeBoundary(int boundary) {
      return false;
   }

   public void setStructureTreeElement(StructureTreeElement structureTreeElement) {
      this.structureTreeElement = structureTreeElement;
   }

   public StructureTreeElement getStructureTreeElement() {
      return this.structureTreeElement;
   }

   public void setBidiLevel(int level, int start, int end) {
      if (start < end) {
         if (this.bidiLevels == null) {
            this.bidiLevels = new int[this.length()];
         }

         int i = start;

         for(int n = end; i < n; ++i) {
            this.bidiLevels[i] = level;
         }

         if (this.parent != null) {
            ((FObj)this.parent).setBidiLevel(level);
         }
      } else {
         assert start < end;
      }

   }

   public int[] getBidiLevels() {
      return this.bidiLevels;
   }

   public int[] getBidiLevels(int start, int end) {
      if (this.bidiLevels != null) {
         assert start <= end;

         int n = end - start;
         int[] bidiLevels = new int[n];
         System.arraycopy(this.bidiLevels, start + 0, bidiLevels, 0, n);
         return bidiLevels;
      } else {
         return null;
      }
   }

   public int bidiLevelAt(int position) throws IndexOutOfBoundsException {
      if (position >= 0 && position < this.length()) {
         return this.bidiLevels != null ? this.bidiLevels[position] : -1;
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   protected Stack collectDelimitedTextRanges(Stack ranges, DelimitedTextRange currentRange) {
      if (currentRange != null) {
         currentRange.append(this.charIterator(), this);
      }

      return ranges;
   }

   private static class MapRange {
      private int start;
      private int end;

      MapRange(int start, int end) {
         this.start = start;
         this.end = end;
      }

      public int hashCode() {
         return this.start * 31 + this.end;
      }

      public boolean equals(Object o) {
         if (!(o instanceof MapRange)) {
            return false;
         } else {
            MapRange r = (MapRange)o;
            return r.start == this.start && r.end == this.end;
         }
      }
   }

   private class TextCharIterator extends CharIterator {
      private int currentPosition;
      private boolean canRemove;
      private boolean canReplace;

      public TextCharIterator() {
      }

      public boolean hasNext() {
         return this.currentPosition < FOText.this.charBuffer.limit();
      }

      public char nextChar() {
         if (this.currentPosition < FOText.this.charBuffer.limit()) {
            this.canRemove = true;
            this.canReplace = true;
            return FOText.this.charBuffer.get(this.currentPosition++);
         } else {
            throw new NoSuchElementException();
         }
      }

      public void remove() {
         if (this.canRemove) {
            FOText.this.charBuffer.position(this.currentPosition);
            CharBuffer tmp = FOText.this.charBuffer.slice();
            FOText.this.charBuffer.position(--this.currentPosition);
            if (tmp.hasRemaining()) {
               FOText.this.charBuffer.mark();
               FOText.this.charBuffer.put(tmp);
               FOText.this.charBuffer.reset();
            }

            FOText.this.charBuffer.limit(FOText.this.charBuffer.limit() - 1);
            this.canRemove = false;
         } else {
            throw new IllegalStateException();
         }
      }

      public void replaceChar(char c) {
         if (this.canReplace) {
            FOText.this.charBuffer.put(this.currentPosition - 1, c);
         } else {
            throw new IllegalStateException();
         }
      }
   }
}
