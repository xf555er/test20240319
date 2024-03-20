package org.apache.batik.anim.values;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.dom.svg.AbstractSVGTransform;
import org.apache.batik.dom.svg.SVGOMTransform;
import org.w3c.dom.svg.SVGMatrix;

public class AnimatableTransformListValue extends AnimatableValue {
   protected static SVGOMTransform IDENTITY_SKEWX = new SVGOMTransform();
   protected static SVGOMTransform IDENTITY_SKEWY = new SVGOMTransform();
   protected static SVGOMTransform IDENTITY_SCALE = new SVGOMTransform();
   protected static SVGOMTransform IDENTITY_ROTATE = new SVGOMTransform();
   protected static SVGOMTransform IDENTITY_TRANSLATE = new SVGOMTransform();
   protected Vector transforms;

   protected AnimatableTransformListValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableTransformListValue(AnimationTarget target, AbstractSVGTransform t) {
      super(target);
      this.transforms = new Vector();
      this.transforms.add(t);
   }

   public AnimatableTransformListValue(AnimationTarget target, List transforms) {
      super(target);
      this.transforms = new Vector(transforms);
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableTransformListValue toTransformList = (AnimatableTransformListValue)to;
      AnimatableTransformListValue accTransformList = (AnimatableTransformListValue)accumulation;
      int accSize = accumulation == null ? 0 : accTransformList.transforms.size();
      int newSize = this.transforms.size() + accSize * multiplier;
      AnimatableTransformListValue res;
      if (result == null) {
         res = new AnimatableTransformListValue(this.target);
         res.transforms = new Vector(newSize);
         res.transforms.setSize(newSize);
      } else {
         res = (AnimatableTransformListValue)result;
         if (res.transforms == null) {
            res.transforms = new Vector(newSize);
            res.transforms.setSize(newSize);
         } else if (res.transforms.size() != newSize) {
            res.transforms.setSize(newSize);
         }
      }

      int index = 0;

      int i;
      for(i = 0; i < multiplier; ++i) {
         for(int i = 0; i < accSize; ++index) {
            res.transforms.setElementAt(accTransformList.transforms.elementAt(i), index);
            ++i;
         }
      }

      for(i = 0; i < this.transforms.size() - 1; ++index) {
         res.transforms.setElementAt(this.transforms.elementAt(i), index);
         ++i;
      }

      AbstractSVGTransform tt;
      Object ft;
      if (to != null) {
         tt = (AbstractSVGTransform)toTransformList.transforms.lastElement();
         ft = null;
         short type;
         if (this.transforms.isEmpty()) {
            type = tt.getType();
            switch (type) {
               case 2:
                  ft = IDENTITY_TRANSLATE;
                  break;
               case 3:
                  ft = IDENTITY_SCALE;
                  break;
               case 4:
                  ft = IDENTITY_ROTATE;
                  break;
               case 5:
                  ft = IDENTITY_SKEWX;
                  break;
               case 6:
                  ft = IDENTITY_SKEWY;
            }
         } else {
            ft = (AbstractSVGTransform)this.transforms.lastElement();
            type = ((AbstractSVGTransform)ft).getType();
         }

         if (type == tt.getType()) {
            Object t;
            if (res.transforms.isEmpty()) {
               t = new SVGOMTransform();
               res.transforms.add(t);
            } else {
               t = (AbstractSVGTransform)res.transforms.elementAt(index);
               if (t == null) {
                  t = new SVGOMTransform();
                  res.transforms.setElementAt(t, index);
               }
            }

            float r = 0.0F;
            float x;
            float y;
            SVGMatrix fm;
            SVGMatrix tm;
            switch (type) {
               case 2:
                  fm = ((AbstractSVGTransform)ft).getMatrix();
                  tm = tt.getMatrix();
                  x = fm.getE();
                  y = fm.getF();
                  x += interpolation * (tm.getE() - x);
                  y += interpolation * (tm.getF() - y);
                  ((AbstractSVGTransform)t).setTranslate(x, y);
                  break;
               case 3:
                  fm = ((AbstractSVGTransform)ft).getMatrix();
                  tm = tt.getMatrix();
                  x = fm.getA();
                  y = fm.getD();
                  x += interpolation * (tm.getA() - x);
                  y += interpolation * (tm.getD() - y);
                  ((AbstractSVGTransform)t).setScale(x, y);
                  break;
               case 4:
                  x = ((AbstractSVGTransform)ft).getX();
                  y = ((AbstractSVGTransform)ft).getY();
                  x += interpolation * (tt.getX() - x);
                  y += interpolation * (tt.getY() - y);
                  r = ((AbstractSVGTransform)ft).getAngle();
                  r += interpolation * (tt.getAngle() - r);
                  ((AbstractSVGTransform)t).setRotate(r, x, y);
                  break;
               case 5:
               case 6:
                  r = ((AbstractSVGTransform)ft).getAngle();
                  r += interpolation * (tt.getAngle() - r);
                  if (type == 5) {
                     ((AbstractSVGTransform)t).setSkewX(r);
                  } else if (type == 6) {
                     ((AbstractSVGTransform)t).setSkewY(r);
                  }
            }
         }
      } else {
         tt = (AbstractSVGTransform)this.transforms.lastElement();
         ft = (AbstractSVGTransform)res.transforms.elementAt(index);
         if (ft == null) {
            ft = new SVGOMTransform();
            res.transforms.setElementAt(ft, index);
         }

         ((AbstractSVGTransform)ft).assign(tt);
      }

      res.hasChanged = true;
      return res;
   }

