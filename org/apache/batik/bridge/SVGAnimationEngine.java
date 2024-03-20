package org.apache.batik.bridge;

import java.awt.Color;
import java.awt.Paint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.apache.batik.anim.AnimationEngine;
import org.apache.batik.anim.AnimationException;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.anim.dom.SVGStylableElement;
import org.apache.batik.anim.timing.TimedDocumentRoot;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.values.AnimatableAngleOrIdentValue;
import org.apache.batik.anim.values.AnimatableAngleValue;
import org.apache.batik.anim.values.AnimatableBooleanValue;
import org.apache.batik.anim.values.AnimatableColorValue;
import org.apache.batik.anim.values.AnimatableIntegerValue;
import org.apache.batik.anim.values.AnimatableLengthListValue;
import org.apache.batik.anim.values.AnimatableLengthOrIdentValue;
import org.apache.batik.anim.values.AnimatableLengthValue;
import org.apache.batik.anim.values.AnimatableNumberListValue;
import org.apache.batik.anim.values.AnimatableNumberOrIdentValue;
import org.apache.batik.anim.values.AnimatableNumberOrPercentageValue;
import org.apache.batik.anim.values.AnimatableNumberValue;
import org.apache.batik.anim.values.AnimatablePaintValue;
import org.apache.batik.anim.values.AnimatablePathDataValue;
import org.apache.batik.anim.values.AnimatablePointListValue;
import org.apache.batik.anim.values.AnimatablePreserveAspectRatioValue;
import org.apache.batik.anim.values.AnimatableRectValue;
import org.apache.batik.anim.values.AnimatableStringValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.parser.DefaultLengthHandler;
import org.apache.batik.parser.DefaultPreserveAspectRatioHandler;
import org.apache.batik.parser.FloatArrayProducer;
import org.apache.batik.parser.LengthArrayProducer;
import org.apache.batik.parser.LengthHandler;
import org.apache.batik.parser.LengthListParser;
import org.apache.batik.parser.LengthParser;
import org.apache.batik.parser.NumberListParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathArrayProducer;
import org.apache.batik.parser.PathParser;
import org.apache.batik.parser.PointsParser;
import org.apache.batik.parser.PreserveAspectRatioParser;
import org.apache.batik.util.RunnableQueue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.events.EventTarget;

public class SVGAnimationEngine extends AnimationEngine {
   protected BridgeContext ctx;
   protected CSSEngine cssEngine;
   protected boolean started;
   protected AnimationTickRunnable animationTickRunnable;
   protected float initialStartTime;
   protected UncomputedAnimatableStringValueFactory uncomputedAnimatableStringValueFactory = new UncomputedAnimatableStringValueFactory();
   protected AnimatableLengthOrIdentFactory animatableLengthOrIdentFactory = new AnimatableLengthOrIdentFactory();
   protected AnimatableNumberOrIdentFactory animatableNumberOrIdentFactory = new AnimatableNumberOrIdentFactory(false);
   protected Factory[] factories;
   protected boolean isSVG12;
   protected LinkedList initialBridges;
   protected StyleMap dummyStyleMap;
   protected AnimationThread animationThread;
   protected int animationLimitingMode;
   protected float animationLimitingAmount;
   protected static final Set animationEventNames11 = new HashSet();
   protected static final Set animationEventNames12 = new HashSet();

   public SVGAnimationEngine(Document doc, BridgeContext ctx) {
      super(doc);
      this.factories = new Factory[]{null, new AnimatableIntegerValueFactory(), new AnimatableNumberValueFactory(), new AnimatableLengthValueFactory(), null, new AnimatableAngleValueFactory(), new AnimatableColorValueFactory(), new AnimatablePaintValueFactory(), null, null, this.uncomputedAnimatableStringValueFactory, null, null, new AnimatableNumberListValueFactory(), new AnimatableLengthListValueFactory(), this.uncomputedAnimatableStringValueFactory, this.uncomputedAnimatableStringValueFactory, this.animatableLengthOrIdentFactory, this.uncomputedAnimatableStringValueFactory, this.uncomputedAnimatableStringValueFactory, this.uncomputedAnimatableStringValueFactory, this.uncomputedAnimatableStringValueFactory, new AnimatablePathDataFactory(), this.uncomputedAnimatableStringValueFactory, null, this.animatableNumberOrIdentFactory, this.uncomputedAnimatableStringValueFactory, null, new AnimatableNumberOrIdentFactory(true), new AnimatableAngleOrIdentFactory(), null, new AnimatablePointListValueFactory(), new AnimatablePreserveAspectRatioValueFactory(), null, this.uncomputedAnimatableStringValueFactory, null, null, null, null, this.animatableLengthOrIdentFactory, this.animatableLengthOrIdentFactory, this.animatableLengthOrIdentFactory, this.animatableLengthOrIdentFactory, this.animatableLengthOrIdentFactory, this.animatableNumberOrIdentFactory, null, null, new AnimatableNumberOrPercentageValueFactory(), null, new AnimatableBooleanValueFactory(), new AnimatableRectValueFactory()};
      this.initialBridges = new LinkedList();
      this.ctx = ctx;
      SVGOMDocument d = (SVGOMDocument)doc;
      this.cssEngine = d.getCSSEngine();
      this.dummyStyleMap = new StyleMap(this.cssEngine.getNumberOfProperties());
      this.isSVG12 = d.isSVG12();
   }

