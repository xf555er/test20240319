package org.apache.batik.extension.svg;

import java.awt.Color;
import java.awt.font.TextAttribute;
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
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.CursorManager;
import org.apache.batik.bridge.SVGAElementBridge;
import org.apache.batik.bridge.SVGTextElementBridge;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextUtilities;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextPath;
import org.apache.batik.parser.UnitProcessor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BatikFlowTextElementBridge extends SVGTextElementBridge implements BatikExtConstants {
   public static final AttributedCharacterIterator.Attribute FLOW_PARAGRAPH;
   public static final AttributedCharacterIterator.Attribute FLOW_EMPTY_PARAGRAPH;
   public static final AttributedCharacterIterator.Attribute FLOW_LINE_BREAK;
   public static final AttributedCharacterIterator.Attribute FLOW_REGIONS;
   public static final AttributedCharacterIterator.Attribute PREFORMATTED;
   protected static final GVTAttributedCharacterIterator.TextAttribute TEXTPATH;
   protected static final GVTAttributedCharacterIterator.TextAttribute ANCHOR_TYPE;
   protected static final GVTAttributedCharacterIterator.TextAttribute LETTER_SPACING;
   protected static final GVTAttributedCharacterIterator.TextAttribute WORD_SPACING;
   protected static final GVTAttributedCharacterIterator.TextAttribute KERNING;

   public String getNamespaceURI() {
      return "http://xml.apache.org/batik/ext";
   }

   public String getLocalName() {
      return "flowText";
   }

   public Bridge getInstance() {
      return new BatikFlowTextElementBridge();
   }

   public boolean isComposite() {
      return false;
   }

   protected GraphicsNode instantiateGraphicsNode() {
      return new FlowExtTextNode();
   }

   protected Point2D getLocation(BridgeContext ctx, Element e) {
      return new Point2D.Float(0.0F, 0.0F);
   }

   protected void addContextToChild(BridgeContext ctx, Element e) {
      if (this.getNamespaceURI().equals(e.getNamespaceURI())) {
         String ln = e.getLocalName();
         if (ln.equals("flowPara") || ln.equals("flowRegionBreak") || ln.equals("flowLine") || ln.equals("flowSpan") || ln.equals("a") || ln.equals("tref")) {
            ((SVGOMElement)e).setSVGContext(new BatikFlowContentBridge(ctx, this, e));
         }
      }

      for(Node child = this.getFirstChild(e); child != null; child = this.getNextSibling(child)) {
         if (child.getNodeType() == 1) {
            this.addContextToChild(ctx, (Element)child);
         }
      }

   }

   protected AttributedString buildAttributedString(BridgeContext ctx, Element element) {
      List rgns = this.getRegions(ctx, element);
      AttributedString ret = this.getFlowDiv(ctx, element);
      if (ret == null) {
         return ret;
      } else {
         ret.addAttribute(FLOW_REGIONS, rgns, 0, 1);
         return ret;
      }
   }

   protected void addGlyphPositionAttributes(AttributedString as, Element element, BridgeContext ctx) {
      if (element.getNodeType() == 1) {
         String eNS = element.getNamespaceURI();
         if (eNS.equals(this.getNamespaceURI()) || eNS.equals("http://www.w3.org/2000/svg")) {
            if (element.getLocalName() != "flowText") {
               super.addGlyphPositionAttributes(as, element, ctx);
            } else {
               for(Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
                  if (n.getNodeType() == 1) {
                     String nNS = n.getNamespaceURI();
                     if (this.getNamespaceURI().equals(nNS) || "http://www.w3.org/2000/svg".equals(nNS)) {
                        Element e = (Element)n;
                        String ln = e.getLocalName();
                        if (ln.equals("flowDiv")) {
                           super.addGlyphPositionAttributes(as, e, ctx);
                           return;
                        }
                     }
                  }
               }

            }
         }
      }
   }

   protected void addChildGlyphPositionAttributes(AttributedString as, Element element, BridgeContext ctx) {
      for(Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
         if (child.getNodeType() == 1) {
            String cNS = child.getNamespaceURI();
            if (this.getNamespaceURI().equals(cNS) || "http://www.w3.org/2000/svg".equals(cNS)) {
               String ln = child.getLocalName();
               if (ln.equals("flowPara") || ln.equals("flowRegionBreak") || ln.equals("flowLine") || ln.equals("flowSpan") || ln.equals("a") || ln.equals("tref")) {
                  this.addGlyphPositionAttributes(as, (Element)child, ctx);
               }
            }
         }
      }

   }

   protected void addPaintAttributes(AttributedString as, Element element, TextNode node, TextPaintInfo parentPI, BridgeContext ctx) {
      if (element.getNodeType() == 1) {
         String eNS = element.getNamespaceURI();
         if (eNS.equals(this.getNamespaceURI()) || eNS.equals("http://www.w3.org/2000/svg")) {
            if (element.getLocalName() != "flowText") {
               super.addPaintAttributes(as, element, node, parentPI, ctx);
            } else {
               for(Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
                  if (n.getNodeType() == 1 && this.getNamespaceURI().equals(n.getNamespaceURI())) {
                     Element e = (Element)n;
                     String ln = e.getLocalName();
                     if (ln.equals("flowDiv")) {
                        super.addPaintAttributes(as, e, node, parentPI, ctx);
                        return;
                     }
                  }
               }

            }
         }
      }
   }

   protected void addChildPaintAttributes(AttributedString as, Element element, TextNode node, TextPaintInfo parentPI, BridgeContext ctx) {
      for(Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
         if (child.getNodeType() == 1) {
            String cNS = child.getNamespaceURI();
            if (this.getNamespaceURI().equals(cNS) || "http://www.w3.org/2000/svg".equals(cNS)) {
               String ln = child.getLocalName();
               if (ln.equals("flowPara") || ln.equals("flowRegionBreak") || ln.equals("flowLine") || ln.equals("flowSpan") || ln.equals("a") || ln.equals("tref")) {
                  Element childElement = (Element)child;
                  TextPaintInfo pi = this.getTextPaintInfo(childElement, node, parentPI, ctx);
                  this.addPaintAttributes(as, childElement, node, pi, ctx);
               }
            }
         }
      }

   }

   protected AttributedString getFlowDiv(BridgeContext ctx, Element element) {
      for(Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1 && this.getNamespaceURI().equals(n.getNamespaceURI())) {
            Element e = (Element)n;
            String ln = n.getLocalName();
            if (ln.equals("flowDiv")) {
               return this.gatherFlowPara(ctx, e);
            }
         }
      }

      return null;
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

      for(Node n = div.getFirstChild(); n != null; n = n.getNextSibling()) {
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

            emptyPara.add(this.makeMarginInfo(elem));
         } else {
            ret.addAttribute(FLOW_PARAGRAPH, this.makeMarginInfo(elem), start, end);
            if (emptyPara != null) {
               ret.addAttribute(FLOW_EMPTY_PARAGRAPH, emptyPara, start, end);
               emptyPara = null;
            }
         }

         ++i;
      }

      return ret;
   }

   protected List getRegions(BridgeContext ctx, Element element) {
      List ret = new LinkedList();

      for(Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1 && this.getNamespaceURI().equals(n.getNamespaceURI())) {
            Element e = (Element)n;
            String ln = e.getLocalName();
            if ("flowRegion".equals(ln)) {
               float verticalAlignment = 0.0F;
               String verticalAlignmentAttribute = e.getAttribute("vertical-align");
               if (verticalAlignmentAttribute != null && verticalAlignmentAttribute.length() > 0) {
                  if ("top".equals(verticalAlignmentAttribute)) {
                     verticalAlignment = 0.0F;
                  } else if ("middle".equals(verticalAlignmentAttribute)) {
                     verticalAlignment = 0.5F;
                  } else if ("bottom".equals(verticalAlignmentAttribute)) {
                     verticalAlignment = 1.0F;
                  }
               }

               this.gatherRegionInfo(ctx, e, verticalAlignment, ret);
            }
         }
      }

      return ret;
   }

   protected void gatherRegionInfo(BridgeContext ctx, Element rgn, float verticalAlign, List regions) {
      for(Node n = rgn.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1 && this.getNamespaceURI().equals(n.getNamespaceURI())) {
            Element e = (Element)n;
            String ln = n.getLocalName();
            if (ln.equals("rect")) {
               UnitProcessor.Context uctx = org.apache.batik.bridge.UnitProcessor.createContext(ctx, e);
               RegionInfo ri = this.buildRegion(uctx, e, verticalAlign);
               if (ri != null) {
                  regions.add(ri);
               }
            }
         }
      }

   }

   protected RegionInfo buildRegion(UnitProcessor.Context uctx, Element e, float verticalAlignment) {
      String s = e.getAttribute("x");
      float x = 0.0F;
      if (s.length() != 0) {
         x = org.apache.batik.bridge.UnitProcessor.svgHorizontalCoordinateToUserSpace(s, "x", uctx);
      }

      s = e.getAttribute("y");
      float y = 0.0F;
      if (s.length() != 0) {
         y = org.apache.batik.bridge.UnitProcessor.svgVerticalCoordinateToUserSpace(s, "y", uctx);
      }

      s = e.getAttribute("width");
      if (s.length() != 0) {
         float w = org.apache.batik.bridge.UnitProcessor.svgHorizontalLengthToUserSpace(s, "width", uctx);
         if (w == 0.0F) {
            return null;
         } else {
            s = e.getAttribute("height");
            if (s.length() != 0) {
               float h = org.apache.batik.bridge.UnitProcessor.svgVerticalLengthToUserSpace(s, "height", uctx);
               return h == 0.0F ? null : new RegionInfo(x, y, w, h, verticalAlignment);
            } else {
               throw new BridgeException(this.ctx, e, "attribute.missing", new Object[]{"height", s});
            }
         }
      } else {
         throw new BridgeException(this.ctx, e, "attribute.missing", new Object[]{"width", s});
      }
   }

   protected void fillAttributedStringBuffer(BridgeContext ctx, Element element, boolean top, Integer bidiLevel, Map initialAttributes, SVGTextElementBridge.AttributedStringBuffer asb, List lnLocs) {
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
         initialAttributes = this.getAttributeMap(ctx, element, (TextPath)null, bidiLevel, map);
         Object o = map.get(TextAttribute.BIDI_EMBEDDING);
         Integer subBidiLevel = bidiLevel;
         if (o != null) {
            subBidiLevel = (Integer)o;
         }

         for(Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
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
                  if (this.getNamespaceURI().equals(n.getNamespaceURI()) || "http://www.w3.org/2000/svg".equals(n.getNamespaceURI())) {
                     Element nodeElement = (Element)n;
                     String ln = n.getLocalName();
                     int before;
                     if (ln.equals("flowLine")) {
                        before = asb.length();
                        this.fillAttributedStringBuffer(ctx, nodeElement, false, subBidiLevel, initialAttributes, asb, lnLocs);
                        lnLocs.add(asb.length());
                        if (asb.length() != before) {
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
                              HashMap m = initialAttributes == null ? new HashMap() : new HashMap(initialAttributes);
                              this.getAttributeMap(ctx, nodeElement, (TextPath)null, bidiLevel, m);
                              asb.append(s, m);
                              int trefEnd = asb.length() - 1;
                              TextPaintInfo tpi = (TextPaintInfo)this.elemTPI.get(nodeElement);
                              tpi.startChar = trefStart;
                              tpi.endChar = trefEnd;
                              initialAttributes = null;
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

   protected Map getAttributeMap(BridgeContext ctx, Element element, TextPath textPath, Integer bidiLevel, Map result) {
      Map initialMap = super.getAttributeMap(ctx, element, textPath, bidiLevel, result);
      String s = element.getAttribute("preformatted");
      if (s.length() != 0 && s.equals("true")) {
         result.put(PREFORMATTED, Boolean.TRUE);
      }

      return initialMap;
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

   public MarginInfo makeMarginInfo(Element e) {
      float top = 0.0F;
      float right = 0.0F;
      float bottom = 0.0F;
      float left = 0.0F;
      String s = e.getAttribute("margin");

      float indent;
      try {
         if (s.length() != 0) {
            indent = Float.parseFloat(s);
            left = indent;
            bottom = indent;
            right = indent;
            top = indent;
         }
      } catch (NumberFormatException var17) {
      }

      s = e.getAttribute("top-margin");

      try {
         if (s.length() != 0) {
            indent = Float.parseFloat(s);
            top = indent;
         }
      } catch (NumberFormatException var16) {
      }

      s = e.getAttribute("right-margin");

      try {
         if (s.length() != 0) {
            indent = Float.parseFloat(s);
            right = indent;
         }
      } catch (NumberFormatException var15) {
      }

      s = e.getAttribute("bottom-margin");

      try {
         if (s.length() != 0) {
            indent = Float.parseFloat(s);
            bottom = indent;
         }
      } catch (NumberFormatException var14) {
      }

      s = e.getAttribute("left-margin");

      try {
         if (s.length() != 0) {
            indent = Float.parseFloat(s);
            left = indent;
         }
      } catch (NumberFormatException var13) {
      }

      indent = 0.0F;
      s = e.getAttribute("indent");

      try {
         if (s.length() != 0) {
            float f = Float.parseFloat(s);
            indent = f;
         }
      } catch (NumberFormatException var12) {
      }

      int justification = 0;
      s = e.getAttribute("justification");

      try {
         if (s.length() != 0) {
            if ("start".equals(s)) {
               justification = 0;
            } else if ("middle".equals(s)) {
               justification = 1;
            } else if ("end".equals(s)) {
               justification = 2;
            } else if ("full".equals(s)) {
               justification = 3;
            }
         }
      } catch (NumberFormatException var11) {
      }

      String ln = e.getLocalName();
      boolean rgnBr = ln.equals("flowRegionBreak");
      return new MarginInfo(top, right, bottom, left, indent, justification, rgnBr);
   }

   static {
      FLOW_PARAGRAPH = GVTAttributedCharacterIterator.TextAttribute.FLOW_PARAGRAPH;
      FLOW_EMPTY_PARAGRAPH = GVTAttributedCharacterIterator.TextAttribute.FLOW_EMPTY_PARAGRAPH;
      FLOW_LINE_BREAK = GVTAttributedCharacterIterator.TextAttribute.FLOW_LINE_BREAK;
      FLOW_REGIONS = GVTAttributedCharacterIterator.TextAttribute.FLOW_REGIONS;
      PREFORMATTED = GVTAttributedCharacterIterator.TextAttribute.PREFORMATTED;
      TEXTPATH = GVTAttributedCharacterIterator.TextAttribute.TEXTPATH;
      ANCHOR_TYPE = GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE;
      LETTER_SPACING = GVTAttributedCharacterIterator.TextAttribute.LETTER_SPACING;
      WORD_SPACING = GVTAttributedCharacterIterator.TextAttribute.WORD_SPACING;
      KERNING = GVTAttributedCharacterIterator.TextAttribute.KERNING;
   }

   protected class BatikFlowContentBridge extends SVGTextElementBridge.AbstractTextChildTextContent {
      public BatikFlowContentBridge(BridgeContext ctx, SVGTextElementBridge parent, Element e) {
         super(ctx, parent, e);
      }
   }

   public static class LineBreakInfo {
      int breakIdx;
      float lineAdvAdj;
      boolean relative;

      public LineBreakInfo(int breakIdx, float lineAdvAdj, boolean relative) {
         this.breakIdx = breakIdx;
         this.lineAdvAdj = lineAdvAdj;
         this.relative = relative;
      }

      public int getBreakIdx() {
         return this.breakIdx;
      }

      public boolean isRelative() {
         return this.relative;
      }

      public float getLineAdvAdj() {
         return this.lineAdvAdj;
      }
   }
}
