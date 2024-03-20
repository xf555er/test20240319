package org.apache.batik.bridge;

public class TextHit {
   private int charIndex;
   private boolean leadingEdge;

   public TextHit(int charIndex, boolean leadingEdge) {
      this.charIndex = charIndex;
      this.leadingEdge = leadingEdge;
   }

   public int getCharIndex() {
      return this.charIndex;
   }

   public boolean isLeadingEdge() {
      return this.leadingEdge;
   }
}