   public void dispose() {
      synchronized(this) {
         this.pause();
         super.dispose();
      }
   }

   public void addInitialBridge(SVGAnimationElementBridge b) {
      if (this.initialBridges != null) {
         this.initialBridges.add(b);
      }

   }

   public boolean hasStarted() {
      return this.started;
   }

   public AnimatableValue parseAnimatableValue(Element animElt, AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
      SVGOMElement elt = (SVGOMElement)target.getElement();
      int type;
      if (isCSS) {
         type = elt.getPropertyType(ln);
      } else {
         type = elt.getAttributeType(ns, ln);
      }

      Factory factory = this.factories[type];
      if (factory == null) {
         String an = ns == null ? ln : '{' + ns + '}' + ln;
         throw new BridgeException(this.ctx, animElt, "attribute.not.animatable", new Object[]{target.getElement().getNodeName(), an});
      } else {
         return this.factories[type].createValue(target, ns, ln, isCSS, s);
      }
   }

   public AnimatableValue getUnderlyingCSSValue(Element animElt, AnimationTarget target, String pn) {
      ValueManager[] vms = this.cssEngine.getValueManagers();
      int idx = this.cssEngine.getPropertyIndex(pn);
      if (idx != -1) {
         int type = vms[idx].getPropertyType();
         Factory factory = this.factories[type];
         if (factory == null) {
            throw new BridgeException(this.ctx, animElt, "attribute.not.animatable", new Object[]{target.getElement().getNodeName(), pn});
         } else {
            SVGStylableElement e = (SVGStylableElement)target.getElement();
            CSSStyleDeclaration over = e.getOverrideStyle();
            String oldValue = over.getPropertyValue(pn);
            if (oldValue != null) {
               over.removeProperty(pn);
            }

            Value v = this.cssEngine.getComputedStyle(e, (String)null, idx);
            if (oldValue != null && !oldValue.equals("")) {
               over.setProperty(pn, oldValue, (String)null);
            }

            return this.factories[type].createValue(target, pn, v);
         }
      } else {
         return null;
      }
   }

   public void pause() {
      super.pause();
      UpdateManager um = this.ctx.getUpdateManager();
      if (um != null) {
         um.getUpdateRunnableQueue().setIdleRunnable((RunnableQueue.IdleRunnable)null);
      }

   }

   public void unpause() {
      super.unpause();
      UpdateManager um = this.ctx.getUpdateManager();
      if (um != null) {
         um.getUpdateRunnableQueue().setIdleRunnable(this.animationTickRunnable);
      }

   }

   public float getCurrentTime() {
      boolean p = this.pauseTime != 0L;
      this.unpause();
      float t = this.timedDocumentRoot.getCurrentTime();
      if (p) {
         this.pause();
      }

      return Float.isNaN(t) ? 0.0F : t;
   }

   public float setCurrentTime(float t) {
      if (this.started) {
         float ret = super.setCurrentTime(t);
         if (this.animationTickRunnable != null) {
            this.animationTickRunnable.resume();
         }

         return ret;
      } else {
         this.initialStartTime = t;
         return 0.0F;
      }
   }

   protected TimedDocumentRoot createDocumentRoot() {
      return new AnimationRoot();
   }

