package org.apache.batik.css.dom;

import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.ICCColor;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGPaint;

public class CSSOMSVGPaint extends CSSOMSVGColor implements SVGPaint {
   public CSSOMSVGPaint(CSSOMSVGColor.ValueProvider vp) {
      super(vp);
   }

   public void setModificationHandler(CSSOMSVGColor.ModificationHandler h) {
      if (!(h instanceof PaintModificationHandler)) {
         throw new IllegalArgumentException();
      } else {
         super.setModificationHandler(h);
      }
   }

   public short getColorType() {
      throw new DOMException((short)15, "");
   }

   public short getPaintType() {
      Value value = this.valueProvider.getValue();
      switch (value.getCssValueType()) {
         case 1:
            switch (value.getPrimitiveType()) {
               case 20:
                  return 107;
               case 21:
                  String str = value.getStringValue();
                  if (str.equalsIgnoreCase("none")) {
                     return 101;
                  }

                  if (str.equalsIgnoreCase("currentcolor")) {
                     return 102;
                  }

                  return 1;
               case 25:
                  return 1;
               default:
                  return 0;
            }
         case 2:
            Value v0 = value.item(0);
            Value v1 = value.item(1);
            switch (v0.getPrimitiveType()) {
               case 20:
                  if (v1.getCssValueType() == 2) {
                     return 106;
                  }

                  switch (v1.getPrimitiveType()) {
                     case 21:
                        String str = v1.getStringValue();
                        if (str.equalsIgnoreCase("none")) {
                           return 103;
                        }

                        if (str.equalsIgnoreCase("currentcolor")) {
                           return 104;
                        }

                        return 105;
                     case 25:
                        return 105;
                  }
               case 25:
                  return 2;
               case 21:
                  return 2;
            }
      }

      return 0;
   }

   public String getUri() {
      switch (this.getPaintType()) {
         case 103:
         case 104:
         case 105:
         case 106:
            return this.valueProvider.getValue().item(0).getStringValue();
         case 107:
            return this.valueProvider.getValue().getStringValue();
         default:
            throw new InternalError();
      }
   }

