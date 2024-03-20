package org.apache.batik.svggen.font.table;

public class Panose {
   byte bFamilyType = 0;
   byte bSerifStyle = 0;
   byte bWeight = 0;
   byte bProportion = 0;
   byte bContrast = 0;
   byte bStrokeVariation = 0;
   byte bArmStyle = 0;
   byte bLetterform = 0;
   byte bMidline = 0;
   byte bXHeight = 0;

   public Panose(byte[] panose) {
      this.bFamilyType = panose[0];
      this.bSerifStyle = panose[1];
      this.bWeight = panose[2];
      this.bProportion = panose[3];
      this.bContrast = panose[4];
      this.bStrokeVariation = panose[5];
      this.bArmStyle = panose[6];
      this.bLetterform = panose[7];
      this.bMidline = panose[8];
      this.bXHeight = panose[9];
   }

   public byte getFamilyType() {
      return this.bFamilyType;
   }

   public byte getSerifStyle() {
      return this.bSerifStyle;
   }

   public byte getWeight() {
      return this.bWeight;
   }

   public byte getProportion() {
      return this.bProportion;
   }

   public byte getContrast() {
      return this.bContrast;
   }

   public byte getStrokeVariation() {
      return this.bStrokeVariation;
   }

   public byte getArmStyle() {
      return this.bArmStyle;
   }

   public byte getLetterForm() {
      return this.bLetterform;
   }

   public byte getMidline() {
      return this.bMidline;
   }

   public byte getXHeight() {
      return this.bXHeight;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(String.valueOf(this.bFamilyType)).append(" ").append(String.valueOf(this.bSerifStyle)).append(" ").append(String.valueOf(this.bWeight)).append(" ").append(String.valueOf(this.bProportion)).append(" ").append(String.valueOf(this.bContrast)).append(" ").append(String.valueOf(this.bStrokeVariation)).append(" ").append(String.valueOf(this.bArmStyle)).append(" ").append(String.valueOf(this.bLetterform)).append(" ").append(String.valueOf(this.bMidline)).append(" ").append(String.valueOf(this.bXHeight));
      return sb.toString();
   }
}
