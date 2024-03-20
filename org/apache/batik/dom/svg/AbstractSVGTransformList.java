package org.apache.batik.dom.svg;

import java.awt.geom.AffineTransform;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.TransformListHandler;
import org.apache.batik.parser.TransformListParser;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGTransform;
import org.w3c.dom.svg.SVGTransformList;

public abstract class AbstractSVGTransformList extends AbstractSVGList implements SVGTransformList {
   public static final String SVG_TRANSFORMATION_LIST_SEPARATOR = "";

   protected String getItemSeparator() {
      return "";
   }

   protected abstract SVGException createSVGException(short var1, String var2, Object[] var3);

   public SVGTransform initialize(SVGTransform newItem) throws DOMException, SVGException {
      return (SVGTransform)this.initializeImpl(newItem);
   }

   public SVGTransform getItem(int index) throws DOMException {
      return (SVGTransform)this.getItemImpl(index);
   }

   public SVGTransform insertItemBefore(SVGTransform newItem, int index) throws DOMException, SVGException {
      return (SVGTransform)this.insertItemBeforeImpl(newItem, index);
   }

   public SVGTransform replaceItem(SVGTransform newItem, int index) throws DOMException, SVGException {
      return (SVGTransform)this.replaceItemImpl(newItem, index);
   }

   public SVGTransform removeItem(int index) throws DOMException {
      return (SVGTransform)this.removeItemImpl(index);
   }

   public SVGTransform appendItem(SVGTransform newItem) throws DOMException, SVGException {
      return (SVGTransform)this.appendItemImpl(newItem);
   }

   public SVGTransform createSVGTransformFromMatrix(SVGMatrix matrix) {
      SVGOMTransform transform = new SVGOMTransform();
      transform.setMatrix(matrix);
      return transform;
   }

   public SVGTransform consolidate() {
      this.revalidate();
      int size = this.itemList.size();
      if (size == 0) {
         return null;
      } else if (size == 1) {
         return this.getItem(0);
      } else {
         SVGTransformItem t = (SVGTransformItem)this.getItemImpl(0);
         AffineTransform at = (AffineTransform)t.affineTransform.clone();

         for(int i = 1; i < size; ++i) {
            t = (SVGTransformItem)this.getItemImpl(i);
            at.concatenate(t.affineTransform);
         }

         SVGOMMatrix matrix = new SVGOMMatrix(at);
         return this.initialize(this.createSVGTransformFromMatrix(matrix));
      }
   }

   public AffineTransform getAffineTransform() {
      AffineTransform at = new AffineTransform();

      for(int i = 0; i < this.getNumberOfItems(); ++i) {
         SVGTransformItem item = (SVGTransformItem)this.getItem(i);
         at.concatenate(item.affineTransform);
      }

      return at;
   }

   protected SVGItem createSVGItem(Object newItem) {
      return new SVGTransformItem((SVGTransform)newItem);
   }

   protected void doParse(String value, ListHandler handler) throws ParseException {
      TransformListParser transformListParser = new TransformListParser();
      TransformListBuilder builder = new TransformListBuilder(handler);
      transformListParser.setTransformListHandler(builder);
      transformListParser.parse(value);
   }

   protected void checkItemType(Object newItem) {
      if (!(newItem instanceof SVGTransform)) {
         this.createSVGException((short)0, "expected.transform", (Object[])null);
      }

   }

   protected static class TransformListBuilder implements TransformListHandler {
      protected ListHandler listHandler;

      public TransformListBuilder(ListHandler listHandler) {
         this.listHandler = listHandler;
      }

      public void startTransformList() throws ParseException {
         this.listHandler.startList();
      }

      public void matrix(float a, float b, float c, float d, float e, float f) throws ParseException {
         SVGTransformItem item = new SVGTransformItem();
         item.matrix(a, b, c, d, e, f);
         this.listHandler.item(item);
      }

      public void rotate(float theta) throws ParseException {
         SVGTransformItem item = new SVGTransformItem();
         item.rotate(theta);
         this.listHandler.item(item);
      }

      public void rotate(float theta, float cx, float cy) throws ParseException {
         SVGTransformItem item = new SVGTransformItem();
         item.setRotate(theta, cx, cy);
         this.listHandler.item(item);
      }

      public void translate(float tx) throws ParseException {
         SVGTransformItem item = new SVGTransformItem();
         item.translate(tx);
         this.listHandler.item(item);
      }

      public void translate(float tx, float ty) throws ParseException {
         SVGTransformItem item = new SVGTransformItem();
         item.setTranslate(tx, ty);
         this.listHandler.item(item);
      }

      public void scale(float sx) throws ParseException {
         SVGTransformItem item = new SVGTransformItem();
         item.scale(sx);
         this.listHandler.item(item);
      }

      public void scale(float sx, float sy) throws ParseException {
         SVGTransformItem item = new SVGTransformItem();
         item.setScale(sx, sy);
         this.listHandler.item(item);
      }

      public void skewX(float skx) throws ParseException {
         SVGTransformItem item = new SVGTransformItem();
         item.setSkewX(skx);
         this.listHandler.item(item);
      }

      public void skewY(float sky) throws ParseException {
         SVGTransformItem item = new SVGTransformItem();
         item.setSkewY(sky);
         this.listHandler.item(item);
      }

      public void endTransformList() throws ParseException {
         this.listHandler.endList();
      }
   }

   public static class SVGTransformItem extends AbstractSVGTransform implements SVGItem {
      protected boolean xOnly;
      protected boolean angleOnly;
      protected AbstractSVGList parent;
      protected String itemStringValue;