   public void start(long documentStartTime) {
      if (!this.started) {
         this.started = true;

         try {
            try {
               Calendar cal = Calendar.getInstance();
               cal.setTime(new Date(documentStartTime));
               this.timedDocumentRoot.resetDocument(cal);
               Object[] bridges = this.initialBridges.toArray();
               this.initialBridges = null;
               Object[] var5 = bridges;
               int var6 = bridges.length;

               int var7;
               Object bridge1;
               SVGAnimationElementBridge bridge;
               for(var7 = 0; var7 < var6; ++var7) {
                  bridge1 = var5[var7];
                  bridge = (SVGAnimationElementBridge)bridge1;
                  bridge.initializeAnimation();
               }

               var5 = bridges;
               var6 = bridges.length;

               for(var7 = 0; var7 < var6; ++var7) {
                  bridge1 = var5[var7];
                  bridge = (SVGAnimationElementBridge)bridge1;
                  bridge.initializeTimedElement();
               }

               UpdateManager um = this.ctx.getUpdateManager();
               if (um != null) {
                  RunnableQueue q = um.getUpdateRunnableQueue();
                  this.animationTickRunnable = new AnimationTickRunnable(q, this);
                  q.setIdleRunnable(this.animationTickRunnable);
                  if (this.initialStartTime != 0.0F) {
                     this.setCurrentTime(this.initialStartTime);
                  }
               }
            } catch (AnimationException var10) {
               throw new BridgeException(this.ctx, var10.getElement().getElement(), var10.getMessage());
            }
         } catch (Exception var11) {
            if (this.ctx.getUserAgent() == null) {
               var11.printStackTrace();
            } else {
               this.ctx.getUserAgent().displayError(var11);
            }
         }

      }
   }

   public void setAnimationLimitingNone() {
      this.animationLimitingMode = 0;
   }

   public void setAnimationLimitingCPU(float pc) {
      this.animationLimitingMode = 1;
      this.animationLimitingAmount = pc;
   }

   public void setAnimationLimitingFPS(float fps) {
      this.animationLimitingMode = 2;
      this.animationLimitingAmount = fps;
   }

   static {
      String[] eventNamesCommon = new String[]{"click", "mousedown", "mouseup", "mouseover", "mousemove", "mouseout", "beginEvent", "endEvent"};
      String[] eventNamesSVG11 = new String[]{"DOMSubtreeModified", "DOMNodeInserted", "DOMNodeRemoved", "DOMNodeRemovedFromDocument", "DOMNodeInsertedIntoDocument", "DOMAttrModified", "DOMCharacterDataModified", "SVGLoad", "SVGUnload", "SVGAbort", "SVGError", "SVGResize", "SVGScroll", "repeatEvent"};
      String[] eventNamesSVG12 = new String[]{"load", "resize", "scroll", "zoom"};
      String[] var3 = eventNamesCommon;
      int var4 = eventNamesCommon.length;

      int var5;
      String anEventNamesSVG12;
      for(var5 = 0; var5 < var4; ++var5) {
         anEventNamesSVG12 = var3[var5];
         animationEventNames11.add(anEventNamesSVG12);
         animationEventNames12.add(anEventNamesSVG12);
      }

      var3 = eventNamesSVG11;
      var4 = eventNamesSVG11.length;

      for(var5 = 0; var5 < var4; ++var5) {
         anEventNamesSVG12 = var3[var5];
         animationEventNames11.add(anEventNamesSVG12);
      }

      var3 = eventNamesSVG12;
      var4 = eventNamesSVG12.length;

      for(var5 = 0; var5 < var4; ++var5) {
         anEventNamesSVG12 = var3[var5];
         animationEventNames12.add(anEventNamesSVG12);
      }

   }

   protected class AnimatableStringValueFactory extends CSSValueFactory {
      protected AnimatableStringValueFactory() {
         super();
      }

      protected AnimatableValue createAnimatableValue(AnimationTarget target, String pn, Value v) {
         return new AnimatableStringValue(target, v.getCssText());
      }
   }

   protected class AnimatablePaintValueFactory extends CSSValueFactory {
      protected AnimatablePaintValueFactory() {
         super();
      }

