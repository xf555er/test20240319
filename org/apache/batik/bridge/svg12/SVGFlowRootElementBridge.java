package org.apache.batik.bridge.svg12;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.anim.dom.SVGOMFlowRegionElement;
import org.apache.batik.anim.dom.XBLEventSupport;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.CursorManager;
import org.apache.batik.bridge.FlowTextNode;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGAElementBridge;
import org.apache.batik.bridge.SVGTextElementBridge;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextUtilities;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.ComputedValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.svg12.LineHeightValue;
import org.apache.batik.css.engine.value.svg12.SVG12ValueConstants;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.flow.BlockInfo;
import org.apache.batik.gvt.flow.RegionInfo;
import org.apache.batik.gvt.flow.TextLineBreaks;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

public class SVGFlowRootElementBridge extends SVG12TextElementBridge {
   public static final AttributedCharacterIterator.Attribute FLOW_PARAGRAPH;
   public static final AttributedCharacterIterator.Attribute FLOW_EMPTY_PARAGRAPH;
   public static final AttributedCharacterIterator.Attribute FLOW_LINE_BREAK;
   public static final AttributedCharacterIterator.Attribute FLOW_REGIONS;
   public static final AttributedCharacterIterator.Attribute LINE_HEIGHT;
   public static final GVTAttributedCharacterIterator.TextAttribute TEXTPATH;
   public static final GVTAttributedCharacterIterator.TextAttribute ANCHOR_TYPE;
   public static final GVTAttributedCharacterIterator.TextAttribute LETTER_SPACING;
   public static final GVTAttributedCharacterIterator.TextAttribute WORD_SPACING;
   public static final GVTAttributedCharacterIterator.TextAttribute KERNING;
   protected Map flowRegionNodes;
   protected TextNode textNode;
   protected RegionChangeListener regionChangeListener;
   protected int startLen;
   int marginTopIndex = -1;
   int marginRightIndex = -1;
   int marginBottomIndex = -1;
   int marginLeftIndex = -1;
   int indentIndex = -1;
   int textAlignIndex = -1;
   int lineHeightIndex = -1;

   protected TextNode getTextNode() {
      return this.textNode;
   }

   public String getNamespaceURI() {
      return "http://www.w3.org/2000/svg";
   }

   public String getLocalName() {
      return "flowRoot";
   }

   public Bridge getInstance() {
      return new SVGFlowRootElementBridge();
   }

