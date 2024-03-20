package org.apache.batik.bridge;

import java.util.ArrayList;
import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.anim.TransformAnimation;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.anim.values.AnimatableTransformListValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.svg.SVGOMTransform;

public class SVGAnimateTransformElementBridge extends SVGAnimateElementBridge {
   public String getLocalName() {
      return "animateTransform";
   }

   public Bridge getInstance() {
      return new SVGAnimateTransformElementBridge();
   }

   protected AbstractAnimation createAnimation(AnimationTarget target) {
      short type = this.parseType();
      AnimatableValue from = null;
      AnimatableValue to = null;
      AnimatableValue by = null;
      if (this.element.hasAttributeNS((String)null, "from")) {
         from = this.parseValue(this.element.getAttributeNS((String)null, "from"), type, target);
      }

      if (this.element.hasAttributeNS((String)null, "to")) {
         to = this.parseValue(this.element.getAttributeNS((String)null, "to"), type, target);
      }

      if (this.element.hasAttributeNS((String)null, "by")) {
         by = this.parseValue(this.element.getAttributeNS((String)null, "by"), type, target);
      }

      return new TransformAnimation(this.timedElement, this, this.parseCalcMode(), this.parseKeyTimes(), this.parseKeySplines(), this.parseAdditive(), this.parseAccumulate(), this.parseValues(type, target), from, to, by, type);
   }

   protected short parseType() {
      String typeString = this.element.getAttributeNS((String)null, "type");
      if (typeString.equals("translate")) {
         return 2;
      } else if (typeString.equals("scale")) {
         return 3;
      } else if (typeString.equals("rotate")) {
         return 4;
      } else if (typeString.equals("skewX")) {
         return 5;
      } else if (typeString.equals("skewY")) {
         return 6;
      } else {
         throw new BridgeException(this.ctx, this.element, "attribute.malformed", new Object[]{"type", typeString});
      }
   }

   protected AnimatableValue parseValue(String s, short type, AnimationTarget target) {
      float val2 = 0.0F;
      float val3 = 0.0F;
      int i = 0;
      char c = ',';

      int len;
      for(len = s.length(); i < len; ++i) {
         c = s.charAt(i);
         if (c == ' ' || c == ',') {
            break;
         }
      }

      float val1 = Float.parseFloat(s.substring(0, i));
      if (i < len) {
         ++i;
      }

      int count = 1;
      if (i < len && c == ' ') {
         while(i < len) {
            c = s.charAt(i);
            if (c != ' ') {
               break;
            }

            ++i;
         }

         if (c == ',') {
            ++i;
         }
      }

      while(i < len && s.charAt(i) == ' ') {
         ++i;
      }

      int j = i;
      if (i < len && type != 5 && type != 6) {
         while(i < len) {
            c = s.charAt(i);
            if (c == ' ' || c == ',') {
               break;
            }

            ++i;
         }

         val2 = Float.parseFloat(s.substring(j, i));
         if (i < len) {
            ++i;
         }

         ++count;
         if (i < len && c == ' ') {
            while(i < len) {
               c = s.charAt(i);
               if (c != ' ') {
                  break;
               }

               ++i;
            }

            if (c == ',') {
               ++i;
            }
         }

         while(i < len && s.charAt(i) == ' ') {
            ++i;
         }

         j = i;
         if (i < len && type == 4) {
            while(i < len) {
               c = s.charAt(i);
               if (c == ',' || c == ' ') {
                  break;
               }

               ++i;
            }

            val3 = Float.parseFloat(s.substring(j, i));
            if (i < len) {
               ++i;
            }

            ++count;

            while(i < len && s.charAt(i) == ' ') {
               ++i;
            }
         }
      }

      if (i != len) {
         return null;
      } else {
         SVGOMTransform t = new SVGOMTransform();
         switch (type) {
            case 2:
               if (count == 2) {
                  t.setTranslate(val1, val2);
               } else {
                  t.setTranslate(val1, 0.0F);
               }
               break;
            case 3:
               if (count == 2) {
                  t.setScale(val1, val2);
               } else {
                  t.setScale(val1, val1);
               }
               break;
            case 4:
               if (count == 3) {
                  t.setRotate(val1, val2, val3);
               } else {
                  t.setRotate(val1, 0.0F, 0.0F);
               }
               break;
            case 5:
               t.setSkewX(val1);
               break;
            case 6:
               t.setSkewY(val1);
         }

         return new AnimatableTransformListValue(target, t);
      }
   }

   protected AnimatableValue[] parseValues(short type, AnimationTarget target) {
      String valuesString = this.element.getAttributeNS((String)null, "values");
      int len = valuesString.length();
      if (len == 0) {
         return null;
      } else {
         ArrayList values = new ArrayList(7);
         int i = 0;
         int start = false;

         label43:
         while(i < len) {
            while(valuesString.charAt(i) == ' ') {
               ++i;
               if (i == len) {
                  break label43;
               }
            }

            int start = i++;
            if (i < len) {
               for(char c = valuesString.charAt(i); c != ';'; c = valuesString.charAt(i)) {
                  ++i;
                  if (i == len) {
                     break;
                  }
               }
            }

            int end = i++;
            String valueString = valuesString.substring(start, end);
            AnimatableValue value = this.parseValue(valueString, type, target);
            if (value == null) {
               throw new BridgeException(this.ctx, this.element, "attribute.malformed", new Object[]{"values", valuesString});
            }

            values.add(value);
         }

         AnimatableValue[] ret = new AnimatableValue[values.size()];
         return (AnimatableValue[])((AnimatableValue[])values.toArray(ret));
      }
   }

   protected boolean canAnimateType(int type) {
      return type == 9;
   }
}
