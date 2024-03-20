package org.apache.batik.gvt.text;

import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.Set;

public class AttributedCharacterSpanIterator implements AttributedCharacterIterator {
   private AttributedCharacterIterator aci;
   private int begin;
   private int end;

   public AttributedCharacterSpanIterator(AttributedCharacterIterator aci, int start, int stop) {
      this.aci = aci;
      this.end = Math.min(aci.getEndIndex(), stop);
      this.begin = Math.max(aci.getBeginIndex(), start);
      this.aci.setIndex(this.begin);
   }

   public Set getAllAttributeKeys() {
      return this.aci.getAllAttributeKeys();
   }

   public Object getAttribute(AttributedCharacterIterator.Attribute attribute) {
      return this.aci.getAttribute(attribute);
   }

   public Map getAttributes() {
      return this.aci.getAttributes();
   }

   public int getRunLimit() {
      return Math.min(this.aci.getRunLimit(), this.end);
   }

   public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
      return Math.min(this.aci.getRunLimit(attribute), this.end);
   }

   public int getRunLimit(Set attributes) {
      return Math.min(this.aci.getRunLimit(attributes), this.end);
   }

   public int getRunStart() {
      return Math.max(this.aci.getRunStart(), this.begin);
   }

   public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
      return Math.max(this.aci.getRunStart(attribute), this.begin);
   }

   public int getRunStart(Set attributes) {
      return Math.max(this.aci.getRunStart(attributes), this.begin);
   }

   public Object clone() {
      return new AttributedCharacterSpanIterator((AttributedCharacterIterator)this.aci.clone(), this.begin, this.end);
   }

   public char current() {
      return this.aci.current();
   }

   public char first() {
      return this.aci.setIndex(this.begin);
   }

   public int getBeginIndex() {
      return this.begin;
   }

   public int getEndIndex() {
      return this.end;
   }

   public int getIndex() {
      return this.aci.getIndex();
   }

   public char last() {
      return this.setIndex(this.end - 1);
   }

   public char next() {
      return this.getIndex() < this.end - 1 ? this.aci.next() : this.setIndex(this.end);
   }

   public char previous() {
      return this.getIndex() > this.begin ? this.aci.previous() : '\uffff';
   }

   public char setIndex(int position) {
      int ndx = Math.max(position, this.begin);
      ndx = Math.min(ndx, this.end);
      char c = this.aci.setIndex(ndx);
      if (ndx == this.end) {
         c = '\uffff';
      }

      return c;
   }
}