   public static AnimatableTransformListValue interpolate(AnimatableTransformListValue res, AnimatableTransformListValue value1, AnimatableTransformListValue value2, AnimatableTransformListValue to1, AnimatableTransformListValue to2, float interpolation1, float interpolation2, AnimatableTransformListValue accumulation, int multiplier) {
      int accSize = accumulation == null ? 0 : accumulation.transforms.size();
      int newSize = accSize * multiplier + 1;
      if (res == null) {
         res = new AnimatableTransformListValue(to1.target);
         res.transforms = new Vector(newSize);
         res.transforms.setSize(newSize);
      } else if (res.transforms == null) {
         res.transforms = new Vector(newSize);
         res.transforms.setSize(newSize);
      } else if (res.transforms.size() != newSize) {
         res.transforms.setSize(newSize);
      }

      int index = 0;

      for(int j = 0; j < multiplier; ++j) {
         for(int i = 0; i < accSize; ++index) {
            res.transforms.setElementAt(accumulation.transforms.elementAt(i), index);
            ++i;
         }
      }

      AbstractSVGTransform ft1 = (AbstractSVGTransform)value1.transforms.lastElement();
      AbstractSVGTransform ft2 = (AbstractSVGTransform)value2.transforms.lastElement();
      AbstractSVGTransform t = (AbstractSVGTransform)res.transforms.elementAt(index);
      if (t == null) {
         t = new SVGOMTransform();
         res.transforms.setElementAt(t, index);
      }

      int type = ft1.getType();
      float x;
      float y;
      if (type == 3) {
         x = ft1.getMatrix().getA();
         y = ft2.getMatrix().getD();
      } else {
         x = ft1.getMatrix().getE();
         y = ft2.getMatrix().getF();
      }

      if (to1 != null) {
         AbstractSVGTransform tt1 = (AbstractSVGTransform)to1.transforms.lastElement();
         AbstractSVGTransform tt2 = (AbstractSVGTransform)to2.transforms.lastElement();
         if (type == 3) {
            x += interpolation1 * (tt1.getMatrix().getA() - x);
            y += interpolation2 * (tt2.getMatrix().getD() - y);
         } else {
            x += interpolation1 * (tt1.getMatrix().getE() - x);
            y += interpolation2 * (tt2.getMatrix().getF() - y);
         }
      }

      if (type == 3) {
         ((AbstractSVGTransform)t).setScale(x, y);
      } else {
         ((AbstractSVGTransform)t).setTranslate(x, y);
      }

      res.hasChanged = true;
      return res;
   }

