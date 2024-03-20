package org.apache.batik.bridge;

import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.batik.anim.dom.AbstractSVGAnimatedLength;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMAnimatedRect;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGSVGContext;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGAnimatedRect;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGRect;

public class SVGSVGElementBridge extends SVGGElementBridge implements SVGSVGContext {
   public String getLocalName() {
      return "svg";
   }

   public Bridge getInstance() {
      return new SVGSVGElementBridge();
   }

   protected GraphicsNode instantiateGraphicsNode() {
      return new CanvasGraphicsNode();
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
         return null;
      } else {
         CanvasGraphicsNode cgn = (CanvasGraphicsNode)this.instantiateGraphicsNode();
         this.associateSVGContext(ctx, e, cgn);

         try {
            SVGDocument doc = (SVGDocument)e.getOwnerDocument();
            SVGOMSVGElement se = (SVGOMSVGElement)e;
            boolean isOutermost = doc.getRootElement() == e;
            float x = 0.0F;
            float y = 0.0F;
            AbstractSVGAnimatedLength _width;
            if (!isOutermost) {
               _width = (AbstractSVGAnimatedLength)se.getX();
               x = _width.getCheckedValue();
               AbstractSVGAnimatedLength _y = (AbstractSVGAnimatedLength)se.getY();
               y = _y.getCheckedValue();
            }

            _width = (AbstractSVGAnimatedLength)se.getWidth();
            float w = _width.getCheckedValue();
            AbstractSVGAnimatedLength _height = (AbstractSVGAnimatedLength)se.getHeight();
            float h = _height.getCheckedValue();
            cgn.setVisible(CSSUtilities.convertVisibility(e));
            SVGOMAnimatedRect vb = (SVGOMAnimatedRect)se.getViewBox();
            SVGAnimatedPreserveAspectRatio par = se.getPreserveAspectRatio();
            AffineTransform viewingTransform = ViewBox.getPreserveAspectRatioTransform(e, (SVGAnimatedRect)vb, (SVGAnimatedPreserveAspectRatio)par, w, h, ctx);
            float actualWidth = w;
            float actualHeight = h;

            AffineTransform positionTransform;
            try {
               positionTransform = viewingTransform.createInverse();
               actualWidth = (float)((double)w * positionTransform.getScaleX());
               actualHeight = (float)((double)h * positionTransform.getScaleY());
            } catch (NoninvertibleTransformException var24) {
            }

            positionTransform = AffineTransform.getTranslateInstance((double)x, (double)y);
            if (!isOutermost) {
               cgn.setPositionTransform(positionTransform);
            } else if (doc == ctx.getDocument()) {
               final double dw = (double)w;
               final double dh = (double)h;
               ctx.setDocumentSize(new Dimension2D() {
                  double w = dw;
                  double h = dh;

                  public double getWidth() {
                     return this.w;
                  }

                  public double getHeight() {
                     return this.h;
                  }

                  public void setSize(double w, double h) {
                     this.w = w;
                     this.h = h;
                  }
               });
            }

            cgn.setViewingTransform(viewingTransform);
            Shape clip = null;
            if (CSSUtilities.convertOverflow(e)) {
               float[] offsets = CSSUtilities.convertClip(e);
               if (offsets == null) {
                  clip = new Rectangle2D.Float(x, y, w, h);
               } else {
                  clip = new Rectangle2D.Float(x + offsets[3], y + offsets[0], w - offsets[1] - offsets[3], h - offsets[2] - offsets[0]);
               }
            }

            AffineTransform at;
            if (clip != null) {
               try {
                  at = new AffineTransform(positionTransform);
                  at.concatenate(viewingTransform);
                  at = at.createInverse();
                  Shape clip = at.createTransformedShape(clip);
                  Filter filter = cgn.getGraphicsNodeRable(true);
                  cgn.setClip(new ClipRable8Bit(filter, clip));
               } catch (NoninvertibleTransformException var23) {
               }
            }

            at = null;
            RenderingHints hints = CSSUtilities.convertColorRendering(e, at);
            if (hints != null) {
               cgn.setRenderingHints(hints);
            }

            Rectangle2D r = CSSUtilities.convertEnableBackground(e);
            if (r != null) {
               cgn.setBackgroundEnable(r);
            }

            if (vb.isSpecified()) {
               SVGRect vbr = vb.getAnimVal();
               actualWidth = vbr.getWidth();
               actualHeight = vbr.getHeight();
            }

            ctx.openViewport(e, new SVGSVGElementViewport(actualWidth, actualHeight));
            return cgn;
         } catch (LiveAttributeException var25) {
            throw new BridgeException(ctx, var25);
         }
      }
   }

   public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
      node.setComposite(CSSUtilities.convertOpacity(e));
      node.setFilter(CSSUtilities.convertFilter(e, node, ctx));
      node.setMask(CSSUtilities.convertMask(e, node, ctx));
      node.setPointerEventType(CSSUtilities.convertPointerEvents(e));
      this.initializeDynamicSupport(ctx, e, node);
      ctx.closeViewport(e);
   }

   public void dispose() {
      this.ctx.removeViewport(this.e);
      super.dispose();
   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      try {
         boolean rebuild = false;
         if (alav.getNamespaceURI() == null) {
            String ln = alav.getLocalName();
            if (!ln.equals("width") && !ln.equals("height")) {
               SVGDocument doc;
               SVGOMSVGElement se;
               boolean isOutermost;
               float y;
               AbstractSVGAnimatedLength _width;
               float w;
               if (!ln.equals("x") && !ln.equals("y")) {
                  if (ln.equals("viewBox") || ln.equals("preserveAspectRatio")) {
                     doc = (SVGDocument)this.e.getOwnerDocument();
                     se = (SVGOMSVGElement)this.e;
                     isOutermost = doc.getRootElement() == this.e;
                     float x = 0.0F;
                     y = 0.0F;
                     if (!isOutermost) {
                        _width = (AbstractSVGAnimatedLength)se.getX();
                        x = _width.getCheckedValue();
                        AbstractSVGAnimatedLength _y = (AbstractSVGAnimatedLength)se.getY();
                        y = _y.getCheckedValue();
                     }

                     _width = (AbstractSVGAnimatedLength)se.getWidth();
                     w = _width.getCheckedValue();
                     AbstractSVGAnimatedLength _height = (AbstractSVGAnimatedLength)se.getHeight();
                     float h = _height.getCheckedValue();
                     CanvasGraphicsNode cgn = (CanvasGraphicsNode)this.node;
                     SVGOMAnimatedRect vb = (SVGOMAnimatedRect)se.getViewBox();
                     SVGAnimatedPreserveAspectRatio par = se.getPreserveAspectRatio();
                     AffineTransform newVT = ViewBox.getPreserveAspectRatioTransform(this.e, (SVGAnimatedRect)vb, (SVGAnimatedPreserveAspectRatio)par, w, h, this.ctx);
                     AffineTransform oldVT = cgn.getViewingTransform();
                     if (newVT.getScaleX() == oldVT.getScaleX() && newVT.getScaleY() == oldVT.getScaleY() && newVT.getShearX() == oldVT.getShearX() && newVT.getShearY() == oldVT.getShearY()) {
                        cgn.setViewingTransform(newVT);
                        Shape clip = null;
                        if (CSSUtilities.convertOverflow(this.e)) {
                           float[] offsets = CSSUtilities.convertClip(this.e);
                           if (offsets == null) {
                              clip = new Rectangle2D.Float(x, y, w, h);
                           } else {
                              clip = new Rectangle2D.Float(x + offsets[3], y + offsets[0], w - offsets[1] - offsets[3], h - offsets[2] - offsets[0]);
                           }
                        }

                        if (clip != null) {
                           try {
                              AffineTransform at = cgn.getPositionTransform();
                              if (at == null) {
                                 at = new AffineTransform();
                              } else {
                                 at = new AffineTransform(at);
                              }

                              at.concatenate(newVT);
                              at = at.createInverse();
                              Shape clip = at.createTransformedShape(clip);
                              Filter filter = cgn.getGraphicsNodeRable(true);
                              cgn.setClip(new ClipRable8Bit(filter, clip));
                           } catch (NoninvertibleTransformException var21) {
                           }
                        }
                     } else {
                        rebuild = true;
                     }
                  }
               } else {
                  doc = (SVGDocument)this.e.getOwnerDocument();
                  se = (SVGOMSVGElement)this.e;
                  isOutermost = doc.getRootElement() == this.e;
                  if (!isOutermost) {
                     AbstractSVGAnimatedLength _x = (AbstractSVGAnimatedLength)se.getX();
                     y = _x.getCheckedValue();
                     _width = (AbstractSVGAnimatedLength)se.getY();
                     w = _width.getCheckedValue();
                     AffineTransform positionTransform = AffineTransform.getTranslateInstance((double)y, (double)w);
                     CanvasGraphicsNode cgn = (CanvasGraphicsNode)this.node;
                     cgn.setPositionTransform(positionTransform);
                     return;
                  }
               }
            } else {
               rebuild = true;
            }

            if (rebuild) {
               CompositeGraphicsNode gn = this.node.getParent();
               gn.remove(this.node);
               disposeTree(this.e, false);
               this.handleElementAdded(gn, this.e.getParentNode(), this.e);
               return;
            }
         }
      } catch (LiveAttributeException var22) {
         throw new BridgeException(this.ctx, var22);
      }

      super.handleAnimatedAttributeChanged(alav);
   }

   public List getIntersectionList(SVGRect svgRect, Element end) {
      List ret = new ArrayList();
      Rectangle2D rect = new Rectangle2D.Float(svgRect.getX(), svgRect.getY(), svgRect.getWidth(), svgRect.getHeight());
      GraphicsNode svgGN = this.ctx.getGraphicsNode(this.e);
      if (svgGN == null) {
         return ret;
      } else {
         Rectangle2D svgBounds = svgGN.getSensitiveBounds();
         if (svgBounds == null) {
            return ret;
         } else if (!rect.intersects(svgBounds)) {
            return ret;
         } else {
            Element base = this.e;
            AffineTransform ati = svgGN.getGlobalTransform();

            try {
               ati = ati.createInverse();
            } catch (NoninvertibleTransformException var21) {
            }

            Node next;
            for(next = base.getFirstChild(); next != null && !(next instanceof Element); next = next.getNextSibling()) {
            }

            if (next == null) {
               return ret;
            } else {
               Element curr = (Element)next;
               Set ancestors = null;
               if (end != null) {
                  ancestors = this.getAncestors(end, base);
                  if (ancestors == null) {
                     end = null;
                  }
               }

               while(curr != null) {
                  String nsURI = curr.getNamespaceURI();
                  String tag = curr.getLocalName();
                  boolean isGroup = "http://www.w3.org/2000/svg".equals(nsURI) && ("g".equals(tag) || "svg".equals(tag) || "a".equals(tag));
                  GraphicsNode gn = this.ctx.getGraphicsNode(curr);
                  if (gn == null) {
                     if (ancestors != null && ancestors.contains(curr)) {
                        break;
                     }

                     curr = this.getNext(curr, base, end);
                  } else {
                     AffineTransform at = gn.getGlobalTransform();
                     Rectangle2D gnBounds = gn.getSensitiveBounds();
                     at.preConcatenate(ati);
                     if (gnBounds != null) {
                        gnBounds = at.createTransformedShape(gnBounds).getBounds2D();
                     }

                     if (gnBounds != null && rect.intersects(gnBounds)) {
                        if (isGroup) {
                           for(next = curr.getFirstChild(); next != null && !(next instanceof Element); next = next.getNextSibling()) {
                           }

                           if (next != null) {
                              curr = (Element)next;
                              continue;
                           }
                        } else {
                           if (curr == end) {
                              break;
                           }

                           if ("http://www.w3.org/2000/svg".equals(nsURI) && "use".equals(tag) && rect.contains(gnBounds)) {
                              ret.add(curr);
                           }

                           if (gn instanceof ShapeNode) {
                              ShapeNode sn = (ShapeNode)gn;
                              Shape sensitive = sn.getSensitiveArea();
                              if (sensitive != null) {
                                 sensitive = at.createTransformedShape(sensitive);
                                 if (sensitive.intersects(rect)) {
                                    ret.add(curr);
                                 }
                              }
                           } else if (gn instanceof TextNode) {
                              SVGOMElement svgElem = (SVGOMElement)curr;
                              SVGTextElementBridge txtBridge = (SVGTextElementBridge)svgElem.getSVGContext();
                              Set elems = txtBridge.getTextIntersectionSet(at, rect);
                              if (ancestors != null && ancestors.contains(curr)) {
                                 this.filterChildren(curr, end, elems, ret);
                              } else {
                                 ret.addAll(elems);
                              }
                           } else {
                              ret.add(curr);
                           }
                        }

                        curr = this.getNext(curr, base, end);
                     } else {
                        if (ancestors != null && ancestors.contains(curr)) {
                           break;
                        }

                        curr = this.getNext(curr, base, end);
                     }
                  }
               }

               return ret;
            }
         }
      }
   }

   public List getEnclosureList(SVGRect svgRect, Element end) {
      List ret = new ArrayList();
      Rectangle2D rect = new Rectangle2D.Float(svgRect.getX(), svgRect.getY(), svgRect.getWidth(), svgRect.getHeight());
      GraphicsNode svgGN = this.ctx.getGraphicsNode(this.e);
      if (svgGN == null) {
         return ret;
      } else {
         Rectangle2D svgBounds = svgGN.getSensitiveBounds();
         if (svgBounds == null) {
            return ret;
         } else if (!rect.intersects(svgBounds)) {
            return ret;
         } else {
            Element base = this.e;
            AffineTransform ati = svgGN.getGlobalTransform();

            try {
               ati = ati.createInverse();
            } catch (NoninvertibleTransformException var21) {
            }

            Node next;
            for(next = base.getFirstChild(); next != null && !(next instanceof Element); next = next.getNextSibling()) {
            }

            if (next == null) {
               return ret;
            } else {
               Element curr = (Element)next;
               Set ancestors = null;
               if (end != null) {
                  ancestors = this.getAncestors(end, base);
                  if (ancestors == null) {
                     end = null;
                  }
               }

               while(curr != null) {
                  String nsURI = curr.getNamespaceURI();
                  String tag = curr.getLocalName();
                  boolean isGroup = "http://www.w3.org/2000/svg".equals(nsURI) && ("g".equals(tag) || "svg".equals(tag) || "a".equals(tag));
                  GraphicsNode gn = this.ctx.getGraphicsNode(curr);
                  if (gn == null) {
                     if (ancestors != null && ancestors.contains(curr)) {
                        break;
                     }

                     curr = this.getNext(curr, base, end);
                  } else {
                     AffineTransform at = gn.getGlobalTransform();
                     Rectangle2D gnBounds = gn.getSensitiveBounds();
                     at.preConcatenate(ati);
                     if (gnBounds != null) {
                        gnBounds = at.createTransformedShape(gnBounds).getBounds2D();
                     }

                     if (gnBounds != null && rect.intersects(gnBounds)) {
                        if (isGroup) {
                           for(next = curr.getFirstChild(); next != null && !(next instanceof Element); next = next.getNextSibling()) {
                           }

                           if (next != null) {
                              curr = (Element)next;
                              continue;
                           }
                        } else {
                           if (curr == end) {
                              break;
                           }

                           if ("http://www.w3.org/2000/svg".equals(nsURI) && "use".equals(tag)) {
                              if (rect.contains(gnBounds)) {
                                 ret.add(curr);
                              }
                           } else if (gn instanceof TextNode) {
                              SVGOMElement svgElem = (SVGOMElement)curr;
                              SVGTextElementBridge txtBridge = (SVGTextElementBridge)svgElem.getSVGContext();
                              Set elems = txtBridge.getTextEnclosureSet(at, rect);
                              if (ancestors != null && ancestors.contains(curr)) {
                                 this.filterChildren(curr, end, elems, ret);
                              } else {
                                 ret.addAll(elems);
                              }
                           } else if (rect.contains(gnBounds)) {
                              ret.add(curr);
                           }
                        }

                        curr = this.getNext(curr, base, end);
                     } else {
                        if (ancestors != null && ancestors.contains(curr)) {
                           break;
                        }

                        curr = this.getNext(curr, base, end);
                     }
                  }
               }

               return ret;
            }
         }
      }
   }

   public boolean checkIntersection(Element element, SVGRect svgRect) {
      GraphicsNode svgGN = this.ctx.getGraphicsNode(this.e);
      if (svgGN == null) {
         return false;
      } else {
         Rectangle2D rect = new Rectangle2D.Float(svgRect.getX(), svgRect.getY(), svgRect.getWidth(), svgRect.getHeight());
         AffineTransform ati = svgGN.getGlobalTransform();

         try {
            ati = ati.createInverse();
         } catch (NoninvertibleTransformException var12) {
         }

         SVGContext svgctx = null;
         if (element instanceof SVGOMElement) {
            svgctx = ((SVGOMElement)element).getSVGContext();
            if (svgctx instanceof SVGTextElementBridge || svgctx instanceof SVGTextElementBridge.AbstractTextChildSVGContext) {
               return SVGTextElementBridge.getTextIntersection(this.ctx, element, ati, rect, true);
            }
         }

         Rectangle2D gnBounds = null;
         GraphicsNode gn = this.ctx.getGraphicsNode(element);
         if (gn != null) {
            gnBounds = gn.getSensitiveBounds();
         }

         if (gnBounds == null) {
            return false;
         } else {
            AffineTransform at = gn.getGlobalTransform();
            at.preConcatenate(ati);
            gnBounds = at.createTransformedShape(gnBounds).getBounds2D();
            if (!rect.intersects(gnBounds)) {
               return false;
            } else if (!(gn instanceof ShapeNode)) {
               return true;
            } else {
               ShapeNode sn = (ShapeNode)gn;
               Shape sensitive = sn.getSensitiveArea();
               if (sensitive == null) {
                  return false;
               } else {
                  sensitive = at.createTransformedShape(sensitive);
                  return sensitive.intersects(rect);
               }
            }
         }
      }
   }

   public boolean checkEnclosure(Element element, SVGRect svgRect) {
      GraphicsNode gn = this.ctx.getGraphicsNode(element);
      Rectangle2D gnBounds = null;
      SVGContext svgctx = null;
      if (element instanceof SVGOMElement) {
         svgctx = ((SVGOMElement)element).getSVGContext();
         if (!(svgctx instanceof SVGTextElementBridge) && !(svgctx instanceof SVGTextElementBridge.AbstractTextChildSVGContext)) {
            if (gn != null) {
               gnBounds = gn.getSensitiveBounds();
            }
         } else {
            gnBounds = SVGTextElementBridge.getTextBounds(this.ctx, element, true);

            for(Element p = (Element)element.getParentNode(); p != null && gn == null; p = (Element)p.getParentNode()) {
               gn = this.ctx.getGraphicsNode(p);
            }
         }
      } else if (gn != null) {
         gnBounds = gn.getSensitiveBounds();
      }

      if (gnBounds == null) {
         return false;
      } else {
         GraphicsNode svgGN = this.ctx.getGraphicsNode(this.e);
         if (svgGN == null) {
            return false;
         } else {
            Rectangle2D rect = new Rectangle2D.Float(svgRect.getX(), svgRect.getY(), svgRect.getWidth(), svgRect.getHeight());
            AffineTransform ati = svgGN.getGlobalTransform();

            try {
               ati = ati.createInverse();
            } catch (NoninvertibleTransformException var10) {
            }

            AffineTransform at = gn.getGlobalTransform();
            at.preConcatenate(ati);
            gnBounds = at.createTransformedShape(gnBounds).getBounds2D();
            return rect.contains(gnBounds);
         }
      }
   }

   public boolean filterChildren(Element curr, Element end, Set elems, List ret) {
      for(Node child = curr.getFirstChild(); child != null; child = child.getNextSibling()) {
         if (child instanceof Element && this.filterChildren((Element)child, end, elems, ret)) {
            return true;
         }
      }

      if (curr == end) {
         return true;
      } else {
         if (elems.contains(curr)) {
            ret.add(curr);
         }

         return false;
      }
   }

   protected Set getAncestors(Element end, Element base) {
      Set ret = new HashSet();
      Element p = end;

      do {
         ret.add(p);
         p = (Element)p.getParentNode();
      } while(p != null && p != base);

      return p == null ? null : ret;
   }

   protected Element getNext(Element curr, Element base, Element end) {
      Node next;
      for(next = curr.getNextSibling(); next != null && !(next instanceof Element); next = next.getNextSibling()) {
      }

      label31:
      while(next == null) {
         curr = (Element)curr.getParentNode();
         if (curr != end && curr != base) {
            next = curr.getNextSibling();

            while(true) {
               if (next == null || next instanceof Element) {
                  continue label31;
               }

               next = next.getNextSibling();
            }
         }

         next = null;
         break;
      }

      return (Element)next;
   }

   public void deselectAll() {
      this.ctx.getUserAgent().deselectAll();
   }

   public int suspendRedraw(int max_wait_milliseconds) {
      UpdateManager um = this.ctx.getUpdateManager();
      return um != null ? um.addRedrawSuspension(max_wait_milliseconds) : -1;
   }

   public boolean unsuspendRedraw(int suspend_handle_id) {
      UpdateManager um = this.ctx.getUpdateManager();
      return um != null ? um.releaseRedrawSuspension(suspend_handle_id) : false;
   }

   public void unsuspendRedrawAll() {
      UpdateManager um = this.ctx.getUpdateManager();
      if (um != null) {
         um.releaseAllRedrawSuspension();
      }

   }

   public void forceRedraw() {
      UpdateManager um = this.ctx.getUpdateManager();
      if (um != null) {
         um.forceRepaint();
      }

   }

   public void pauseAnimations() {
      this.ctx.getAnimationEngine().pause();
   }

   public void unpauseAnimations() {
      this.ctx.getAnimationEngine().unpause();
   }

   public boolean animationsPaused() {
      return this.ctx.getAnimationEngine().isPaused();
   }

   public float getCurrentTime() {
      return this.ctx.getAnimationEngine().getCurrentTime();
   }

   public void setCurrentTime(float t) {
      this.ctx.getAnimationEngine().setCurrentTime(t);
   }

   public static class SVGSVGElementViewport implements Viewport {
      private float width;
      private float height;

      public SVGSVGElementViewport(float w, float h) {
         this.width = w;
         this.height = h;
      }

      public float getWidth() {
         return this.width;
      }

      public float getHeight() {
         return this.height;
      }
   }
}
