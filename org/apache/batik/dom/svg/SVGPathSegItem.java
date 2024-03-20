package org.apache.batik.dom.svg;

import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegClosePath;

public class SVGPathSegItem extends AbstractSVGItem implements SVGPathSeg, SVGPathSegClosePath {
   protected short type;
   protected String letter;
   private float x;
   private float y;
   private float x1;
   private float y1;
   private float x2;
   private float y2;
   private float r1;
   private float r2;
   private float angle;
   private boolean largeArcFlag;
   private boolean sweepFlag;

   protected SVGPathSegItem() {
   }

   public SVGPathSegItem(short type, String letter) {
      this.type = type;
      this.letter = letter;
   }

   public SVGPathSegItem(SVGPathSeg pathSeg) {
      this.type = pathSeg.getPathSegType();
      switch (this.type) {
         case 1:
            this.letter = "z";
         default:
      }
   }

   protected String getStringValue() {
      return this.letter;
   }

   public short getPathSegType() {
      return this.type;
   }

   public String getPathSegTypeAsLetter() {
      return this.letter;
   }

   public float getR1() {
      return this.r1;
   }

   public void setR1(float r1) {
      this.r1 = r1;
   }

   public float getR2() {
      return this.r2;
   }

   public void setR2(float r2) {
      this.r2 = r2;
   }

   public float getAngle() {
      return this.angle;
   }

   public void setAngle(float angle) {
      this.angle = angle;
   }

   public boolean isLargeArcFlag() {
      return this.largeArcFlag;
   }

   public void setLargeArcFlag(boolean largeArcFlag) {
      this.largeArcFlag = largeArcFlag;
   }

   public boolean isSweepFlag() {
      return this.sweepFlag;
   }

   public void setSweepFlag(boolean sweepFlag) {
      this.sweepFlag = sweepFlag;
   }

   public float getX() {
      return this.x;
   }

   public void setX(float x) {
      this.x = x;
   }

   public float getY() {
      return this.y;
   }

   public void setY(float y) {
      this.y = y;
   }

   public float getX1() {
      return this.x1;
   }

   public void setX1(float x1) {
      this.x1 = x1;
   }

   public float getY1() {
      return this.y1;
   }

   public void setY1(float y1) {
      this.y1 = y1;
   }

   public float getX2() {
      return this.x2;
   }

   public void setX2(float x2) {
      this.x2 = x2;
   }

   public float getY2() {
      return this.y2;
   }

   public void setY2(float y2) {
      this.y2 = y2;
   }
}
