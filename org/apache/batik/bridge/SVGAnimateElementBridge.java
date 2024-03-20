package org.apache.batik.bridge;

import java.util.ArrayList;
import java.util.List;
import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.anim.SimpleAnimation;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.anim.values.AnimatableValue;

public class SVGAnimateElementBridge extends SVGAnimationElementBridge {
   public String getLocalName() {
      return "animate";
   }

   public Bridge getInstance() {
      return new SVGAnimateElementBridge();
   }

   protected AbstractAnimation createAnimation(AnimationTarget target) {
      AnimatableValue from = this.parseAnimatableValue("from");
      AnimatableValue to = this.parseAnimatableValue("to");
      AnimatableValue by = this.parseAnimatableValue("by");
      return new SimpleAnimation(this.timedElement, this, this.parseCalcMode(), this.parseKeyTimes(), this.parseKeySplines(), this.parseAdditive(), this.parseAccumulate(), this.parseValues(), from, to, by);
   }

   protected int parseCalcMode() {
      if ((this.animationType != 1 || this.targetElement.isPropertyAdditive(this.attributeLocalName)) && (this.animationType != 0 || this.targetElement.isAttributeAdditive(this.attributeNamespaceURI, this.attributeLocalName))) {
         String calcModeString = this.element.getAttributeNS((String)null, "calcMode");
         if (calcModeString.length() == 0) {
            return this.getDefaultCalcMode();
         } else if (calcModeString.equals("linear")) {
            return 1;
         } else if (calcModeString.equals("discrete")) {
            return 0;
         } else if (calcModeString.equals("paced")) {
            return 2;
         } else if (calcModeString.equals("spline")) {
            return 3;
         } else {
            throw new BridgeException(this.ctx, this.element, "attribute.malformed", new Object[]{"calcMode", calcModeString});
         }
      } else {
         return 0;
      }
   }

   protected boolean parseAdditive() {
      String additiveString = this.element.getAttributeNS((String)null, "additive");
      if (additiveString.length() != 0 && !additiveString.equals("replace")) {
         if (additiveString.equals("sum")) {
            return true;
         } else {
            throw new BridgeException(this.ctx, this.element, "attribute.malformed", new Object[]{"additive", additiveString});
         }
      } else {
         return false;
      }
   }

   protected boolean parseAccumulate() {
      String accumulateString = this.element.getAttributeNS((String)null, "accumulate");
      if (accumulateString.length() != 0 && !accumulateString.equals("none")) {
         if (accumulateString.equals("sum")) {
            return true;
         } else {
            throw new BridgeException(this.ctx, this.element, "attribute.malformed", new Object[]{"accumulate", accumulateString});
         }
      } else {
         return false;
      }
   }

   protected AnimatableValue[] parseValues() {
      boolean isCSS = this.animationType == 1;
      String valuesString = this.element.getAttributeNS((String)null, "values");
      int len = valuesString.length();
      if (len == 0) {
         return null;
      } else {
         ArrayList values = new ArrayList(7);
         int i = 0;
         int start = false;

         label48:
         while(i < len) {
            while(valuesString.charAt(i) == ' ') {
               ++i;
               if (i == len) {
                  break label48;
               }
            }

            int start = i++;
            if (i != len) {
               for(char c = valuesString.charAt(i); c != ';'; c = valuesString.charAt(i)) {
                  ++i;
                  if (i == len) {
                     break;
                  }
               }
            }

            int end = i++;
            AnimatableValue val = this.eng.parseAnimatableValue(this.element, this.animationTarget, this.attributeNamespaceURI, this.attributeLocalName, isCSS, valuesString.substring(start, end));
            if (!this.checkValueType(val)) {
               throw new BridgeException(this.ctx, this.element, "attribute.malformed", new Object[]{"values", valuesString});
            }

            values.add(val);
         }

         AnimatableValue[] ret = new AnimatableValue[values.size()];
         return (AnimatableValue[])((AnimatableValue[])values.toArray(ret));
      }
   }

   protected float[] parseKeyTimes() {
      String keyTimesString = this.element.getAttributeNS((String)null, "keyTimes");
      int len = keyTimesString.length();
      if (len == 0) {
         return null;
      } else {
         ArrayList keyTimes = new ArrayList(7);
         int i = 0;
         int start = false;

         label58:
         while(i < len) {
            while(keyTimesString.charAt(i) == ' ') {
               ++i;
               if (i == len) {
                  break label58;
               }
            }

            int start = i++;
            if (i != len) {
               for(char c = keyTimesString.charAt(i); c != ' ' && c != ';'; c = keyTimesString.charAt(i)) {
                  ++i;
                  if (i == len) {
                     break;
                  }
               }
            }

            int end = i++;

            try {
               float keyTime = Float.parseFloat(keyTimesString.substring(start, end));
               keyTimes.add(keyTime);
            } catch (NumberFormatException var10) {
               throw new BridgeException(this.ctx, this.element, var10, "attribute.malformed", new Object[]{"keyTimes", keyTimesString});
            }
         }

         len = keyTimes.size();
         float[] ret = new float[len];

         for(int j = 0; j < len; ++j) {
            ret[j] = (Float)keyTimes.get(j);
         }

         return ret;
      }
   }

   protected float[] parseKeySplines() {
      String keySplinesString = this.element.getAttributeNS((String)null, "keySplines");
      int len = keySplinesString.length();
      if (len == 0) {
         return null;
      } else {
         List keySplines = new ArrayList(7);
         int count = 0;
         int i = 0;
         int start = false;

         label88:
         while(i < len) {
            while(keySplinesString.charAt(i) == ' ') {
               ++i;
               if (i == len) {
                  break label88;
               }
            }

            int start = i++;
            int end;
            if (i != len) {
               char c;
               for(c = keySplinesString.charAt(i); c != ' ' && c != ',' && c != ';'; c = keySplinesString.charAt(i)) {
                  ++i;
                  if (i == len) {
                     break;
                  }
               }

               end = i++;
               if (c == ' ') {
                  while(i != len) {
                     c = keySplinesString.charAt(i++);
                     if (c != ' ') {
                        break;
                     }
                  }

                  if (c != ';' && c != ',') {
                     --i;
                  }
               }

               if (c == ';') {
                  if (count != 3) {
                     throw new BridgeException(this.ctx, this.element, "attribute.malformed", new Object[]{"keySplines", keySplinesString});
                  }

                  count = 0;
               } else {
                  ++count;
               }
            } else {
               end = i++;
            }

            try {
               float keySplineValue = Float.parseFloat(keySplinesString.substring(start, end));
               keySplines.add(keySplineValue);
            } catch (NumberFormatException var11) {
               throw new BridgeException(this.ctx, this.element, var11, "attribute.malformed", new Object[]{"keySplines", keySplinesString});
            }
         }

         len = keySplines.size();
         float[] ret = new float[len];

         for(int j = 0; j < len; ++j) {
            ret[j] = (Float)keySplines.get(j);
         }

         return ret;
      }
   }

   protected int getDefaultCalcMode() {
      return 1;
   }

   protected boolean canAnimateType(int type) {
      return true;
   }
}
