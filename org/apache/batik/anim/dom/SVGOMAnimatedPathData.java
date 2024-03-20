package org.apache.batik.anim.dom;

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.batik.anim.values.AnimatablePathDataValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.svg.AbstractSVGNormPathSegList;
import org.apache.batik.dom.svg.AbstractSVGPathSegList;
import org.apache.batik.dom.svg.ListBuilder;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGAnimatedPathDataSupport;
import org.apache.batik.dom.svg.SVGItem;
import org.apache.batik.dom.svg.SVGPathSegItem;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathArrayProducer;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedPathData;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegList;

public class SVGOMAnimatedPathData extends AbstractSVGAnimatedValue implements SVGAnimatedPathData {
   protected boolean changing;
   protected BaseSVGPathSegList pathSegs;
   protected NormalizedBaseSVGPathSegList normalizedPathSegs;
   protected AnimSVGPathSegList animPathSegs;
   protected String defaultValue;

   public SVGOMAnimatedPathData(AbstractElement elt, String ns, String ln, String defaultValue) {
      super(elt, ns, ln);
      this.defaultValue = defaultValue;
   }

   public SVGPathSegList getAnimatedNormalizedPathSegList() {
      throw new UnsupportedOperationException("SVGAnimatedPathData.getAnimatedNormalizedPathSegList is not implemented");
   }

   public SVGPathSegList getAnimatedPathSegList() {
      if (this.animPathSegs == null) {
         this.animPathSegs = new AnimSVGPathSegList();
      }

      return this.animPathSegs;
   }

   public SVGPathSegList getNormalizedPathSegList() {
      if (this.normalizedPathSegs == null) {
         this.normalizedPathSegs = new NormalizedBaseSVGPathSegList();
      }

      return this.normalizedPathSegs;
   }

   public SVGPathSegList getPathSegList() {
      if (this.pathSegs == null) {
         this.pathSegs = new BaseSVGPathSegList();
      }

      return this.pathSegs;
   }

   public void check() {
      if (!this.hasAnimVal) {
         if (this.pathSegs == null) {
            this.pathSegs = new BaseSVGPathSegList();
         }

         this.pathSegs.revalidate();
         if (this.pathSegs.missing) {
            throw new LiveAttributeException(this.element, this.localName, (short)0, (String)null);
         }

         if (this.pathSegs.malformed) {
            throw new LiveAttributeException(this.element, this.localName, (short)1, this.pathSegs.getValueAsString());
         }
      }

   }

   public AnimatableValue getUnderlyingValue(AnimationTarget target) {
      SVGPathSegList psl = this.getPathSegList();
      PathArrayProducer pp = new PathArrayProducer();
      SVGAnimatedPathDataSupport.handlePathSegList(psl, pp);
      return new AnimatablePathDataValue(target, pp.getPathCommands(), pp.getPathParameters());
   }

   protected void updateAnimatedValue(AnimatableValue val) {
      if (val == null) {
         this.hasAnimVal = false;
      } else {
         this.hasAnimVal = true;
         AnimatablePathDataValue animPath = (AnimatablePathDataValue)val;
         if (this.animPathSegs == null) {
            this.animPathSegs = new AnimSVGPathSegList();
         }

         this.animPathSegs.setAnimatedValue(animPath.getCommands(), animPath.getParameters());
      }

      this.fireAnimatedAttributeListeners();
   }

