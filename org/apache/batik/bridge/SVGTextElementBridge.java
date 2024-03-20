package org.apache.batik.bridge;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.apache.batik.anim.dom.AbstractSVGAnimatedLength;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMAnimatedEnumeration;
import org.apache.batik.anim.dom.SVGOMAnimatedLengthList;
import org.apache.batik.anim.dom.SVGOMAnimatedNumberList;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.anim.dom.SVGOMTextPositioningElement;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGTextContent;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.UnresolvedFontFamily;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGLengthList;
import org.w3c.dom.svg.SVGNumberList;
import org.w3c.dom.svg.SVGTextContentElement;
import org.w3c.dom.svg.SVGTextPositioningElement;

public class SVGTextElementBridge extends AbstractGraphicsNodeBridge implements SVGTextContent {
   protected static final Integer ZERO = 0;
   public static final AttributedCharacterIterator.Attribute TEXT_COMPOUND_DELIMITER;
   public static final AttributedCharacterIterator.Attribute TEXT_COMPOUND_ID;
   public static final AttributedCharacterIterator.Attribute PAINT_INFO;
   public static final AttributedCharacterIterator.Attribute ALT_GLYPH_HANDLER;
   public static final AttributedCharacterIterator.Attribute TEXTPATH;
   public static final AttributedCharacterIterator.Attribute ANCHOR_TYPE;
   public static final AttributedCharacterIterator.Attribute GVT_FONT_FAMILIES;
   public static final AttributedCharacterIterator.Attribute GVT_FONTS;
   public static final AttributedCharacterIterator.Attribute BASELINE_SHIFT;
   protected AttributedString laidoutText;
   protected WeakHashMap elemTPI = new WeakHashMap();
   protected boolean usingComplexSVGFont = false;
   protected DOMChildNodeRemovedEventListener childNodeRemovedEventListener;
   protected DOMSubtreeModifiedEventListener subtreeModifiedEventListener;
   private boolean hasNewACI;
   private Element cssProceedElement;
   protected int endLimit;

   public String getLocalName() {
      return "text";
   }

   public Bridge getInstance() {
      return new SVGTextElementBridge();
   }

   protected TextNode getTextNode() {
      return (TextNode)this.node;
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      TextNode node = (TextNode)super.createGraphicsNode(ctx, e);
      if (node == null) {
         return null;
      } else {
         this.associateSVGContext(ctx, e, node);

         for(Node child = this.getFirstChild(e); child != null; child = this.getNextSibling(child)) {
            if (child.getNodeType() == 1) {
               this.addContextToChild(ctx, (Element)child);
            }
         }

         if (ctx.getTextPainter() != null) {
            node.setTextPainter(ctx.getTextPainter());
         }

         RenderingHints hints = null;
         hints = CSSUtilities.convertColorRendering(e, hints);
         hints = CSSUtilities.convertTextRendering(e, hints);
         if (hints != null) {
            node.setRenderingHints(hints);
         }

         node.setLocation(this.getLocation(ctx, e));
         return node;
      }
   }

   protected GraphicsNode instantiateGraphicsNode() {
      return new TextNode();
   }

   protected Point2D getLocation(BridgeContext ctx, Element e) {
      try {
         SVGOMTextPositioningElement te = (SVGOMTextPositioningElement)e;
         SVGOMAnimatedLengthList _x = (SVGOMAnimatedLengthList)te.getX();
         _x.check();
         SVGLengthList xs = _x.getAnimVal();
         float x = 0.0F;
         if (xs.getNumberOfItems() > 0) {
            x = xs.getItem(0).getValue();
         }

         SVGOMAnimatedLengthList _y = (SVGOMAnimatedLengthList)te.getY();
         _y.check();
         SVGLengthList ys = _y.getAnimVal();
         float y = 0.0F;
         if (ys.getNumberOfItems() > 0) {
            y = ys.getItem(0).getValue();
         }

         return new Point2D.Float(x, y);
      } catch (LiveAttributeException var10) {
         throw new BridgeException(ctx, var10);
      }
   }

   protected boolean isTextElement(Element e) {
      if (!"http://www.w3.org/2000/svg".equals(e.getNamespaceURI())) {
         return false;
      } else {
         String nodeName = e.getLocalName();
         return nodeName.equals("text") || nodeName.equals("tspan") || nodeName.equals("altGlyph") || nodeName.equals("a") || nodeName.equals("textPath") || nodeName.equals("tref");
      }
   }

   protected boolean isTextChild(Element e) {
      if (!"http://www.w3.org/2000/svg".equals(e.getNamespaceURI())) {
         return false;
      } else {
         String nodeName = e.getLocalName();
         return nodeName.equals("tspan") || nodeName.equals("altGlyph") || nodeName.equals("a") || nodeName.equals("textPath") || nodeName.equals("tref");
      }
   }

