package org.apache.fop.tools.fontlist;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.FontTriplet;

public class FontSpec implements Comparable {
   private String key;
   private FontMetrics metrics;
   private SortedSet familyNames = new TreeSet();
   private Collection triplets = new TreeSet();

   public FontSpec(String key, FontMetrics metrics) {
      this.key = key;
      this.metrics = metrics;
   }

   public void addFamilyNames(Collection names) {
      this.familyNames.addAll(names);
   }

   public void addTriplet(FontTriplet triplet) {
      this.triplets.add(triplet);
   }

   public SortedSet getFamilyNames() {
      return Collections.unmodifiableSortedSet(this.familyNames);
   }

   public Collection getTriplets() {
      return Collections.unmodifiableCollection(this.triplets);
   }

   public String getKey() {
      return this.key;
   }

   public FontMetrics getFontMetrics() {
      return this.metrics;
   }

   public int hashCode() {
      return this.metrics.getFullName().hashCode();
   }

   public boolean equals(Object o) {
      if (o instanceof FontSpec) {
         FontSpec other = (FontSpec)o;
         return this.metrics.getFullName().equals(other.metrics.getFullName());
      } else {
         return false;
      }
   }

   public int compareTo(Object o) {
      if (o instanceof FontSpec) {
         FontSpec other = (FontSpec)o;
         return this.metrics.getFullName().compareTo(other.metrics.getFullName());
      } else {
         return 1;
      }
   }
}