   public static AnimatableTransformListValue interpolate(AnimatableTransformListValue res, AnimatableTransformListValue value1, AnimatableTransformListValue value2, AnimatableTransformListValue value3, AnimatableTransformListValue to1, AnimatableTransformListValue to2, AnimatableTransformListValue to3, float interpolation1, float interpolation2, float interpolation3, AnimatableTransformListValue accumulation, int multiplier) {
      int accSize = accumulation == null ? 0 : accumulation.transforms.size();
      int newSize = accSize * multiplier + 1;
      if (res == null) {
         res = new AnimatableTransformListValue(to1.target);
         res.transforms = new Vector(newSize);
         res.transforms.setSize(newSize);
      } else if (res.transforms == null) {
         res.transforms = new Vector(newSize);
         res.transforms.setSize(newSize);
      } else if (res.transforms.size() != newSize) {
         res.transforms.setSize(newSize);
      }

      int index = 0;

      for(int j = 0; j < multiplier; ++j) {
         for(int i = 0; i < accSize; ++index) {
            res.transforms.setElementAt(accumulation.transforms.elementAt(i), index);
            ++i;
         }
      }

      AbstractSVGTransform ft1 = (AbstractSVGTransform)value1.transforms.lastElement();
      AbstractSVGTransform ft2 = (AbstractSVGTransform)value2.transforms.lastElement();
      AbstractSVGTransform ft3 = (AbstractSVGTransform)value3.transforms.lastElement();
      AbstractSVGTransform t = (AbstractSVGTransform)res.transforms.elementAt(index);
      if (t == null) {
         t = new SVGOMTransform();
         res.transforms.setElementAt(t, index);
      }

      float r = ft1.getAngle();
      float x = ft2.getX();
      float y = ft3.getY();
      if (to1 != null) {
         AbstractSVGTransform tt1 = (AbstractSVGTransform)to1.transforms.lastElement();
         AbstractSVGTransform tt2 = (AbstractSVGTransform)to2.transforms.lastElement();
         AbstractSVGTransform tt3 = (AbstractSVGTransform)to3.transforms.lastElement();
         r += interpolation1 * (tt1.getAngle() - r);
         x += interpolation2 * (tt2.getX() - x);
         y += interpolation3 * (tt3.getY() - y);
      }

      ((AbstractSVGTransform)t).setRotate(r, x, y);
      res.hasChanged = true;
      return res;
   }

   public Iterator getTransforms() {
      return this.transforms.iterator();
   }

   public boolean canPace() {
      return true;
   }

   public float distanceTo(AnimatableValue other) {
      AnimatableTransformListValue o = (AnimatableTransformListValue)other;
      if (!this.transforms.isEmpty() && !o.transforms.isEmpty()) {
         AbstractSVGTransform t1 = (AbstractSVGTransform)this.transforms.lastElement();
         AbstractSVGTransform t2 = (AbstractSVGTransform)o.transforms.lastElement();
         short type1 = t1.getType();
         if (type1 != t2.getType()) {
            return 0.0F;
         } else {
            SVGMatrix m1 = t1.getMatrix();
            SVGMatrix m2 = t2.getMatrix();
            switch (type1) {
               case 2:
                  return Math.abs(m1.getE() - m2.getE()) + Math.abs(m1.getF() - m2.getF());
               case 3:
                  return Math.abs(m1.getA() - m2.getA()) + Math.abs(m1.getD() - m2.getD());
               case 4:
               case 5:
               case 6:
                  return Math.abs(t1.getAngle() - t2.getAngle());
               default:
                  return 0.0F;
            }
         }
      } else {
         return 0.0F;
      }
   }