   public void attrAdded(Attr node, String newv) {
      if (!this.changing) {
         if (this.pathSegs != null) {
            this.pathSegs.invalidate();
         }

         if (this.normalizedPathSegs != null) {
            this.normalizedPathSegs.invalidate();
         }
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public void attrModified(Attr node, String oldv, String newv) {
      if (!this.changing) {
         if (this.pathSegs != null) {
            this.pathSegs.invalidate();
         }

         if (this.normalizedPathSegs != null) {
            this.normalizedPathSegs.invalidate();
         }
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public void attrRemoved(Attr node, String oldv) {
      if (!this.changing) {
         if (this.pathSegs != null) {
            this.pathSegs.invalidate();
         }

         if (this.normalizedPathSegs != null) {
            this.normalizedPathSegs.invalidate();
         }
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public class AnimSVGPathSegList extends AbstractSVGPathSegList {
      private int[] parameterIndex = new int[1];

      public AnimSVGPathSegList() {
         this.itemList = new ArrayList(1);
      }

      protected DOMException createDOMException(short type, String key, Object[] args) {
         return SVGOMAnimatedPathData.this.element.createDOMException(type, key, args);
      }

      protected SVGException createSVGException(short type, String key, Object[] args) {
         return ((SVGOMElement)SVGOMAnimatedPathData.this.element).createSVGException(type, key, args);
      }

      public int getNumberOfItems() {
         return SVGOMAnimatedPathData.this.hasAnimVal ? super.getNumberOfItems() : SVGOMAnimatedPathData.this.getPathSegList().getNumberOfItems();
      }

      public SVGPathSeg getItem(int index) throws DOMException {
         return SVGOMAnimatedPathData.this.hasAnimVal ? super.getItem(index) : SVGOMAnimatedPathData.this.getPathSegList().getItem(index);
      }

      protected String getValueAsString() {
         if (this.itemList.size() == 0) {
            return "";
         } else {
            StringBuffer sb = new StringBuffer(this.itemList.size() * 8);
            Iterator i = this.itemList.iterator();
            if (i.hasNext()) {
               sb.append(((SVGItem)i.next()).getValueAsString());
            }

            while(i.hasNext()) {
               sb.append(this.getItemSeparator());
               sb.append(((SVGItem)i.next()).getValueAsString());
            }

            return sb.toString();
         }
      }

      protected void setAttributeValue(String value) {
      }

      public void clear() throws DOMException {
         throw SVGOMAnimatedPathData.this.element.createDOMException((short)7, "readonly.pathseg.list", (Object[])null);
      }

      public SVGPathSeg initialize(SVGPathSeg newItem) throws DOMException, SVGException {
         throw SVGOMAnimatedPathData.this.element.createDOMException((short)7, "readonly.pathseg.list", (Object[])null);
      }

      public SVGPathSeg insertItemBefore(SVGPathSeg newItem, int index) throws DOMException, SVGException {
         throw SVGOMAnimatedPathData.this.element.createDOMException((short)7, "readonly.pathseg.list", (Object[])null);
      }

      public SVGPathSeg replaceItem(SVGPathSeg newItem, int index) throws DOMException, SVGException {
         throw SVGOMAnimatedPathData.this.element.createDOMException((short)7, "readonly.pathseg.list", (Object[])null);
      }

      public SVGPathSeg removeItem(int index) throws DOMException {
         throw SVGOMAnimatedPathData.this.element.createDOMException((short)7, "readonly.pathseg.list", (Object[])null);
      }

      public SVGPathSeg appendItem(SVGPathSeg newItem) throws DOMException {
         throw SVGOMAnimatedPathData.this.element.createDOMException((short)7, "readonly.pathseg.list", (Object[])null);
      }

      protected SVGPathSegItem newItem(short command, float[] parameters, int[] j) {
         float var4;
         float var5;
         String var10003;
         float var10004;
         int var10005;
         int var10006;
         int var10007;
         int var10008;
         int var10009;
         int var10010;
         int var10011;
         int var10012;
         int var10013;
         switch (command) {
            case 1:
               return new SVGPathSegItem(command, PATHSEG_LETTERS[command]);
            case 2:
            case 3:
            case 4:
            case 5:
               var10003 = PATHSEG_LETTERS[command];
               var10008 = j[0];
               var10005 = j[0];
               j[0] = var10008 + 1;
               var10004 = parameters[var10005];
               var10009 = j[0];
               var10006 = j[0];
               j[0] = var10009 + 1;
               return new AbstractSVGPathSegList.SVGPathSegMovetoLinetoItem(command, var10003, var10004, parameters[var10006]);
            case 6:
            case 7:
               var10003 = PATHSEG_LETTERS[command];
               var10008 = j[0];
               var10005 = j[0];
               j[0] = var10008 + 1;
               var10004 = parameters[var10005];
               var10009 = j[0];
               var10006 = j[0];
               j[0] = var10009 + 1;
               var4 = parameters[var10006];
               var10010 = j[0];
               var10007 = j[0];
               j[0] = var10010 + 1;
               var5 = parameters[var10007];
               var10011 = j[0];
               var10008 = j[0];
               j[0] = var10011 + 1;
               float var7 = parameters[var10008];
               var10012 = j[0];
               var10009 = j[0];
               j[0] = var10012 + 1;
               float var10 = parameters[var10009];
               var10013 = j[0];
               var10010 = j[0];
               j[0] = var10013 + 1;
               return new AbstractSVGPathSegList.SVGPathSegCurvetoCubicItem(command, var10003, var10004, var4, var5, var7, var10, parameters[var10010]);
            case 8:
            case 9:
               var10003 = PATHSEG_LETTERS[command];
               var10008 = j[0];
               var10005 = j[0];
               j[0] = var10008 + 1;
               var10004 = parameters[var10005];
               var10009 = j[0];
               var10006 = j[0];
               j[0] = var10009 + 1;
               var4 = parameters[var10006];
               var10010 = j[0];
               var10007 = j[0];
               j[0] = var10010 + 1;
               var5 = parameters[var10007];
               var10011 = j[0];
               var10008 = j[0];
               j[0] = var10011 + 1;
               return new AbstractSVGPathSegList.SVGPathSegCurvetoQuadraticItem(command, var10003, var10004, var4, var5, parameters[var10008]);
            case 10:
            case 11:
               var10003 = PATHSEG_LETTERS[command];
               var10008 = j[0];
               var10005 = j[0];
               j[0] = var10008 + 1;
               var10004 = parameters[var10005];
               var10009 = j[0];
               var10006 = j[0];
               j[0] = var10009 + 1;
               var4 = parameters[var10006];
               var10010 = j[0];
               var10007 = j[0];
               j[0] = var10010 + 1;
               var5 = parameters[var10007];
               var10011 = j[0];
               var10008 = j[0];
               j[0] = var10011 + 1;
               boolean var6 = parameters[var10008] != 0.0F;
               var10012 = j[0];
               var10009 = j[0];
               j[0] = var10012 + 1;
               boolean var8 = parameters[var10009] != 0.0F;
               var10013 = j[0];
               var10010 = j[0];
               j[0] = var10013 + 1;
               float var9 = parameters[var10010];
               int var10014 = j[0];
               var10011 = j[0];
               j[0] = var10014 + 1;
               return new AbstractSVGPathSegList.SVGPathSegArcItem(command, var10003, var10004, var4, var5, var6, var8, var9, parameters[var10011]);
            case 12:
            case 13:
               var10003 = PATHSEG_LETTERS[command];
               var10008 = j[0];
               var10005 = j[0];
               j[0] = var10008 + 1;
               return new AbstractSVGPathSegList.SVGPathSegLinetoHorizontalItem(command, var10003, parameters[var10005]);
            case 14:
            case 15:
               var10003 = PATHSEG_LETTERS[command];
               var10008 = j[0];
               var10005 = j[0];
               j[0] = var10008 + 1;
               return new AbstractSVGPathSegList.SVGPathSegLinetoVerticalItem(command, var10003, parameters[var10005]);
            case 16:
            case 17:
               var10003 = PATHSEG_LETTERS[command];
               var10008 = j[0];
               var10005 = j[0];
               j[0] = var10008 + 1;
               var10004 = parameters[var10005];
               var10009 = j[0];
               var10006 = j[0];
               j[0] = var10009 + 1;
               var4 = parameters[var10006];
               var10010 = j[0];
               var10007 = j[0];
               j[0] = var10010 + 1;
               var5 = parameters[var10007];
               var10011 = j[0];
               var10008 = j[0];
               j[0] = var10011 + 1;
               return new AbstractSVGPathSegList.SVGPathSegCurvetoCubicSmoothItem(command, var10003, var10004, var4, var5, parameters[var10008]);
            case 18:
            case 19:
               var10003 = PATHSEG_LETTERS[command];
               var10008 = j[0];
               var10005 = j[0];
               j[0] = var10008 + 1;
               var10004 = parameters[var10005];
               var10009 = j[0];
               var10006 = j[0];
               j[0] = var10009 + 1;
               return new AbstractSVGPathSegList.SVGPathSegCurvetoQuadraticSmoothItem(command, var10003, var10004, parameters[var10006]);
            default:
               return null;
         }
      }

      protected void setAnimatedValue(short[] commands, float[] parameters) {
         int size = this.itemList.size();
         int i = 0;
         int[] j = this.parameterIndex;

         for(j[0] = 0; i < size && i < commands.length; ++i) {
            SVGPathSeg s = (SVGPathSeg)this.itemList.get(i);
            if (s.getPathSegType() != commands[i]) {
               this.newItem(commands[i], parameters, j);
            } else {
               int var10002;
               int var10005;
               switch (commands[i]) {
                  case 1:
                  default:
                     break;
                  case 2:
                  case 3:
                  case 4:
                  case 5:
                     AbstractSVGPathSegList.SVGPathSegMovetoLinetoItem psxxx = (AbstractSVGPathSegList.SVGPathSegMovetoLinetoItem)s;
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxxx.setX(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxxx.setY(parameters[var10002]);
                     break;
                  case 6:
                  case 7:
                     AbstractSVGPathSegList.SVGPathSegCurvetoCubicItem psxx = (AbstractSVGPathSegList.SVGPathSegCurvetoCubicItem)s;
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxx.setX1(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxx.setY1(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxx.setX2(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxx.setY2(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxx.setX(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxx.setY(parameters[var10002]);
                     break;
                  case 8:
                  case 9:
                     AbstractSVGPathSegList.SVGPathSegCurvetoQuadraticItem psx = (AbstractSVGPathSegList.SVGPathSegCurvetoQuadraticItem)s;
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psx.setX1(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psx.setY1(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psx.setX(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psx.setY(parameters[var10002]);
                     break;
                  case 10:
                  case 11:
                     AbstractSVGPathSegList.SVGPathSegArcItem ps = (AbstractSVGPathSegList.SVGPathSegArcItem)s;
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     ps.setR1(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     ps.setR2(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     ps.setAngle(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     ps.setLargeArcFlag(parameters[var10002] != 0.0F);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     ps.setSweepFlag(parameters[var10002] != 0.0F);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     ps.setX(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     ps.setY(parameters[var10002]);
                     break;
                  case 12:
                  case 13:
                     AbstractSVGPathSegList.SVGPathSegLinetoHorizontalItem psxxxxxxx = (AbstractSVGPathSegList.SVGPathSegLinetoHorizontalItem)s;
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxxxxxxx.setX(parameters[var10002]);
                     break;
                  case 14:
                  case 15:
                     AbstractSVGPathSegList.SVGPathSegLinetoVerticalItem psxxxxxx = (AbstractSVGPathSegList.SVGPathSegLinetoVerticalItem)s;
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxxxxxx.setY(parameters[var10002]);
                     break;
                  case 16:
                  case 17:
                     AbstractSVGPathSegList.SVGPathSegCurvetoCubicSmoothItem psxxxxx = (AbstractSVGPathSegList.SVGPathSegCurvetoCubicSmoothItem)s;
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxxxxx.setX2(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxxxxx.setY2(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxxxxx.setX(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxxxxx.setY(parameters[var10002]);
                     break;
                  case 18:
                  case 19:
                     AbstractSVGPathSegList.SVGPathSegCurvetoQuadraticSmoothItem psxxxx = (AbstractSVGPathSegList.SVGPathSegCurvetoQuadraticSmoothItem)s;
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxxxx.setX(parameters[var10002]);
                     var10005 = j[0];
                     var10002 = j[0];
                     j[0] = var10005 + 1;
                     psxxxx.setY(parameters[var10002]);
               }
            }
         }

         while(i < commands.length) {
            this.appendItemImpl(this.newItem(commands[i], parameters, j));
            ++i;
         }

         while(size > commands.length) {
            --size;
            this.removeItemImpl(size);
         }

      }

      protected void resetAttribute() {
      }

      protected void resetAttribute(SVGItem item) {
      }

      protected void revalidate() {
         this.valid = true;
      }
   }

   public class NormalizedBaseSVGPathSegList extends AbstractSVGNormPathSegList {
      protected boolean missing;
      protected boolean malformed;

      protected DOMException createDOMException(short type, String key, Object[] args) {
         return SVGOMAnimatedPathData.this.element.createDOMException(type, key, args);
      }

      protected SVGException createSVGException(short type, String key, Object[] args) {
         return ((SVGOMElement)SVGOMAnimatedPathData.this.element).createSVGException(type, key, args);
      }

      protected String getValueAsString() throws SVGException {
         Attr attr = SVGOMAnimatedPathData.this.element.getAttributeNodeNS(SVGOMAnimatedPathData.this.namespaceURI, SVGOMAnimatedPathData.this.localName);
         return attr == null ? SVGOMAnimatedPathData.this.defaultValue : attr.getValue();
      }

      protected void setAttributeValue(String value) {
         try {
            SVGOMAnimatedPathData.this.changing = true;
            SVGOMAnimatedPathData.this.element.setAttributeNS(SVGOMAnimatedPathData.this.namespaceURI, SVGOMAnimatedPathData.this.localName, value);
         } finally {
            SVGOMAnimatedPathData.this.changing = false;
         }

      }

      protected void revalidate() {
         if (!this.valid) {
            this.valid = true;
            this.missing = false;
            this.malformed = false;
            String s = this.getValueAsString();
            if (s == null) {
               this.missing = true;
            } else {
               try {
                  ListBuilder builder = new ListBuilder(this);
                  this.doParse(s, builder);
                  if (builder.getList() != null) {
                     this.clear(this.itemList);
                  }

                  this.itemList = builder.getList();
               } catch (ParseException var3) {
                  this.itemList = new ArrayList(1);
                  this.malformed = true;
               }

            }
         }
      }
   }

   public class BaseSVGPathSegList extends AbstractSVGPathSegList {
      protected boolean missing;
      protected boolean malformed;

      protected DOMException createDOMException(short type, String key, Object[] args) {
         return SVGOMAnimatedPathData.this.element.createDOMException(type, key, args);
      }

      protected SVGException createSVGException(short type, String key, Object[] args) {
         return ((SVGOMElement)SVGOMAnimatedPathData.this.element).createSVGException(type, key, args);
      }

      protected String getValueAsString() {
         Attr attr = SVGOMAnimatedPathData.this.element.getAttributeNodeNS(SVGOMAnimatedPathData.this.namespaceURI, SVGOMAnimatedPathData.this.localName);
         return attr == null ? SVGOMAnimatedPathData.this.defaultValue : attr.getValue();
      }

      protected void setAttributeValue(String value) {
         try {
            SVGOMAnimatedPathData.this.changing = true;
            SVGOMAnimatedPathData.this.element.setAttributeNS(SVGOMAnimatedPathData.this.namespaceURI, SVGOMAnimatedPathData.this.localName, value);
         } finally {
            SVGOMAnimatedPathData.this.changing = false;
         }

      }

      protected void resetAttribute() {
         super.resetAttribute();
         this.missing = false;
         this.malformed = false;
      }

      protected void resetAttribute(SVGItem item) {
         super.resetAttribute(item);
         this.missing = false;
         this.malformed = false;
      }

      protected void revalidate() {
         if (!this.valid) {
            this.valid = true;
            this.missing = false;
            this.malformed = false;
            String s = this.getValueAsString();
            if (s == null) {
               this.missing = true;
            } else {
               try {
                  ListBuilder builder = new ListBuilder(this);
                  this.doParse(s, builder);
                  if (builder.getList() != null) {
                     this.clear(this.itemList);
                  }

                  this.itemList = builder.getList();
               } catch (ParseException var3) {
                  this.itemList = new ArrayList(1);
                  this.malformed = true;
               }

            }
         }
      }
   }
}
