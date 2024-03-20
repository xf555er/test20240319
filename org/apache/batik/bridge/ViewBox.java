package org.apache.batik.bridge;

import java.awt.geom.AffineTransform;
import java.util.StringTokenizer;
import org.apache.batik.anim.dom.SVGOMAnimatedRect;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.FragmentIdentifierHandler;
import org.apache.batik.parser.FragmentIdentifierParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PreserveAspectRatioParser;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGAnimatedRect;
import org.w3c.dom.svg.SVGPreserveAspectRatio;
import org.w3c.dom.svg.SVGRect;

public abstract class ViewBox implements SVGConstants, ErrorConstants {
   protected ViewBox() {
   }

   public static AffineTransform getViewTransform(String ref, Element e, float w, float h, BridgeContext ctx) {
      if (ref != null && ref.length() != 0) {
         ViewHandler vh = new ViewHandler();
         FragmentIdentifierParser p = new FragmentIdentifierParser();
         p.setFragmentIdentifierHandler(vh);
         p.parse(ref);
         Element viewElement = e;
         if (vh.hasId) {
            Document document = e.getOwnerDocument();
            viewElement = document.getElementById(vh.id);
         }

         if (viewElement == null) {
            throw new BridgeException(ctx, e, "uri.malformed", new Object[]{ref});
         } else {
            Element ancestorSVG = getClosestAncestorSVGElement(e);
            if (!viewElement.getNamespaceURI().equals("http://www.w3.org/2000/svg") || !viewElement.getLocalName().equals("view")) {
               viewElement = ancestorSVG;
            }

            float[] vb;
            if (vh.hasViewBox) {
               vb = vh.viewBox;
            } else {
               Element elt;
               if (DOMUtilities.isAttributeSpecifiedNS(viewElement, (String)null, "viewBox")) {
                  elt = viewElement;
               } else {
                  elt = ancestorSVG;
               }

               String viewBoxStr = elt.getAttributeNS((String)null, "viewBox");
               vb = parseViewBoxAttribute(elt, viewBoxStr, ctx);
            }

            short align;
            boolean meet;
            if (vh.hasPreserveAspectRatio) {
               align = vh.align;
               meet = vh.meet;
            } else {
               Element elt;
               if (DOMUtilities.isAttributeSpecifiedNS(viewElement, (String)null, "preserveAspectRatio")) {
                  elt = viewElement;
               } else {
                  elt = ancestorSVG;
               }

               String aspectRatio = elt.getAttributeNS((String)null, "preserveAspectRatio");
               PreserveAspectRatioParser pp = new PreserveAspectRatioParser();
               ViewHandler ph = new ViewHandler();
               pp.setPreserveAspectRatioHandler(ph);

               try {
                  pp.parse(aspectRatio);
               } catch (ParseException var17) {
                  throw new BridgeException(ctx, elt, var17, "attribute.malformed", new Object[]{"preserveAspectRatio", aspectRatio, var17});
               }

               align = ph.align;
               meet = ph.meet;
            }

            AffineTransform transform = getPreserveAspectRatioTransform(vb, align, meet, w, h);
            if (vh.hasTransform) {
               transform.concatenate(vh.getAffineTransform());
            }

            return transform;
         }
      } else {
         return getPreserveAspectRatioTransform(e, w, h, ctx);
      }
   }

   private static Element getClosestAncestorSVGElement(Element e) {
      for(Node n = e; n != null && ((Node)n).getNodeType() == 1; n = ((Node)n).getParentNode()) {
         Element tmp = (Element)n;
         if (tmp.getNamespaceURI().equals("http://www.w3.org/2000/svg") && tmp.getLocalName().equals("svg")) {
            return tmp;
         }
      }

      return null;
   }

   /** @deprecated */
   public static AffineTransform getPreserveAspectRatioTransform(Element e, float w, float h) {
      return getPreserveAspectRatioTransform(e, w, h, (BridgeContext)null);
   }

   public static AffineTransform getPreserveAspectRatioTransform(Element e, float w, float h, BridgeContext ctx) {
      String viewBox = e.getAttributeNS((String)null, "viewBox");
      String aspectRatio = e.getAttributeNS((String)null, "preserveAspectRatio");
      return getPreserveAspectRatioTransform(e, viewBox, aspectRatio, w, h, ctx);
   }