   public float distanceTo1(AnimatableValue other) {
      AnimatableTransformListValue o = (AnimatableTransformListValue)other;
      if (!this.transforms.isEmpty() && !o.transforms.isEmpty()) {
         AbstractSVGTransform t1 = (AbstractSVGTransform)this.transforms.lastElement();
         AbstractSVGTransform t2 = (AbstractSVGTransform)o.transforms.lastElement();
         short type1 = t1.getType();
         if (type1 != t2.getType()) {
            return 0.0F;
         } else {
            SVGMatrix m1 = t1.getMatrix();
            SVGMatrix m2 = t2.getMatrix();
            switch (type1) {
               case 2:
                  return Math.abs(m1.getE() - m2.getE());
               case 3:
                  return Math.abs(m1.getA() - m2.getA());
               case 4:
               case 5:
               case 6:
                  return Math.abs(t1.getAngle() - t2.getAngle());
               default:
                  return 0.0F;
            }
         }
      } else {
         return 0.0F;
      }
   }

   public float distanceTo2(AnimatableValue other) {
      AnimatableTransformListValue o = (AnimatableTransformListValue)other;
      if (!this.transforms.isEmpty() && !o.transforms.isEmpty()) {
         AbstractSVGTransform t1 = (AbstractSVGTransform)this.transforms.lastElement();
         AbstractSVGTransform t2 = (AbstractSVGTransform)o.transforms.lastElement();
         short type1 = t1.getType();
         if (type1 != t2.getType()) {
            return 0.0F;
         } else {
            SVGMatrix m1 = t1.getMatrix();
            SVGMatrix m2 = t2.getMatrix();
            switch (type1) {
               case 2:
                  return Math.abs(m1.getF() - m2.getF());
               case 3:
                  return Math.abs(m1.getD() - m2.getD());
               case 4:
                  return Math.abs(t1.getX() - t2.getX());
               default:
                  return 0.0F;
            }
         }
      } else {
         return 0.0F;
      }
   }

   public float distanceTo3(AnimatableValue other) {
      AnimatableTransformListValue o = (AnimatableTransformListValue)other;
      if (!this.transforms.isEmpty() && !o.transforms.isEmpty()) {
         AbstractSVGTransform t1 = (AbstractSVGTransform)this.transforms.lastElement();
         AbstractSVGTransform t2 = (AbstractSVGTransform)o.transforms.lastElement();
         short type1 = t1.getType();
         if (type1 != t2.getType()) {
            return 0.0F;
         } else {
            return type1 == 4 ? Math.abs(t1.getY() - t2.getY()) : 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableTransformListValue(this.target, new Vector(5));
   }

   public String toStringRep() {
      StringBuffer sb = new StringBuffer();
      Iterator i = this.transforms.iterator();

      while(i.hasNext()) {
         AbstractSVGTransform t = (AbstractSVGTransform)i.next();
         if (t == null) {
            sb.append("null");
         } else {
            SVGMatrix m = t.getMatrix();
            switch (t.getType()) {
               case 2:
                  sb.append("translate(");
                  sb.append(m.getE());
                  sb.append(',');
                  sb.append(m.getF());
                  sb.append(')');
                  break;
               case 3:
                  sb.append("scale(");
                  sb.append(m.getA());
                  sb.append(',');
                  sb.append(m.getD());
                  sb.append(')');
                  break;
               case 4:
                  sb.append("rotate(");
                  sb.append(t.getAngle());
                  sb.append(',');
                  sb.append(t.getX());
                  sb.append(',');
                  sb.append(t.getY());
                  sb.append(')');
                  break;
               case 5:
                  sb.append("skewX(");
                  sb.append(t.getAngle());
                  sb.append(')');
                  break;
               case 6:
                  sb.append("skewY(");
                  sb.append(t.getAngle());
                  sb.append(')');
            }
         }

         if (i.hasNext()) {
            sb.append(' ');
         }
      }

      return sb.toString();
   }

   static {
      IDENTITY_SKEWX.setSkewX(0.0F);
      IDENTITY_SKEWY.setSkewY(0.0F);
      IDENTITY_SCALE.setScale(0.0F, 0.0F);
      IDENTITY_ROTATE.setRotate(0.0F, 0.0F, 0.0F);
      IDENTITY_TRANSLATE.setTranslate(0.0F, 0.0F);
   }
}
