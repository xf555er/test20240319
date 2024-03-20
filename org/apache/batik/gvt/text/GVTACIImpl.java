package org.apache.batik.gvt.text;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GVTACIImpl implements GVTAttributedCharacterIterator {
   private String simpleString;
   private Set allAttributes;
   private ArrayList mapList;
   private static int START_RUN = 2;
   private static int END_RUN = 3;
   private static int MID_RUN = 1;
   private static int SINGLETON = 0;
   private int[] charInRun;
   private CharacterIterator iter = null;
   private int currentIndex = -1;

   public GVTACIImpl() {
      this.simpleString = "";
      this.buildAttributeTables();
   }

   public GVTACIImpl(AttributedCharacterIterator aci) {
      this.buildAttributeTables(aci);
   }

   public void setString(String s) {
      this.simpleString = s;
      this.iter = new StringCharacterIterator(this.simpleString);
      this.buildAttributeTables();
   }

   public void setString(AttributedString s) {
      this.iter = s.getIterator();
      this.buildAttributeTables((AttributedCharacterIterator)this.iter);
   }

   public void setAttributeArray(GVTAttributedCharacterIterator.TextAttribute attr, Object[] attValues, int beginIndex, int endIndex) {
      beginIndex = Math.max(beginIndex, 0);
      endIndex = Math.min(endIndex, this.simpleString.length());
      if (this.charInRun[beginIndex] == END_RUN) {
         if (this.charInRun[beginIndex - 1] == MID_RUN) {
            this.charInRun[beginIndex - 1] = END_RUN;
         } else {
            this.charInRun[beginIndex - 1] = SINGLETON;
         }
      }

      if (this.charInRun[endIndex + 1] == END_RUN) {
         this.charInRun[endIndex + 1] = SINGLETON;
      } else if (this.charInRun[endIndex + 1] == MID_RUN) {
         this.charInRun[endIndex + 1] = START_RUN;
      }

      for(int i = beginIndex; i <= endIndex; ++i) {
         this.charInRun[i] = SINGLETON;
         int n = Math.min(i, attValues.length - 1);
         ((Map)this.mapList.get(i)).put(attr, attValues[n]);
      }

   }

   public Set getAllAttributeKeys() {
      return this.allAttributes;
   }

   public Object getAttribute(AttributedCharacterIterator.Attribute attribute) {
      return this.getAttributes().get(attribute);
   }

   public Map getAttributes() {
      return (Map)this.mapList.get(this.currentIndex);
   }

   public int getRunLimit() {
      int ndx = this.currentIndex;

      do {
         ++ndx;
      } while(this.charInRun[ndx] == MID_RUN);

      return ndx;
   }

   public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
      int ndx = this.currentIndex;
      Object value = this.getAttributes().get(attribute);
      if (value == null) {
         do {
            ++ndx;
         } while(((Map)this.mapList.get(ndx)).get(attribute) == null);
      } else {
         do {
            ++ndx;
         } while(value.equals(((Map)this.mapList.get(ndx)).get(attribute)));
      }

      return ndx;
   }

   public int getRunLimit(Set attributes) {
      int ndx = this.currentIndex;

      do {
         ++ndx;
      } while(attributes.equals(this.mapList.get(ndx)));

      return ndx;
   }

   public int getRunStart() {
      int ndx;
      for(ndx = this.currentIndex; this.charInRun[ndx] == MID_RUN; --ndx) {
      }

      return ndx;
   }

   public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
      int ndx = this.currentIndex - 1;
      Object value = this.getAttributes().get(attribute);

      try {
         if (value == null) {
            while(((Map)this.mapList.get(ndx - 1)).get(attribute) == null) {
               --ndx;
            }
         } else {
            while(value.equals(((Map)this.mapList.get(ndx - 1)).get(attribute))) {
               --ndx;
            }
         }
      } catch (IndexOutOfBoundsException var5) {
      }

      return ndx;
   }

   public int getRunStart(Set attributes) {
      int ndx = this.currentIndex;

      try {
         while(attributes.equals(this.mapList.get(ndx - 1))) {
            --ndx;
         }
      } catch (IndexOutOfBoundsException var4) {
      }

      return ndx;
   }

   public Object clone() {
      GVTAttributedCharacterIterator cloneACI = new GVTACIImpl(this);
      return cloneACI;
   }

   public char current() {
      return this.iter.current();
   }

   public char first() {
      return this.iter.first();
   }

   public int getBeginIndex() {
      return this.iter.getBeginIndex();
   }

   public int getEndIndex() {
      return this.iter.getEndIndex();
   }

   public int getIndex() {
      return this.iter.getIndex();
   }

   public char last() {
      return this.iter.last();
   }

   public char next() {
      return this.iter.next();
   }

   public char previous() {
      return this.iter.previous();
   }

   public char setIndex(int position) {
      return this.iter.setIndex(position);
   }

   private void buildAttributeTables() {
      this.allAttributes = new HashSet();
      this.mapList = new ArrayList(this.simpleString.length());
      this.charInRun = new int[this.simpleString.length()];

      for(int i = 0; i < this.charInRun.length; ++i) {
         this.charInRun[i] = SINGLETON;
         this.mapList.set(i, new HashMap());
      }

   }

   private void buildAttributeTables(AttributedCharacterIterator aci) {
      this.allAttributes = aci.getAllAttributeKeys();
      int length = aci.getEndIndex() - aci.getBeginIndex();
      this.mapList = new ArrayList(length);
      this.charInRun = new int[length];
      char c = aci.first();
      char[] chars = new char[length];

      for(int i = 0; i < length; ++i) {
         chars[i] = c;
         this.charInRun[i] = SINGLETON;
         this.mapList.set(i, new HashMap(aci.getAttributes()));
         c = aci.next();
      }

      this.simpleString = new String(chars);
   }

   public static class TransformAttributeFilter implements GVTAttributedCharacterIterator.AttributeFilter {
      public AttributedCharacterIterator mutateAttributes(AttributedCharacterIterator aci) {
         return aci;
      }
   }
}