   public static AffineTransform getPreserveAspectRatioTransform(Element e, String viewBox, String aspectRatio, float w, float h, BridgeContext ctx) {
      if (viewBox.length() == 0) {
         return new AffineTransform();
      } else {
         float[] vb = parseViewBoxAttribute(e, viewBox, ctx);
         PreserveAspectRatioParser p = new PreserveAspectRatioParser();
         ViewHandler ph = new ViewHandler();
         p.setPreserveAspectRatioHandler(ph);

         try {
            p.parse(aspectRatio);
         } catch (ParseException var10) {
            throw new BridgeException(ctx, e, var10, "attribute.malformed", new Object[]{"preserveAspectRatio", aspectRatio, var10});
         }

         return getPreserveAspectRatioTransform(vb, ph.align, ph.meet, w, h);
      }
   }

   public static AffineTransform getPreserveAspectRatioTransform(Element e, float[] vb, float w, float h, BridgeContext ctx) {
      String aspectRatio = e.getAttributeNS((String)null, "preserveAspectRatio");
      PreserveAspectRatioParser p = new PreserveAspectRatioParser();
      ViewHandler ph = new ViewHandler();
      p.setPreserveAspectRatioHandler(ph);

      try {
         p.parse(aspectRatio);
      } catch (ParseException var9) {
         throw new BridgeException(ctx, e, var9, "attribute.malformed", new Object[]{"preserveAspectRatio", aspectRatio, var9});
      }

      return getPreserveAspectRatioTransform(vb, ph.align, ph.meet, w, h);
   }

   public static AffineTransform getPreserveAspectRatioTransform(Element e, float[] vb, float w, float h, SVGAnimatedPreserveAspectRatio aPAR, BridgeContext ctx) {
      try {
         SVGPreserveAspectRatio pAR = aPAR.getAnimVal();
         short align = pAR.getAlign();
         boolean meet = pAR.getMeetOrSlice() == 1;
         return getPreserveAspectRatioTransform(vb, align, meet, w, h);
      } catch (LiveAttributeException var9) {
         throw new BridgeException(ctx, var9);
      }
   }

   public static AffineTransform getPreserveAspectRatioTransform(Element e, SVGAnimatedRect aViewBox, SVGAnimatedPreserveAspectRatio aPAR, float w, float h, BridgeContext ctx) {
      if (!((SVGOMAnimatedRect)aViewBox).isSpecified()) {
         return new AffineTransform();
      } else {
         SVGRect viewBox = aViewBox.getAnimVal();
         float[] vb = new float[]{viewBox.getX(), viewBox.getY(), viewBox.getWidth(), viewBox.getHeight()};
         return getPreserveAspectRatioTransform(e, vb, w, h, aPAR, ctx);
      }
   }

   public static float[] parseViewBoxAttribute(Element e, String value, BridgeContext ctx) {
      if (value.length() == 0) {
         return null;
      } else {
         int i = 0;
         float[] vb = new float[4];
         StringTokenizer st = new StringTokenizer(value, " ,");

         try {
            while(i < 4 && st.hasMoreTokens()) {
               vb[i] = Float.parseFloat(st.nextToken());
               ++i;
            }
         } catch (NumberFormatException var7) {
            throw new BridgeException(ctx, e, var7, "attribute.malformed", new Object[]{"viewBox", value, var7});
         }

         if (i != 4) {
            throw new BridgeException(ctx, e, "attribute.malformed", new Object[]{"viewBox", value});
         } else if (!(vb[2] < 0.0F) && !(vb[3] < 0.0F)) {
            return vb[2] != 0.0F && vb[3] != 0.0F ? vb : null;
         } else {
            throw new BridgeException(ctx, e, "attribute.malformed", new Object[]{"viewBox", value});
         }
      }
   }

