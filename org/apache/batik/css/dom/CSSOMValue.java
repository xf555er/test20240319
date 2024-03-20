package org.apache.batik.css.dom;

import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;

public class CSSOMValue implements CSSPrimitiveValue, CSSValueList, Counter, Rect, RGBColor {
   protected ValueProvider valueProvider;
   protected ModificationHandler handler;
   protected LeftComponent leftComponent;
   protected RightComponent rightComponent;
   protected BottomComponent bottomComponent;
   protected TopComponent topComponent;
   protected RedComponent redComponent;
   protected GreenComponent greenComponent;
   protected BlueComponent blueComponent;
   protected CSSValue[] items;

   public CSSOMValue(ValueProvider vp) {
      this.valueProvider = vp;
   }

   public void setModificationHandler(ModificationHandler h) {
      this.handler = h;
   }

   public String getCssText() {
      return this.valueProvider.getValue().getCssText();
   }

   public void setCssText(String cssText) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         this.handler.textChanged(cssText);
      }
   }

   public short getCssValueType() {
      return this.valueProvider.getValue().getCssValueType();
   }

   public short getPrimitiveType() {
      return this.valueProvider.getValue().getPrimitiveType();
   }

   public void setFloatValue(short unitType, float floatValue) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         this.handler.floatValueChanged(unitType, floatValue);
      }
   }

   public float getFloatValue(short unitType) throws DOMException {
      return convertFloatValue(unitType, this.valueProvider.getValue());
   }

   public static float convertFloatValue(short unitType, Value value) {
      switch (unitType) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 18:
            if (value.getPrimitiveType() == unitType) {
               return value.getFloatValue();
            }
         default:
            throw new DOMException((short)15, "");
         case 6:
            return toCentimeters(value);
         case 7:
            return toMillimeters(value);
         case 8:
            return toInches(value);
         case 9:
            return toPoints(value);
         case 10:
            return toPicas(value);
         case 11:
            return toDegrees(value);
         case 12:
            return toRadians(value);
         case 13:
            return toGradians(value);
         case 14:
            return toMilliseconds(value);
         case 15:
            return toSeconds(value);
         case 16:
            return toHertz(value);
         case 17:
            return tokHertz(value);
      }
   }

   protected static float toCentimeters(Value value) {
      switch (value.getPrimitiveType()) {
         case 6:
            return value.getFloatValue();
         case 7:
            return value.getFloatValue() / 10.0F;
         case 8:
            return value.getFloatValue() * 2.54F;
         case 9:
            return value.getFloatValue() * 2.54F / 72.0F;
         case 10:
            return value.getFloatValue() * 2.54F / 6.0F;
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float toInches(Value value) {
      switch (value.getPrimitiveType()) {
         case 6:
            return value.getFloatValue() / 2.54F;
         case 7:
            return value.getFloatValue() / 25.4F;
         case 8:
            return value.getFloatValue();
         case 9:
            return value.getFloatValue() / 72.0F;
         case 10:
            return value.getFloatValue() / 6.0F;
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float toMillimeters(Value value) {
      switch (value.getPrimitiveType()) {
         case 6:
            return value.getFloatValue() * 10.0F;
         case 7:
            return value.getFloatValue();
         case 8:
            return value.getFloatValue() * 25.4F;
         case 9:
            return value.getFloatValue() * 25.4F / 72.0F;
         case 10:
            return value.getFloatValue() * 25.4F / 6.0F;
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float toPoints(Value value) {
      switch (value.getPrimitiveType()) {
         case 6:
            return value.getFloatValue() * 72.0F / 2.54F;
         case 7:
            return value.getFloatValue() * 72.0F / 25.4F;
         case 8:
            return value.getFloatValue() * 72.0F;
         case 9:
            return value.getFloatValue();
         case 10:
            return value.getFloatValue() * 12.0F;
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float toPicas(Value value) {
      switch (value.getPrimitiveType()) {
         case 6:
            return value.getFloatValue() * 6.0F / 2.54F;
         case 7:
            return value.getFloatValue() * 6.0F / 25.4F;
         case 8:
            return value.getFloatValue() * 6.0F;
         case 9:
            return value.getFloatValue() / 12.0F;
         case 10:
            return value.getFloatValue();
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float toDegrees(Value value) {
      switch (value.getPrimitiveType()) {
         case 11:
            return value.getFloatValue();
         case 12:
            return (float)Math.toDegrees((double)value.getFloatValue());
         case 13:
            return value.getFloatValue() * 9.0F / 5.0F;
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float toRadians(Value value) {
      switch (value.getPrimitiveType()) {
         case 11:
            return value.getFloatValue() * 5.0F / 9.0F;
         case 12:
            return value.getFloatValue();
         case 13:
            return (float)((double)(value.getFloatValue() * 100.0F) / Math.PI);
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float toGradians(Value value) {
      switch (value.getPrimitiveType()) {
         case 11:
            return (float)((double)value.getFloatValue() * Math.PI / 180.0);
         case 12:
            return (float)((double)value.getFloatValue() * Math.PI / 100.0);
         case 13:
            return value.getFloatValue();
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float toMilliseconds(Value value) {
      switch (value.getPrimitiveType()) {
         case 14:
            return value.getFloatValue();
         case 15:
            return value.getFloatValue() * 1000.0F;
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float toSeconds(Value value) {
      switch (value.getPrimitiveType()) {
         case 14:
            return value.getFloatValue() / 1000.0F;
         case 15:
            return value.getFloatValue();
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float toHertz(Value value) {
      switch (value.getPrimitiveType()) {
         case 16:
            return value.getFloatValue();
         case 17:
            return value.getFloatValue() / 1000.0F;
         default:
            throw new DOMException((short)15, "");
      }
   }

   protected static float tokHertz(Value value) {
      switch (value.getPrimitiveType()) {
         case 16:
            return value.getFloatValue() * 1000.0F;
         case 17:
            return value.getFloatValue();
         default:
            throw new DOMException((short)15, "");
      }
   }

   public void setStringValue(short stringType, String stringValue) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         this.handler.stringValueChanged(stringType, stringValue);
      }
   }

   public String getStringValue() throws DOMException {
      return this.valueProvider.getValue().getStringValue();
   }

   public Counter getCounterValue() throws DOMException {
      return this;
   }

   public Rect getRectValue() throws DOMException {
      return this;
   }

   public RGBColor getRGBColorValue() throws DOMException {
      return this;
   }

   public int getLength() {
      return this.valueProvider.getValue().getLength();
   }

   public CSSValue item(int index) {
      int len = this.valueProvider.getValue().getLength();
      if (index >= 0 && index < len) {
         if (this.items == null) {
            this.items = new CSSValue[this.valueProvider.getValue().getLength()];
         } else if (this.items.length < len) {
            CSSValue[] nitems = new CSSValue[len];
            System.arraycopy(this.items, 0, nitems, 0, this.items.length);
            this.items = nitems;
         }

         CSSValue result = this.items[index];
         if (result == null) {
            this.items[index] = (CSSValue)(result = new ListComponent(index));
         }

         return (CSSValue)result;
      } else {
         return null;
      }
   }

   public String getIdentifier() {
      return this.valueProvider.getValue().getIdentifier();
   }

   public String getListStyle() {
      return this.valueProvider.getValue().getListStyle();
   }

   public String getSeparator() {
      return this.valueProvider.getValue().getSeparator();
   }

   public CSSPrimitiveValue getTop() {
      this.valueProvider.getValue().getTop();
      if (this.topComponent == null) {
         this.topComponent = new TopComponent();
      }

      return this.topComponent;
   }

   public CSSPrimitiveValue getRight() {
      this.valueProvider.getValue().getRight();
      if (this.rightComponent == null) {
         this.rightComponent = new RightComponent();
      }

      return this.rightComponent;
   }

   public CSSPrimitiveValue getBottom() {
      this.valueProvider.getValue().getBottom();
      if (this.bottomComponent == null) {
         this.bottomComponent = new BottomComponent();
      }

      return this.bottomComponent;
   }

   public CSSPrimitiveValue getLeft() {
      this.valueProvider.getValue().getLeft();
      if (this.leftComponent == null) {
         this.leftComponent = new LeftComponent();
      }

      return this.leftComponent;
   }

   public CSSPrimitiveValue getRed() {
      this.valueProvider.getValue().getRed();
      if (this.redComponent == null) {
         this.redComponent = new RedComponent();
      }

      return this.redComponent;
   }

   public CSSPrimitiveValue getGreen() {
      this.valueProvider.getValue().getGreen();
      if (this.greenComponent == null) {
         this.greenComponent = new GreenComponent();
      }

      return this.greenComponent;
   }

   public CSSPrimitiveValue getBlue() {
      this.valueProvider.getValue().getBlue();
      if (this.blueComponent == null) {
         this.blueComponent = new BlueComponent();
      }

      return this.blueComponent;
   }

   protected class ListComponent extends AbstractComponent {
      protected int index;

      public ListComponent(int idx) {
         super();
         this.index = idx;
      }

      protected Value getValue() {
         if (this.index >= CSSOMValue.this.valueProvider.getValue().getLength()) {
            throw new DOMException((short)7, "");
         } else {
            return CSSOMValue.this.valueProvider.getValue().item(this.index);
         }
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.listTextChanged(this.index, cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.listFloatValueChanged(this.index, unitType, floatValue);
         }
      }

      public void setStringValue(short stringType, String stringValue) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.listStringValueChanged(this.index, stringType, stringValue);
         }
      }
   }

   protected class BlueComponent extends FloatComponent {
      protected BlueComponent() {
         super();
      }

      protected Value getValue() {
         return CSSOMValue.this.valueProvider.getValue().getBlue();
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.blueTextChanged(cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.blueFloatValueChanged(unitType, floatValue);
         }
      }
   }

   protected class GreenComponent extends FloatComponent {
      protected GreenComponent() {
         super();
      }

      protected Value getValue() {
         return CSSOMValue.this.valueProvider.getValue().getGreen();
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.greenTextChanged(cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.greenFloatValueChanged(unitType, floatValue);
         }
      }
   }

   protected class RedComponent extends FloatComponent {
      protected RedComponent() {
         super();
      }

      protected Value getValue() {
         return CSSOMValue.this.valueProvider.getValue().getRed();
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.redTextChanged(cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.redFloatValueChanged(unitType, floatValue);
         }
      }
   }

   protected class BottomComponent extends FloatComponent {
      protected BottomComponent() {
         super();
      }

      protected Value getValue() {
         return CSSOMValue.this.valueProvider.getValue().getBottom();
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.bottomTextChanged(cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.bottomFloatValueChanged(unitType, floatValue);
         }
      }
   }

   protected class RightComponent extends FloatComponent {
      protected RightComponent() {
         super();
      }

      protected Value getValue() {
         return CSSOMValue.this.valueProvider.getValue().getRight();
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.rightTextChanged(cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.rightFloatValueChanged(unitType, floatValue);
         }
      }
   }

   protected class TopComponent extends FloatComponent {
      protected TopComponent() {
         super();
      }

      protected Value getValue() {
         return CSSOMValue.this.valueProvider.getValue().getTop();
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.topTextChanged(cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.topFloatValueChanged(unitType, floatValue);
         }
      }
   }

   protected class LeftComponent extends FloatComponent {
      protected LeftComponent() {
         super();
      }

      protected Value getValue() {
         return CSSOMValue.this.valueProvider.getValue().getLeft();
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.leftTextChanged(cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMValue.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMValue.this.handler.leftFloatValueChanged(unitType, floatValue);
         }
      }
   }

   protected abstract class FloatComponent extends AbstractComponent {
      protected FloatComponent() {
         super();
      }

      public void setStringValue(short stringType, String stringValue) throws DOMException {
         throw new DOMException((short)15, "");
      }
   }

   protected abstract class AbstractComponent implements CSSPrimitiveValue {
      protected abstract Value getValue();

      public String getCssText() {
         return this.getValue().getCssText();
      }

      public short getCssValueType() {
         return this.getValue().getCssValueType();
      }

      public short getPrimitiveType() {
         return this.getValue().getPrimitiveType();
      }

      public float getFloatValue(short unitType) throws DOMException {
         return CSSOMValue.convertFloatValue(unitType, this.getValue());
      }

      public String getStringValue() throws DOMException {
         return CSSOMValue.this.valueProvider.getValue().getStringValue();
      }

      public Counter getCounterValue() throws DOMException {
         throw new DOMException((short)15, "");
      }

      public Rect getRectValue() throws DOMException {
         throw new DOMException((short)15, "");
      }

      public RGBColor getRGBColorValue() throws DOMException {
         throw new DOMException((short)15, "");
      }

      public int getLength() {
         throw new DOMException((short)15, "");
      }

      public CSSValue item(int index) {
         throw new DOMException((short)15, "");
      }
   }

   public abstract static class AbstractModificationHandler implements ModificationHandler {
      protected abstract Value getValue();

      public void floatValueChanged(short unit, float value) throws DOMException {
         this.textChanged(FloatValue.getCssText(unit, value));
      }

      public void stringValueChanged(short type, String value) throws DOMException {
         this.textChanged(StringValue.getCssText(type, value));
      }

      public void leftTextChanged(String text) throws DOMException {
         Value val = this.getValue();
         text = "rect(" + val.getTop().getCssText() + ", " + val.getRight().getCssText() + ", " + val.getBottom().getCssText() + ", " + text + ')';
         this.textChanged(text);
      }

      public void leftFloatValueChanged(short unit, float value) throws DOMException {
         Value val = this.getValue();
         String text = "rect(" + val.getTop().getCssText() + ", " + val.getRight().getCssText() + ", " + val.getBottom().getCssText() + ", " + FloatValue.getCssText(unit, value) + ')';
         this.textChanged(text);
      }

      public void topTextChanged(String text) throws DOMException {
         Value val = this.getValue();
         text = "rect(" + text + ", " + val.getRight().getCssText() + ", " + val.getBottom().getCssText() + ", " + val.getLeft().getCssText() + ')';
         this.textChanged(text);
      }

      public void topFloatValueChanged(short unit, float value) throws DOMException {
         Value val = this.getValue();
         String text = "rect(" + FloatValue.getCssText(unit, value) + ", " + val.getRight().getCssText() + ", " + val.getBottom().getCssText() + ", " + val.getLeft().getCssText() + ')';
         this.textChanged(text);
      }

      public void rightTextChanged(String text) throws DOMException {
         Value val = this.getValue();
         text = "rect(" + val.getTop().getCssText() + ", " + text + ", " + val.getBottom().getCssText() + ", " + val.getLeft().getCssText() + ')';
         this.textChanged(text);
      }

      public void rightFloatValueChanged(short unit, float value) throws DOMException {
         Value val = this.getValue();
         String text = "rect(" + val.getTop().getCssText() + ", " + FloatValue.getCssText(unit, value) + ", " + val.getBottom().getCssText() + ", " + val.getLeft().getCssText() + ')';
         this.textChanged(text);
      }

      public void bottomTextChanged(String text) throws DOMException {
         Value val = this.getValue();
         text = "rect(" + val.getTop().getCssText() + ", " + val.getRight().getCssText() + ", " + text + ", " + val.getLeft().getCssText() + ')';
         this.textChanged(text);
      }

      public void bottomFloatValueChanged(short unit, float value) throws DOMException {
         Value val = this.getValue();
         String text = "rect(" + val.getTop().getCssText() + ", " + val.getRight().getCssText() + ", " + FloatValue.getCssText(unit, value) + ", " + val.getLeft().getCssText() + ')';
         this.textChanged(text);
      }

      public void redTextChanged(String text) throws DOMException {
         Value val = this.getValue();
         text = "rgb(" + text + ", " + val.getGreen().getCssText() + ", " + val.getBlue().getCssText() + ')';
         this.textChanged(text);
      }

      public void redFloatValueChanged(short unit, float value) throws DOMException {
         Value val = this.getValue();
         String text = "rgb(" + FloatValue.getCssText(unit, value) + ", " + val.getGreen().getCssText() + ", " + val.getBlue().getCssText() + ')';
         this.textChanged(text);
      }

      public void greenTextChanged(String text) throws DOMException {
         Value val = this.getValue();
         text = "rgb(" + val.getRed().getCssText() + ", " + text + ", " + val.getBlue().getCssText() + ')';
         this.textChanged(text);
      }

      public void greenFloatValueChanged(short unit, float value) throws DOMException {
         Value val = this.getValue();
         String text = "rgb(" + val.getRed().getCssText() + ", " + FloatValue.getCssText(unit, value) + ", " + val.getBlue().getCssText() + ')';
         this.textChanged(text);
      }

      public void blueTextChanged(String text) throws DOMException {
         Value val = this.getValue();
         text = "rgb(" + val.getRed().getCssText() + ", " + val.getGreen().getCssText() + ", " + text + ')';
         this.textChanged(text);
      }

      public void blueFloatValueChanged(short unit, float value) throws DOMException {
         Value val = this.getValue();
         String text = "rgb(" + val.getRed().getCssText() + ", " + val.getGreen().getCssText() + ", " + FloatValue.getCssText(unit, value) + ')';
         this.textChanged(text);
      }

      public void listTextChanged(int idx, String text) throws DOMException {
         ListValue lv = (ListValue)this.getValue();
         int len = lv.getLength();
         StringBuffer sb = new StringBuffer(len * 8);

         int i;
         for(i = 0; i < idx; ++i) {
            sb.append(lv.item(i).getCssText());
            sb.append(lv.getSeparatorChar());
         }

         sb.append(text);

         for(i = idx + 1; i < len; ++i) {
            sb.append(lv.getSeparatorChar());
            sb.append(lv.item(i).getCssText());
         }

         text = sb.toString();
         this.textChanged(text);
      }

      public void listFloatValueChanged(int idx, short unit, float value) throws DOMException {
         ListValue lv = (ListValue)this.getValue();
         int len = lv.getLength();
         StringBuffer sb = new StringBuffer(len * 8);

         int i;
         for(i = 0; i < idx; ++i) {
            sb.append(lv.item(i).getCssText());
            sb.append(lv.getSeparatorChar());
         }

         sb.append(FloatValue.getCssText(unit, value));

         for(i = idx + 1; i < len; ++i) {
            sb.append(lv.getSeparatorChar());
            sb.append(lv.item(i).getCssText());
         }

         this.textChanged(sb.toString());
      }

      public void listStringValueChanged(int idx, short unit, String value) throws DOMException {
         ListValue lv = (ListValue)this.getValue();
         int len = lv.getLength();
         StringBuffer sb = new StringBuffer(len * 8);

         int i;
         for(i = 0; i < idx; ++i) {
            sb.append(lv.item(i).getCssText());
            sb.append(lv.getSeparatorChar());
         }

         sb.append(StringValue.getCssText(unit, value));

         for(i = idx + 1; i < len; ++i) {
            sb.append(lv.getSeparatorChar());
            sb.append(lv.item(i).getCssText());
         }

         this.textChanged(sb.toString());
      }
   }

   public interface ModificationHandler {
      void textChanged(String var1) throws DOMException;

      void floatValueChanged(short var1, float var2) throws DOMException;

      void stringValueChanged(short var1, String var2) throws DOMException;

      void leftTextChanged(String var1) throws DOMException;

      void leftFloatValueChanged(short var1, float var2) throws DOMException;

      void topTextChanged(String var1) throws DOMException;

      void topFloatValueChanged(short var1, float var2) throws DOMException;

      void rightTextChanged(String var1) throws DOMException;

      void rightFloatValueChanged(short var1, float var2) throws DOMException;

      void bottomTextChanged(String var1) throws DOMException;

      void bottomFloatValueChanged(short var1, float var2) throws DOMException;

      void redTextChanged(String var1) throws DOMException;

      void redFloatValueChanged(short var1, float var2) throws DOMException;

      void greenTextChanged(String var1) throws DOMException;

      void greenFloatValueChanged(short var1, float var2) throws DOMException;

      void blueTextChanged(String var1) throws DOMException;

      void blueFloatValueChanged(short var1, float var2) throws DOMException;

      void listTextChanged(int var1, String var2) throws DOMException;

      void listFloatValueChanged(int var1, short var2, float var3) throws DOMException;

      void listStringValueChanged(int var1, short var2, String var3) throws DOMException;
   }

   public interface ValueProvider {
      Value getValue();
   }
}