   public void setUri(String uri) {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         ((PaintModificationHandler)this.handler).uriChanged(uri);
      }
   }

   public void setPaint(short paintType, String uri, String rgbColor, String iccColor) {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         ((PaintModificationHandler)this.handler).paintChanged(paintType, uri, rgbColor, iccColor);
      }
   }

   public abstract class AbstractModificationHandler implements PaintModificationHandler {
      protected abstract Value getValue();

      public void redTextChanged(String text) throws DOMException {
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 1:
               text = "rgb(" + text + ", " + this.getValue().getGreen().getCssText() + ", " + this.getValue().getBlue().getCssText() + ')';
               break;
            case 2:
               text = "rgb(" + text + ", " + this.getValue().item(0).getGreen().getCssText() + ", " + this.getValue().item(0).getBlue().getCssText() + ") " + this.getValue().item(1).getCssText();
               break;
            case 105:
               text = this.getValue().item(0) + " rgb(" + text + ", " + this.getValue().item(1).getGreen().getCssText() + ", " + this.getValue().item(1).getBlue().getCssText() + ')';
               break;
            case 106:
               text = this.getValue().item(0) + " rgb(" + text + ", " + this.getValue().item(1).getGreen().getCssText() + ", " + this.getValue().item(1).getBlue().getCssText() + ") " + this.getValue().item(2).getCssText();
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(text);
      }

      public void redFloatValueChanged(short unit, float value) throws DOMException {
         String text;
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 1:
               text = "rgb(" + FloatValue.getCssText(unit, value) + ", " + this.getValue().getGreen().getCssText() + ", " + this.getValue().getBlue().getCssText() + ')';
               break;
            case 2:
               text = "rgb(" + FloatValue.getCssText(unit, value) + ", " + this.getValue().item(0).getGreen().getCssText() + ", " + this.getValue().item(0).getBlue().getCssText() + ") " + this.getValue().item(1).getCssText();
               break;
            case 105:
               text = this.getValue().item(0) + " rgb(" + FloatValue.getCssText(unit, value) + ", " + this.getValue().item(1).getGreen().getCssText() + ", " + this.getValue().item(1).getBlue().getCssText() + ')';
               break;
            case 106:
               text = this.getValue().item(0) + " rgb(" + FloatValue.getCssText(unit, value) + ", " + this.getValue().item(1).getGreen().getCssText() + ", " + this.getValue().item(1).getBlue().getCssText() + ") " + this.getValue().item(2).getCssText();
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(text);
      }

      public void greenTextChanged(String text) throws DOMException {
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 1:
               text = "rgb(" + this.getValue().getRed().getCssText() + ", " + text + ", " + this.getValue().getBlue().getCssText() + ')';
               break;
            case 2:
               text = "rgb(" + this.getValue().item(0).getRed().getCssText() + ", " + text + ", " + this.getValue().item(0).getBlue().getCssText() + ") " + this.getValue().item(1).getCssText();
               break;
            case 105:
               text = this.getValue().item(0) + " rgb(" + this.getValue().item(1).getRed().getCssText() + ", " + text + ", " + this.getValue().item(1).getBlue().getCssText() + ')';
               break;
            case 106:
               text = this.getValue().item(0) + " rgb(" + this.getValue().item(1).getRed().getCssText() + ", " + text + ", " + this.getValue().item(1).getBlue().getCssText() + ") " + this.getValue().item(2).getCssText();
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(text);
      }

      public void greenFloatValueChanged(short unit, float value) throws DOMException {
         String text;
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 1:
               text = "rgb(" + this.getValue().getRed().getCssText() + ", " + FloatValue.getCssText(unit, value) + ", " + this.getValue().getBlue().getCssText() + ')';
               break;
            case 2:
               text = "rgb(" + this.getValue().item(0).getRed().getCssText() + ", " + FloatValue.getCssText(unit, value) + ", " + this.getValue().item(0).getBlue().getCssText() + ") " + this.getValue().item(1).getCssText();
               break;
            case 105:
               text = this.getValue().item(0) + " rgb(" + this.getValue().item(1).getRed().getCssText() + ", " + FloatValue.getCssText(unit, value) + ", " + this.getValue().item(1).getBlue().getCssText() + ')';
               break;
            case 106:
               text = this.getValue().item(0) + " rgb(" + this.getValue().item(1).getRed().getCssText() + ", " + FloatValue.getCssText(unit, value) + ", " + this.getValue().item(1).getBlue().getCssText() + ") " + this.getValue().item(2).getCssText();
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(text);
      }

      public void blueTextChanged(String text) throws DOMException {
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 1:
               text = "rgb(" + this.getValue().getRed().getCssText() + ", " + this.getValue().getGreen().getCssText() + ", " + text + ')';
               break;
            case 2:
               text = "rgb(" + this.getValue().item(0).getRed().getCssText() + ", " + this.getValue().item(0).getGreen().getCssText() + ", " + text + ") " + this.getValue().item(1).getCssText();
               break;
            case 105:
               text = this.getValue().item(0) + " rgb(" + this.getValue().item(1).getRed().getCssText() + ", " + this.getValue().item(1).getGreen().getCssText() + ", " + text + ")";
               break;
            case 106:
               text = this.getValue().item(0) + " rgb(" + this.getValue().item(1).getRed().getCssText() + ", " + this.getValue().item(1).getGreen().getCssText() + ", " + text + ") " + this.getValue().item(2).getCssText();
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(text);
      }

      public void blueFloatValueChanged(short unit, float value) throws DOMException {
         String text;
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 1:
               text = "rgb(" + this.getValue().getRed().getCssText() + ", " + this.getValue().getGreen().getCssText() + ", " + FloatValue.getCssText(unit, value) + ')';
               break;
            case 2:
               text = "rgb(" + this.getValue().item(0).getRed().getCssText() + ", " + this.getValue().item(0).getGreen().getCssText() + ", " + FloatValue.getCssText(unit, value) + ") " + this.getValue().item(1).getCssText();
               break;
            case 105:
               text = this.getValue().item(0) + " rgb(" + this.getValue().item(1).getRed().getCssText() + ", " + this.getValue().item(1).getGreen().getCssText() + ", " + FloatValue.getCssText(unit, value) + ')';
               break;
            case 106:
               text = this.getValue().item(0) + " rgb(" + this.getValue().item(1).getRed().getCssText() + ", " + this.getValue().item(1).getGreen().getCssText() + ", " + FloatValue.getCssText(unit, value) + ") " + this.getValue().item(2).getCssText();
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(text);
      }

      public void rgbColorChanged(String text) throws DOMException {
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 1:
               break;
            case 2:
               text = text + this.getValue().item(1).getCssText();
               break;
            case 105:
               text = this.getValue().item(0).getCssText() + ' ' + text;
               break;
            case 106:
               text = this.getValue().item(0).getCssText() + ' ' + text + ' ' + this.getValue().item(2).getCssText();
               break;
            default:
               throw new DOMException((short)7, "");
         }

         this.textChanged(text);
      }

      public void rgbColorICCColorChanged(String rgb, String icc) throws DOMException {
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 2:
               this.textChanged(rgb + ' ' + icc);
               break;
            case 106:
               this.textChanged(this.getValue().item(0).getCssText() + ' ' + rgb + ' ' + icc);
               break;
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
            case 102:
               this.textChanged("currentcolor");
               break;
            default:
               throw new DOMException((short)9, "");
         }

      }

      public void colorProfileChanged(String cp) throws DOMException {
         StringBuffer sb;
         ICCColor iccc;
         int i;
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 2:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(" icc-color(");
               sb.append(cp);
               iccc = (ICCColor)this.getValue().item(1);

               for(i = 0; i < iccc.getLength(); ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(')');
               this.textChanged(sb.toString());
               break;
            case 106:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(' ');
               sb.append(this.getValue().item(1).getCssText());
               sb.append(" icc-color(");
               sb.append(cp);
               iccc = (ICCColor)this.getValue().item(1);

               for(i = 0; i < iccc.getLength(); ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(')');
               this.textChanged(sb.toString());
               break;
            default:
               throw new DOMException((short)7, "");
         }

      }

      public void colorsCleared() throws DOMException {
         StringBuffer sb;
         ICCColor iccc;
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 2:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());
               sb.append(')');
               this.textChanged(sb.toString());
               break;
            case 106:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(' ');
               sb.append(this.getValue().item(1).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());
               sb.append(')');
               this.textChanged(sb.toString());
               break;
            default:
               throw new DOMException((short)7, "");
         }

      }

      public void colorsInitialized(float f) throws DOMException {
         StringBuffer sb;
         ICCColor iccc;
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 2:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());
               sb.append(',');
               sb.append(f);
               sb.append(')');
               this.textChanged(sb.toString());
               break;
            case 106:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(' ');
               sb.append(this.getValue().item(1).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());
               sb.append(',');
               sb.append(f);
               sb.append(')');
               this.textChanged(sb.toString());
               break;
            default:
               throw new DOMException((short)7, "");
         }

      }

      public void colorInsertedBefore(float f, int idx) throws DOMException {
         StringBuffer sb;
         ICCColor iccc;
         int i;
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 2:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());

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
               break;
            case 106:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(' ');
               sb.append(this.getValue().item(1).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());

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
               break;
            default:
               throw new DOMException((short)7, "");
         }

      }

      public void colorReplaced(float f, int idx) throws DOMException {
         StringBuffer sb;
         ICCColor iccc;
         int i;
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 2:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());

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
               break;
            case 106:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(' ');
               sb.append(this.getValue().item(1).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());

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
               break;
            default:
               throw new DOMException((short)7, "");
         }

      }

      public void colorRemoved(int idx) throws DOMException {
         StringBuffer sb;
         ICCColor iccc;
         int i;
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 2:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());

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
               break;
            case 106:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(' ');
               sb.append(this.getValue().item(1).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());

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
               break;
            default:
               throw new DOMException((short)7, "");
         }

      }

      public void colorAppend(float f) throws DOMException {
         StringBuffer sb;
         ICCColor iccc;
         int i;
         switch (CSSOMSVGPaint.this.getPaintType()) {
            case 2:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());

               for(i = 0; i < iccc.getLength(); ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(',');
               sb.append(f);
               sb.append(')');
               this.textChanged(sb.toString());
               break;
            case 106:
               sb = new StringBuffer(this.getValue().item(0).getCssText());
               sb.append(' ');
               sb.append(this.getValue().item(1).getCssText());
               sb.append(" icc-color(");
               iccc = (ICCColor)this.getValue().item(1);
               sb.append(iccc.getColorProfile());

               for(i = 0; i < iccc.getLength(); ++i) {
                  sb.append(',');
                  sb.append(iccc.getColor(i));
               }

               sb.append(',');
               sb.append(f);
               sb.append(')');
               this.textChanged(sb.toString());
               break;
            default:
               throw new DOMException((short)7, "");
         }

      }

      public void uriChanged(String uri) {
         this.textChanged("url(" + uri + ") none");
      }

      public void paintChanged(short type, String uri, String rgb, String icc) {
         switch (type) {
            case 1:
               this.textChanged(rgb);
               break;
            case 2:
               this.textChanged(rgb + ' ' + icc);
               break;
            case 101:
               this.textChanged("none");
               break;
            case 102:
               this.textChanged("currentcolor");
               break;
            case 103:
               this.textChanged("url(" + uri + ") none");
               break;
            case 104:
               this.textChanged("url(" + uri + ") currentcolor");
               break;
            case 105:
               this.textChanged("url(" + uri + ") " + rgb);
               break;
            case 106:
               this.textChanged("url(" + uri + ") " + rgb + ' ' + icc);
               break;
            case 107:
               this.textChanged("url(" + uri + ')');
         }

      }
   }

   public interface PaintModificationHandler extends CSSOMSVGColor.ModificationHandler {
      void uriChanged(String var1);

      void paintChanged(short var1, String var2, String var3, String var4);
   }
}