   public boolean isComposite() {
      return false;
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
         return null;
      } else {
         CompositeGraphicsNode cgn = new CompositeGraphicsNode();
         String s = e.getAttributeNS((String)null, "transform");
         if (s.length() != 0) {
            cgn.setTransform(SVGUtilities.convertTransform(e, "transform", s, ctx));
         }

         cgn.setVisible(CSSUtilities.convertVisibility(e));
         RenderingHints hints = null;
         hints = CSSUtilities.convertColorRendering(e, hints);
         hints = CSSUtilities.convertTextRendering(e, hints);
         if (hints != null) {
            cgn.setRenderingHints(hints);
         }

         CompositeGraphicsNode cgn2 = new CompositeGraphicsNode();
         cgn.add(cgn2);
         FlowTextNode tn = (FlowTextNode)this.instantiateGraphicsNode();
         tn.setLocation(this.getLocation(ctx, e));
         if (ctx.getTextPainter() != null) {
            tn.setTextPainter(ctx.getTextPainter());
         }

         this.textNode = tn;
         cgn.add(tn);
         this.associateSVGContext(ctx, e, cgn);

         for(Node child = this.getFirstChild(e); child != null; child = this.getNextSibling(child)) {
            if (child.getNodeType() == 1) {
               this.addContextToChild(ctx, (Element)child);
            }
         }

         return cgn;
      }
   }

   protected GraphicsNode instantiateGraphicsNode() {
      return new FlowTextNode();
   }

   protected Point2D getLocation(BridgeContext ctx, Element e) {
      return new Point2D.Float(0.0F, 0.0F);
   }

   protected boolean isTextElement(Element e) {
      if (!"http://www.w3.org/2000/svg".equals(e.getNamespaceURI())) {
         return false;
      } else {
         String nodeName = e.getLocalName();
         return nodeName.equals("flowDiv") || nodeName.equals("flowLine") || nodeName.equals("flowPara") || nodeName.equals("flowRegionBreak") || nodeName.equals("flowSpan");
      }
   }

   protected boolean isTextChild(Element e) {
      if (!"http://www.w3.org/2000/svg".equals(e.getNamespaceURI())) {
         return false;
      } else {
         String nodeName = e.getLocalName();
         return nodeName.equals("a") || nodeName.equals("flowLine") || nodeName.equals("flowPara") || nodeName.equals("flowRegionBreak") || nodeName.equals("flowSpan");
      }
   }

   public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
      CompositeGraphicsNode cgn = (CompositeGraphicsNode)node;
      boolean isStatic = !ctx.isDynamic();
      if (isStatic) {
         this.flowRegionNodes = new HashMap();
      } else {
         this.regionChangeListener = new RegionChangeListener();
      }

      CompositeGraphicsNode cgn2 = (CompositeGraphicsNode)cgn.get(0);
      GVTBuilder builder = ctx.getGVTBuilder();

      for(Node n = this.getFirstChild(e); n != null; n = this.getNextSibling(n)) {
         if (n instanceof SVGOMFlowRegionElement) {
            for(Node m = this.getFirstChild(n); m != null; m = this.getNextSibling(m)) {
               if (m.getNodeType() == 1) {
                  GraphicsNode gn = builder.build(ctx, (Element)m);
                  if (gn != null) {
                     cgn2.add(gn);
                     if (isStatic) {
                        this.flowRegionNodes.put(m, gn);
                     }
                  }
               }
            }

            if (!isStatic) {
               AbstractNode an = (AbstractNode)n;
               XBLEventSupport es = (XBLEventSupport)an.initializeEventSupport();
               es.addImplementationEventListenerNS("http://www.w3.org/2000/svg", "shapechange", this.regionChangeListener, false);
            }
         }
      }

      GraphicsNode tn = (GraphicsNode)cgn.get(1);
      super.buildGraphicsNode(ctx, e, tn);
      this.flowRegionNodes = null;
   }

   protected void computeLaidoutText(BridgeContext ctx, Element e, GraphicsNode node) {
      super.computeLaidoutText(ctx, this.getFlowDivElement(e), node);
   }

   protected void addContextToChild(BridgeContext ctx, Element e) {
      if ("http://www.w3.org/2000/svg".equals(e.getNamespaceURI())) {
         String ln = e.getLocalName();
         if (ln.equals("flowDiv") || ln.equals("flowLine") || ln.equals("flowPara") || ln.equals("flowSpan")) {
            ((SVGOMElement)e).setSVGContext(new FlowContentBridge(ctx, this, e));
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
         String ln = e.getLocalName();
         if (ln.equals("flowDiv") || ln.equals("flowLine") || ln.equals("flowPara") || ln.equals("flowSpan")) {
            ((SVGTextElementBridge.AbstractTextChildBridgeUpdateHandler)((SVGOMElement)e).getSVGContext()).dispose();
         }
      }

      for(Node child = this.getFirstChild(e); child != null; child = this.getNextSibling(child)) {
         if (child.getNodeType() == 1) {
            this.removeContextFromChild(ctx, (Element)child);
         }
      }

   }

   protected AttributedString buildAttributedString(BridgeContext ctx, Element element) {
      if (element == null) {
         return null;
      } else {
         List rgns = this.getRegions(ctx, element);
         AttributedString ret = this.getFlowDiv(ctx, element);
         if (ret == null) {
            return ret;
         } else {
            ret.addAttribute(FLOW_REGIONS, rgns, 0, 1);
            TextLineBreaks.findLineBrk(ret);
            return ret;
         }
      }
   }

   protected void dumpACIWord(AttributedString as) {
      if (as != null) {
         StringBuffer chars = new StringBuffer();
         StringBuffer brkStr = new StringBuffer();
         AttributedCharacterIterator aci = as.getIterator();
         AttributedCharacterIterator.Attribute WORD_LIMIT = TextLineBreaks.WORD_LIMIT;

         for(char ch = aci.current(); ch != '\uffff'; ch = aci.next()) {
            chars.append(ch).append(' ').append(' ');
            int w = (Integer)aci.getAttribute(WORD_LIMIT);
            brkStr.append(w).append(' ');
            if (w < 10) {
               brkStr.append(' ');
            }
         }

         System.out.println(chars.toString());
         System.out.println(brkStr.toString());
      }
   }

   protected Element getFlowDivElement(Element elem) {
      String eNS = elem.getNamespaceURI();
      if (!eNS.equals("http://www.w3.org/2000/svg")) {
         return null;
      } else {
         String nodeName = elem.getLocalName();
         if (nodeName.equals("flowDiv")) {
            return elem;
         } else if (!nodeName.equals("flowRoot")) {
            return null;
         } else {
            for(Node n = this.getFirstChild(elem); n != null; n = this.getNextSibling(n)) {
               if (n.getNodeType() == 1) {
                  String nNS = n.getNamespaceURI();
                  if ("http://www.w3.org/2000/svg".equals(nNS)) {
                     Element e = (Element)n;
                     String ln = e.getLocalName();
                     if (ln.equals("flowDiv")) {
                        return e;
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   protected AttributedString getFlowDiv(BridgeContext ctx, Element element) {
      Element flowDiv = this.getFlowDivElement(element);
      return flowDiv == null ? null : this.gatherFlowPara(ctx, flowDiv);
   }

   protected AttributedString gatherFlowPara(BridgeContext ctx, Element div) {
      TextPaintInfo divTPI = new TextPaintInfo();
      divTPI.visible = true;
      divTPI.fillPaint = Color.black;
      this.elemTPI.put(div, divTPI);
      SVGTextElementBridge.AttributedStringBuffer asb = new SVGTextElementBridge.AttributedStringBuffer();
      List paraEnds = new ArrayList();
      List paraElems = new ArrayList();
      List lnLocs = new ArrayList();

      for(Node n = this.getFirstChild(div); n != null; n = this.getNextSibling(n)) {
         if (n.getNodeType() == 1 && this.getNamespaceURI().equals(n.getNamespaceURI())) {
            Element e = (Element)n;
            String ln = e.getLocalName();
            if (ln.equals("flowPara")) {
               this.fillAttributedStringBuffer(ctx, e, true, (Integer)null, (Map)null, asb, lnLocs);
               paraElems.add(e);
               paraEnds.add(asb.length());
            } else if (ln.equals("flowRegionBreak")) {
               this.fillAttributedStringBuffer(ctx, e, true, (Integer)null, (Map)null, asb, lnLocs);
               paraElems.add(e);
               paraEnds.add(asb.length());
            }
         }
      }

      divTPI.startChar = 0;
      divTPI.endChar = asb.length() - 1;
      AttributedString ret = asb.toAttributedString();
      if (ret == null) {
         return null;
      } else {
         int prevLN = 0;
         Iterator var17 = lnLocs.iterator();

         while(var17.hasNext()) {
            Object lnLoc = var17.next();
            int nextLN = (Integer)lnLoc;
            if (nextLN != prevLN) {
               ret.addAttribute(FLOW_LINE_BREAK, new Object(), prevLN, nextLN);
               prevLN = nextLN;
            }
         }

         int start = 0;
         List emptyPara = null;

         int end;
         for(int i = 0; i < paraElems.size(); start = end) {
            Element elem = (Element)paraElems.get(i);
            end = (Integer)paraEnds.get(i);
            if (start == end) {
               if (emptyPara == null) {
                  emptyPara = new LinkedList();
               }

               emptyPara.add(this.makeBlockInfo(ctx, elem));
            } else {
               ret.addAttribute(FLOW_PARAGRAPH, this.makeBlockInfo(ctx, elem), start, end);
               if (emptyPara != null) {
                  ret.addAttribute(FLOW_EMPTY_PARAGRAPH, emptyPara, start, end);
                  emptyPara = null;
               }
            }

            ++i;
         }

         return ret;
      }
   }

   protected List getRegions(BridgeContext ctx, Element element) {
      element = (Element)element.getParentNode();
      List ret = new LinkedList();

      for(Node n = this.getFirstChild(element); n != null; n = this.getNextSibling(n)) {
         if (n.getNodeType() == 1 && "http://www.w3.org/2000/svg".equals(n.getNamespaceURI())) {
            Element e = (Element)n;
            String ln = e.getLocalName();
            if ("flowRegion".equals(ln)) {
               float verticalAlignment = 0.0F;
               this.gatherRegionInfo(ctx, e, verticalAlignment, ret);
            }
         }
      }

      return ret;
   }

   protected void gatherRegionInfo(BridgeContext ctx, Element rgn, float verticalAlign, List regions) {
      boolean isStatic = !ctx.isDynamic();

      for(Node n = this.getFirstChild(rgn); n != null; n = this.getNextSibling(n)) {
         if (n.getNodeType() == 1) {
            GraphicsNode gn = isStatic ? (GraphicsNode)this.flowRegionNodes.get(n) : ctx.getGraphicsNode(n);
            Shape s = gn.getOutline();
            if (s != null) {
               AffineTransform at = gn.getTransform();
               if (at != null) {
                  s = at.createTransformedShape(s);
               }

               regions.add(new RegionInfo(s, verticalAlign));
            }
         }
      }

   }

   protected void fillAttributedStringBuffer(BridgeContext ctx, Element element, boolean top, Integer bidiLevel, Map initialAttributes, SVGTextElementBridge.AttributedStringBuffer asb, List lnLocs) {
      if (SVGUtilities.matchUserAgent(element, ctx.getUserAgent()) && CSSUtilities.convertDisplay(element)) {
         String s = XMLSupport.getXMLSpace(element);
         boolean preserve = s.equals("preserve");
         int elementStartChar = asb.length();
         if (top) {
            this.endLimit = this.startLen = asb.length();
         }

         if (preserve) {
            this.endLimit = this.startLen;
         }

         Map map = initialAttributes == null ? new HashMap() : new HashMap(initialAttributes);
         initialAttributes = this.getAttributeMap(ctx, element, (TextPath)null, bidiLevel, map);
         Object o = map.get(TextAttribute.BIDI_EMBEDDING);
         Integer subBidiLevel = bidiLevel;
         if (o != null) {
            subBidiLevel = (Integer)o;
         }

         int lineBreak = true;
         int lineBreak;
         if (lnLocs.size() != 0) {
            lineBreak = (Integer)lnLocs.get(lnLocs.size() - 1);
         }

         int idx;
         int before;
         Integer i;
         for(Node n = this.getFirstChild(element); n != null; n = this.getNextSibling(n)) {
            boolean prevEndsWithSpace;
            if (preserve) {
               prevEndsWithSpace = false;
            } else {
               idx = asb.length();
               if (idx == this.startLen) {
                  prevEndsWithSpace = true;
               } else {
                  prevEndsWithSpace = asb.getLastChar() == 32;
                  before = lnLocs.size() - 1;
                  if (!prevEndsWithSpace && before >= 0) {
                     i = (Integer)lnLocs.get(before);
                     if (i == idx) {
                        prevEndsWithSpace = true;
                     }
                  }
               }
            }

            switch (n.getNodeType()) {
               case 1:
                  if ("http://www.w3.org/2000/svg".equals(n.getNamespaceURI())) {
                     Element nodeElement = (Element)n;
                     String ln = n.getLocalName();
                     if (ln.equals("flowLine")) {
                        before = asb.length();
                        this.fillAttributedStringBuffer(ctx, nodeElement, false, subBidiLevel, initialAttributes, asb, lnLocs);
                        lineBreak = asb.length();
                        lnLocs.add(lineBreak);
                        if (before != lineBreak) {
                           initialAttributes = null;
                        }
                     } else if (!ln.equals("flowSpan") && !ln.equals("altGlyph")) {
                        if (ln.equals("a")) {
                           if (ctx.isInteractive()) {
                              NodeEventTarget target = (NodeEventTarget)nodeElement;
                              UserAgent ua = ctx.getUserAgent();
                              SVGAElementBridge.CursorHolder ch = new SVGAElementBridge.CursorHolder(CursorManager.DEFAULT_CURSOR);
                              target.addEventListenerNS("http://www.w3.org/2001/xml-events", "click", new SVGAElementBridge.AnchorListener(ua, ch), false, (Object)null);
                              target.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", new SVGAElementBridge.CursorMouseOverListener(ua, ch), false, (Object)null);
                              target.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", new SVGAElementBridge.CursorMouseOutListener(ua, ch), false, (Object)null);
                           }

                           before = asb.length();
                           this.fillAttributedStringBuffer(ctx, nodeElement, false, subBidiLevel, initialAttributes, asb, lnLocs);
                           if (asb.length() != before) {
                              initialAttributes = null;
                           }
                        } else if (ln.equals("tref")) {
                           String uriStr = XLinkSupport.getXLinkHref((Element)n);
                           Element ref = ctx.getReferencedElement((Element)n, uriStr);
                           s = TextUtilities.getElementContent(ref);
                           s = this.normalizeString(s, preserve, prevEndsWithSpace);
                           if (s.length() != 0) {
                              int trefStart = asb.length();
                              Map m = new HashMap();
                              this.getAttributeMap(ctx, nodeElement, (TextPath)null, bidiLevel, m);
                              asb.append(s, m);
                              int trefEnd = asb.length() - 1;
                              TextPaintInfo tpi = (TextPaintInfo)this.elemTPI.get(nodeElement);
                              tpi.startChar = trefStart;
                              tpi.endChar = trefEnd;
                           }
                        }
                     } else {
                        before = asb.length();
                        this.fillAttributedStringBuffer(ctx, nodeElement, false, subBidiLevel, initialAttributes, asb, lnLocs);
                        if (asb.length() != before) {
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
               idx = lnLocs.size() - 1;
               before = asb.length();
               if (idx >= 0) {
                  i = (Integer)lnLocs.get(idx);
                  if (i >= before) {
                     i = before - 1;
                     lnLocs.set(idx, i);
                     --idx;

                     while(idx >= 0) {
                        i = (Integer)lnLocs.get(idx);
                        if (i < before - 1) {
                           break;
                        }

                        lnLocs.remove(idx);
                        --idx;
                     }
                  }
               }

               asb.stripLast();
            }

            if (strippedSome) {
               Iterator var29 = this.elemTPI.values().iterator();

               while(var29.hasNext()) {
                  Object o1 = var29.next();
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

   protected Map getAttributeMap(BridgeContext ctx, Element element, TextPath textPath, Integer bidiLevel, Map result) {
      Map inheritingMap = super.getAttributeMap(ctx, element, textPath, bidiLevel, result);
      float fontSize = TextUtilities.convertFontSize(element);
      float lineHeight = this.getLineHeight(ctx, element, fontSize);
      result.put(LINE_HEIGHT, lineHeight);
      return inheritingMap;
   }

   protected void checkMap(Map attrs) {
      if (!attrs.containsKey(TEXTPATH)) {
         if (!attrs.containsKey(ANCHOR_TYPE)) {
            if (!attrs.containsKey(LETTER_SPACING)) {
               if (!attrs.containsKey(WORD_SPACING)) {
                  if (!attrs.containsKey(KERNING)) {
                     ;
                  }
               }
            }
         }
      }
   }

   protected void initCSSPropertyIndexes(Element e) {
      CSSEngine eng = CSSUtilities.getCSSEngine(e);
      this.marginTopIndex = eng.getPropertyIndex("margin-top");
      this.marginRightIndex = eng.getPropertyIndex("margin-right");
      this.marginBottomIndex = eng.getPropertyIndex("margin-bottom");
      this.marginLeftIndex = eng.getPropertyIndex("margin-left");
      this.indentIndex = eng.getPropertyIndex("indent");
      this.textAlignIndex = eng.getPropertyIndex("text-align");
      this.lineHeightIndex = eng.getPropertyIndex("line-height");
   }

   public BlockInfo makeBlockInfo(BridgeContext ctx, Element element) {
      if (this.marginTopIndex == -1) {
         this.initCSSPropertyIndexes(element);
      }

      Value v = CSSUtilities.getComputedStyle(element, this.marginTopIndex);
      float top = v.getFloatValue();
      v = CSSUtilities.getComputedStyle(element, this.marginRightIndex);
      float right = v.getFloatValue();
      v = CSSUtilities.getComputedStyle(element, this.marginBottomIndex);
      float bottom = v.getFloatValue();
      v = CSSUtilities.getComputedStyle(element, this.marginLeftIndex);
      float left = v.getFloatValue();
      v = CSSUtilities.getComputedStyle(element, this.indentIndex);
      float indent = v.getFloatValue();
      v = CSSUtilities.getComputedStyle(element, this.textAlignIndex);
      if (v == ValueConstants.INHERIT_VALUE) {
         v = CSSUtilities.getComputedStyle(element, 11);
         if (v == ValueConstants.LTR_VALUE) {
            v = SVG12ValueConstants.START_VALUE;
         } else {
            v = SVG12ValueConstants.END_VALUE;
         }
      }

      byte textAlign;
      if (v == SVG12ValueConstants.START_VALUE) {
         textAlign = 0;
      } else if (v == SVG12ValueConstants.MIDDLE_VALUE) {
         textAlign = 1;
      } else if (v == SVG12ValueConstants.END_VALUE) {
         textAlign = 2;
      } else {
         textAlign = 3;
      }

      Map fontAttrs = new HashMap(20);
      List fontList = this.getFontList(ctx, element, fontAttrs);
      Float fs = (Float)fontAttrs.get(TextAttribute.SIZE);
      float fontSize = fs;
      float lineHeight = this.getLineHeight(ctx, element, fontSize);
      String ln = element.getLocalName();
      boolean rgnBr = ln.equals("flowRegionBreak");
      return new BlockInfo(top, right, bottom, left, indent, textAlign, lineHeight, fontList, fontAttrs, rgnBr);
   }

   protected float getLineHeight(BridgeContext ctx, Element element, float fontSize) {
      if (this.lineHeightIndex == -1) {
         this.initCSSPropertyIndexes(element);
      }

      Value v = CSSUtilities.getComputedStyle(element, this.lineHeightIndex);
      if (v != ValueConstants.INHERIT_VALUE && v != SVG12ValueConstants.NORMAL_VALUE) {
         float lineHeight = v.getFloatValue();
         if (v instanceof ComputedValue) {
            v = ((ComputedValue)v).getComputedValue();
         }

         if (v instanceof LineHeightValue && ((LineHeightValue)v).getFontSizeRelative()) {
            lineHeight *= fontSize;
         }

         return lineHeight;
      } else {
         return fontSize * 1.1F;
      }
   }

   static {
      FLOW_PARAGRAPH = GVTAttributedCharacterIterator.TextAttribute.FLOW_PARAGRAPH;
      FLOW_EMPTY_PARAGRAPH = GVTAttributedCharacterIterator.TextAttribute.FLOW_EMPTY_PARAGRAPH;
      FLOW_LINE_BREAK = GVTAttributedCharacterIterator.TextAttribute.FLOW_LINE_BREAK;
      FLOW_REGIONS = GVTAttributedCharacterIterator.TextAttribute.FLOW_REGIONS;
      LINE_HEIGHT = GVTAttributedCharacterIterator.TextAttribute.LINE_HEIGHT;
      TEXTPATH = GVTAttributedCharacterIterator.TextAttribute.TEXTPATH;
      ANCHOR_TYPE = GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE;
      LETTER_SPACING = GVTAttributedCharacterIterator.TextAttribute.LETTER_SPACING;
      WORD_SPACING = GVTAttributedCharacterIterator.TextAttribute.WORD_SPACING;
      KERNING = GVTAttributedCharacterIterator.TextAttribute.KERNING;
   }

   protected class RegionChangeListener implements EventListener {
      public void handleEvent(Event evt) {
         SVGFlowRootElementBridge.this.laidoutText = null;
         SVGFlowRootElementBridge.this.computeLaidoutText(SVGFlowRootElementBridge.this.ctx, SVGFlowRootElementBridge.this.e, SVGFlowRootElementBridge.this.getTextNode());
      }
   }

   protected class FlowContentBridge extends SVGTextElementBridge.AbstractTextChildTextContent {
      public FlowContentBridge(BridgeContext ctx, SVGTextElementBridge parent, Element e) {
         super(ctx, parent, e);
      }
   }
}
