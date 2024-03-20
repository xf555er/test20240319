package org.apache.batik.anim.dom;

import org.apache.batik.parser.LengthParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.UnitProcessor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGLength;

public abstract class AbstractSVGLength implements SVGLength {
   public static final short HORIZONTAL_LENGTH = 2;
   public static final short VERTICAL_LENGTH = 1;
   public static final short OTHER_LENGTH = 0;
   protected short unitType;
   protected float value;
   protected short direction;
   protected UnitProcessor.Context context = new DefaultContext();
   protected static final String[] UNITS = new String[]{"", "", "%", "em", "ex", "px", "cm", "mm", "in", "pt", "pc"};

   protected abstract SVGOMElement getAssociatedElement();

   public AbstractSVGLength(short direction) {
      this.direction = direction;
      this.value = 0.0F;
      this.unitType = 1;
   }

   public short getUnitType() {
      this.revalidate();
      return this.unitType;
   }

   public float getValue() {
      this.revalidate();

      try {
         return UnitProcessor.svgToUserSpace(this.value, this.unitType, this.direction, this.context);
      } catch (IllegalArgumentException var2) {
         return 0.0F;
      }
   }

   public void setValue(float value) throws DOMException {
      this.value = UnitProcessor.userSpaceToSVG(value, this.unitType, this.direction, this.context);
      this.reset();
   }

   public float getValueInSpecifiedUnits() {
      this.revalidate();
      return this.value;
   }

   public void setValueInSpecifiedUnits(float value) throws DOMException {
      this.revalidate();
      this.value = value;
      this.reset();
   }

   public String getValueAsString() {
      this.revalidate();
      return this.unitType == 0 ? "" : Float.toString(this.value) + UNITS[this.unitType];
   }

   public void setValueAsString(String value) throws DOMException {
      this.parse(value);
      this.reset();
   }

   public void newValueSpecifiedUnits(short unit, float value) {
      this.unitType = unit;
      this.value = value;
      this.reset();
   }

   public void convertToSpecifiedUnits(short unit) {
      float v = this.getValue();
      this.unitType = unit;
      this.setValue(v);
   }

   protected void reset() {
   }

   protected void revalidate() {
   }

   protected void parse(String s) {
      try {
         LengthParser lengthParser = new LengthParser();
         UnitProcessor.UnitResolver ur = new UnitProcessor.UnitResolver();
         lengthParser.setLengthHandler(ur);
         lengthParser.parse(s);
         this.unitType = ur.unit;
         this.value = ur.value;
      } catch (ParseException var4) {
         this.unitType = 0;
         this.value = 0.0F;
      }

   }

   protected class DefaultContext implements UnitProcessor.Context {
      public Element getElement() {
         return AbstractSVGLength.this.getAssociatedElement();
      }

      public float getPixelUnitToMillimeter() {
         return AbstractSVGLength.this.getAssociatedElement().getSVGContext().getPixelUnitToMillimeter();
      }

      public float getPixelToMM() {
         return this.getPixelUnitToMillimeter();
      }

      public float getFontSize() {
         return AbstractSVGLength.this.getAssociatedElement().getSVGContext().getFontSize();
      }

      public float getXHeight() {
         return 0.5F;
      }

      public float getViewportWidth() {
         return AbstractSVGLength.this.getAssociatedElement().getSVGContext().getViewportWidth();
      }

      public float getViewportHeight() {
         return AbstractSVGLength.this.getAssociatedElement().getSVGContext().getViewportHeight();
      }
   }
}