      protected AnimatablePaintValue createColorPaintValue(AnimationTarget t, Color c) {
         return AnimatablePaintValue.createColorPaintValue(t, (float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F);
      }

      protected AnimatableValue createAnimatableValue(AnimationTarget target, String pn, Value v) {
         if (v.getCssValueType() == 1) {
            switch (v.getPrimitiveType()) {
               case 20:
                  return AnimatablePaintValue.createURIPaintValue(target, v.getStringValue());
               case 21:
                  return AnimatablePaintValue.createNonePaintValue(target);
               case 25:
                  Paint px = PaintServer.convertPaint(target.getElement(), (GraphicsNode)null, v, 1.0F, SVGAnimationEngine.this.ctx);
                  return this.createColorPaintValue(target, (Color)px);
            }
         } else {
            Value v1 = v.item(0);
            switch (v1.getPrimitiveType()) {
               case 20:
                  Value v2 = v.item(1);
                  switch (v2.getPrimitiveType()) {
                     case 21:
                        return AnimatablePaintValue.createURINonePaintValue(target, v1.getStringValue());
                     case 25:
                        Paint p = PaintServer.convertPaint(target.getElement(), (GraphicsNode)null, v.item(1), 1.0F, SVGAnimationEngine.this.ctx);
                        return this.createColorPaintValue(target, (Color)p);
                     default:
                        return null;
                  }
               case 25:
                  Paint pxx = PaintServer.convertPaint(target.getElement(), (GraphicsNode)null, v, 1.0F, SVGAnimationEngine.this.ctx);
                  return this.createColorPaintValue(target, (Color)pxx);
            }
         }

         return null;
      }
   }

   protected class AnimatableColorValueFactory extends CSSValueFactory {
      protected AnimatableColorValueFactory() {
         super();
      }

      protected AnimatableValue createAnimatableValue(AnimationTarget target, String pn, Value v) {
         Paint p = PaintServer.convertPaint(target.getElement(), (GraphicsNode)null, v, 1.0F, SVGAnimationEngine.this.ctx);
         if (p instanceof Color) {
            Color c = (Color)p;
            return new AnimatableColorValue(target, (float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F);
         } else {
            return null;
         }
      }
   }

   protected class AnimatableAngleOrIdentFactory extends CSSValueFactory {
      protected AnimatableAngleOrIdentFactory() {
         super();
      }

      protected AnimatableValue createAnimatableValue(AnimationTarget target, String pn, Value v) {
         if (v instanceof StringValue) {
            return new AnimatableAngleOrIdentValue(target, v.getStringValue());
         } else {
            FloatValue fv = (FloatValue)v;
            byte unit;
            switch (fv.getPrimitiveType()) {
               case 1:
               case 11:
                  unit = 2;
                  break;
               case 12:
                  unit = 3;
                  break;
               case 13:
                  unit = 4;
                  break;
               default:
                  return null;
            }

            return new AnimatableAngleOrIdentValue(target, fv.getFloatValue(), unit);
         }
      }
   }

   protected class AnimatableAngleValueFactory extends CSSValueFactory {
      protected AnimatableAngleValueFactory() {
         super();
      }

      protected AnimatableValue createAnimatableValue(AnimationTarget target, String pn, Value v) {
         FloatValue fv = (FloatValue)v;
         byte unit;
         switch (fv.getPrimitiveType()) {
            case 1:
            case 11:
               unit = 2;
               break;
            case 12:
               unit = 3;
               break;
            case 13:
               unit = 4;
               break;
            default:
               return null;
         }

         return new AnimatableAngleValue(target, fv.getFloatValue(), unit);
      }
   }

   protected class AnimatableNumberOrIdentFactory extends CSSValueFactory {
      protected boolean numericIdents;

      public AnimatableNumberOrIdentFactory(boolean numericIdents) {
         super();
         this.numericIdents = numericIdents;
      }

      protected AnimatableValue createAnimatableValue(AnimationTarget target, String pn, Value v) {
         if (v instanceof StringValue) {
            return new AnimatableNumberOrIdentValue(target, v.getStringValue());
         } else {
            FloatValue fv = (FloatValue)v;
            return new AnimatableNumberOrIdentValue(target, fv.getFloatValue(), this.numericIdents);
         }
      }
   }

   protected class AnimatableLengthOrIdentFactory extends CSSValueFactory {
      protected AnimatableLengthOrIdentFactory() {
         super();
      }

      protected AnimatableValue createAnimatableValue(AnimationTarget target, String pn, Value v) {
         if (v instanceof StringValue) {
            return new AnimatableLengthOrIdentValue(target, v.getStringValue());
         } else {
            short pcInterp = target.getPercentageInterpretation((String)null, pn, true);
            FloatValue fv = (FloatValue)v;
            return new AnimatableLengthOrIdentValue(target, fv.getPrimitiveType(), fv.getFloatValue(), pcInterp);
         }
      }
   }