   public static AffineTransform getPreserveAspectRatioTransform(float[] vb, short align, boolean meet, float w, float h) {
      if (vb == null) {
         return new AffineTransform();
      } else {
         AffineTransform result = new AffineTransform();
         float vpar = vb[2] / vb[3];
         float svgar = w / h;
         if (align == 1) {
            result.scale((double)(w / vb[2]), (double)(h / vb[3]));
            result.translate((double)(-vb[0]), (double)(-vb[1]));
         } else {
            float sf;
            if (vpar < svgar && meet || vpar >= svgar && !meet) {
               sf = h / vb[3];
               result.scale((double)sf, (double)sf);
               switch (align) {
                  case 2:
                  case 5:
                  case 8:
                     result.translate((double)(-vb[0]), (double)(-vb[1]));
                     break;
                  case 3:
                  case 6:
                  case 9:
                     result.translate((double)(-vb[0] - (vb[2] - w * vb[3] / h) / 2.0F), (double)(-vb[1]));
                     break;
                  case 4:
                  case 7:
                  default:
                     result.translate((double)(-vb[0] - (vb[2] - w * vb[3] / h)), (double)(-vb[1]));
               }
            } else {
               sf = w / vb[2];
               result.scale((double)sf, (double)sf);
               switch (align) {
                  case 2:
                  case 3:
                  case 4:
                     result.translate((double)(-vb[0]), (double)(-vb[1]));
                     break;
                  case 5:
                  case 6:
                  case 7:
                     result.translate((double)(-vb[0]), (double)(-vb[1] - (vb[3] - h * vb[2] / w) / 2.0F));
                     break;
                  default:
                     result.translate((double)(-vb[0]), (double)(-vb[1] - (vb[3] - h * vb[2] / w)));
               }
            }
         }

         return result;
      }
   }

   protected static class ViewHandler extends AWTTransformProducer implements FragmentIdentifierHandler {
      public boolean hasTransform;
      public boolean hasId;
      public boolean hasViewBox;
      public boolean hasViewTargetParams;
      public boolean hasZoomAndPanParams;
      public String id;
      public float[] viewBox;
      public String viewTargetParams;
      public boolean isMagnify;
      public boolean hasPreserveAspectRatio;
      public short align;
      public boolean meet = true;

      public void endTransformList() throws ParseException {
         super.endTransformList();
         this.hasTransform = true;
      }

      public void startFragmentIdentifier() throws ParseException {
      }

      public void idReference(String s) throws ParseException {
         this.id = s;
         this.hasId = true;
      }

      public void viewBox(float x, float y, float width, float height) throws ParseException {
         this.hasViewBox = true;
         this.viewBox = new float[4];
         this.viewBox[0] = x;
         this.viewBox[1] = y;
         this.viewBox[2] = width;
         this.viewBox[3] = height;
      }

      public void startViewTarget() throws ParseException {
      }

      public void viewTarget(String name) throws ParseException {
         this.viewTargetParams = name;
         this.hasViewTargetParams = true;
      }

      public void endViewTarget() throws ParseException {
      }

      public void zoomAndPan(boolean magnify) {
         this.isMagnify = magnify;
         this.hasZoomAndPanParams = true;
      }

      public void endFragmentIdentifier() throws ParseException {
      }

      public void startPreserveAspectRatio() throws ParseException {
      }

      public void none() throws ParseException {
         this.align = 1;
      }

      public void xMaxYMax() throws ParseException {
         this.align = 10;
      }

      public void xMaxYMid() throws ParseException {
         this.align = 7;
      }

      public void xMaxYMin() throws ParseException {
         this.align = 4;
      }

      public void xMidYMax() throws ParseException {
         this.align = 9;
      }

      public void xMidYMid() throws ParseException {
         this.align = 6;
      }

      public void xMidYMin() throws ParseException {
         this.align = 3;
      }

      public void xMinYMax() throws ParseException {
         this.align = 8;
      }

      public void xMinYMid() throws ParseException {
         this.align = 5;
      }

      public void xMinYMin() throws ParseException {
         this.align = 2;
      }

      public void meet() throws ParseException {
         this.meet = true;
      }

      public void slice() throws ParseException {
         this.meet = false;
      }

      public void endPreserveAspectRatio() throws ParseException {
         this.hasPreserveAspectRatio = true;
      }
   }
}