   public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
      e.normalize();
      this.computeLaidoutText(ctx, e, node);
      node.setComposite(CSSUtilities.convertOpacity(e));
      node.setFilter(CSSUtilities.convertFilter(e, node, ctx));
      node.setMask(CSSUtilities.convertMask(e, node, ctx));
      node.setClip(CSSUtilities.convertClipPath(e, node, ctx));
      node.setPointerEventType(CSSUtilities.convertPointerEvents(e));
      this.initializeDynamicSupport(ctx, e, node);
      if (!ctx.isDynamic()) {
         this.elemTPI.clear();
      }

   }

   public boolean isComposite() {
      return false;
   }

   protected Node getFirstChild(Node n) {
      return n.getFirstChild();
   }

   protected Node getNextSibling(Node n) {
      return n.getNextSibling();
   }

   protected Node getParentNode(Node n) {
      return n.getParentNode();
   }

   protected void initializeDynamicSupport(BridgeContext ctx, Element e, GraphicsNode node) {
      super.initializeDynamicSupport(ctx, e, node);
      if (ctx.isDynamic()) {
         this.addTextEventListeners(ctx, (NodeEventTarget)e);
      }

   }

   protected void addTextEventListeners(BridgeContext ctx, NodeEventTarget e) {
      if (this.childNodeRemovedEventListener == null) {
         this.childNodeRemovedEventListener = new DOMChildNodeRemovedEventListener();
      }

      if (this.subtreeModifiedEventListener == null) {
         this.subtreeModifiedEventListener = new DOMSubtreeModifiedEventListener();
      }

      e.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.childNodeRemovedEventListener, true, (Object)null);
      ctx.storeEventListenerNS(e, "http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.childNodeRemovedEventListener, true);
      e.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.subtreeModifiedEventListener, false, (Object)null);
      ctx.storeEventListenerNS(e, "http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.subtreeModifiedEventListener, false);
   }

   protected void removeTextEventListeners(BridgeContext ctx, NodeEventTarget e) {
      e.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.childNodeRemovedEventListener, true);
      e.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.subtreeModifiedEventListener, false);
   }

   public void dispose() {
      this.removeTextEventListeners(this.ctx, (NodeEventTarget)this.e);
      super.dispose();
   }

   protected void addContextToChild(BridgeContext ctx, Element e) {
      if ("http://www.w3.org/2000/svg".equals(e.getNamespaceURI())) {
         if (e.getLocalName().equals("tspan")) {
            ((SVGOMElement)e).setSVGContext(new TspanBridge(ctx, this, e));
         } else if (e.getLocalName().equals("textPath")) {
            ((SVGOMElement)e).setSVGContext(new TextPathBridge(ctx, this, e));
         } else if (e.getLocalName().equals("tref")) {
            ((SVGOMElement)e).setSVGContext(new TRefBridge(ctx, this, e));
         }
      }

      for(Node child = this.getFirstChild(e); child != null; child = this.getNextSibling(child)) {
         if (child.getNodeType() == 1) {
            this.addContextToChild(ctx, (Element)child);
         }
      }

   }

   protected void removeContextFromChild(BridgeContext ctx, Element e) {
      if ("http://www.w3.org/2000/svg".equals(e.getNamespaceURI())) {
         if (e.getLocalName().equals("tspan")) {
            ((AbstractTextChildBridgeUpdateHandler)((SVGOMElement)e).getSVGContext()).dispose();
         } else if (e.getLocalName().equals("textPath")) {
            ((AbstractTextChildBridgeUpdateHandler)((SVGOMElement)e).getSVGContext()).dispose();
         } else if (e.getLocalName().equals("tref")) {
            ((AbstractTextChildBridgeUpdateHandler)((SVGOMElement)e).getSVGContext()).dispose();
         }
      }

      for(Node child = this.getFirstChild(e); child != null; child = this.getNextSibling(child)) {
         if (child.getNodeType() == 1) {
            this.removeContextFromChild(ctx, (Element)child);
         }
      }

   }

   public void handleDOMNodeInsertedEvent(MutationEvent evt) {
      Node childNode = (Node)evt.getTarget();
      switch (childNode.getNodeType()) {
         case 1:
            Element childElement = (Element)childNode;
            if (this.isTextChild(childElement)) {
               this.addContextToChild(this.ctx, childElement);
               this.laidoutText = null;
            }
         case 2:
         default:
            break;
         case 3:
         case 4:
            this.laidoutText = null;
      }

      if (this.laidoutText == null) {
         this.computeLaidoutText(this.ctx, this.e, this.getTextNode());
      }

   }

   public void handleDOMChildNodeRemovedEvent(MutationEvent evt) {
      Node childNode = (Node)evt.getTarget();
      switch (childNode.getNodeType()) {
         case 1:
            Element childElt = (Element)childNode;
            if (this.isTextChild(childElt)) {
               this.laidoutText = null;
               this.removeContextFromChild(this.ctx, childElt);
            }
         case 2:
         default:
            break;
         case 3:
         case 4:
            if (this.isParentDisplayed(childNode)) {
               this.laidoutText = null;
            }
      }

   }

   public void handleDOMSubtreeModifiedEvent(MutationEvent evt) {
      if (this.laidoutText == null) {
         this.computeLaidoutText(this.ctx, this.e, this.getTextNode());
      }

   }

   public void handleDOMCharacterDataModified(MutationEvent evt) {
      Node childNode = (Node)evt.getTarget();
      if (this.isParentDisplayed(childNode)) {
         this.laidoutText = null;
      }

   }

   protected boolean isParentDisplayed(Node childNode) {
      Node parentNode = this.getParentNode(childNode);
      return this.isTextElement((Element)parentNode);
   }

   protected void computeLaidoutText(BridgeContext ctx, Element e, GraphicsNode node) {
      TextNode tn = (TextNode)node;
      this.elemTPI.clear();
      AttributedString as = this.buildAttributedString(ctx, e);
      if (as == null) {
         tn.setAttributedCharacterIterator((AttributedCharacterIterator)null);
      } else {
         this.addGlyphPositionAttributes(as, e, ctx);
         if (ctx.isDynamic()) {
            this.laidoutText = new AttributedString(as.getIterator());
         }

         tn.setAttributedCharacterIterator(as.getIterator());
         TextPaintInfo pi = new TextPaintInfo();
         this.setBaseTextPaintInfo(pi, e, node, ctx);
         this.setDecorationTextPaintInfo(pi, e);
         this.addPaintAttributes(as, e, tn, pi, ctx);
         if (this.usingComplexSVGFont) {
            tn.setAttributedCharacterIterator(as.getIterator());
         }

         if (ctx.isDynamic()) {
            this.checkBBoxChange();
         }

      }
   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      if (alav.getNamespaceURI() == null) {
         String ln = alav.getLocalName();
         if (ln.equals("x") || ln.equals("y") || ln.equals("dx") || ln.equals("dy") || ln.equals("rotate") || ln.equals("textLength") || ln.equals("lengthAdjust")) {
            char c = ln.charAt(0);
            if (c == 'x' || c == 'y') {
               this.getTextNode().setLocation(this.getLocation(this.ctx, this.e));
            }

            this.computeLaidoutText(this.ctx, this.e, this.getTextNode());
            return;
         }
      }

      super.handleAnimatedAttributeChanged(alav);
   }

   public void handleCSSEngineEvent(CSSEngineEvent evt) {
      this.hasNewACI = false;
      int[] properties = evt.getProperties();
      int[] var3 = properties;
      int var4 = properties.length;
      int var5 = 0;

      while(var5 < var4) {
         int property = var3[var5];
         switch (property) {
            case 1:
            case 11:
            case 12:
            case 21:
            case 22:
            case 24:
            case 25:
            case 27:
            case 28:
            case 29:
            case 31:
            case 32:
            case 53:
            case 56:
            case 58:
            case 59:
               if (!this.hasNewACI) {
                  this.hasNewACI = true;
                  this.computeLaidoutText(this.ctx, this.e, this.getTextNode());
               }
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 23:
            case 26:
            case 30:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 54:
            case 55:
            case 57:
            default:
               ++var5;
         }
      }

      this.cssProceedElement = evt.getElement();
      super.handleCSSEngineEvent(evt);
      this.cssProceedElement = null;
   }

   protected void handleCSSPropertyChanged(int property) {
      RenderingHints hints;
      switch (property) {
         case 9:
            hints = this.node.getRenderingHints();
            hints = CSSUtilities.convertColorRendering(this.e, hints);
            if (hints != null) {
               this.node.setRenderingHints(hints);
            }
            break;
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 53:
         case 56:
         default:
            super.handleCSSPropertyChanged(property);
            break;
         case 15:
         case 16:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 54:
            this.rebuildACI();
            break;
         case 55:
            hints = this.node.getRenderingHints();
            hints = CSSUtilities.convertTextRendering(this.e, hints);
            if (hints != null) {
               this.node.setRenderingHints(hints);
            }
            break;
         case 57:
            this.rebuildACI();
            super.handleCSSPropertyChanged(property);
      }

   }

   protected void rebuildACI() {
      if (!this.hasNewACI) {
         TextNode textNode = this.getTextNode();
         if (textNode.getAttributedCharacterIterator() != null) {
            TextPaintInfo pi;
            TextPaintInfo oldPI;
            if (this.cssProceedElement == this.e) {
               pi = new TextPaintInfo();
               this.setBaseTextPaintInfo(pi, this.e, this.node, this.ctx);
               this.setDecorationTextPaintInfo(pi, this.e);
               oldPI = (TextPaintInfo)this.elemTPI.get(this.e);
            } else {
               TextPaintInfo parentPI = this.getParentTextPaintInfo(this.cssProceedElement);
               pi = this.getTextPaintInfo(this.cssProceedElement, textNode, parentPI, this.ctx);
               oldPI = (TextPaintInfo)this.elemTPI.get(this.cssProceedElement);
            }

            if (oldPI != null) {
               textNode.swapTextPaintInfo(pi, oldPI);
               if (this.usingComplexSVGFont) {
                  textNode.setAttributedCharacterIterator(textNode.getAttributedCharacterIterator());
               }

            }
         }
      }
   }

   int getElementStartIndex(Element element) {
      TextPaintInfo tpi = (TextPaintInfo)this.elemTPI.get(element);
      return tpi == null ? -1 : tpi.startChar;
   }

   int getElementEndIndex(Element element) {
      TextPaintInfo tpi = (TextPaintInfo)this.elemTPI.get(element);
      return tpi == null ? -1 : tpi.endChar;
   }

   protected AttributedString buildAttributedString(BridgeContext ctx, Element element) {
      AttributedStringBuffer asb = new AttributedStringBuffer();
      this.fillAttributedStringBuffer(ctx, element, true, (TextPath)null, (Integer)null, (Map)null, asb);
      return asb.toAttributedString();
   }

   protected void fillAttributedStringBuffer(BridgeContext ctx, Element element, boolean top, TextPath textPath, Integer bidiLevel, Map initialAttributes, AttributedStringBuffer asb) {
      if (SVGUtilities.matchUserAgent(element, ctx.getUserAgent()) && CSSUtilities.convertDisplay(element)) {
         String s = XMLSupport.getXMLSpace(element);
         boolean preserve = s.equals("preserve");
         int elementStartChar = asb.length();
         if (top) {
            this.endLimit = 0;
         }

         if (preserve) {
            this.endLimit = asb.length();
         }

         Map map = initialAttributes == null ? new HashMap() : new HashMap(initialAttributes);
         initialAttributes = this.getAttributeMap(ctx, element, textPath, bidiLevel, map);
         Object o = map.get(TextAttribute.BIDI_EMBEDDING);
         Integer subBidiLevel = bidiLevel;
         if (o != null) {
            subBidiLevel = (Integer)o;
         }

         for(Node n = this.getFirstChild(element); n != null; n = this.getNextSibling(n)) {
            boolean prevEndsWithSpace;
            if (preserve) {
               prevEndsWithSpace = false;
            } else if (asb.length() == 0) {
               prevEndsWithSpace = true;
            } else {
               prevEndsWithSpace = asb.getLastChar() == 32;
            }

            switch (n.getNodeType()) {
               case 1:
                  if ("http://www.w3.org/2000/svg".equals(n.getNamespaceURI())) {
                     Element nodeElement = (Element)n;
                     String ln = n.getLocalName();
                     if (!ln.equals("tspan") && !ln.equals("altGlyph")) {
                        int trefStart;
                        if (ln.equals("textPath")) {
                           SVGTextPathElementBridge textPathBridge = (SVGTextPathElementBridge)ctx.getBridge(nodeElement);
                           TextPath newTextPath = textPathBridge.createTextPath(ctx, nodeElement);
                           if (newTextPath != null) {
                              trefStart = asb.count;
                              this.fillAttributedStringBuffer(ctx, nodeElement, false, newTextPath, subBidiLevel, initialAttributes, asb);
                              if (asb.count != trefStart) {
                                 initialAttributes = null;
                              }
                           }
                        } else {
                           int before;
                           if (ln.equals("tref")) {
                              String uriStr = XLinkSupport.getXLinkHref((Element)n);
                              Element ref = ctx.getReferencedElement((Element)n, uriStr);
                              s = TextUtilities.getElementContent(ref);
                              s = this.normalizeString(s, preserve, prevEndsWithSpace);
                              if (s.length() != 0) {
                                 trefStart = asb.length();
                                 Map m = initialAttributes == null ? new HashMap() : new HashMap(initialAttributes);
                                 this.getAttributeMap(ctx, nodeElement, textPath, bidiLevel, m);
                                 asb.append(s, m);
                                 before = asb.length() - 1;
                                 TextPaintInfo tpi = (TextPaintInfo)this.elemTPI.get(nodeElement);
                                 tpi.startChar = trefStart;
                                 tpi.endChar = before;
                                 initialAttributes = null;
                              }
                           } else if (ln.equals("a")) {
                              NodeEventTarget target = (NodeEventTarget)nodeElement;
                              UserAgent ua = ctx.getUserAgent();
                              SVGAElementBridge.CursorHolder ch = new SVGAElementBridge.CursorHolder(CursorManager.DEFAULT_CURSOR);
                              EventListener l = new SVGAElementBridge.AnchorListener(ua, ch);
                              target.addEventListenerNS("http://www.w3.org/2001/xml-events", "click", l, false, (Object)null);
                              ctx.storeEventListenerNS(target, "http://www.w3.org/2001/xml-events", "click", l, false);
                              before = asb.count;
                              this.fillAttributedStringBuffer(ctx, nodeElement, false, textPath, subBidiLevel, initialAttributes, asb);
                              if (asb.count != before) {
                                 initialAttributes = null;
                              }
                           }
                        }
                     } else {
                        int before = asb.count;
                        this.fillAttributedStringBuffer(ctx, nodeElement, false, textPath, subBidiLevel, initialAttributes, asb);
                        if (asb.count != before) {
                           initialAttributes = null;
                        }
                     }
                  }
               case 2:
               default:
                  break;
               case 3:
               case 4:
                  s = n.getNodeValue();
                  s = this.normalizeString(s, preserve, prevEndsWithSpace);
                  if (s.length() != 0) {
                     asb.append(s, map);
                     if (preserve) {
                        this.endLimit = asb.length();
                     }

                     initialAttributes = null;
                  }
            }
         }

         if (top) {
            boolean strippedSome;
            for(strippedSome = false; this.endLimit < asb.length() && asb.getLastChar() == 32; strippedSome = true) {
               asb.stripLast();
            }

            if (strippedSome) {
               Iterator var26 = this.elemTPI.values().iterator();

               while(var26.hasNext()) {
                  Object o1 = var26.next();
                  TextPaintInfo tpi = (TextPaintInfo)o1;
                  if (tpi.endChar >= asb.length()) {
                     tpi.endChar = asb.length() - 1;
                     if (tpi.startChar > tpi.endChar) {
                        tpi.startChar = tpi.endChar;
                     }
                  }
               }
            }
         }

         int elementEndChar = asb.length() - 1;
         TextPaintInfo tpi = (TextPaintInfo)this.elemTPI.get(element);
         tpi.startChar = elementStartChar;
         tpi.endChar = elementEndChar;
      }
   }

   protected String normalizeString(String s, boolean preserve, boolean stripfirst) {
      StringBuffer sb = new StringBuffer(s.length());
      int idx;
      if (preserve) {
         for(idx = 0; idx < s.length(); ++idx) {
            char c = s.charAt(idx);
            switch (c) {
               case '\t':
               case '\n':
               case '\r':
                  sb.append(' ');
                  break;
               case '\u000b':
               case '\f':
               default:
                  sb.append(c);
            }
         }

         return sb.toString();
      } else {
         idx = 0;
         if (stripfirst) {
            label48:
            while(idx < s.length()) {
               switch (s.charAt(idx)) {
                  case '\t':
                  case '\n':
                  case '\r':
                  case ' ':
                     ++idx;
                     break;
                  default:
                     break label48;
               }
            }
         }

         boolean space = false;

         for(int i = idx; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
               case '\t':
               case ' ':
                  if (!space) {
                     sb.append(' ');
                     space = true;
                  }
               case '\n':
               case '\r':
                  break;
               default:
                  sb.append(c);
                  space = false;
            }
         }

         return sb.toString();
      }
   }

   protected boolean nodeAncestorOf(Node node1, Node node2) {
      if (node2 != null && node1 != null) {
         Node parent;
         for(parent = this.getParentNode(node2); parent != null && parent != node1; parent = this.getParentNode(parent)) {
         }

         return parent == node1;
      } else {
         return false;
      }
   }

   protected void addGlyphPositionAttributes(AttributedString as, Element element, BridgeContext ctx) {
      if (SVGUtilities.matchUserAgent(element, ctx.getUserAgent()) && CSSUtilities.convertDisplay(element)) {
         if (element.getLocalName().equals("textPath")) {
            this.addChildGlyphPositionAttributes(as, element, ctx);
         } else {
            int firstChar = this.getElementStartIndex(element);
            if (firstChar != -1) {
               int lastChar = this.getElementEndIndex(element);
               if (!(element instanceof SVGTextPositioningElement)) {
                  this.addChildGlyphPositionAttributes(as, element, ctx);
               } else {
                  SVGTextPositioningElement te = (SVGTextPositioningElement)element;

                  try {
                     SVGOMAnimatedLengthList _x = (SVGOMAnimatedLengthList)te.getX();
                     _x.check();
                     SVGOMAnimatedLengthList _y = (SVGOMAnimatedLengthList)te.getY();
                     _y.check();
                     SVGOMAnimatedLengthList _dx = (SVGOMAnimatedLengthList)te.getDx();
                     _dx.check();
                     SVGOMAnimatedLengthList _dy = (SVGOMAnimatedLengthList)te.getDy();
                     _dy.check();
                     SVGOMAnimatedNumberList _rotate = (SVGOMAnimatedNumberList)te.getRotate();
                     _rotate.check();
                     SVGLengthList xs = _x.getAnimVal();
                     SVGLengthList ys = _y.getAnimVal();
                     SVGLengthList dxs = _dx.getAnimVal();
                     SVGLengthList dys = _dy.getAnimVal();
                     SVGNumberList rs = _rotate.getAnimVal();
                     int len = xs.getNumberOfItems();

                     int i;
                     for(i = 0; i < len && firstChar + i <= lastChar; ++i) {
                        as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.X, xs.getItem(i).getValue(), firstChar + i, firstChar + i + 1);
                     }

                     len = ys.getNumberOfItems();

                     for(i = 0; i < len && firstChar + i <= lastChar; ++i) {
                        as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.Y, ys.getItem(i).getValue(), firstChar + i, firstChar + i + 1);
                     }

                     len = dxs.getNumberOfItems();

                     for(i = 0; i < len && firstChar + i <= lastChar; ++i) {
                        as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.DX, dxs.getItem(i).getValue(), firstChar + i, firstChar + i + 1);
                     }

                     len = dys.getNumberOfItems();

                     for(i = 0; i < len && firstChar + i <= lastChar; ++i) {
                        as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.DY, dys.getItem(i).getValue(), firstChar + i, firstChar + i + 1);
                     }

                     len = rs.getNumberOfItems();
                     if (len == 1) {
                        Float rad = (float)Math.toRadians((double)rs.getItem(0).getValue());
                        as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.ROTATION, rad, firstChar, lastChar + 1);
                     } else if (len > 1) {
                        for(i = 0; i < len && firstChar + i <= lastChar; ++i) {
                           Float rad = (float)Math.toRadians((double)rs.getItem(i).getValue());
                           as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.ROTATION, rad, firstChar + i, firstChar + i + 1);
                        }
                     }

                     this.addChildGlyphPositionAttributes(as, element, ctx);
                  } catch (LiveAttributeException var20) {
                     throw new BridgeException(ctx, var20);
                  }
               }
            }
         }
      }
   }

   protected void addChildGlyphPositionAttributes(AttributedString as, Element element, BridgeContext ctx) {
      for(Node child = this.getFirstChild(element); child != null; child = this.getNextSibling(child)) {
         if (child.getNodeType() == 1) {
            Element childElement = (Element)child;
            if (this.isTextChild(childElement)) {
               this.addGlyphPositionAttributes(as, childElement, ctx);
            }
         }
      }

   }

   protected void addPaintAttributes(AttributedString as, Element element, TextNode node, TextPaintInfo pi, BridgeContext ctx) {
      if (SVGUtilities.matchUserAgent(element, ctx.getUserAgent()) && CSSUtilities.convertDisplay(element)) {
         Object o = this.elemTPI.get(element);
         if (o != null) {
            node.swapTextPaintInfo(pi, (TextPaintInfo)o);
         }

         this.addChildPaintAttributes(as, element, node, pi, ctx);
      }
   }

   protected void addChildPaintAttributes(AttributedString as, Element element, TextNode node, TextPaintInfo parentPI, BridgeContext ctx) {
      for(Node child = this.getFirstChild(element); child != null; child = this.getNextSibling(child)) {
         if (child.getNodeType() == 1) {
            Element childElement = (Element)child;
            if (this.isTextChild(childElement)) {
               TextPaintInfo pi = this.getTextPaintInfo(childElement, node, parentPI, ctx);
               this.addPaintAttributes(as, childElement, node, pi, ctx);
            }
         }
      }

   }

   protected List getFontList(BridgeContext ctx, Element element, Map result) {
      result.put(TEXT_COMPOUND_ID, new SoftReference(element));
      Float fsFloat = TextUtilities.convertFontSize(element);
      float fontSize = fsFloat;
      result.put(TextAttribute.SIZE, fsFloat);
      result.put(TextAttribute.WIDTH, TextUtilities.convertFontStretch(element));
      result.put(TextAttribute.POSTURE, TextUtilities.convertFontStyle(element));
      result.put(TextAttribute.WEIGHT, TextUtilities.convertFontWeight(element));
      Value v = CSSUtilities.getComputedStyle(element, 27);
      String fontWeightString = v.getCssText();
      String fontStyleString = CSSUtilities.getComputedStyle(element, 25).getStringValue();
      result.put(TEXT_COMPOUND_DELIMITER, element);
      Value val = CSSUtilities.getComputedStyle(element, 21);
      List fontFamilyList = new ArrayList();
      List fontList = new ArrayList();
      int len = val.getLength();

      for(int i = 0; i < len; ++i) {
         Value it = val.item(i);
         String fontFamilyName = it.getStringValue();
         GVTFontFamily fontFamily = SVGFontUtilities.getFontFamily(element, ctx, fontFamilyName, fontWeightString, fontStyleString);
         if (fontFamily != null && fontFamily instanceof UnresolvedFontFamily) {
            fontFamily = ctx.getFontFamilyResolver().resolve(fontFamily.getFamilyName());
         }

         if (fontFamily != null) {
            fontFamilyList.add(fontFamily);
            if (fontFamily.isComplex()) {
               this.usingComplexSVGFont = true;
            }

            GVTFont ft = fontFamily.deriveFont(fontSize, result);
            fontList.add(ft);
         }
      }

      result.put(GVT_FONT_FAMILIES, fontFamilyList);
      if (!ctx.isDynamic()) {
         result.remove(TEXT_COMPOUND_DELIMITER);
      }

      return fontList;
   }

   protected Map getAttributeMap(BridgeContext ctx, Element element, TextPath textPath, Integer bidiLevel, Map result) {
      SVGTextContentElement tce = null;
      if (element instanceof SVGTextContentElement) {
         tce = (SVGTextContentElement)element;
      }

      Map inheritMap = null;
      if ("http://www.w3.org/2000/svg".equals(element.getNamespaceURI()) && element.getLocalName().equals("altGlyph")) {
         result.put(ALT_GLYPH_HANDLER, new SVGAltGlyphHandler(ctx, element));
      }

      TextPaintInfo pi = new TextPaintInfo();
      pi.visible = true;
      pi.fillPaint = Color.black;
      result.put(PAINT_INFO, pi);
      this.elemTPI.put(element, pi);
      if (textPath != null) {
         result.put(TEXTPATH, textPath);
      }

      TextNode.Anchor a = TextUtilities.convertTextAnchor(element);
      result.put(ANCHOR_TYPE, a);
      List fontList = this.getFontList(ctx, element, result);
      result.put(GVT_FONTS, fontList);
      Object bs = TextUtilities.convertBaselineShift(element);
      if (bs != null) {
         result.put(BASELINE_SHIFT, bs);
      }

      Value val = CSSUtilities.getComputedStyle(element, 56);
      String s = val.getStringValue();
      if (s.charAt(0) == 'n') {
         if (bidiLevel != null) {
            result.put(TextAttribute.BIDI_EMBEDDING, bidiLevel);
         }
      } else {
         val = CSSUtilities.getComputedStyle(element, 11);
         String rs = val.getStringValue();
         int cbidi = 0;
         if (bidiLevel != null) {
            cbidi = bidiLevel;
         }

         if (cbidi < 0) {
            cbidi = -cbidi;
         }

         switch (rs.charAt(0)) {
            case 'l':
               result.put(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_LTR);
               if ((cbidi & 1) == 1) {
                  ++cbidi;
               } else {
                  cbidi += 2;
               }
               break;
            case 'r':
               result.put(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
               if ((cbidi & 1) == 1) {
                  cbidi += 2;
               } else {
                  ++cbidi;
               }
         }

         switch (s.charAt(0)) {
            case 'b':
               cbidi = -cbidi;
            default:
               result.put(TextAttribute.BIDI_EMBEDDING, cbidi);
         }
      }

      val = CSSUtilities.getComputedStyle(element, 59);
      s = val.getStringValue();
      switch (s.charAt(0)) {
         case 'l':
            result.put(GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE, GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_LTR);
            break;
         case 'r':
            result.put(GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE, GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_RTL);
            break;
         case 't':
            result.put(GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE, GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_TTB);
      }

      val = CSSUtilities.getComputedStyle(element, 29);
      int primitiveType = val.getPrimitiveType();
      switch (primitiveType) {
         case 11:
            result.put(GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION, GVTAttributedCharacterIterator.TextAttribute.ORIENTATION_ANGLE);
            result.put(GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION_ANGLE, val.getFloatValue());
            break;
         case 12:
            result.put(GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION, GVTAttributedCharacterIterator.TextAttribute.ORIENTATION_ANGLE);
            result.put(GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION_ANGLE, (float)Math.toDegrees((double)val.getFloatValue()));
            break;
         case 13:
            result.put(GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION, GVTAttributedCharacterIterator.TextAttribute.ORIENTATION_ANGLE);
            result.put(GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION_ANGLE, val.getFloatValue() * 9.0F / 5.0F);
            break;
         case 21:
            result.put(GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION, GVTAttributedCharacterIterator.TextAttribute.ORIENTATION_AUTO);
            break;
         default:
            throw new IllegalStateException("unexpected primitiveType (V):" + primitiveType);
      }

      val = CSSUtilities.getComputedStyle(element, 28);
      primitiveType = val.getPrimitiveType();
      switch (primitiveType) {
         case 11:
            result.put(GVTAttributedCharacterIterator.TextAttribute.HORIZONTAL_ORIENTATION_ANGLE, val.getFloatValue());
            break;
         case 12:
            result.put(GVTAttributedCharacterIterator.TextAttribute.HORIZONTAL_ORIENTATION_ANGLE, (float)Math.toDegrees((double)val.getFloatValue()));
            break;
         case 13:
            result.put(GVTAttributedCharacterIterator.TextAttribute.HORIZONTAL_ORIENTATION_ANGLE, val.getFloatValue() * 9.0F / 5.0F);
            break;
         default:
            throw new IllegalStateException("unexpected primitiveType (H):" + primitiveType);
      }

      Float sp = TextUtilities.convertLetterSpacing(element);
      if (sp != null) {
         result.put(GVTAttributedCharacterIterator.TextAttribute.LETTER_SPACING, sp);
         result.put(GVTAttributedCharacterIterator.TextAttribute.CUSTOM_SPACING, Boolean.TRUE);
      }

      sp = TextUtilities.convertWordSpacing(element);
      if (sp != null) {
         result.put(GVTAttributedCharacterIterator.TextAttribute.WORD_SPACING, sp);
         result.put(GVTAttributedCharacterIterator.TextAttribute.CUSTOM_SPACING, Boolean.TRUE);
      }

      sp = TextUtilities.convertKerning(element);
      if (sp != null) {
         result.put(GVTAttributedCharacterIterator.TextAttribute.KERNING, sp);
         result.put(GVTAttributedCharacterIterator.TextAttribute.CUSTOM_SPACING, Boolean.TRUE);
      }

      if (tce == null) {
         return inheritMap;
      } else {
         try {
            AbstractSVGAnimatedLength textLength = (AbstractSVGAnimatedLength)tce.getTextLength();
            if (textLength.isSpecified()) {
               if (inheritMap == null) {
                  inheritMap = new HashMap();
               }

               Object value = textLength.getCheckedValue();
               result.put(GVTAttributedCharacterIterator.TextAttribute.BBOX_WIDTH, value);
               inheritMap.put(GVTAttributedCharacterIterator.TextAttribute.BBOX_WIDTH, value);
               SVGOMAnimatedEnumeration _lengthAdjust = (SVGOMAnimatedEnumeration)tce.getLengthAdjust();
               if (_lengthAdjust.getCheckedVal() == 2) {
                  result.put(GVTAttributedCharacterIterator.TextAttribute.LENGTH_ADJUST, GVTAttributedCharacterIterator.TextAttribute.ADJUST_ALL);
                  inheritMap.put(GVTAttributedCharacterIterator.TextAttribute.LENGTH_ADJUST, GVTAttributedCharacterIterator.TextAttribute.ADJUST_ALL);
               } else {
                  result.put(GVTAttributedCharacterIterator.TextAttribute.LENGTH_ADJUST, GVTAttributedCharacterIterator.TextAttribute.ADJUST_SPACING);
                  inheritMap.put(GVTAttributedCharacterIterator.TextAttribute.LENGTH_ADJUST, GVTAttributedCharacterIterator.TextAttribute.ADJUST_SPACING);
                  result.put(GVTAttributedCharacterIterator.TextAttribute.CUSTOM_SPACING, Boolean.TRUE);
                  inheritMap.put(GVTAttributedCharacterIterator.TextAttribute.CUSTOM_SPACING, Boolean.TRUE);
               }
            }

            return inheritMap;
         } catch (LiveAttributeException var19) {
            throw new BridgeException(ctx, var19);
         }
      }
   }

   protected TextPaintInfo getParentTextPaintInfo(Element child) {
      for(Node parent = this.getParentNode(child); parent != null; parent = this.getParentNode(parent)) {
         TextPaintInfo tpi = (TextPaintInfo)this.elemTPI.get(parent);
         if (tpi != null) {
            return tpi;
         }
      }

      return null;
   }

   protected TextPaintInfo getTextPaintInfo(Element element, GraphicsNode node, TextPaintInfo parentTPI, BridgeContext ctx) {
      CSSUtilities.getComputedStyle(element, 54);
      TextPaintInfo pi = new TextPaintInfo(parentTPI);
      StyleMap sm = ((CSSStylableElement)element).getComputedStyleMap((String)null);
      if (sm.isNullCascaded(54) && sm.isNullCascaded(15) && sm.isNullCascaded(45) && sm.isNullCascaded(52) && sm.isNullCascaded(38)) {
         return pi;
      } else {
         this.setBaseTextPaintInfo(pi, element, node, ctx);
         if (!sm.isNullCascaded(54)) {
            this.setDecorationTextPaintInfo(pi, element);
         }

         return pi;
      }
   }

   public void setBaseTextPaintInfo(TextPaintInfo pi, Element element, GraphicsNode node, BridgeContext ctx) {
      if (!element.getLocalName().equals("text")) {
         pi.composite = CSSUtilities.convertOpacity(element);
      } else {
         pi.composite = AlphaComposite.SrcOver;
      }

      pi.visible = CSSUtilities.convertVisibility(element);
      pi.fillPaint = PaintServer.convertFillPaint(element, node, ctx);
      pi.strokePaint = PaintServer.convertStrokePaint(element, node, ctx);
      pi.strokeStroke = PaintServer.convertStroke(element);
   }

   public void setDecorationTextPaintInfo(TextPaintInfo pi, Element element) {
      Value val = CSSUtilities.getComputedStyle(element, 54);
      switch (val.getCssValueType()) {
         case 2:
            ListValue lst = (ListValue)val;
            int len = lst.getLength();

            for(int i = 0; i < len; ++i) {
               Value v = lst.item(i);
               String s = v.getStringValue();
               switch (s.charAt(0)) {
                  case 'l':
                     if (pi.fillPaint != null) {
                        pi.strikethroughPaint = pi.fillPaint;
                     }

                     if (pi.strokePaint != null) {
                        pi.strikethroughStrokePaint = pi.strokePaint;
                     }

                     if (pi.strokeStroke != null) {
                        pi.strikethroughStroke = pi.strokeStroke;
                     }
                     break;
                  case 'o':
                     if (pi.fillPaint != null) {
                        pi.overlinePaint = pi.fillPaint;
                     }

                     if (pi.strokePaint != null) {
                        pi.overlineStrokePaint = pi.strokePaint;
                     }

                     if (pi.strokeStroke != null) {
                        pi.overlineStroke = pi.strokeStroke;
                     }
                     break;
                  case 'u':
                     if (pi.fillPaint != null) {
                        pi.underlinePaint = pi.fillPaint;
                     }

                     if (pi.strokePaint != null) {
                        pi.underlineStrokePaint = pi.strokePaint;
                     }

                     if (pi.strokeStroke != null) {
                        pi.underlineStroke = pi.strokeStroke;
                     }
               }
            }

            return;
         default:
            pi.underlinePaint = null;
            pi.underlineStrokePaint = null;
            pi.underlineStroke = null;
            pi.overlinePaint = null;
            pi.overlineStrokePaint = null;
            pi.overlineStroke = null;
            pi.strikethroughPaint = null;
            pi.strikethroughStrokePaint = null;
            pi.strikethroughStroke = null;
      }
   }

   public int getNumberOfChars() {
      return this.getNumberOfChars(this.e);
   }

   public Rectangle2D getExtentOfChar(int charnum) {
      return this.getExtentOfChar(this.e, charnum);
   }

   public Point2D getStartPositionOfChar(int charnum) {
      return this.getStartPositionOfChar(this.e, charnum);
   }

   public Point2D getEndPositionOfChar(int charnum) {
      return this.getEndPositionOfChar(this.e, charnum);
   }

   public void selectSubString(int charnum, int nchars) {
      this.selectSubString(this.e, charnum, nchars);
   }

   public float getRotationOfChar(int charnum) {
      return this.getRotationOfChar(this.e, charnum);
   }

   public float getComputedTextLength() {
      return this.getComputedTextLength(this.e);
   }

   public float getSubStringLength(int charnum, int nchars) {
      return this.getSubStringLength(this.e, charnum, nchars);
   }

   public int getCharNumAtPosition(float x, float y) {
      return this.getCharNumAtPosition(this.e, x, y);
   }

   protected int getNumberOfChars(Element element) {
      AttributedCharacterIterator aci = this.getTextNode().getAttributedCharacterIterator();
      if (aci == null) {
         return 0;
      } else {
         int firstChar = this.getElementStartIndex(element);
         if (firstChar == -1) {
            return 0;
         } else {
            int lastChar = this.getElementEndIndex(element);
            return lastChar - firstChar + 1;
         }
      }
   }

   protected Rectangle2D getExtentOfChar(Element element, int charnum) {
      TextNode textNode = this.getTextNode();
      AttributedCharacterIterator aci = textNode.getAttributedCharacterIterator();
      if (aci == null) {
         return null;
      } else {
         int firstChar = this.getElementStartIndex(element);
         if (firstChar == -1) {
            return null;
         } else {
            List list = this.getTextRuns(textNode);
            CharacterInformation info = this.getCharacterInformation(list, firstChar, charnum, aci);
            if (info == null) {
               return null;
            } else {
               GVTGlyphVector it = info.layout.getGlyphVector();
               Shape b = null;
               if (info.glyphIndexStart == info.glyphIndexEnd) {
                  if (it.isGlyphVisible(info.glyphIndexStart)) {
                     b = it.getGlyphCellBounds(info.glyphIndexStart);
                  }
               } else {
                  GeneralPath path = null;

                  for(int k = info.glyphIndexStart; k <= info.glyphIndexEnd; ++k) {
                     if (it.isGlyphVisible(k)) {
                        Rectangle2D gb = it.getGlyphCellBounds(k);
                        if (path == null) {
                           path = new GeneralPath(gb);
                        } else {
                           path.append(gb, false);
                        }
                     }
                  }

                  b = path;
               }

               return b == null ? null : ((Shape)b).getBounds2D();
            }
         }
      }
   }

   protected Point2D getStartPositionOfChar(Element element, int charnum) {
      TextNode textNode = this.getTextNode();
      AttributedCharacterIterator aci = textNode.getAttributedCharacterIterator();
      if (aci == null) {
         return null;
      } else {
         int firstChar = this.getElementStartIndex(element);
         if (firstChar == -1) {
            return null;
         } else {
            List list = this.getTextRuns(textNode);
            CharacterInformation info = this.getCharacterInformation(list, firstChar, charnum, aci);
            return info == null ? null : this.getStartPoint(info);
         }
      }
   }

   protected Point2D getStartPoint(CharacterInformation info) {
      GVTGlyphVector it = info.layout.getGlyphVector();
      if (!it.isGlyphVisible(info.glyphIndexStart)) {
         return null;
      } else {
         Point2D b = it.getGlyphPosition(info.glyphIndexStart);
         AffineTransform glyphTransform = it.getGlyphTransform(info.glyphIndexStart);
         Point2D.Float result = new Point2D.Float(0.0F, 0.0F);
         if (glyphTransform != null) {
            glyphTransform.transform(result, result);
         }

         result.x = (float)((double)result.x + b.getX());
         result.y = (float)((double)result.y + b.getY());
         return result;
      }
   }

   protected Point2D getEndPositionOfChar(Element element, int charnum) {
      TextNode textNode = this.getTextNode();
      AttributedCharacterIterator aci = textNode.getAttributedCharacterIterator();
      if (aci == null) {
         return null;
      } else {
         int firstChar = this.getElementStartIndex(element);
         if (firstChar == -1) {
            return null;
         } else {
            List list = this.getTextRuns(textNode);
            CharacterInformation info = this.getCharacterInformation(list, firstChar, charnum, aci);
            return info == null ? null : this.getEndPoint(info);
         }
      }
   }

   protected Point2D getEndPoint(CharacterInformation info) {
      GVTGlyphVector it = info.layout.getGlyphVector();
      if (!it.isGlyphVisible(info.glyphIndexEnd)) {
         return null;
      } else {
         Point2D b = it.getGlyphPosition(info.glyphIndexEnd);
         AffineTransform glyphTransform = it.getGlyphTransform(info.glyphIndexEnd);
         GVTGlyphMetrics metrics = it.getGlyphMetrics(info.glyphIndexEnd);
         Point2D.Float result = new Point2D.Float(metrics.getHorizontalAdvance(), 0.0F);
         if (glyphTransform != null) {
            glyphTransform.transform(result, result);
         }

         result.x = (float)((double)result.x + b.getX());
         result.y = (float)((double)result.y + b.getY());
         return result;
      }
   }

   protected float getRotationOfChar(Element element, int charnum) {
      TextNode textNode = this.getTextNode();
      AttributedCharacterIterator aci = textNode.getAttributedCharacterIterator();
      if (aci == null) {
         return 0.0F;
      } else {
         int firstChar = this.getElementStartIndex(element);
         if (firstChar == -1) {
            return 0.0F;
         } else {
            List list = this.getTextRuns(textNode);
            CharacterInformation info = this.getCharacterInformation(list, firstChar, charnum, aci);
            double angle = 0.0;
            int nbGlyphs = 0;
            if (info != null) {
               GVTGlyphVector it = info.layout.getGlyphVector();

               for(int k = info.glyphIndexStart; k <= info.glyphIndexEnd; ++k) {
                  if (it.isGlyphVisible(k)) {
                     ++nbGlyphs;
                     AffineTransform glyphTransform = it.getGlyphTransform(k);
                     if (glyphTransform != null) {
                        double glyphAngle = 0.0;
                        double cosTheta = glyphTransform.getScaleX();
                        double sinTheta = glyphTransform.getShearX();
                        if (cosTheta == 0.0) {
                           if (sinTheta > 0.0) {
                              glyphAngle = Math.PI;
                           } else {
                              glyphAngle = -3.141592653589793;
                           }
                        } else {
                           glyphAngle = Math.atan(sinTheta / cosTheta);
                           if (cosTheta < 0.0) {
                              glyphAngle += Math.PI;
                           }
                        }

                        glyphAngle = Math.toDegrees(-glyphAngle) % 360.0;
                        angle += glyphAngle - info.getComputedOrientationAngle();
                     }
                  }
               }
            }

            return nbGlyphs == 0 ? 0.0F : (float)(angle / (double)nbGlyphs);
         }
      }
   }

   protected float getComputedTextLength(Element e) {
      return this.getSubStringLength(e, 0, this.getNumberOfChars(e));
   }

   protected float getSubStringLength(Element element, int charnum, int nchars) {
      if (nchars == 0) {
         return 0.0F;
      } else {
         float length = 0.0F;
         TextNode textNode = this.getTextNode();
         AttributedCharacterIterator aci = textNode.getAttributedCharacterIterator();
         if (aci == null) {
            return -1.0F;
         } else {
            int firstChar = this.getElementStartIndex(element);
            if (firstChar == -1) {
               return -1.0F;
            } else {
               List list = this.getTextRuns(textNode);
               CharacterInformation currentInfo = this.getCharacterInformation(list, firstChar, charnum, aci);
               CharacterInformation lastCharacterInRunInfo = null;
               int chIndex = currentInfo.characterIndex + 1;
               GVTGlyphVector vector = currentInfo.layout.getGlyphVector();
               float[] advs = currentInfo.layout.getGlyphAdvances();
               boolean[] glyphTrack = new boolean[advs.length];

               int gi;
               for(gi = charnum + 1; gi < charnum + nchars; ++gi) {
                  if (!currentInfo.layout.isOnATextPath()) {
                     if (currentInfo.layout.hasCharacterIndex(chIndex)) {
                        ++chIndex;
                     } else {
                        lastCharacterInRunInfo = this.getCharacterInformation(list, firstChar, gi - 1, aci);
                        length += this.distanceFirstLastCharacterInRun(currentInfo, lastCharacterInRunInfo);
                        currentInfo = this.getCharacterInformation(list, firstChar, gi, aci);
                        chIndex = currentInfo.characterIndex + 1;
                        vector = currentInfo.layout.getGlyphVector();
                        advs = currentInfo.layout.getGlyphAdvances();
                        glyphTrack = new boolean[advs.length];
                        lastCharacterInRunInfo = null;
                     }
                  } else {
                     for(int gi = currentInfo.glyphIndexStart; gi <= currentInfo.glyphIndexEnd; ++gi) {
                        if (vector.isGlyphVisible(gi) && !glyphTrack[gi]) {
                           length += advs[gi + 1] - advs[gi];
                        }

                        glyphTrack[gi] = true;
                     }

                     CharacterInformation newInfo = this.getCharacterInformation(list, firstChar, gi, aci);
                     if (newInfo.layout != currentInfo.layout) {
                        vector = newInfo.layout.getGlyphVector();
                        advs = newInfo.layout.getGlyphAdvances();
                        glyphTrack = new boolean[advs.length];
                        chIndex = currentInfo.characterIndex + 1;
                     }

                     currentInfo = newInfo;
                  }
               }

               if (currentInfo.layout.isOnATextPath()) {
                  for(gi = currentInfo.glyphIndexStart; gi <= currentInfo.glyphIndexEnd; ++gi) {
                     if (vector.isGlyphVisible(gi) && !glyphTrack[gi]) {
                        length += advs[gi + 1] - advs[gi];
                     }

                     glyphTrack[gi] = true;
                  }
               } else {
                  if (lastCharacterInRunInfo == null) {
                     lastCharacterInRunInfo = this.getCharacterInformation(list, firstChar, charnum + nchars - 1, aci);
                  }

                  length += this.distanceFirstLastCharacterInRun(currentInfo, lastCharacterInRunInfo);
               }

               return length;
            }
         }
      }
   }

   protected float distanceFirstLastCharacterInRun(CharacterInformation first, CharacterInformation last) {
      float[] advs = first.layout.getGlyphAdvances();
      int firstStart = first.glyphIndexStart;
      int firstEnd = first.glyphIndexEnd;
      int lastStart = last.glyphIndexStart;
      int lastEnd = last.glyphIndexEnd;
      int start = firstStart < lastStart ? firstStart : lastStart;
      int end = firstEnd < lastEnd ? lastEnd : firstEnd;
      return advs[end + 1] - advs[start];
   }

   protected float distanceBetweenRun(CharacterInformation last, CharacterInformation first) {
      CharacterInformation info = new CharacterInformation();
      info.layout = last.layout;
      info.glyphIndexEnd = last.layout.getGlyphCount() - 1;
      Point2D startPoint = this.getEndPoint(info);
      info.layout = first.layout;
      info.glyphIndexStart = 0;
      Point2D endPoint = this.getStartPoint(info);
      float distance;
      if (first.isVertical()) {
         distance = (float)(endPoint.getY() - startPoint.getY());
      } else {
         distance = (float)(endPoint.getX() - startPoint.getX());
      }

      return distance;
   }

   protected void selectSubString(Element element, int charnum, int nchars) {
      TextNode textNode = this.getTextNode();
      AttributedCharacterIterator aci = textNode.getAttributedCharacterIterator();
      if (aci != null) {
         int firstChar = this.getElementStartIndex(element);
         if (firstChar != -1) {
            List list = this.getTextRuns(textNode);
            int lastChar = this.getElementEndIndex(element);
            CharacterInformation firstInfo = this.getCharacterInformation(list, firstChar, charnum, aci);
            CharacterInformation lastInfo = this.getCharacterInformation(list, firstChar, charnum + nchars - 1, aci);
            Mark firstMark = textNode.getMarkerForChar(firstInfo.characterIndex, true);
            Mark lastMark;
            if (lastInfo != null && lastInfo.characterIndex <= lastChar) {
               lastMark = textNode.getMarkerForChar(lastInfo.characterIndex, false);
            } else {
               lastMark = textNode.getMarkerForChar(lastChar, false);
            }

            this.ctx.getUserAgent().setTextSelection(firstMark, lastMark);
         }
      }
   }

   protected int getCharNumAtPosition(Element e, float x, float y) {
      TextNode textNode = this.getTextNode();
      AttributedCharacterIterator aci = textNode.getAttributedCharacterIterator();
      if (aci == null) {
         return -1;
      } else {
         List list = this.getTextRuns(textNode);
         TextHit hit = null;

         int first;
         for(first = list.size() - 1; first >= 0 && hit == null; --first) {
            StrokingTextPainter.TextRun textRun = (StrokingTextPainter.TextRun)list.get(first);
            hit = textRun.getLayout().hitTestChar(x, y);
         }

         if (hit == null) {
            return -1;
         } else {
            first = this.getElementStartIndex(e);
            int last = this.getElementEndIndex(e);
            int hitIndex = hit.getCharIndex();
            return hitIndex >= first && hitIndex <= last ? hitIndex - first : -1;
         }
      }
   }

   protected List getTextRuns(TextNode node) {
      if (node.getTextRuns() == null) {
         node.getPrimitiveBounds();
      }

      return node.getTextRuns();
   }

   protected CharacterInformation getCharacterInformation(List list, int startIndex, int charnum, AttributedCharacterIterator aci) {
      CharacterInformation info = new CharacterInformation();
      info.characterIndex = startIndex + charnum;
      Iterator var6 = list.iterator();

      StrokingTextPainter.TextRun run;
      do {
         if (!var6.hasNext()) {
            return null;
         }

         Object aList = var6.next();
         run = (StrokingTextPainter.TextRun)aList;
      } while(!run.getLayout().hasCharacterIndex(info.characterIndex));

      info.layout = run.getLayout();
      aci.setIndex(info.characterIndex);
      if (aci.getAttribute(ALT_GLYPH_HANDLER) != null) {
         info.glyphIndexStart = 0;
         info.glyphIndexEnd = info.layout.getGlyphCount() - 1;
      } else {
         info.glyphIndexStart = info.layout.getGlyphIndex(info.characterIndex);
         if (info.glyphIndexStart == -1) {
            info.glyphIndexStart = 0;
            info.glyphIndexEnd = info.layout.getGlyphCount() - 1;
         } else {
            info.glyphIndexEnd = info.glyphIndexStart;
         }
      }

      return info;
   }

   public Set getTextIntersectionSet(AffineTransform at, Rectangle2D rect) {
      Set elems = new HashSet();
      TextNode tn = this.getTextNode();
      List list = tn.getTextRuns();
      if (list == null) {
         return elems;
      } else {
         Iterator var6 = list.iterator();

         while(true) {
            while(true) {
               TextSpanLayout layout;
               Element elem;
               Rectangle2D glBounds;
               do {
                  do {
                     do {
                        do {
                           if (!var6.hasNext()) {
                              return elems;
                           }

                           Object aList = var6.next();
                           StrokingTextPainter.TextRun run = (StrokingTextPainter.TextRun)aList;
                           layout = run.getLayout();
                           AttributedCharacterIterator aci = run.getACI();
                           aci.first();
                           SoftReference sr = (SoftReference)aci.getAttribute(TEXT_COMPOUND_ID);
                           elem = (Element)sr.get();
                        } while(elem == null);
                     } while(elems.contains(elem));
                  } while(!isTextSensitive(elem));

                  glBounds = layout.getBounds2D();
                  if (glBounds == null) {
                     break;
                  }

                  glBounds = at.createTransformedShape(glBounds).getBounds2D();
               } while(!rect.intersects(glBounds));

               GVTGlyphVector gv = layout.getGlyphVector();

               for(int g = 0; g < gv.getNumGlyphs(); ++g) {
                  Shape gBounds = gv.getGlyphLogicalBounds(g);
                  if (gBounds != null) {
                     Shape gBounds = at.createTransformedShape(gBounds).getBounds2D();
                     if (gBounds.intersects(rect)) {
                        elems.add(elem);
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   public Set getTextEnclosureSet(AffineTransform at, Rectangle2D rect) {
      TextNode tn = this.getTextNode();
      Set elems = new HashSet();
      List list = tn.getTextRuns();
      if (list == null) {
         return elems;
      } else {
         Set reject = new HashSet();
         Iterator var7 = list.iterator();

         while(var7.hasNext()) {
            Object aList = var7.next();
            StrokingTextPainter.TextRun run = (StrokingTextPainter.TextRun)aList;
            TextSpanLayout layout = run.getLayout();
            AttributedCharacterIterator aci = run.getACI();
            aci.first();
            SoftReference sr = (SoftReference)aci.getAttribute(TEXT_COMPOUND_ID);
            Element elem = (Element)sr.get();
            if (elem != null && !reject.contains(elem)) {
               if (!isTextSensitive(elem)) {
                  reject.add(elem);
               } else {
                  Rectangle2D glBounds = layout.getBounds2D();
                  if (glBounds != null) {
                     glBounds = at.createTransformedShape(glBounds).getBounds2D();
                     if (rect.contains(glBounds)) {
                        elems.add(elem);
                     } else {
                        reject.add(elem);
                        elems.remove(elem);
                     }
                  }
               }
            }
         }

         return elems;
      }
   }

   public static boolean getTextIntersection(BridgeContext ctx, Element elem, AffineTransform ati, Rectangle2D rect, boolean checkSensitivity) {
      SVGContext svgCtx = null;
      if (elem instanceof SVGOMElement) {
         svgCtx = ((SVGOMElement)elem).getSVGContext();
      }

      if (svgCtx == null) {
         return false;
      } else {
         SVGTextElementBridge txtBridge = null;
         if (svgCtx instanceof SVGTextElementBridge) {
            txtBridge = (SVGTextElementBridge)svgCtx;
         } else if (svgCtx instanceof AbstractTextChildSVGContext) {
            AbstractTextChildSVGContext childCtx = (AbstractTextChildSVGContext)svgCtx;
            txtBridge = childCtx.getTextBridge();
         }

         if (txtBridge == null) {
            return false;
         } else {
            TextNode tn = txtBridge.getTextNode();
            List list = tn.getTextRuns();
            if (list == null) {
               return false;
            } else {
               Element txtElem = txtBridge.e;
               AffineTransform at = tn.getGlobalTransform();
               at.preConcatenate(ati);
               Rectangle2D tnRect = tn.getBounds();
               tnRect = at.createTransformedShape(tnRect).getBounds2D();
               if (!rect.intersects(tnRect)) {
                  return false;
               } else {
                  Iterator var12 = list.iterator();

                  while(true) {
                     TextSpanLayout layout;
                     Rectangle2D glBounds;
                     do {
                        do {
                           Element p;
                           do {
                              Element runElem;
                              do {
                                 do {
                                    if (!var12.hasNext()) {
                                       return false;
                                    }

                                    Object aList = var12.next();
                                    StrokingTextPainter.TextRun run = (StrokingTextPainter.TextRun)aList;
                                    layout = run.getLayout();
                                    AttributedCharacterIterator aci = run.getACI();
                                    aci.first();
                                    SoftReference sr = (SoftReference)aci.getAttribute(TEXT_COMPOUND_ID);
                                    runElem = (Element)sr.get();
                                 } while(runElem == null);
                              } while(checkSensitivity && !isTextSensitive(runElem));

                              for(p = runElem; p != null && p != txtElem && p != elem; p = (Element)txtBridge.getParentNode(p)) {
                              }
                           } while(p != elem);

                           glBounds = layout.getBounds2D();
                        } while(glBounds == null);

                        glBounds = at.createTransformedShape(glBounds).getBounds2D();
                     } while(!rect.intersects(glBounds));

                     GVTGlyphVector gv = layout.getGlyphVector();

                     for(int g = 0; g < gv.getNumGlyphs(); ++g) {
                        Shape gBounds = gv.getGlyphLogicalBounds(g);
                        if (gBounds != null) {
                           Shape gBounds = at.createTransformedShape(gBounds).getBounds2D();
                           if (gBounds.intersects(rect)) {
                              return true;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static Rectangle2D getTextBounds(BridgeContext ctx, Element elem, boolean checkSensitivity) {
      SVGContext svgCtx = null;
      if (elem instanceof SVGOMElement) {
         svgCtx = ((SVGOMElement)elem).getSVGContext();
      }

      if (svgCtx == null) {
         return null;
      } else {
         SVGTextElementBridge txtBridge = null;
         if (svgCtx instanceof SVGTextElementBridge) {
            txtBridge = (SVGTextElementBridge)svgCtx;
         } else if (svgCtx instanceof AbstractTextChildSVGContext) {
            AbstractTextChildSVGContext childCtx = (AbstractTextChildSVGContext)svgCtx;
            txtBridge = childCtx.getTextBridge();
         }

         if (txtBridge == null) {
            return null;
         } else {
            TextNode tn = txtBridge.getTextNode();
            List list = tn.getTextRuns();
            if (list == null) {
               return null;
            } else {
               Element txtElem = txtBridge.e;
               Rectangle2D ret = null;
               Iterator var9 = list.iterator();

               while(true) {
                  TextSpanLayout layout;
                  Element runElem;
                  do {
                     do {
                        if (!var9.hasNext()) {
                           return ret;
                        }

                        Object aList = var9.next();
                        StrokingTextPainter.TextRun run = (StrokingTextPainter.TextRun)aList;
                        layout = run.getLayout();
                        AttributedCharacterIterator aci = run.getACI();
                        aci.first();
                        SoftReference sr = (SoftReference)aci.getAttribute(TEXT_COMPOUND_ID);
                        runElem = (Element)sr.get();
                     } while(runElem == null);
                  } while(checkSensitivity && !isTextSensitive(runElem));

                  Element p;
                  for(p = runElem; p != null && p != txtElem && p != elem; p = (Element)txtBridge.getParentNode(p)) {
                  }

                  if (p == elem) {
                     Rectangle2D glBounds = layout.getBounds2D();
                     if (glBounds != null) {
                        if (ret == null) {
                           ret = (Rectangle2D)glBounds.clone();
                        } else {
                           ret.add(glBounds);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static boolean isTextSensitive(Element e) {
      int ptrEvts = CSSUtilities.convertPointerEvents(e);
      switch (ptrEvts) {
         case 0:
         case 1:
         case 2:
         case 3:
            return CSSUtilities.convertVisibility(e);
         case 4:
         case 5:
         case 6:
         case 7:
            return true;
         case 8:
         default:
            return false;
      }
   }

   static {
      TEXT_COMPOUND_DELIMITER = GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_DELIMITER;
      TEXT_COMPOUND_ID = GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_ID;
      PAINT_INFO = GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO;
      ALT_GLYPH_HANDLER = GVTAttributedCharacterIterator.TextAttribute.ALT_GLYPH_HANDLER;
      TEXTPATH = GVTAttributedCharacterIterator.TextAttribute.TEXTPATH;
      ANCHOR_TYPE = GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE;
      GVT_FONT_FAMILIES = GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES;
      GVT_FONTS = GVTAttributedCharacterIterator.TextAttribute.GVT_FONTS;
      BASELINE_SHIFT = GVTAttributedCharacterIterator.TextAttribute.BASELINE_SHIFT;
   }

   protected static class CharacterInformation {
      TextSpanLayout layout;
      int glyphIndexStart;
      int glyphIndexEnd;
      int characterIndex;

      public boolean isVertical() {
         return this.layout.isVertical();
      }

      public double getComputedOrientationAngle() {
         return this.layout.getComputedOrientationAngle(this.characterIndex);
      }
   }

   protected class TspanBridge extends AbstractTextChildTextContent {
      protected TspanBridge(BridgeContext ctx, SVGTextElementBridge parent, Element e) {
         super(ctx, parent, e);
      }

      public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
         if (alav.getNamespaceURI() == null) {
            String ln = alav.getLocalName();
            if (ln.equals("x") || ln.equals("y") || ln.equals("dx") || ln.equals("dy") || ln.equals("rotate") || ln.equals("textLength") || ln.equals("lengthAdjust")) {
               this.textBridge.computeLaidoutText(this.ctx, this.textBridge.e, this.textBridge.getTextNode());
               return;
            }
         }

         super.handleAnimatedAttributeChanged(alav);
      }
   }

   protected class TextPathBridge extends AbstractTextChildTextContent {
      protected TextPathBridge(BridgeContext ctx, SVGTextElementBridge parent, Element e) {
         super(ctx, parent, e);
      }
   }

   protected class TRefBridge extends AbstractTextChildTextContent {
      protected TRefBridge(BridgeContext ctx, SVGTextElementBridge parent, Element e) {
         super(ctx, parent, e);
      }

      public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
         if (alav.getNamespaceURI() == null) {
            String ln = alav.getLocalName();
            if (ln.equals("x") || ln.equals("y") || ln.equals("dx") || ln.equals("dy") || ln.equals("rotate") || ln.equals("textLength") || ln.equals("lengthAdjust")) {
               this.textBridge.computeLaidoutText(this.ctx, this.textBridge.e, this.textBridge.getTextNode());
               return;
            }
         }

         super.handleAnimatedAttributeChanged(alav);
      }
   }

   protected class AbstractTextChildTextContent extends AbstractTextChildBridgeUpdateHandler implements SVGTextContent {
      protected AbstractTextChildTextContent(BridgeContext ctx, SVGTextElementBridge parent, Element e) {
         super(ctx, parent, e);
      }

      public int getNumberOfChars() {
         return this.textBridge.getNumberOfChars(this.e);
      }

      public Rectangle2D getExtentOfChar(int charnum) {
         return this.textBridge.getExtentOfChar(this.e, charnum);
      }

      public Point2D getStartPositionOfChar(int charnum) {
         return this.textBridge.getStartPositionOfChar(this.e, charnum);
      }

      public Point2D getEndPositionOfChar(int charnum) {
         return this.textBridge.getEndPositionOfChar(this.e, charnum);
      }

      public void selectSubString(int charnum, int nchars) {
         this.textBridge.selectSubString(this.e, charnum, nchars);
      }

      public float getRotationOfChar(int charnum) {
         return this.textBridge.getRotationOfChar(this.e, charnum);
      }

      public float getComputedTextLength() {
         return this.textBridge.getComputedTextLength(this.e);
      }

      public float getSubStringLength(int charnum, int nchars) {
         return this.textBridge.getSubStringLength(this.e, charnum, nchars);
      }

      public int getCharNumAtPosition(float x, float y) {
         return this.textBridge.getCharNumAtPosition(this.e, x, y);
      }
   }

   protected abstract class AbstractTextChildBridgeUpdateHandler extends AbstractTextChildSVGContext implements BridgeUpdateHandler {
      protected AbstractTextChildBridgeUpdateHandler(BridgeContext ctx, SVGTextElementBridge parent, Element e) {
         super(ctx, parent, e);
      }

      public void handleDOMAttrModifiedEvent(MutationEvent evt) {
      }

      public void handleDOMNodeInsertedEvent(MutationEvent evt) {
         this.textBridge.handleDOMNodeInsertedEvent(evt);
      }

      public void handleDOMNodeRemovedEvent(MutationEvent evt) {
      }

      public void handleDOMCharacterDataModified(MutationEvent evt) {
         this.textBridge.handleDOMCharacterDataModified(evt);
      }

      public void handleCSSEngineEvent(CSSEngineEvent evt) {
         this.textBridge.handleCSSEngineEvent(evt);
      }

      public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      }

      public void handleOtherAnimationChanged(String type) {
      }

      public void dispose() {
         ((SVGOMElement)this.e).setSVGContext((SVGContext)null);
         SVGTextElementBridge.this.elemTPI.remove(this.e);
      }
   }

   public abstract static class AbstractTextChildSVGContext extends AnimatableSVGBridge {
      protected SVGTextElementBridge textBridge;

      public AbstractTextChildSVGContext(BridgeContext ctx, SVGTextElementBridge parent, Element e) {
         this.ctx = ctx;
         this.textBridge = parent;
         this.e = e;
      }

      public String getNamespaceURI() {
         return null;
      }

      public String getLocalName() {
         return null;
      }

      public Bridge getInstance() {
         return null;
      }

      public SVGTextElementBridge getTextBridge() {
         return this.textBridge;
      }

      public float getPixelUnitToMillimeter() {
         return this.ctx.getUserAgent().getPixelUnitToMillimeter();
      }

      public float getPixelToMM() {
         return this.getPixelUnitToMillimeter();
      }

      public Rectangle2D getBBox() {
         return null;
      }

      public AffineTransform getCTM() {
         return null;
      }

      public AffineTransform getGlobalTransform() {
         return null;
      }

      public AffineTransform getScreenTransform() {
         return null;
      }

      public void setScreenTransform(AffineTransform at) {
      }

      public float getViewportWidth() {
         return this.ctx.getBlockWidth(this.e);
      }

      public float getViewportHeight() {
         return this.ctx.getBlockHeight(this.e);
      }

      public float getFontSize() {
         return CSSUtilities.getComputedStyle(this.e, 22).getFloatValue();
      }
   }

   protected static class AttributedStringBuffer {
      protected List strings = new ArrayList();
      protected List attributes = new ArrayList();
      protected int count = 0;
      protected int length = 0;

      public AttributedStringBuffer() {
      }

      public boolean isEmpty() {
         return this.count == 0;
      }

      public int length() {
         return this.length;
      }

      public void append(String s, Map m) {
         if (s.length() != 0) {
            this.strings.add(s);
            this.attributes.add(m);
            ++this.count;
            this.length += s.length();
         }
      }

      public int getLastChar() {
         if (this.count == 0) {
            return -1;
         } else {
            String s = (String)this.strings.get(this.count - 1);
            return s.charAt(s.length() - 1);
         }
      }

      public void stripFirst() {
         String s = (String)this.strings.get(0);
         if (s.charAt(s.length() - 1) == ' ') {
            --this.length;
            if (s.length() == 1) {
               this.attributes.remove(0);
               this.strings.remove(0);
               --this.count;
            } else {
               this.strings.set(0, s.substring(1));
            }
         }
      }

      public void stripLast() {
         String s = (String)this.strings.get(this.count - 1);
         if (s.charAt(s.length() - 1) == ' ') {
            --this.length;
            if (s.length() == 1) {
               this.attributes.remove(--this.count);
               this.strings.remove(this.count);
            } else {
               this.strings.set(this.count - 1, s.substring(0, s.length() - 1));
            }
         }
      }

      public AttributedString toAttributedString() {
         switch (this.count) {
            case 0:
               return null;
            case 1:
               return new AttributedString((String)this.strings.get(0), (Map)this.attributes.get(0));
            default:
               StringBuffer sb = new StringBuffer(this.strings.size() * 5);
               Iterator var2 = this.strings.iterator();

               while(var2.hasNext()) {
                  Object string = var2.next();
                  sb.append((String)string);
               }

               AttributedString result = new AttributedString(sb.toString());
               Iterator sit = this.strings.iterator();
               Iterator ait = this.attributes.iterator();

               int nidx;
               for(int idx = 0; sit.hasNext(); idx = nidx) {
                  String s = (String)sit.next();
                  nidx = idx + s.length();
                  Map m = (Map)ait.next();
                  Iterator kit = m.keySet().iterator();
                  Iterator vit = m.values().iterator();

                  while(kit.hasNext()) {
                     AttributedCharacterIterator.Attribute attr = (AttributedCharacterIterator.Attribute)kit.next();
                     Object val = vit.next();
                     result.addAttribute(attr, val, idx, nidx);
                  }
               }

               return result;
         }
      }

      public String toString() {
         switch (this.count) {
            case 0:
               return "";
            case 1:
               return (String)this.strings.get(0);
            default:
               StringBuffer sb = new StringBuffer(this.strings.size() * 5);
               Iterator var2 = this.strings.iterator();

               while(var2.hasNext()) {
                  Object string = var2.next();
                  sb.append((String)string);
               }

               return sb.toString();
         }
      }
   }

   protected class DOMSubtreeModifiedEventListener implements EventListener {
      public void handleEvent(Event evt) {
         SVGTextElementBridge.this.handleDOMSubtreeModifiedEvent((MutationEvent)evt);
      }
   }

   protected class DOMChildNodeRemovedEventListener implements EventListener {
      public void handleEvent(Event evt) {
         SVGTextElementBridge.this.handleDOMChildNodeRemovedEvent((MutationEvent)evt);
      }
   }
}