      public SVGTransformItem() {
      }

      public SVGTransformItem(SVGTransform transform) {
         this.assign(transform);
      }

      protected void resetAttribute() {
         if (this.parent != null) {
            this.itemStringValue = null;
            this.parent.itemChanged();
         }

      }

      public void setParent(AbstractSVGList list) {
         this.parent = list;
      }

      public AbstractSVGList getParent() {
         return this.parent;
      }

      public String getValueAsString() {
         if (this.itemStringValue == null) {
            this.itemStringValue = this.getStringValue();
         }

         return this.itemStringValue;
      }

      public void assign(SVGTransform transform) {
         this.type = transform.getType();
         SVGMatrix matrix = transform.getMatrix();
         switch (this.type) {
            case 1:
               this.setMatrix(matrix);
               break;
            case 2:
               this.setTranslate(matrix.getE(), matrix.getF());
               break;
            case 3:
               this.setScale(matrix.getA(), matrix.getD());
               break;
            case 4:
               if (matrix.getE() == 0.0F) {
                  this.rotate(transform.getAngle());
               } else {
                  this.angleOnly = false;
                  if (matrix.getA() == 1.0F) {
                     this.setRotate(transform.getAngle(), matrix.getE(), matrix.getF());
                  } else if (transform instanceof AbstractSVGTransform) {
                     AbstractSVGTransform internal = (AbstractSVGTransform)transform;
                     this.setRotate(internal.getAngle(), internal.getX(), internal.getY());
                  }
               }
               break;
            case 5:
               this.setSkewX(transform.getAngle());
               break;
            case 6:
               this.setSkewY(transform.getAngle());
         }

      }

      protected void translate(float x) {
         this.xOnly = true;
         this.setTranslate(x, 0.0F);
      }

      protected void rotate(float angle) {
         this.angleOnly = true;
         this.setRotate(angle, 0.0F, 0.0F);
      }

      protected void scale(float x) {
         this.xOnly = true;
         this.setScale(x, x);
      }

      protected void matrix(float a, float b, float c, float d, float e, float f) {
         this.setMatrix(new SVGOMMatrix(new AffineTransform(a, b, c, d, e, f)));
      }

      public void setMatrix(SVGMatrix matrix) {
         super.setMatrix(matrix);
         this.resetAttribute();
      }

      public void setTranslate(float tx, float ty) {
         super.setTranslate(tx, ty);
         this.resetAttribute();
      }

      public void setScale(float sx, float sy) {
         super.setScale(sx, sy);
         this.resetAttribute();
      }

      public void setRotate(float angle, float cx, float cy) {
         super.setRotate(angle, cx, cy);
         this.resetAttribute();
      }

      public void setSkewX(float angle) {
         super.setSkewX(angle);
         this.resetAttribute();
      }

      public void setSkewY(float angle) {
         super.setSkewY(angle);
         this.resetAttribute();
      }

      protected SVGMatrix createMatrix() {
         return new AbstractSVGMatrix() {
            protected AffineTransform getAffineTransform() {
               return SVGTransformItem.this.affineTransform;
            }

            public void setA(float a) throws DOMException {
               SVGTransformItem.this.type = 1;
               super.setA(a);
               SVGTransformItem.this.resetAttribute();
            }

            public void setB(float b) throws DOMException {
               SVGTransformItem.this.type = 1;
               super.setB(b);
               SVGTransformItem.this.resetAttribute();
            }

            public void setC(float c) throws DOMException {
               SVGTransformItem.this.type = 1;
               super.setC(c);
               SVGTransformItem.this.resetAttribute();
            }

            public void setD(float d) throws DOMException {
               SVGTransformItem.this.type = 1;
               super.setD(d);
               SVGTransformItem.this.resetAttribute();
            }

            public void setE(float e) throws DOMException {
               SVGTransformItem.this.type = 1;
               super.setE(e);
               SVGTransformItem.this.resetAttribute();
            }

            public void setF(float f) throws DOMException {
               SVGTransformItem.this.type = 1;
               super.setF(f);
               SVGTransformItem.this.resetAttribute();
            }
         };
      }

      protected String getStringValue() {
         StringBuffer buf = new StringBuffer();
         switch (this.type) {
            case 1:
               buf.append("matrix(");
               double[] matrix = new double[6];
               this.affineTransform.getMatrix(matrix);

               for(int i = 0; i < 6; ++i) {
                  if (i != 0) {
                     buf.append(' ');
                  }

                  buf.append((float)matrix[i]);
               }

               buf.append(')');
               break;
            case 2:
               buf.append("translate(");
               buf.append((float)this.affineTransform.getTranslateX());
               if (!this.xOnly) {
                  buf.append(' ');
                  buf.append((float)this.affineTransform.getTranslateY());
               }

               buf.append(')');
               break;
            case 3:
               buf.append("scale(");
               buf.append((float)this.affineTransform.getScaleX());
               if (!this.xOnly) {
                  buf.append(' ');
                  buf.append((float)this.affineTransform.getScaleY());
               }

               buf.append(')');
               break;
            case 4:
               buf.append("rotate(");
               buf.append(this.angle);
               if (!this.angleOnly) {
                  buf.append(' ');
                  buf.append(this.x);
                  buf.append(' ');
                  buf.append(this.y);
               }

               buf.append(')');
               break;
            case 5:
               buf.append("skewX(");
               buf.append(this.angle);
               buf.append(')');
               break;
            case 6:
               buf.append("skewY(");
               buf.append(this.angle);
               buf.append(')');
         }

         return buf.toString();
      }
   }
}
