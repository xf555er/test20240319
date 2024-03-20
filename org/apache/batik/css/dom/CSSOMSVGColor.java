package org.apache.batik.css.dom;

import java.util.ArrayList;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.ICCColor;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;
import org.w3c.dom.svg.SVGColor;
import org.w3c.dom.svg.SVGICCColor;
import org.w3c.dom.svg.SVGNumber;
import org.w3c.dom.svg.SVGNumberList;

public class CSSOMSVGColor implements SVGColor, RGBColor, SVGICCColor, SVGNumberList {
   protected ValueProvider valueProvider;
   protected ModificationHandler handler;
   protected RedComponent redComponent;
   protected GreenComponent greenComponent;
   protected BlueComponent blueComponent;
   protected ArrayList iccColors;

   public CSSOMSVGColor(ValueProvider vp) {
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
         this.iccColors = null;
         this.handler.textChanged(cssText);
      }
   }

   public short getCssValueType() {
      return 3;
   }

   public short getColorType() {
      Value value = this.valueProvider.getValue();
      int cssValueType = value.getCssValueType();
      switch (cssValueType) {
         case 1:
            int primitiveType = value.getPrimitiveType();
            switch (primitiveType) {
               case 21:
                  if (value.getStringValue().equalsIgnoreCase("currentcolor")) {
                     return 3;
                  }

                  return 1;
               case 25:
                  return 1;
               default:
                  throw new IllegalStateException("Found unexpected PrimitiveType:" + primitiveType);
            }
         case 2:
            return 2;
         default:
            throw new IllegalStateException("Found unexpected CssValueType:" + cssValueType);
      }
   }

   public RGBColor getRGBColor() {
      return this;
   }

   public RGBColor getRgbColor() {
      return this;
   }

   public void setRGBColor(String color) {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         this.handler.rgbColorChanged(color);
      }
   }

   public SVGICCColor getICCColor() {
      return this;
   }

   public SVGICCColor getIccColor() {
      return this;
   }

   public void setRGBColorICCColor(String rgb, String icc) {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         this.iccColors = null;
         this.handler.rgbColorICCColorChanged(rgb, icc);
      }
   }

   public void setColor(short type, String rgb, String icc) {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         this.iccColors = null;
         this.handler.colorChanged(type, rgb, icc);
      }
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

   public String getColorProfile() {
      if (this.getColorType() != 2) {
         throw new DOMException((short)12, "");
      } else {
         Value value = this.valueProvider.getValue();
         return ((ICCColor)value.item(1)).getColorProfile();
      }
   }

   public void setColorProfile(String colorProfile) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         this.handler.colorProfileChanged(colorProfile);
      }
   }

   public SVGNumberList getColors() {
      return this;
   }

   public int getNumberOfItems() {
      if (this.getColorType() != 2) {
         throw new DOMException((short)12, "");
      } else {
         Value value = this.valueProvider.getValue();
         return ((ICCColor)value.item(1)).getNumberOfColors();
      }
   }

   public void clear() throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         this.iccColors = null;
         this.handler.colorsCleared();
      }
   }

   public SVGNumber initialize(SVGNumber newItem) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         float f = newItem.getValue();
         this.iccColors = new ArrayList();
         SVGNumber result = new ColorNumber(f);
         this.iccColors.add(result);
         this.handler.colorsInitialized(f);
         return result;
      }
   }

   public SVGNumber getItem(int index) throws DOMException {
      if (this.getColorType() != 2) {
         throw new DOMException((short)1, "");
      } else {
         int n = this.getNumberOfItems();
         if (index >= 0 && index < n) {
            if (this.iccColors == null) {
               this.iccColors = new ArrayList(n);

               for(int i = this.iccColors.size(); i < n; ++i) {
                  this.iccColors.add((Object)null);
               }
            }

            Value value = this.valueProvider.getValue().item(1);
            float f = ((ICCColor)value).getColor(index);
            SVGNumber result = new ColorNumber(f);
            this.iccColors.set(index, result);
            return result;
         } else {
            throw new DOMException((short)1, "");
         }
      }
   }

   public SVGNumber insertItemBefore(SVGNumber newItem, int index) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         int n = this.getNumberOfItems();
         if (index >= 0 && index <= n) {
            if (this.iccColors == null) {
               this.iccColors = new ArrayList(n);

               for(int i = this.iccColors.size(); i < n; ++i) {
                  this.iccColors.add((Object)null);
               }
            }

            float f = newItem.getValue();
            SVGNumber result = new ColorNumber(f);
            this.iccColors.add(index, result);
            this.handler.colorInsertedBefore(f, index);
            return result;
         } else {
            throw new DOMException((short)1, "");
         }
      }
   }

   public SVGNumber replaceItem(SVGNumber newItem, int index) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         int n = this.getNumberOfItems();
         if (index >= 0 && index < n) {
            if (this.iccColors == null) {
               this.iccColors = new ArrayList(n);

               for(int i = this.iccColors.size(); i < n; ++i) {
                  this.iccColors.add((Object)null);
               }
            }

            float f = newItem.getValue();
            SVGNumber result = new ColorNumber(f);
            this.iccColors.set(index, result);
            this.handler.colorReplaced(f, index);
            return result;
         } else {
            throw new DOMException((short)1, "");
         }
      }
   }

   public SVGNumber removeItem(int index) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         int n = this.getNumberOfItems();
         if (index >= 0 && index < n) {
            SVGNumber result = null;
            if (this.iccColors != null) {
               result = (ColorNumber)this.iccColors.get(index);
            }

            if (result == null) {
               Value value = this.valueProvider.getValue().item(1);
               result = new ColorNumber(((ICCColor)value).getColor(index));
            }

            this.handler.colorRemoved(index);
            return result;
         } else {
            throw new DOMException((short)1, "");
         }
      }
   }

   public SVGNumber appendItem(SVGNumber newItem) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         if (this.iccColors == null) {
            int n = this.getNumberOfItems();
            this.iccColors = new ArrayList(n);

            for(int i = 0; i < n; ++i) {
               this.iccColors.add((Object)null);
            }
         }

         float f = newItem.getValue();
         SVGNumber result = new ColorNumber(f);
         this.iccColors.add(result);
         this.handler.colorAppend(f);
         return result;
      }
   }

   protected class BlueComponent extends FloatComponent {
      protected BlueComponent() {
         super();
      }

      protected Value getValue() {
         return CSSOMSVGColor.this.valueProvider.getValue().getBlue();
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMSVGColor.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMSVGColor.this.handler.blueTextChanged(cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMSVGColor.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMSVGColor.this.handler.blueFloatValueChanged(unitType, floatValue);
         }
      }
   }

   protected class GreenComponent extends FloatComponent {
      protected GreenComponent() {
         super();
      }

      protected Value getValue() {
         return CSSOMSVGColor.this.valueProvider.getValue().getGreen();
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMSVGColor.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMSVGColor.this.handler.greenTextChanged(cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMSVGColor.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMSVGColor.this.handler.greenFloatValueChanged(unitType, floatValue);
         }
      }
   }

   protected class RedComponent extends FloatComponent {
      protected RedComponent() {
         super();
      }

      protected Value getValue() {
         return CSSOMSVGColor.this.valueProvider.getValue().getRed();
      }

      public void setCssText(String cssText) throws DOMException {
         if (CSSOMSVGColor.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMSVGColor.this.handler.redTextChanged(cssText);
         }
      }

      public void setFloatValue(short unitType, float floatValue) throws DOMException {
         if (CSSOMSVGColor.this.handler == null) {
            throw new DOMException((short)7, "");
         } else {
            this.getValue();
            CSSOMSVGColor.this.handler.redFloatValueChanged(unitType, floatValue);
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
         return CSSOMSVGColor.this.valueProvider.getValue().getStringValue();
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

   public abstract class AbstractModificationHandler implements ModificationHandler {
      protected abstract Value getValue();

      public void redTextChanged(String text) throws DOMException {
         StringBuffer sb = new StringBuffer(40);
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 1:
               sb.append("rgb(");
               sb.append(text);
               sb.append(',');
               sb.append(value.getGreen().getCssText());
               sb.append(',');
               sb.append(value.getBlue().getCssText());
               sb.append(')');
               break;
            case 2:
               sb.append("rgb(");
               sb.append(text);
               sb.append(',');
               sb.append(value.item(0).getGreen().getCssText());
               sb.append(',');
               sb.append(value.item(0).getBlue().getCssText());
               sb.append(')');
               sb.append(value.item(1).getCssText());
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(sb.toString());
      }

      public void redFloatValueChanged(short unit, float fValue) throws DOMException {
         StringBuffer sb = new StringBuffer(40);
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 1:
               sb.append("rgb(");
               sb.append(FloatValue.getCssText(unit, fValue));
               sb.append(',');
               sb.append(value.getGreen().getCssText());
               sb.append(',');
               sb.append(value.getBlue().getCssText());
               sb.append(')');
               break;
            case 2:
               sb.append("rgb(");
               sb.append(FloatValue.getCssText(unit, fValue));
               sb.append(',');
               sb.append(value.item(0).getGreen().getCssText());
               sb.append(',');
               sb.append(value.item(0).getBlue().getCssText());
               sb.append(')');
               sb.append(value.item(1).getCssText());
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(sb.toString());
      }

      public void greenTextChanged(String text) throws DOMException {
         StringBuffer sb = new StringBuffer(40);
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 1:
               sb.append("rgb(");
               sb.append(value.getRed().getCssText());
               sb.append(',');
               sb.append(text);
               sb.append(',');
               sb.append(value.getBlue().getCssText());
               sb.append(')');
               break;
            case 2:
               sb.append("rgb(");
               sb.append(value.item(0).getRed().getCssText());
               sb.append(',');
               sb.append(text);
               sb.append(',');
               sb.append(value.item(0).getBlue().getCssText());
               sb.append(')');
               sb.append(value.item(1).getCssText());
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(sb.toString());
      }

      public void greenFloatValueChanged(short unit, float fValue) throws DOMException {
         StringBuffer sb = new StringBuffer(40);
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 1:
               sb.append("rgb(");
               sb.append(value.getRed().getCssText());
               sb.append(',');
               sb.append(FloatValue.getCssText(unit, fValue));
               sb.append(',');
               sb.append(value.getBlue().getCssText());
               sb.append(')');
               break;
            case 2:
               sb.append("rgb(");
               sb.append(value.item(0).getRed().getCssText());
               sb.append(',');
               sb.append(FloatValue.getCssText(unit, fValue));
               sb.append(',');
               sb.append(value.item(0).getBlue().getCssText());
               sb.append(')');
               sb.append(value.item(1).getCssText());
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(sb.toString());
      }

      public void blueTextChanged(String text) throws DOMException {
         StringBuffer sb = new StringBuffer(40);
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 1:
               sb.append("rgb(");
               sb.append(value.getRed().getCssText());
               sb.append(',');
               sb.append(value.getGreen().getCssText());
               sb.append(',');
               sb.append(text);
               sb.append(')');
               break;
            case 2:
               sb.append("rgb(");
               sb.append(value.item(0).getRed().getCssText());
               sb.append(',');
               sb.append(value.item(0).getGreen().getCssText());
               sb.append(',');
               sb.append(text);
               sb.append(')');
               sb.append(value.item(1).getCssText());
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(sb.toString());
      }

      public void blueFloatValueChanged(short unit, float fValue) throws DOMException {
         StringBuffer sb = new StringBuffer(40);
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 1:
               sb.append("rgb(");
               sb.append(value.getRed().getCssText());
               sb.append(',');
               sb.append(value.getGreen().getCssText());
               sb.append(',');
               sb.append(FloatValue.getCssText(unit, fValue));
               sb.append(')');
               break;
            case 2:
               sb.append("rgb(");
               sb.append(value.item(0).getRed().getCssText());
               sb.append(',');
               sb.append(value.item(0).getGreen().getCssText());
               sb.append(',');
               sb.append(FloatValue.getCssText(unit, fValue));
               sb.append(')');
               sb.append(value.item(1).getCssText());
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(sb.toString());
      }

      public void rgbColorChanged(String text) throws DOMException {
         switch (CSSOMSVGColor.this.getColorType()) {
            case 2:
               text = text + this.getValue().item(1).getCssText();
            case 1:
               this.textChanged(text);
               return;
            default:
               throw new DOMException((short)7, "");
         }
      }

      public void rgbColorICCColorChanged(String rgb, String icc) throws DOMException {
         switch (CSSOMSVGColor.this.getColorType()) {
            case 2:
               this.textChanged(rgb + ' ' + icc);
               return;
            default:
               throw new DOMException((short)7, "");
         }
      }

      public void colorChanged(short type, String rgb, String icc) throws DOMException {
         switch (type) {
            case 1:
               this.textChanged(rgb);
               break;
            case 2:
               this.textChanged(rgb + ' ' + icc);
               break;
            case 3:
               this.textChanged("currentcolor");
               break;
            default:
               throw new DOMException((short)9, "");
         }

      }

      public void colorProfileChanged(String cp) throws DOMException {
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 2:
               StringBuffer sb = new StringBuffer(value.item(0).getCssText());
               sb.append(" icc-color(");
               sb.append(cp);
               ICCColor iccc = (ICCColor)value.item(1);

               for(int i = 0; i < iccc.getLength(); ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(')');
               this.textChanged(sb.toString());
               return;
            default:
               throw new DOMException((short)7, "");
         }
      }

      public void colorsCleared() throws DOMException {
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 2:
               StringBuffer sb = new StringBuffer(value.item(0).getCssText());
               sb.append(" icc-color(");
               ICCColor iccc = (ICCColor)value.item(1);
               sb.append(iccc.getColorProfile());
               sb.append(')');
               this.textChanged(sb.toString());
               return;
            default:
               throw new DOMException((short)7, "");
         }
      }

      public void colorsInitialized(float f) throws DOMException {
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 2:
               StringBuffer sb = new StringBuffer(value.item(0).getCssText());
               sb.append(" icc-color(");
               ICCColor iccc = (ICCColor)value.item(1);
               sb.append(iccc.getColorProfile());
               sb.append(',');
               sb.append(f);
               sb.append(')');
               this.textChanged(sb.toString());
               return;
            default:
               throw new DOMException((short)7, "");
         }
      }

      public void colorInsertedBefore(float f, int idx) throws DOMException {
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 2:
               StringBuffer sb = new StringBuffer(value.item(0).getCssText());
               sb.append(" icc-color(");
               ICCColor iccc = (ICCColor)value.item(1);
               sb.append(iccc.getColorProfile());

               int i;
               for(i = 0; i < idx; ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(',');
               sb.append(f);

               for(i = idx; i < iccc.getLength(); ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(')');
               this.textChanged(sb.toString());
               return;
            default:
               throw new DOMException((short)7, "");
         }
      }

      public void colorReplaced(float f, int idx) throws DOMException {
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 2:
               StringBuffer sb = new StringBuffer(value.item(0).getCssText());
               sb.append(" icc-color(");
               ICCColor iccc = (ICCColor)value.item(1);
               sb.append(iccc.getColorProfile());

               int i;
               for(i = 0; i < idx; ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(',');
               sb.append(f);

               for(i = idx + 1; i < iccc.getLength(); ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(')');
               this.textChanged(sb.toString());
               return;
            default:
               throw new DOMException((short)7, "");
         }
      }

      public void colorRemoved(int idx) throws DOMException {
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 2:
               StringBuffer sb = new StringBuffer(value.item(0).getCssText());
               sb.append(" icc-color(");
               ICCColor iccc = (ICCColor)value.item(1);
               sb.append(iccc.getColorProfile());

               int i;
               for(i = 0; i < idx; ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               for(i = idx + 1; i < iccc.getLength(); ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(')');
               this.textChanged(sb.toString());
               return;
            default:
               throw new DOMException((short)7, "");
         }
      }

      public void colorAppend(float f) throws DOMException {
         Value value = this.getValue();
         switch (CSSOMSVGColor.this.getColorType()) {
            case 2:
               StringBuffer sb = new StringBuffer(value.item(0).getCssText());
               sb.append(" icc-color(");
               ICCColor iccc = (ICCColor)value.item(1);
               sb.append(iccc.getColorProfile());

               for(int i = 0; i < iccc.getLength(); ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(',');
               sb.append(f);
               sb.append(')');
               this.textChanged(sb.toString());
               return;
            default:
               throw new DOMException((short)7, "");
         }
      }
   }

   public interface ModificationHandler {
      void textChanged(String var1) throws DOMException;

      void redTextChanged(String var1) throws DOMException;

      void redFloatValueChanged(short var1, float var2) throws DOMException;

      void greenTextChanged(String var1) throws DOMException;

      void greenFloatValueChanged(short var1, float var2) throws DOMException;

      void blueTextChanged(String var1) throws DOMException;

      void blueFloatValueChanged(short var1, float var2) throws DOMException;

      void rgbColorChanged(String var1) throws DOMException;

      void rgbColorICCColorChanged(String var1, String var2) throws DOMException;

      void colorChanged(short var1, String var2, String var3) throws DOMException;

      void colorProfileChanged(String var1) throws DOMException;

      void colorsCleared() throws DOMException;

      void colorsInitialized(float var1) throws DOMException;

      void colorInsertedBefore(float var1, int var2) throws DOMException;

      void colorReplaced(float var1, int var2) throws DOMException;

      void colorRemoved(int var1) throws DOMException;

      void colorAppend(float var1) throws DOMException;
   }

   public interface ValueProvider {
      Value getValue();
   }

   protected class ColorNumber implements SVGNumber {
      protected float value;

      public ColorNumber(float f) {
         this.value = f;
      }

      public float getValue() {
         if (CSSOMSVGColor.this.iccColors == null) {
            return this.value;
         } else {
            int idx = CSSOMSVGColor.this.iccColors.indexOf(this);
            if (idx == -1) {
               return this.value;
            } else {
               Value value = CSSOMSVGColor.this.valueProvider.getValue().item(1);
               return ((ICCColor)value).getColor(idx);
            }
         }
      }

      public void setValue(float f) {
         this.value = f;
         if (CSSOMSVGColor.this.iccColors != null) {
            int idx = CSSOMSVGColor.this.iccColors.indexOf(this);
            if (idx != -1) {
               if (CSSOMSVGColor.this.handler == null) {
                  throw new DOMException((short)7, "");
               } else {
                  CSSOMSVGColor.this.handler.colorReplaced(f, idx);
               }
            }
         }
      }
   }
}