   protected static class UncomputedAnimatableStringValueFactory implements Factory {
      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         return new AnimatableStringValue(target, s);
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return new AnimatableStringValue(target, v.getCssText());
      }
   }

   protected static class AnimatablePathDataFactory implements Factory {
      protected PathParser parser = new PathParser();
      protected PathArrayProducer producer = new PathArrayProducer();

      public AnimatablePathDataFactory() {
         this.parser.setPathHandler(this.producer);
      }

      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         try {
            this.parser.parse(s);
            return new AnimatablePathDataValue(target, this.producer.getPathCommands(), this.producer.getPathParameters());
         } catch (ParseException var7) {
            return null;
         }
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return null;
      }
   }

   protected static class AnimatablePointListValueFactory implements Factory {
      protected PointsParser parser = new PointsParser();
      protected FloatArrayProducer producer = new FloatArrayProducer();

      public AnimatablePointListValueFactory() {
         this.parser.setPointsHandler(this.producer);
      }

      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         try {
            this.parser.parse(s);
            return new AnimatablePointListValue(target, this.producer.getFloatArray());
         } catch (ParseException var7) {
            return null;
         }
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return null;
      }
   }

   protected static class AnimatableRectValueFactory implements Factory {
      protected NumberListParser parser = new NumberListParser();
      protected FloatArrayProducer producer = new FloatArrayProducer();

      public AnimatableRectValueFactory() {
         this.parser.setNumberListHandler(this.producer);
      }

      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         try {
            this.parser.parse(s);
            float[] r = this.producer.getFloatArray();
            return r.length != 4 ? null : new AnimatableRectValue(target, r[0], r[1], r[2], r[3]);
         } catch (ParseException var7) {
            return null;
         }
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return null;
      }
   }

   protected static class AnimatableNumberListValueFactory implements Factory {
      protected NumberListParser parser = new NumberListParser();
      protected FloatArrayProducer producer = new FloatArrayProducer();

      public AnimatableNumberListValueFactory() {
         this.parser.setNumberListHandler(this.producer);
      }

      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         try {
            this.parser.parse(s);
            return new AnimatableNumberListValue(target, this.producer.getFloatArray());
         } catch (ParseException var7) {
            return null;
         }
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return null;
      }
   }

   protected static class AnimatableLengthListValueFactory implements Factory {
      protected LengthListParser parser = new LengthListParser();
      protected LengthArrayProducer producer = new LengthArrayProducer();

      public AnimatableLengthListValueFactory() {
         this.parser.setLengthListHandler(this.producer);
      }

      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         try {
            short pcInterp = target.getPercentageInterpretation(ns, ln, isCSS);
            this.parser.parse(s);
            return new AnimatableLengthListValue(target, this.producer.getLengthTypeArray(), this.producer.getLengthValueArray(), pcInterp);
         } catch (ParseException var7) {
            return null;
         }
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return null;
      }
   }

   protected static class AnimatableLengthValueFactory implements Factory {
      protected short type;
      protected float value;
      protected LengthParser parser = new LengthParser();
      protected LengthHandler handler = new DefaultLengthHandler() {
         public void startLength() throws ParseException {
            AnimatableLengthValueFactory.this.type = 1;
         }

         public void lengthValue(float v) throws ParseException {
            AnimatableLengthValueFactory.this.value = v;
         }

         public void em() throws ParseException {
            AnimatableLengthValueFactory.this.type = 3;
         }

         public void ex() throws ParseException {
            AnimatableLengthValueFactory.this.type = 4;
         }

         public void in() throws ParseException {
            AnimatableLengthValueFactory.this.type = 8;
         }

         public void cm() throws ParseException {
            AnimatableLengthValueFactory.this.type = 6;
         }

         public void mm() throws ParseException {
            AnimatableLengthValueFactory.this.type = 7;
         }

         public void pc() throws ParseException {
            AnimatableLengthValueFactory.this.type = 10;
         }

         public void pt() throws ParseException {
            AnimatableLengthValueFactory.this.type = 9;
         }

         public void px() throws ParseException {
            AnimatableLengthValueFactory.this.type = 5;
         }

         public void percentage() throws ParseException {
            AnimatableLengthValueFactory.this.type = 2;
         }

         public void endLength() throws ParseException {
         }
      };

      public AnimatableLengthValueFactory() {
         this.parser.setLengthHandler(this.handler);
      }

      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         short pcInterp = target.getPercentageInterpretation(ns, ln, isCSS);

         try {
            this.parser.parse(s);
            return new AnimatableLengthValue(target, this.type, this.value, pcInterp);
         } catch (ParseException var8) {
            return null;
         }
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return new AnimatableIntegerValue(target, Math.round(v.getFloatValue()));
      }
   }

   protected static class AnimatablePreserveAspectRatioValueFactory implements Factory {
      protected short align;
      protected short meetOrSlice;
      protected PreserveAspectRatioParser parser = new PreserveAspectRatioParser();
      protected DefaultPreserveAspectRatioHandler handler = new DefaultPreserveAspectRatioHandler() {
         public void startPreserveAspectRatio() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 0;
            AnimatablePreserveAspectRatioValueFactory.this.meetOrSlice = 0;
         }

         public void none() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 1;
         }

         public void xMaxYMax() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 10;
         }

         public void xMaxYMid() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 7;
         }

         public void xMaxYMin() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 4;
         }

         public void xMidYMax() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 9;
         }

         public void xMidYMid() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 6;
         }

         public void xMidYMin() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 3;
         }

         public void xMinYMax() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 8;
         }

         public void xMinYMid() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 5;
         }

         public void xMinYMin() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.align = 2;
         }

         public void meet() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.meetOrSlice = 1;
         }

         public void slice() throws ParseException {
            AnimatablePreserveAspectRatioValueFactory.this.meetOrSlice = 2;
         }
      };

      public AnimatablePreserveAspectRatioValueFactory() {
         this.parser.setPreserveAspectRatioHandler(this.handler);
      }

      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         try {
            this.parser.parse(s);
            return new AnimatablePreserveAspectRatioValue(target, this.align, this.meetOrSlice);
         } catch (ParseException var7) {
            return null;
         }
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return null;
      }
   }

   protected static class AnimatableNumberOrPercentageValueFactory implements Factory {
      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         float v;
         boolean pc;
         if (s.charAt(s.length() - 1) == '%') {
            v = Float.parseFloat(s.substring(0, s.length() - 1));
            pc = true;
         } else {
            v = Float.parseFloat(s);
            pc = false;
         }

         return new AnimatableNumberOrPercentageValue(target, v, pc);
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         switch (v.getPrimitiveType()) {
            case 1:
               return new AnimatableNumberOrPercentageValue(target, v.getFloatValue());
            case 2:
               return new AnimatableNumberOrPercentageValue(target, v.getFloatValue(), true);
            default:
               return null;
         }
      }
   }

   protected static class AnimatableNumberValueFactory implements Factory {
      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         return new AnimatableNumberValue(target, Float.parseFloat(s));
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return new AnimatableNumberValue(target, v.getFloatValue());
      }
   }

   protected static class AnimatableIntegerValueFactory implements Factory {
      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         return new AnimatableIntegerValue(target, Integer.parseInt(s));
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return new AnimatableIntegerValue(target, Math.round(v.getFloatValue()));
      }
   }

   protected static class AnimatableBooleanValueFactory implements Factory {
      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         return new AnimatableBooleanValue(target, "true".equals(s));
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         return new AnimatableBooleanValue(target, "true".equals(v.getCssText()));
      }
   }

   protected abstract class CSSValueFactory implements Factory {
      public AnimatableValue createValue(AnimationTarget target, String ns, String ln, boolean isCSS, String s) {
         return this.createValue(target, ln, this.createCSSValue(target, ln, s));
      }

      public AnimatableValue createValue(AnimationTarget target, String pn, Value v) {
         CSSStylableElement elt = (CSSStylableElement)target.getElement();
         v = this.computeValue(elt, pn, v);
         return this.createAnimatableValue(target, pn, v);
      }

      protected abstract AnimatableValue createAnimatableValue(AnimationTarget var1, String var2, Value var3);

      protected Value createCSSValue(AnimationTarget t, String pn, String s) {
         CSSStylableElement elt = (CSSStylableElement)t.getElement();
         Value v = SVGAnimationEngine.this.cssEngine.parsePropertyValue(elt, pn, s);
         return this.computeValue(elt, pn, v);
      }

      protected Value computeValue(CSSStylableElement elt, String pn, Value v) {
         ValueManager[] vms = SVGAnimationEngine.this.cssEngine.getValueManagers();
         int idx = SVGAnimationEngine.this.cssEngine.getPropertyIndex(pn);
         if (idx != -1) {
            if (v.getCssValueType() == 0) {
               elt = CSSEngine.getParentCSSStylableElement(elt);
               if (elt != null) {
                  return SVGAnimationEngine.this.cssEngine.getComputedStyle(elt, (String)null, idx);
               }

               return vms[idx].getDefaultValue();
            }

            v = vms[idx].computeValue(elt, (String)null, SVGAnimationEngine.this.cssEngine, idx, SVGAnimationEngine.this.dummyStyleMap, v);
         }

         return v;
      }
   }

   protected interface Factory {
      AnimatableValue createValue(AnimationTarget var1, String var2, String var3, boolean var4, String var5);

      AnimatableValue createValue(AnimationTarget var1, String var2, Value var3);
   }

   protected class AnimationThread extends Thread {
      protected Calendar time = Calendar.getInstance();
      protected RunnableQueue runnableQueue;
      protected Ticker ticker;

      protected AnimationThread() {
         this.runnableQueue = SVGAnimationEngine.this.ctx.getUpdateManager().getUpdateRunnableQueue();
         this.ticker = new Ticker();
      }

      public void run() {
         while(true) {
            this.time.setTime(new Date());
            this.ticker.t = SVGAnimationEngine.this.timedDocumentRoot.convertWallclockTime(this.time);

            try {
               this.runnableQueue.invokeAndWait(this.ticker);
            } catch (InterruptedException var2) {
               return;
            }
         }
      }

      protected class Ticker implements Runnable {
         protected float t;

         public void run() {
            SVGAnimationEngine.this.tick(this.t, false);
         }
      }
   }

   protected static class AnimationTickRunnable implements RunnableQueue.IdleRunnable {
      protected Calendar time = Calendar.getInstance();
      protected long waitTime;
      protected RunnableQueue q;
      private static final int NUM_TIMES = 8;
      protected long[] times = new long[8];
      protected long sumTime;
      protected int timeIndex;
      protected WeakReference engRef;
      protected static final int MAX_EXCEPTION_COUNT = 10;
      protected int exceptionCount;

      public AnimationTickRunnable(RunnableQueue q, SVGAnimationEngine eng) {
         this.q = q;
         this.engRef = new WeakReference(eng);
         Arrays.fill(this.times, 100L);
         this.sumTime = 800L;
      }

      public void resume() {
         this.waitTime = 0L;
         Object lock = this.q.getIteratorLock();
         synchronized(lock) {
            lock.notify();
         }
      }

      public long getWaitTime() {
         return this.waitTime;
      }

      public void run() {
         SVGAnimationEngine eng = this.getAnimationEngine();
         synchronized(eng) {
            int animationLimitingMode = eng.animationLimitingMode;
            float animationLimitingAmount = eng.animationLimitingAmount;

            try {
               try {
                  long before = System.currentTimeMillis();
                  this.time.setTime(new Date(before));
                  float t = eng.timedDocumentRoot.convertWallclockTime(this.time);
                  float t2 = eng.tick(t, false);
                  long after = System.currentTimeMillis();
                  long dur = after - before;
                  if (dur == 0L) {
                     dur = 1L;
                  }

                  this.sumTime -= this.times[this.timeIndex];
                  this.sumTime += dur;
                  this.times[this.timeIndex] = dur;
                  this.timeIndex = (this.timeIndex + 1) % 8;
                  if (t2 == Float.POSITIVE_INFINITY) {
                     this.waitTime = Long.MAX_VALUE;
                  } else {
                     this.waitTime = before + (long)(t2 * 1000.0F) - 1000L;
                     if (this.waitTime < after) {
                        this.waitTime = after;
                     }

                     if (animationLimitingMode != 0) {
                        float ave = (float)this.sumTime / 8.0F;
                        float delay;
                        if (animationLimitingMode == 1) {
                           delay = ave / animationLimitingAmount - ave;
                        } else {
                           delay = 1000.0F / animationLimitingAmount - ave;
                        }

                        long newWaitTime = after + (long)delay;
                        if (newWaitTime > this.waitTime) {
                           this.waitTime = newWaitTime;
                        }
                     }
                  }
               } catch (AnimationException var19) {
                  throw new BridgeException(eng.ctx, var19.getElement().getElement(), var19.getMessage());
               }

               this.exceptionCount = 0;
            } catch (Exception var20) {
               if (++this.exceptionCount < 10) {
                  if (eng.ctx.getUserAgent() == null) {
                     var20.printStackTrace();
                  } else {
                     eng.ctx.getUserAgent().displayError(var20);
                  }
               }
            }

            if (animationLimitingMode == 0) {
               try {
                  Thread.sleep(1L);
               } catch (InterruptedException var18) {
               }
            }

         }
      }

      protected SVGAnimationEngine getAnimationEngine() {
         return (SVGAnimationEngine)this.engRef.get();
      }
   }

   protected static class DebugAnimationTickRunnable extends AnimationTickRunnable {
      float t = 0.0F;

      public DebugAnimationTickRunnable(RunnableQueue q, SVGAnimationEngine eng) {
         super(q, eng);
         this.waitTime = Long.MAX_VALUE;
         (new Thread() {
            public void run() {
               BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
               System.out.println("Enter times.");

               while(true) {
                  String s;
                  try {
                     s = r.readLine();
                  } catch (IOException var4) {
                     s = null;
                  }

                  if (s == null) {
                     System.exit(0);
                  }

                  DebugAnimationTickRunnable.this.t = Float.parseFloat(s);
                  DebugAnimationTickRunnable.this.resume();
               }
            }
         }).start();
      }

      public void resume() {
         this.waitTime = 0L;
         Object lock = this.q.getIteratorLock();
         synchronized(lock) {
            lock.notify();
         }
      }

      public long getWaitTime() {
         long wt = this.waitTime;
         this.waitTime = Long.MAX_VALUE;
         return wt;
      }

      public void run() {
         SVGAnimationEngine eng = this.getAnimationEngine();
         synchronized(eng) {
            try {
               try {
                  eng.tick(this.t, false);
               } catch (AnimationException var5) {
                  throw new BridgeException(eng.ctx, var5.getElement().getElement(), var5.getMessage());
               }
            } catch (Exception var6) {
               if (eng.ctx.getUserAgent() == null) {
                  var6.printStackTrace();
               } else {
                  eng.ctx.getUserAgent().displayError(var6);
               }
            }

         }
      }
   }

   protected class AnimationRoot extends TimedDocumentRoot {
      public AnimationRoot() {
         super(!SVGAnimationEngine.this.isSVG12, SVGAnimationEngine.this.isSVG12);
      }

      protected String getEventNamespaceURI(String eventName) {
         if (!SVGAnimationEngine.this.isSVG12) {
            return null;
         } else {
            return !eventName.equals("focusin") && !eventName.equals("focusout") && !eventName.equals("activate") && !SVGAnimationEngine.animationEventNames12.contains(eventName) ? null : "http://www.w3.org/2001/xml-events";
         }
      }

      protected String getEventType(String eventName) {
         if (eventName.equals("focusin")) {
            return "DOMFocusIn";
         } else if (eventName.equals("focusout")) {
            return "DOMFocusOut";
         } else if (eventName.equals("activate")) {
            return "DOMActivate";
         } else {
            if (SVGAnimationEngine.this.isSVG12) {
               if (SVGAnimationEngine.animationEventNames12.contains(eventName)) {
                  return eventName;
               }
            } else if (SVGAnimationEngine.animationEventNames11.contains(eventName)) {
               return eventName;
            }

            return null;
         }
      }

      protected String getRepeatEventName() {
         return "repeatEvent";
      }

      protected void fireTimeEvent(String eventType, Calendar time, int detail) {
         AnimationSupport.fireTimeEvent((EventTarget)SVGAnimationEngine.this.document, eventType, time, detail);
      }

      protected void toActive(float begin) {
      }

      protected void toInactive(boolean stillActive, boolean isFrozen) {
      }

      protected void removeFill() {
      }

      protected void sampledAt(float simpleTime, float simpleDur, int repeatIteration) {
      }

      protected void sampledLastValue(int repeatIteration) {
      }

      protected TimedElement getTimedElementById(String id) {
         return AnimationSupport.getTimedElementById(id, SVGAnimationEngine.this.document);
      }

      protected EventTarget getEventTargetById(String id) {
         return AnimationSupport.getEventTargetById(id, SVGAnimationEngine.this.document);
      }

      protected EventTarget getAnimationEventTarget() {
         return null;
      }

      protected EventTarget getRootEventTarget() {
         return (EventTarget)SVGAnimationEngine.this.document;
      }

      public Element getElement() {
         return null;
      }

      public boolean isBefore(TimedElement other) {
         return false;
      }

      protected void currentIntervalWillUpdate() {
         if (SVGAnimationEngine.this.animationTickRunnable != null) {
            SVGAnimationEngine.this.animationTickRunnable.resume();
         }

      }
   }
}
