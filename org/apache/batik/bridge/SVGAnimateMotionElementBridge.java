package org.apache.batik.bridge;

import java.util.ArrayList;
import java.util.List;
import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.anim.MotionAnimation;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.anim.dom.SVGOMPathElement;
import org.apache.batik.anim.values.AnimatableMotionPointValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.svg.SVGAnimatedPathDataSupport;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.AngleHandler;
import org.apache.batik.parser.AngleParser;
import org.apache.batik.parser.LengthArrayProducer;
import org.apache.batik.parser.LengthPairListParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SVGAnimateMotionElementBridge extends SVGAnimateElementBridge {
   public String getLocalName() {
      return "animateMotion";
   }

   public Bridge getInstance() {
      return new SVGAnimateMotionElementBridge();
   }

   protected AbstractAnimation createAnimation(AnimationTarget target) {
      this.animationType = 2;
      this.attributeLocalName = "motion";
      AnimatableValue from = this.parseLengthPair("from");
      AnimatableValue to = this.parseLengthPair("to");
      AnimatableValue by = this.parseLengthPair("by");
      boolean rotateAuto = false;
      boolean rotateAutoReverse = false;
      float rotateAngle = 0.0F;
      short rotateAngleUnit = 0;
      String rotateString = this.element.getAttributeNS((String)null, "rotate");
      if (rotateString.length() != 0) {
         if (rotateString.equals("auto")) {
            rotateAuto = true;
         } else if (rotateString.equals("auto-reverse")) {
            rotateAuto = true;
            rotateAutoReverse = true;
         } else {
            AngleParser ap = new AngleParser();

            class Handler implements AngleHandler {
               float theAngle;
               short theUnit = 1;

               public void startAngle() throws ParseException {
               }

               public void angleValue(float v) throws ParseException {
                  this.theAngle = v;
               }

               public void deg() throws ParseException {
                  this.theUnit = 2;
               }

               public void grad() throws ParseException {
                  this.theUnit = 4;
               }

               public void rad() throws ParseException {
                  this.theUnit = 3;
               }

               public void endAngle() throws ParseException {
               }
            }

            Handler h = new Handler();
            ap.setAngleHandler(h);

            try {
               ap.parse(rotateString);
            } catch (ParseException var13) {
               throw new BridgeException(this.ctx, this.element, var13, "attribute.malformed", new Object[]{"rotate", rotateString});
            }

            rotateAngle = h.theAngle;
            rotateAngleUnit = h.theUnit;
         }
      }

      return new MotionAnimation(this.timedElement, this, this.parseCalcMode(), this.parseKeyTimes(), this.parseKeySplines(), this.parseAdditive(), this.parseAccumulate(), this.parseValues(), from, to, by, this.parsePath(), this.parseKeyPoints(), rotateAuto, rotateAutoReverse, rotateAngle, rotateAngleUnit);
   }

   protected ExtendedGeneralPath parsePath() {
      String uri;
      for(Node n = this.element.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1 && "http://www.w3.org/2000/svg".equals(n.getNamespaceURI()) && "mpath".equals(n.getLocalName())) {
            uri = XLinkSupport.getXLinkHref((Element)n);
            Element path = this.ctx.getReferencedElement(this.element, uri);
            if ("http://www.w3.org/2000/svg".equals(path.getNamespaceURI()) && "path".equals(path.getLocalName())) {
               SVGOMPathElement pathElt = (SVGOMPathElement)path;
               AWTPathProducer app = new AWTPathProducer();
               SVGAnimatedPathDataSupport.handlePathSegList(pathElt.getPathSegList(), app);
               return (ExtendedGeneralPath)app.getShape();
            }

            throw new BridgeException(this.ctx, this.element, "uri.badTarget", new Object[]{uri});
         }
      }

      uri = this.element.getAttributeNS((String)null, "path");
      if (uri.length() == 0) {
         return null;
      } else {
         try {
            AWTPathProducer app = new AWTPathProducer();
            PathParser pp = new PathParser();
            pp.setPathHandler(app);
            pp.parse(uri);
            return (ExtendedGeneralPath)app.getShape();
         } catch (ParseException var6) {
            throw new BridgeException(this.ctx, this.element, var6, "attribute.malformed", new Object[]{"path", uri});
         }
      }
   }

   protected float[] parseKeyPoints() {
      String keyPointsString = this.element.getAttributeNS((String)null, "keyPoints");
      int len = keyPointsString.length();
      if (len == 0) {
         return null;
      } else {
         List keyPoints = new ArrayList(7);
         int i = 0;
         int start = false;

         label61:
         while(i < len) {
            while(keyPointsString.charAt(i) == ' ') {
               ++i;
               if (i == len) {
                  break label61;
               }
            }

            int start = i++;
            if (i != len) {
               for(char c = keyPointsString.charAt(i); c != ' ' && c != ';' && c != ','; c = keyPointsString.charAt(i)) {
                  ++i;
                  if (i == len) {
                     break;
                  }
               }
            }

            int end = i++;

            try {
               float keyPointCoord = Float.parseFloat(keyPointsString.substring(start, end));
               keyPoints.add(keyPointCoord);
            } catch (NumberFormatException var10) {
               throw new BridgeException(this.ctx, this.element, var10, "attribute.malformed", new Object[]{"keyPoints", keyPointsString});
            }
         }

         len = keyPoints.size();
         float[] ret = new float[len];

         for(int j = 0; j < len; ++j) {
            ret[j] = (Float)keyPoints.get(j);
         }

         return ret;
      }
   }

   protected int getDefaultCalcMode() {
      return 2;
   }

   protected AnimatableValue[] parseValues() {
      String valuesString = this.element.getAttributeNS((String)null, "values");
      int len = valuesString.length();
      return len == 0 ? null : this.parseValues(valuesString);
   }

   protected AnimatableValue[] parseValues(String s) {
      try {
         LengthPairListParser lplp = new LengthPairListParser();
         LengthArrayProducer lap = new LengthArrayProducer();
         lplp.setLengthListHandler(lap);
         lplp.parse(s);
         short[] types = lap.getLengthTypeArray();
         float[] values = lap.getLengthValueArray();
         AnimatableValue[] ret = new AnimatableValue[types.length / 2];

         for(int i = 0; i < types.length; i += 2) {
            float x = this.animationTarget.svgToUserSpace(values[i], types[i], (short)1);
            float y = this.animationTarget.svgToUserSpace(values[i + 1], types[i + 1], (short)2);
            ret[i / 2] = new AnimatableMotionPointValue(this.animationTarget, x, y, 0.0F);
         }

         return ret;
      } catch (ParseException var10) {
         throw new BridgeException(this.ctx, this.element, var10, "attribute.malformed", new Object[]{"values", s});
      }
   }

   protected AnimatableValue parseLengthPair(String ln) {
      String s = this.element.getAttributeNS((String)null, ln);
      return s.length() == 0 ? null : this.parseValues(s)[0];
   }

   public AnimatableValue getUnderlyingValue() {
      return new AnimatableMotionPointValue(this.animationTarget, 0.0F, 0.0F, 0.0F);
   }

   protected void initializeAnimation() {
      String uri = XLinkSupport.getXLinkHref(this.element);
      Object t;
      if (uri.length() == 0) {
         t = this.element.getParentNode();
      } else {
         t = this.ctx.getReferencedElement(this.element, uri);
         if (((Node)t).getOwnerDocument() != this.element.getOwnerDocument()) {
            throw new BridgeException(this.ctx, this.element, "uri.badTarget", new Object[]{uri});
         }
      }

      this.animationTarget = null;
      if (t instanceof SVGOMElement) {
         this.targetElement = (SVGOMElement)t;
         this.animationTarget = this.targetElement;
      }

      if (this.animationTarget == null) {
         throw new BridgeException(this.ctx, this.element, "uri.badTarget", new Object[]{uri});
      } else {
         this.timedElement = this.createTimedElement();
         this.animation = this.createAnimation(this.animationTarget);
         this.eng.addAnimation(this.animationTarget, (short)2, this.attributeNamespaceURI, this.attributeLocalName, this.animation);
      }
   }
}
