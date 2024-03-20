package org.apache.batik.css.engine;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.batik.css.engine.sac.CSSConditionFactory;
import org.apache.batik.css.engine.sac.CSSSelectorFactory;
import org.apache.batik.css.engine.sac.ExtendedSelector;
import org.apache.batik.css.engine.value.ComputedValue;
import org.apache.batik.css.engine.value.InheritValue;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.util.ParsedURL;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

public abstract class CSSEngine {
   protected CSSEngineUserAgent userAgent;
   protected CSSContext cssContext;
   protected Document document;
   protected ParsedURL documentURI;
   protected boolean isCSSNavigableDocument;
   protected StringIntMap indexes;
   protected StringIntMap shorthandIndexes;
   protected ValueManager[] valueManagers;
   protected ShorthandManager[] shorthandManagers;
   protected ExtendedParser parser;
   protected String[] pseudoElementNames;
   protected int fontSizeIndex = -1;
   protected int lineHeightIndex = -1;
   protected int colorIndex = -1;
   protected StyleSheet userAgentStyleSheet;
   protected StyleSheet userStyleSheet;
   protected SACMediaList media;
   protected List styleSheetNodes;
   protected List fontFaces = new LinkedList();
   protected String styleNamespaceURI;
   protected String styleLocalName;
   protected String classNamespaceURI;
   protected String classLocalName;
   protected Set nonCSSPresentationalHints;
   protected String nonCSSPresentationalHintsNamespaceURI;
   protected StyleDeclarationDocumentHandler styleDeclarationDocumentHandler = new StyleDeclarationDocumentHandler();
   protected StyleDeclarationUpdateHandler styleDeclarationUpdateHandler;
   protected StyleSheetDocumentHandler styleSheetDocumentHandler = new StyleSheetDocumentHandler();
   protected StyleDeclarationBuilder styleDeclarationBuilder = new StyleDeclarationBuilder();
   protected CSSStylableElement element;
   protected ParsedURL cssBaseURI;
   protected String alternateStyleSheet;
   protected CSSNavigableDocumentHandler cssNavigableDocumentListener;
   protected EventListener domAttrModifiedListener;
   protected EventListener domNodeInsertedListener;
   protected EventListener domNodeRemovedListener;
   protected EventListener domSubtreeModifiedListener;
   protected EventListener domCharacterDataModifiedListener;
   protected boolean styleSheetRemoved;
   protected Node removedStylableElementSibling;
   protected List listeners = Collections.synchronizedList(new LinkedList());
   protected Set selectorAttributes;
   protected final int[] ALL_PROPERTIES;
   protected CSSConditionFactory cssConditionFactory;
   protected static final CSSEngineListener[] LISTENER_ARRAY = new CSSEngineListener[0];

   public static Node getCSSParentNode(Node n) {
      return n instanceof CSSNavigableNode ? ((CSSNavigableNode)n).getCSSParentNode() : n.getParentNode();
   }

   protected static Node getCSSFirstChild(Node n) {
      return n instanceof CSSNavigableNode ? ((CSSNavigableNode)n).getCSSFirstChild() : n.getFirstChild();
   }

   protected static Node getCSSNextSibling(Node n) {
      return n instanceof CSSNavigableNode ? ((CSSNavigableNode)n).getCSSNextSibling() : n.getNextSibling();
   }

   protected static Node getCSSPreviousSibling(Node n) {
      return n instanceof CSSNavigableNode ? ((CSSNavigableNode)n).getCSSPreviousSibling() : n.getPreviousSibling();
   }

   public static CSSStylableElement getParentCSSStylableElement(Element elt) {
      for(Node n = getCSSParentNode(elt); n != null; n = getCSSParentNode(n)) {
         if (n instanceof CSSStylableElement) {
            return (CSSStylableElement)n;
         }
      }

      return null;
   }

   protected CSSEngine(Document doc, ParsedURL uri, ExtendedParser p, ValueManager[] vm, ShorthandManager[] sm, String[] pe, String sns, String sln, String cns, String cln, boolean hints, String hintsNS, CSSContext ctx) {
      this.document = doc;
      this.documentURI = uri;
      this.parser = p;
      this.pseudoElementNames = pe;
      this.styleNamespaceURI = sns;
      this.styleLocalName = sln;
      this.classNamespaceURI = cns;
      this.classLocalName = cln;
      this.cssContext = ctx;
      this.isCSSNavigableDocument = doc instanceof CSSNavigableDocument;
      this.cssConditionFactory = new CSSConditionFactory(cns, cln, (String)null, "id");
      int len = vm.length;
      this.indexes = new StringIntMap(len);
      this.valueManagers = vm;

      int i;
      String pn;
      for(i = len - 1; i >= 0; --i) {
         pn = vm[i].getPropertyName();
         this.indexes.put(pn, i);
         if (this.fontSizeIndex == -1 && pn.equals("font-size")) {
            this.fontSizeIndex = i;
         }

         if (this.lineHeightIndex == -1 && pn.equals("line-height")) {
            this.lineHeightIndex = i;
         }

         if (this.colorIndex == -1 && pn.equals("color")) {
            this.colorIndex = i;
         }
      }

      len = sm.length;
      this.shorthandIndexes = new StringIntMap(len);
      this.shorthandManagers = sm;

      for(i = len - 1; i >= 0; --i) {
         this.shorthandIndexes.put(sm[i].getPropertyName(), i);
      }

      if (hints) {
         this.nonCSSPresentationalHints = new HashSet(vm.length + sm.length);
         this.nonCSSPresentationalHintsNamespaceURI = hintsNS;
         len = vm.length;

         for(i = 0; i < len; ++i) {
            pn = vm[i].getPropertyName();
            this.nonCSSPresentationalHints.add(pn);
         }

         len = sm.length;

         for(i = 0; i < len; ++i) {
            pn = sm[i].getPropertyName();
            this.nonCSSPresentationalHints.add(pn);
         }
      }

      if (this.cssContext.isDynamic() && this.document instanceof EventTarget) {
         this.addEventListeners((EventTarget)this.document);
         this.styleDeclarationUpdateHandler = new StyleDeclarationUpdateHandler();
      }

      this.ALL_PROPERTIES = new int[this.getNumberOfProperties()];

      for(i = this.getNumberOfProperties() - 1; i >= 0; this.ALL_PROPERTIES[i] = i--) {
      }

   }

   protected void addEventListeners(EventTarget doc) {
      if (this.isCSSNavigableDocument) {
         this.cssNavigableDocumentListener = new CSSNavigableDocumentHandler();
         CSSNavigableDocument cnd = (CSSNavigableDocument)doc;
         cnd.addCSSNavigableDocumentListener(this.cssNavigableDocumentListener);
      } else {
         this.domAttrModifiedListener = new DOMAttrModifiedListener();
         doc.addEventListener("DOMAttrModified", this.domAttrModifiedListener, false);
         this.domNodeInsertedListener = new DOMNodeInsertedListener();
         doc.addEventListener("DOMNodeInserted", this.domNodeInsertedListener, false);
         this.domNodeRemovedListener = new DOMNodeRemovedListener();
         doc.addEventListener("DOMNodeRemoved", this.domNodeRemovedListener, false);
         this.domSubtreeModifiedListener = new DOMSubtreeModifiedListener();
         doc.addEventListener("DOMSubtreeModified", this.domSubtreeModifiedListener, false);
         this.domCharacterDataModifiedListener = new DOMCharacterDataModifiedListener();
         doc.addEventListener("DOMCharacterDataModified", this.domCharacterDataModifiedListener, false);
      }

   }

   protected void removeEventListeners(EventTarget doc) {
      if (this.isCSSNavigableDocument) {
         CSSNavigableDocument cnd = (CSSNavigableDocument)doc;
         cnd.removeCSSNavigableDocumentListener(this.cssNavigableDocumentListener);
      } else {
         doc.removeEventListener("DOMAttrModified", this.domAttrModifiedListener, false);
         doc.removeEventListener("DOMNodeInserted", this.domNodeInsertedListener, false);
         doc.removeEventListener("DOMNodeRemoved", this.domNodeRemovedListener, false);
         doc.removeEventListener("DOMSubtreeModified", this.domSubtreeModifiedListener, false);
         doc.removeEventListener("DOMCharacterDataModified", this.domCharacterDataModifiedListener, false);
      }

   }

   public void dispose() {
      this.setCSSEngineUserAgent((CSSEngineUserAgent)null);
      this.disposeStyleMaps(this.document.getDocumentElement());
      if (this.document instanceof EventTarget) {
         this.removeEventListeners((EventTarget)this.document);
      }

   }

   protected void disposeStyleMaps(Node node) {
      if (node instanceof CSSStylableElement) {
         ((CSSStylableElement)node).setComputedStyleMap((String)null, (StyleMap)null);
      }

      for(Node n = getCSSFirstChild(node); n != null; n = getCSSNextSibling(n)) {
         if (n.getNodeType() == 1) {
            this.disposeStyleMaps(n);
         }
      }

   }

   public CSSContext getCSSContext() {
      return this.cssContext;
   }

   public Document getDocument() {
      return this.document;
   }

   public int getFontSizeIndex() {
      return this.fontSizeIndex;
   }

   public int getLineHeightIndex() {
      return this.lineHeightIndex;
   }

   public int getColorIndex() {
      return this.colorIndex;
   }

   public int getNumberOfProperties() {
      return this.valueManagers.length;
   }

   public int getPropertyIndex(String name) {
      return this.indexes.get(name);
   }

   public int getShorthandIndex(String name) {
      return this.shorthandIndexes.get(name);
   }

   public String getPropertyName(int idx) {
      return this.valueManagers[idx].getPropertyName();
   }

   public void setCSSEngineUserAgent(CSSEngineUserAgent userAgent) {
      this.userAgent = userAgent;
   }

   public CSSEngineUserAgent getCSSEngineUserAgent() {
      return this.userAgent;
   }

   public void setUserAgentStyleSheet(StyleSheet ss) {
      this.userAgentStyleSheet = ss;
   }

   public void setUserStyleSheet(StyleSheet ss) {
      this.userStyleSheet = ss;
   }

   public ValueManager[] getValueManagers() {
      return this.valueManagers;
   }

   public ShorthandManager[] getShorthandManagers() {
      return this.shorthandManagers;
   }

   public List getFontFaces() {
      return this.fontFaces;
   }

   public void setMedia(String str) {
      try {
         this.media = this.parser.parseMedia(str);
      } catch (Exception var5) {
         String m = var5.getMessage();
         if (m == null) {
            m = "";
         }

         String s = Messages.formatMessage("media.error", new Object[]{str, m});
         throw new DOMException((short)12, s);
      }
   }

   public void setAlternateStyleSheet(String str) {
      this.alternateStyleSheet = str;
   }

   public void importCascadedStyleMaps(Element src, CSSEngine srceng, Element dest) {
      if (src instanceof CSSStylableElement) {
         CSSStylableElement csrc = (CSSStylableElement)src;
         CSSStylableElement cdest = (CSSStylableElement)dest;
         StyleMap sm = srceng.getCascadedStyleMap(csrc, (String)null);
         sm.setFixedCascadedStyle(true);
         cdest.setComputedStyleMap((String)null, sm);
         if (this.pseudoElementNames != null) {
            int len = this.pseudoElementNames.length;
            String[] var8 = this.pseudoElementNames;
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
               String pe = var8[var10];
               sm = srceng.getCascadedStyleMap(csrc, pe);
               cdest.setComputedStyleMap(pe, sm);
            }
         }
      }

      Node dn = getCSSFirstChild(dest);

      for(Node sn = getCSSFirstChild(src); dn != null; sn = getCSSNextSibling(sn)) {
         if (sn.getNodeType() == 1) {
            this.importCascadedStyleMaps((Element)sn, srceng, (Element)dn);
         }

         dn = getCSSNextSibling(dn);
      }

   }

   public ParsedURL getCSSBaseURI() {
      if (this.cssBaseURI == null) {
         this.cssBaseURI = this.element.getCSSBase();
      }

      return this.cssBaseURI;
   }

   public StyleMap getCascadedStyleMap(CSSStylableElement elt, String pseudo) {
      int props = this.getNumberOfProperties();
      final StyleMap result = new StyleMap(props);
      ArrayList rules;
      if (this.userAgentStyleSheet != null) {
         rules = new ArrayList();
         this.addMatchingRules(rules, this.userAgentStyleSheet, elt, pseudo);
         this.addRules(elt, pseudo, result, rules, (short)0);
      }

      if (this.userStyleSheet != null) {
         rules = new ArrayList();
         this.addMatchingRules(rules, this.userStyleSheet, elt, pseudo);
         this.addRules(elt, pseudo, result, rules, (short)8192);
      }

      this.element = elt;

      try {
         int len;
         String m;
         String s;
         if (this.nonCSSPresentationalHints != null) {
            ShorthandManager.PropertyHandler ph = new ShorthandManager.PropertyHandler() {
               public void property(String pname, LexicalUnit lu, boolean important) {
                  int idx = CSSEngine.this.getPropertyIndex(pname);
                  if (idx != -1) {
                     ValueManager vm = CSSEngine.this.valueManagers[idx];
                     Value v = vm.createValue(lu, CSSEngine.this);
                     CSSEngine.this.putAuthorProperty(result, idx, v, important, (short)16384);
                  } else {
                     idx = CSSEngine.this.getShorthandIndex(pname);
                     if (idx != -1) {
                        CSSEngine.this.shorthandManagers[idx].setValues(CSSEngine.this, this, lu, important);
                     }
                  }
               }
            };
            NamedNodeMap attrs = elt.getAttributes();
            len = attrs.getLength();

            for(int i = 0; i < len; ++i) {
               Node attr = attrs.item(i);
               m = attr.getNodeName();
               if (this.nonCSSPresentationalHints.contains(m)) {
                  try {
                     LexicalUnit lu = this.parser.parsePropertyValue(attr.getNodeValue());
                     ph.property(m, lu, false);
                  } catch (Exception var20) {
                     s = var20.getMessage();
                     if (s == null) {
                        s = "";
                     }

                     String u = this.documentURI == null ? "<unknown>" : this.documentURI.toString();
                     String s = Messages.formatMessage("property.syntax.error.at", new Object[]{u, m, attr.getNodeValue(), s});
                     DOMException de = new DOMException((short)12, s);
                     if (this.userAgent == null) {
                        throw de;
                     }

                     this.userAgent.displayError(de);
                  }
               }
            }
         }

         CSSEngine eng = this.cssContext.getCSSEngineForElement(elt);
         List snodes = eng.getStyleSheetNodes();
         len = snodes.size();
         if (len > 0) {
            ArrayList rules = new ArrayList();
            Iterator var28 = snodes.iterator();

            label257:
            while(true) {
               StyleSheet ss;
               do {
                  do {
                     if (!var28.hasNext()) {
                        this.addRules(elt, pseudo, result, rules, (short)24576);
                        break label257;
                     }

                     Object snode = var28.next();
                     CSSStyleSheetNode ssn = (CSSStyleSheetNode)snode;
                     ss = ssn.getCSSStyleSheet();
                  } while(ss == null);
               } while(ss.isAlternate() && ss.getTitle() != null && !ss.getTitle().equals(this.alternateStyleSheet));

               if (this.mediaMatch(ss.getMedia())) {
                  this.addMatchingRules(rules, ss, elt, pseudo);
               }
            }
         }

         if (this.styleLocalName != null) {
            String style = elt.getAttributeNS(this.styleNamespaceURI, this.styleLocalName);
            if (style.length() > 0) {
               try {
                  this.parser.setSelectorFactory(CSSSelectorFactory.INSTANCE);
                  this.parser.setConditionFactory(this.cssConditionFactory);
                  this.styleDeclarationDocumentHandler.styleMap = result;
                  this.parser.setDocumentHandler(this.styleDeclarationDocumentHandler);
                  this.parser.parseStyleDeclaration(style);
                  this.styleDeclarationDocumentHandler.styleMap = null;
               } catch (Exception var21) {
                  m = var21.getMessage();
                  if (m == null) {
                     m = var21.getClass().getName();
                  }

                  String u = this.documentURI == null ? "<unknown>" : this.documentURI.toString();
                  s = Messages.formatMessage("style.syntax.error.at", new Object[]{u, this.styleLocalName, style, m});
                  DOMException de = new DOMException((short)12, s);
                  if (this.userAgent == null) {
                     throw de;
                  }

                  this.userAgent.displayError(de);
               }
            }
         }

         StyleDeclarationProvider p = elt.getOverrideStyleDeclarationProvider();
         if (p != null) {
            StyleDeclaration over = p.getStyleDeclaration();
            if (over != null) {
               int ol = over.size();

               for(int i = 0; i < ol; ++i) {
                  int idx = over.getIndex(i);
                  Value value = over.getValue(i);
                  boolean important = over.getPriority(i);
                  if (!result.isImportant(idx) || important) {
                     result.putValue(idx, value);
                     result.putImportant(idx, important);
                     result.putOrigin(idx, (short)-24576);
                  }
               }
            }
         }
      } finally {
         this.element = null;
         this.cssBaseURI = null;
      }

      return result;
   }

   public Value getComputedStyle(CSSStylableElement elt, String pseudo, int propidx) {
      StyleMap sm = elt.getComputedStyleMap(pseudo);
      if (sm == null) {
         sm = this.getCascadedStyleMap(elt, pseudo);
         elt.setComputedStyleMap(pseudo, sm);
      }

      Value value = sm.getValue(propidx);
      if (sm.isComputed(propidx)) {
         return value;
      } else {
         Value result = value;
         ValueManager vm = this.valueManagers[propidx];
         CSSStylableElement p = getParentCSSStylableElement(elt);
         if (value == null) {
            if (p == null || !vm.isInheritedProperty()) {
               result = vm.getDefaultValue();
            }
         } else if (p != null && value == InheritValue.INSTANCE) {
            result = null;
         }

         Object result;
         if (result == null) {
            result = this.getComputedStyle(p, (String)null, propidx);
            sm.putParentRelative(propidx, true);
            sm.putInherited(propidx, true);
         } else {
            result = vm.computeValue(elt, pseudo, this, propidx, sm, result);
         }

         if (value == null) {
            sm.putValue(propidx, (Value)result);
            sm.putNullCascaded(propidx, true);
         } else if (result != value) {
            ComputedValue cv = new ComputedValue(value);
            cv.setComputedValue((Value)result);
            sm.putValue(propidx, cv);
            result = cv;
         }

         sm.putComputed(propidx, true);
         return (Value)result;
      }
   }

   public List getStyleSheetNodes() {
      if (this.styleSheetNodes == null) {
         this.styleSheetNodes = new ArrayList();
         this.selectorAttributes = new HashSet();
         this.findStyleSheetNodes(this.document);
         int len = this.styleSheetNodes.size();
         Iterator var2 = this.styleSheetNodes.iterator();

         while(var2.hasNext()) {
            Object styleSheetNode = var2.next();
            CSSStyleSheetNode ssn = (CSSStyleSheetNode)styleSheetNode;
            StyleSheet ss = ssn.getCSSStyleSheet();
            if (ss != null) {
               this.findSelectorAttributes(this.selectorAttributes, ss);
            }
         }
      }

      return this.styleSheetNodes;
   }

   protected void findStyleSheetNodes(Node n) {
      if (n instanceof CSSStyleSheetNode) {
         this.styleSheetNodes.add(n);
      }

      for(Node nd = getCSSFirstChild(n); nd != null; nd = getCSSNextSibling(nd)) {
         this.findStyleSheetNodes(nd);
      }

   }

   protected void findSelectorAttributes(Set attrs, StyleSheet ss) {
      int len = ss.getSize();

      label27:
      for(int i = 0; i < len; ++i) {
         Rule r = ss.getRule(i);
         switch (r.getType()) {
            case 0:
               StyleRule style = (StyleRule)r;
               SelectorList sl = style.getSelectorList();
               int slen = sl.getLength();
               int j = 0;

               while(true) {
                  if (j >= slen) {
                     continue label27;
                  }

                  ExtendedSelector s = (ExtendedSelector)sl.item(j);
                  s.fillAttributeSet(attrs);
                  ++j;
               }
            case 1:
            case 2:
               MediaRule mr = (MediaRule)r;
               if (this.mediaMatch(mr.getMediaList())) {
                  this.findSelectorAttributes(attrs, mr);
               }
         }
      }

   }

   public void setMainProperties(CSSStylableElement elt, final MainPropertyReceiver dst, String pname, String value, boolean important) {
      try {
         this.element = elt;
         LexicalUnit lu = this.parser.parsePropertyValue(value);
         ShorthandManager.PropertyHandler ph = new ShorthandManager.PropertyHandler() {
            public void property(String pname, LexicalUnit lu, boolean important) {
               int idx = CSSEngine.this.getPropertyIndex(pname);
               if (idx != -1) {
                  ValueManager vm = CSSEngine.this.valueManagers[idx];
                  Value v = vm.createValue(lu, CSSEngine.this);
                  dst.setMainProperty(pname, v, important);
               } else {
                  idx = CSSEngine.this.getShorthandIndex(pname);
                  if (idx != -1) {
                     CSSEngine.this.shorthandManagers[idx].setValues(CSSEngine.this, this, lu, important);
                  }
               }
            }
         };
         ph.property(pname, lu, important);
      } catch (Exception var14) {
         String m = var14.getMessage();
         if (m == null) {
            m = "";
         }

         String u = this.documentURI == null ? "<unknown>" : this.documentURI.toString();
         String s = Messages.formatMessage("property.syntax.error.at", new Object[]{u, pname, value, m});
         DOMException de = new DOMException((short)12, s);
         if (this.userAgent == null) {
            throw de;
         }

         this.userAgent.displayError(de);
      } finally {
         this.element = null;
         this.cssBaseURI = null;
      }

   }

   public Value parsePropertyValue(CSSStylableElement elt, String prop, String value) {
      int idx = this.getPropertyIndex(prop);
      if (idx == -1) {
         return null;
      } else {
         ValueManager vm = this.valueManagers[idx];

         try {
            String m;
            try {
               this.element = elt;
               LexicalUnit lu = this.parser.parsePropertyValue(value);
               Value var16 = vm.createValue(lu, this);
               return var16;
            } catch (Exception var14) {
               m = var14.getMessage();
               if (m == null) {
                  m = "";
               }
            }

            String u = this.documentURI == null ? "<unknown>" : this.documentURI.toString();
            String s = Messages.formatMessage("property.syntax.error.at", new Object[]{u, prop, value, m});
            DOMException de = new DOMException((short)12, s);
            if (this.userAgent == null) {
               throw de;
            }

            this.userAgent.displayError(de);
         } finally {
            this.element = null;
            this.cssBaseURI = null;
         }

         return vm.getDefaultValue();
      }
   }

   public StyleDeclaration parseStyleDeclaration(CSSStylableElement elt, String value) {
      this.styleDeclarationBuilder.styleDeclaration = new StyleDeclaration();

      try {
         this.element = elt;
         this.parser.setSelectorFactory(CSSSelectorFactory.INSTANCE);
         this.parser.setConditionFactory(this.cssConditionFactory);
         this.parser.setDocumentHandler(this.styleDeclarationBuilder);
         this.parser.parseStyleDeclaration(value);
      } catch (Exception var11) {
         String m = var11.getMessage();
         if (m == null) {
            m = "";
         }

         String u = this.documentURI == null ? "<unknown>" : this.documentURI.toString();
         String s = Messages.formatMessage("syntax.error.at", new Object[]{u, m});
         DOMException de = new DOMException((short)12, s);
         if (this.userAgent == null) {
            throw de;
         }

         this.userAgent.displayError(de);
      } finally {
         this.element = null;
         this.cssBaseURI = null;
      }

      return this.styleDeclarationBuilder.styleDeclaration;
   }

   public StyleSheet parseStyleSheet(ParsedURL uri, String media) throws DOMException {
      StyleSheet ss = new StyleSheet();

      try {
         ss.setMedia(this.parser.parseMedia(media));
      } catch (Exception var9) {
         String m = var9.getMessage();
         if (m == null) {
            m = "";
         }

         String u = this.documentURI == null ? "<unknown>" : this.documentURI.toString();
         String s = Messages.formatMessage("syntax.error.at", new Object[]{u, m});
         DOMException de = new DOMException((short)12, s);
         if (this.userAgent == null) {
            throw de;
         }

         this.userAgent.displayError(de);
         return ss;
      }

      this.parseStyleSheet(ss, uri);
      return ss;
   }

   public StyleSheet parseStyleSheet(InputSource is, ParsedURL uri, String media) throws DOMException {
      StyleSheet ss = new StyleSheet();

      try {
         ss.setMedia(this.parser.parseMedia(media));
         this.parseStyleSheet(ss, is, uri);
      } catch (Exception var10) {
         String m = var10.getMessage();
         if (m == null) {
            m = "";
         }

         String u = this.documentURI == null ? "<unknown>" : this.documentURI.toString();
         String s = Messages.formatMessage("syntax.error.at", new Object[]{u, m});
         DOMException de = new DOMException((short)12, s);
         if (this.userAgent == null) {
            throw de;
         }

         this.userAgent.displayError(de);
      }

      return ss;
   }

   public void parseStyleSheet(StyleSheet ss, ParsedURL uri) throws DOMException {
      if (uri == null) {
         String s = Messages.formatMessage("syntax.error.at", new Object[]{"Null Document reference", ""});
         DOMException de = new DOMException((short)12, s);
         if (this.userAgent == null) {
            throw de;
         } else {
            this.userAgent.displayError(de);
         }
      } else {
         try {
            this.cssContext.checkLoadExternalResource(uri, this.documentURI);
            this.parseStyleSheet(ss, new InputSource(uri.toString()), uri);
         } catch (SecurityException var7) {
            throw var7;
         } catch (Exception var8) {
            String m = var8.getMessage();
            if (m == null) {
               m = var8.getClass().getName();
            }

            String s = Messages.formatMessage("syntax.error.at", new Object[]{uri.toString(), m});
            DOMException de = new DOMException((short)12, s);
            if (this.userAgent == null) {
               throw de;
            }

            this.userAgent.displayError(de);
         }

      }
   }

   public StyleSheet parseStyleSheet(String rules, ParsedURL uri, String media) throws DOMException {
      StyleSheet ss = new StyleSheet();

      try {
         ss.setMedia(this.parser.parseMedia(media));
      } catch (Exception var10) {
         String m = var10.getMessage();
         if (m == null) {
            m = "";
         }

         String u = this.documentURI == null ? "<unknown>" : this.documentURI.toString();
         String s = Messages.formatMessage("syntax.error.at", new Object[]{u, m});
         DOMException de = new DOMException((short)12, s);
         if (this.userAgent == null) {
            throw de;
         }

         this.userAgent.displayError(de);
         return ss;
      }

      this.parseStyleSheet(ss, rules, uri);
      return ss;
   }

   public void parseStyleSheet(StyleSheet ss, String rules, ParsedURL uri) throws DOMException {
      try {
         this.parseStyleSheet(ss, new InputSource(new StringReader(rules)), uri);
      } catch (Exception var8) {
         String m = var8.getMessage();
         if (m == null) {
            m = "";
         }

         String s = Messages.formatMessage("stylesheet.syntax.error", new Object[]{uri.toString(), rules, m});
         DOMException de = new DOMException((short)12, s);
         if (this.userAgent == null) {
            throw de;
         }

         this.userAgent.displayError(de);
      }

   }

   protected void parseStyleSheet(StyleSheet ss, InputSource is, ParsedURL uri) throws IOException {
      this.parser.setSelectorFactory(CSSSelectorFactory.INSTANCE);
      this.parser.setConditionFactory(this.cssConditionFactory);

      try {
         this.cssBaseURI = uri;
         this.styleSheetDocumentHandler.styleSheet = ss;
         this.parser.setDocumentHandler(this.styleSheetDocumentHandler);
         this.parser.parseStyleSheet(is);
         int len = ss.getSize();

         for(int i = 0; i < len; ++i) {
            Rule r = ss.getRule(i);
            if (r.getType() != 2) {
               break;
            }

            ImportRule ir = (ImportRule)r;
            this.parseStyleSheet((StyleSheet)ir, (ParsedURL)ir.getURI());
         }
      } finally {
         this.cssBaseURI = null;
      }

   }

   protected void putAuthorProperty(StyleMap dest, int idx, Value sval, boolean imp, short origin) {
      Value dval = dest.getValue(idx);
      short dorg = dest.getOrigin(idx);
      boolean dimp = dest.isImportant(idx);
      boolean cond = dval == null;
      if (!cond) {
         switch (dorg) {
            case -24576:
               cond = false;
               break;
            case 8192:
               cond = !dimp;
               break;
            case 24576:
               cond = !dimp || imp;
               break;
            default:
               cond = true;
         }
      }

      if (cond) {
         dest.putValue(idx, sval);
         dest.putImportant(idx, imp);
         dest.putOrigin(idx, origin);
      }

   }

   protected void addMatchingRules(List rules, StyleSheet ss, Element elt, String pseudo) {
      int len = ss.getSize();

      label31:
      for(int i = 0; i < len; ++i) {
         Rule r = ss.getRule(i);
         switch (r.getType()) {
            case 0:
               StyleRule style = (StyleRule)r;
               SelectorList sl = style.getSelectorList();
               int slen = sl.getLength();
               int j = 0;

               while(true) {
                  if (j >= slen) {
                     continue label31;
                  }

                  ExtendedSelector s = (ExtendedSelector)sl.item(j);
                  if (s.match(elt, pseudo)) {
                     rules.add(style);
                  }

                  ++j;
               }
            case 1:
            case 2:
               MediaRule mr = (MediaRule)r;
               if (this.mediaMatch(mr.getMediaList())) {
                  this.addMatchingRules(rules, mr, elt, pseudo);
               }
         }
      }

   }

   protected void addRules(Element elt, String pseudo, StyleMap sm, ArrayList rules, short origin) {
      this.sortRules(rules, elt, pseudo);
      int rlen = rules.size();
      Iterator var7;
      Object rule;
      StyleRule sr;
      StyleDeclaration sd;
      int len;
      int i;
      if (origin == 24576) {
         var7 = rules.iterator();

         while(var7.hasNext()) {
            rule = var7.next();
            sr = (StyleRule)rule;
            sd = sr.getStyleDeclaration();
            len = sd.size();

            for(i = 0; i < len; ++i) {
               this.putAuthorProperty(sm, sd.getIndex(i), sd.getValue(i), sd.getPriority(i), origin);
            }
         }
      } else {
         var7 = rules.iterator();

         while(var7.hasNext()) {
            rule = var7.next();
            sr = (StyleRule)rule;
            sd = sr.getStyleDeclaration();
            len = sd.size();

            for(i = 0; i < len; ++i) {
               int idx = sd.getIndex(i);
               sm.putValue(idx, sd.getValue(i));
               sm.putImportant(idx, sd.getPriority(i));
               sm.putOrigin(idx, origin);
            }
         }
      }

   }

   protected void sortRules(ArrayList rules, Element elt, String pseudo) {
      int len = rules.size();
      int[] specificities = new int[len];

      int i;
      int spec;
      for(i = 0; i < len; ++i) {
         StyleRule r = (StyleRule)rules.get(i);
         SelectorList sl = r.getSelectorList();
         spec = 0;
         int slen = sl.getLength();

         for(int k = 0; k < slen; ++k) {
            ExtendedSelector s = (ExtendedSelector)sl.item(k);
            if (s.match(elt, pseudo)) {
               int sp = s.getSpecificity();
               if (sp > spec) {
                  spec = sp;
               }
            }
         }

         specificities[i] = spec;
      }

      for(i = 1; i < len; ++i) {
         Object rule = rules.get(i);
         int spec = specificities[i];

         for(spec = i - 1; spec >= 0 && specificities[spec] > spec; --spec) {
            rules.set(spec + 1, rules.get(spec));
            specificities[spec + 1] = specificities[spec];
         }

         rules.set(spec + 1, rule);
         specificities[spec + 1] = spec;
      }

   }

   protected boolean mediaMatch(SACMediaList ml) {
      if (this.media != null && ml != null && this.media.getLength() != 0 && ml.getLength() != 0) {
         for(int i = 0; i < ml.getLength(); ++i) {
            if (ml.item(i).equalsIgnoreCase("all")) {
               return true;
            }

            for(int j = 0; j < this.media.getLength(); ++j) {
               if (this.media.item(j).equalsIgnoreCase("all") || ml.item(i).equalsIgnoreCase(this.media.item(j))) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public void addCSSEngineListener(CSSEngineListener l) {
      this.listeners.add(l);
   }

   public void removeCSSEngineListener(CSSEngineListener l) {
      this.listeners.remove(l);
   }

   protected void firePropertiesChangedEvent(Element target, int[] props) {
      CSSEngineListener[] ll = (CSSEngineListener[])((CSSEngineListener[])this.listeners.toArray(LISTENER_ARRAY));
      int len = ll.length;
      if (len > 0) {
         CSSEngineEvent evt = new CSSEngineEvent(this, target, props);
         CSSEngineListener[] var6 = ll;
         int var7 = ll.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            CSSEngineListener aLl = var6[var8];
            aLl.propertiesChanged(evt);
         }
      }

   }

   protected void inlineStyleAttributeUpdated(CSSStylableElement elt, StyleMap style, short attrChange, String prevValue, String newValue) {
      boolean[] updated = this.styleDeclarationUpdateHandler.updatedProperties;

      for(int i = this.getNumberOfProperties() - 1; i >= 0; --i) {
         updated[i] = false;
      }

      switch (attrChange) {
         case 1:
         case 2:
            if (newValue.length() > 0) {
               this.element = elt;

               try {
                  this.parser.setSelectorFactory(CSSSelectorFactory.INSTANCE);
                  this.parser.setConditionFactory(this.cssConditionFactory);
                  this.styleDeclarationUpdateHandler.styleMap = style;
                  this.parser.setDocumentHandler(this.styleDeclarationUpdateHandler);
                  this.parser.parseStyleDeclaration(newValue);
                  this.styleDeclarationUpdateHandler.styleMap = null;
               } catch (Exception var16) {
                  String m = var16.getMessage();
                  if (m == null) {
                     m = "";
                  }

                  String u = this.documentURI == null ? "<unknown>" : this.documentURI.toString();
                  String s = Messages.formatMessage("style.syntax.error.at", new Object[]{u, this.styleLocalName, newValue, m});
                  DOMException de = new DOMException((short)12, s);
                  if (this.userAgent == null) {
                     throw de;
                  } else {
                     this.userAgent.displayError(de);
                  }
               } finally {
                  this.element = null;
                  this.cssBaseURI = null;
               }
            }
         case 3:
            boolean removed = false;
            int count;
            if (prevValue != null && prevValue.length() > 0) {
               for(count = this.getNumberOfProperties() - 1; count >= 0; --count) {
                  if (style.isComputed(count) && !updated[count]) {
                     short origin = style.getOrigin(count);
                     if (origin >= Short.MIN_VALUE) {
                        removed = true;
                        updated[count] = true;
                     }
                  }
               }
            }

            if (removed) {
               this.invalidateProperties(elt, (int[])null, updated, true);
            } else {
               count = 0;
               boolean fs = this.fontSizeIndex == -1 ? false : updated[this.fontSizeIndex];
               boolean lh = this.lineHeightIndex == -1 ? false : updated[this.lineHeightIndex];
               boolean cl = this.colorIndex == -1 ? false : updated[this.colorIndex];

               for(int i = this.getNumberOfProperties() - 1; i >= 0; --i) {
                  if (updated[i]) {
                     ++count;
                  } else if (fs && style.isFontSizeRelative(i) || lh && style.isLineHeightRelative(i) || cl && style.isColorRelative(i)) {
                     updated[i] = true;
                     clearComputedValue(style, i);
                     ++count;
                  }
               }

               if (count > 0) {
                  int[] props = new int[count];
                  count = 0;

                  for(int i = this.getNumberOfProperties() - 1; i >= 0; --i) {
                     if (updated[i]) {
                        props[count++] = i;
                     }
                  }

                  this.invalidateProperties(elt, props, (boolean[])null, true);
               }
            }

            return;
         default:
            throw new IllegalStateException("Invalid attrChangeType");
      }
   }

   private static void clearComputedValue(StyleMap style, int n) {
      if (style.isNullCascaded(n)) {
         style.putValue(n, (Value)null);
      } else {
         Value v = style.getValue(n);
         if (v instanceof ComputedValue) {
            ComputedValue cv = (ComputedValue)v;
            v = cv.getCascadedValue();
            style.putValue(n, v);
         }
      }

      style.putComputed(n, false);
   }

   protected void invalidateProperties(Node node, int[] properties, boolean[] updated, boolean recascade) {
      if (node instanceof CSSStylableElement) {
         CSSStylableElement elt = (CSSStylableElement)node;
         StyleMap style = elt.getComputedStyleMap((String)null);
         if (style != null) {
            boolean[] diffs = new boolean[this.getNumberOfProperties()];
            if (updated != null) {
               System.arraycopy(updated, 0, diffs, 0, updated.length);
            }

            int i;
            int property;
            if (properties != null) {
               int[] var8 = properties;
               int var9 = properties.length;

               for(i = 0; i < var9; ++i) {
                  property = var8[i];
                  diffs[property] = true;
               }
            }

            int count = 0;
            if (!recascade) {
               boolean[] var16 = diffs;
               i = diffs.length;

               for(property = 0; property < i; ++property) {
                  boolean diff = var16[property];
                  if (diff) {
                     ++count;
                  }
               }
            } else {
               StyleMap newStyle = this.getCascadedStyleMap(elt, (String)null);
               elt.setComputedStyleMap((String)null, newStyle);

               for(i = 0; i < diffs.length; ++i) {
                  if (diffs[i]) {
                     ++count;
                  } else {
                     Value nv = newStyle.getValue(i);
                     Value ov = null;
                     if (!style.isNullCascaded(i)) {
                        ov = style.getValue(i);
                        if (ov instanceof ComputedValue) {
                           ov = ((ComputedValue)ov).getCascadedValue();
                        }
                     }

                     if (nv != ov) {
                        if (nv != null && ov != null) {
                           if (nv.equals(ov)) {
                              continue;
                           }

                           String ovCssText = ov.getCssText();
                           String nvCssText = nv.getCssText();
                           if (nvCssText == ovCssText || nvCssText != null && nvCssText.equals(ovCssText)) {
                              continue;
                           }
                        }

                        ++count;
                        diffs[i] = true;
                     }
                  }
               }
            }

            int[] props = null;
            if (count != 0) {
               props = new int[count];
               count = 0;

               for(i = 0; i < diffs.length; ++i) {
                  if (diffs[i]) {
                     props[count++] = i;
                  }
               }
            }

            this.propagateChanges(elt, props, recascade);
         }
      }
   }

   protected void propagateChanges(Node node, int[] props, boolean recascade) {
      if (node instanceof CSSStylableElement) {
         CSSStylableElement elt = (CSSStylableElement)node;
         StyleMap style = elt.getComputedStyleMap((String)null);
         int count;
         int i;
         int count;
         int i;
         if (style != null) {
            boolean[] updated = this.styleDeclarationUpdateHandler.updatedProperties;

            for(count = this.getNumberOfProperties() - 1; count >= 0; --count) {
               updated[count] = false;
            }

            if (props != null) {
               for(count = props.length - 1; count >= 0; --count) {
                  i = props[count];
                  updated[i] = true;
               }
            }

            boolean fs = this.fontSizeIndex == -1 ? false : updated[this.fontSizeIndex];
            boolean lh = this.lineHeightIndex == -1 ? false : updated[this.lineHeightIndex];
            boolean cl = this.colorIndex == -1 ? false : updated[this.colorIndex];
            count = 0;

            for(i = this.getNumberOfProperties() - 1; i >= 0; --i) {
               if (updated[i]) {
                  ++count;
               } else if (fs && style.isFontSizeRelative(i) || lh && style.isLineHeightRelative(i) || cl && style.isColorRelative(i)) {
                  updated[i] = true;
                  clearComputedValue(style, i);
                  ++count;
               }
            }

            if (count == 0) {
               props = null;
            } else {
               props = new int[count];
               count = 0;

               for(i = this.getNumberOfProperties() - 1; i >= 0; --i) {
                  if (updated[i]) {
                     props[count++] = i;
                  }
               }

               this.firePropertiesChangedEvent(elt, props);
            }
         }

         int[] inherited = props;
         if (props != null) {
            count = 0;

            for(i = 0; i < props.length; ++i) {
               ValueManager vm = this.valueManagers[props[i]];
               if (vm.isInheritedProperty()) {
                  ++count;
               } else {
                  props[i] = -1;
               }
            }

            if (count == 0) {
               inherited = null;
            } else {
               inherited = new int[count];
               count = 0;
               int[] var16 = props;
               int var17 = props.length;

               for(count = 0; count < var17; ++count) {
                  i = var16[count];
                  if (i != -1) {
                     inherited[count++] = i;
                  }
               }
            }
         }

         for(Node n = getCSSFirstChild(node); n != null; n = getCSSNextSibling(n)) {
            if (n.getNodeType() == 1) {
               this.invalidateProperties(n, inherited, (boolean[])null, recascade);
            }
         }

      }
   }

   protected void nonCSSPresentationalHintUpdated(CSSStylableElement elt, StyleMap style, String property, short attrChange, String newValue) {
      int idx = this.getPropertyIndex(property);
      if (!style.isImportant(idx)) {
         if (style.getOrigin(idx) < 24576) {
            switch (attrChange) {
               case 1:
               case 2:
                  this.element = elt;

                  try {
                     LexicalUnit lu = this.parser.parsePropertyValue(newValue);
                     ValueManager vm = this.valueManagers[idx];
                     Value v = vm.createValue(lu, this);
                     style.putMask(idx, (short)0);
                     style.putValue(idx, v);
                     style.putOrigin(idx, (short)16384);
                  } catch (Exception var16) {
                     String m = var16.getMessage();
                     if (m == null) {
                        m = "";
                     }

                     String u = this.documentURI == null ? "<unknown>" : this.documentURI.toString();
                     String s = Messages.formatMessage("property.syntax.error.at", new Object[]{u, property, newValue, m});
                     DOMException de = new DOMException((short)12, s);
                     if (this.userAgent == null) {
                        throw de;
                     } else {
                        this.userAgent.displayError(de);
                     }
                  } finally {
                     this.element = null;
                     this.cssBaseURI = null;
                  }
               default:
                  boolean[] updated = this.styleDeclarationUpdateHandler.updatedProperties;

                  for(int i = this.getNumberOfProperties() - 1; i >= 0; --i) {
                     updated[i] = false;
                  }

                  updated[idx] = true;
                  boolean fs = idx == this.fontSizeIndex;
                  boolean lh = idx == this.lineHeightIndex;
                  boolean cl = idx == this.colorIndex;
                  int count = 0;

                  for(int i = this.getNumberOfProperties() - 1; i >= 0; --i) {
                     if (updated[i]) {
                        ++count;
                     } else if (fs && style.isFontSizeRelative(i) || lh && style.isLineHeightRelative(i) || cl && style.isColorRelative(i)) {
                        updated[i] = true;
                        clearComputedValue(style, i);
                        ++count;
                     }
                  }

                  int[] props = new int[count];
                  count = 0;

                  for(int i = this.getNumberOfProperties() - 1; i >= 0; --i) {
                     if (updated[i]) {
                        props[count++] = i;
                     }
                  }

                  this.invalidateProperties(elt, props, (boolean[])null, true);
                  return;
               case 3:
                  int[] invalid = new int[]{idx};
                  this.invalidateProperties(elt, invalid, (boolean[])null, true);
            }
         }
      }
   }

   protected boolean hasStyleSheetNode(Node n) {
      if (n instanceof CSSStyleSheetNode) {
         return true;
      } else {
         for(n = getCSSFirstChild(n); n != null; n = getCSSNextSibling(n)) {
            if (this.hasStyleSheetNode(n)) {
               return true;
            }
         }

         return false;
      }
   }

   protected void handleAttrModified(Element e, Attr attr, short attrChange, String prevValue, String newValue) {
      if (e instanceof CSSStylableElement) {
         if (!newValue.equals(prevValue)) {
            String attrNS = attr.getNamespaceURI();
            String name = attrNS == null ? attr.getNodeName() : attr.getLocalName();
            CSSStylableElement elt = (CSSStylableElement)e;
            StyleMap style = elt.getComputedStyleMap((String)null);
            if (style != null) {
               if ((attrNS == this.styleNamespaceURI || attrNS != null && attrNS.equals(this.styleNamespaceURI)) && name.equals(this.styleLocalName)) {
                  this.inlineStyleAttributeUpdated(elt, style, attrChange, prevValue, newValue);
                  return;
               }

               if (this.nonCSSPresentationalHints != null && (attrNS == this.nonCSSPresentationalHintsNamespaceURI || attrNS != null && attrNS.equals(this.nonCSSPresentationalHintsNamespaceURI)) && this.nonCSSPresentationalHints.contains(name)) {
                  this.nonCSSPresentationalHintUpdated(elt, style, name, attrChange, newValue);
                  return;
               }
            }

            if (this.selectorAttributes != null && this.selectorAttributes.contains(name)) {
               this.invalidateProperties(elt, (int[])null, (boolean[])null, true);

               for(Node n = getCSSNextSibling(elt); n != null; n = getCSSNextSibling(n)) {
                  this.invalidateProperties(n, (int[])null, (boolean[])null, true);
               }
            }

         }
      }
   }

   protected void handleNodeInserted(Node n) {
      if (this.hasStyleSheetNode(n)) {
         this.styleSheetNodes = null;
         this.invalidateProperties(this.document.getDocumentElement(), (int[])null, (boolean[])null, true);
      } else if (n instanceof CSSStylableElement) {
         for(n = getCSSNextSibling(n); n != null; n = getCSSNextSibling(n)) {
            this.invalidateProperties(n, (int[])null, (boolean[])null, true);
         }
      }

   }

   protected void handleNodeRemoved(Node n) {
      if (this.hasStyleSheetNode(n)) {
         this.styleSheetRemoved = true;
      } else if (n instanceof CSSStylableElement) {
         this.removedStylableElementSibling = getCSSNextSibling(n);
      }

      this.disposeStyleMaps(n);
   }

   protected void handleSubtreeModified(Node ignored) {
      if (this.styleSheetRemoved) {
         this.styleSheetRemoved = false;
         this.styleSheetNodes = null;
         this.invalidateProperties(this.document.getDocumentElement(), (int[])null, (boolean[])null, true);
      } else if (this.removedStylableElementSibling != null) {
         for(Node n = this.removedStylableElementSibling; n != null; n = getCSSNextSibling(n)) {
            this.invalidateProperties(n, (int[])null, (boolean[])null, true);
         }

         this.removedStylableElementSibling = null;
      }

   }

   protected void handleCharacterDataModified(Node n) {
      if (getCSSParentNode(n) instanceof CSSStyleSheetNode) {
         this.styleSheetNodes = null;
         this.invalidateProperties(this.document.getDocumentElement(), (int[])null, (boolean[])null, true);
      }

   }

   protected class DOMAttrModifiedListener implements EventListener {
      public void handleEvent(Event evt) {
         MutationEvent mevt = (MutationEvent)evt;
         CSSEngine.this.handleAttrModified((Element)evt.getTarget(), (Attr)mevt.getRelatedNode(), mevt.getAttrChange(), mevt.getPrevValue(), mevt.getNewValue());
      }
   }

   protected class DOMCharacterDataModifiedListener implements EventListener {
      public void handleEvent(Event evt) {
         CSSEngine.this.handleCharacterDataModified((Node)evt.getTarget());
      }
   }

   protected class DOMSubtreeModifiedListener implements EventListener {
      public void handleEvent(Event evt) {
         CSSEngine.this.handleSubtreeModified((Node)evt.getTarget());
      }
   }

   protected class DOMNodeRemovedListener implements EventListener {
      public void handleEvent(Event evt) {
         CSSEngine.this.handleNodeRemoved((Node)evt.getTarget());
      }
   }

   protected class DOMNodeInsertedListener implements EventListener {
      public void handleEvent(Event evt) {
         CSSEngine.this.handleNodeInserted((Node)evt.getTarget());
      }
   }

   protected class CSSNavigableDocumentHandler implements CSSNavigableDocumentListener, MainPropertyReceiver {
      protected boolean[] mainPropertiesChanged;
      protected StyleDeclaration declaration;

      public void nodeInserted(Node newNode) {
         CSSEngine.this.handleNodeInserted(newNode);
      }

      public void nodeToBeRemoved(Node oldNode) {
         CSSEngine.this.handleNodeRemoved(oldNode);
      }

      public void subtreeModified(Node rootOfModifications) {
         CSSEngine.this.handleSubtreeModified(rootOfModifications);
      }

      public void characterDataModified(Node text) {
         CSSEngine.this.handleCharacterDataModified(text);
      }

      public void attrModified(Element e, Attr attr, short attrChange, String prevValue, String newValue) {
         CSSEngine.this.handleAttrModified(e, attr, attrChange, prevValue, newValue);
      }

      public void overrideStyleTextChanged(CSSStylableElement elt, String text) {
         StyleDeclarationProvider p = elt.getOverrideStyleDeclarationProvider();
         StyleDeclaration declaration = p.getStyleDeclaration();
         int ds = declaration.size();
         boolean[] updated = new boolean[CSSEngine.this.getNumberOfProperties()];

         int i;
         for(i = 0; i < ds; ++i) {
            updated[declaration.getIndex(i)] = true;
         }

         declaration = CSSEngine.this.parseStyleDeclaration(elt, text);
         p.setStyleDeclaration(declaration);
         ds = declaration.size();

         for(i = 0; i < ds; ++i) {
            updated[declaration.getIndex(i)] = true;
         }

         CSSEngine.this.invalidateProperties(elt, (int[])null, updated, true);
      }

      public void overrideStylePropertyRemoved(CSSStylableElement elt, String name) {
         StyleDeclarationProvider p = elt.getOverrideStyleDeclarationProvider();
         StyleDeclaration declaration = p.getStyleDeclaration();
         int idx = CSSEngine.this.getPropertyIndex(name);
         int ds = declaration.size();

         for(int i = 0; i < ds; ++i) {
            if (idx == declaration.getIndex(i)) {
               declaration.remove(i);
               StyleMap style = elt.getComputedStyleMap((String)null);
               if (style != null && style.getOrigin(idx) == -24576) {
                  CSSEngine.this.invalidateProperties(elt, new int[]{idx}, (boolean[])null, true);
               }
               break;
            }
         }

      }

      public void overrideStylePropertyChanged(CSSStylableElement elt, String name, String val, String prio) {
         boolean important = prio != null && prio.length() != 0;
         StyleDeclarationProvider p = elt.getOverrideStyleDeclarationProvider();
         this.declaration = p.getStyleDeclaration();
         CSSEngine.this.setMainProperties(elt, this, name, val, important);
         this.declaration = null;
         CSSEngine.this.invalidateProperties(elt, (int[])null, this.mainPropertiesChanged, true);
      }

      public void setMainProperty(String name, Value v, boolean important) {
         int idx = CSSEngine.this.getPropertyIndex(name);
         if (idx != -1) {
            int i;
            for(i = 0; i < this.declaration.size() && idx != this.declaration.getIndex(i); ++i) {
            }

            if (i < this.declaration.size()) {
               this.declaration.put(i, v, idx, important);
            } else {
               this.declaration.append(v, idx, important);
            }

         }
      }
   }

   protected class StyleDeclarationUpdateHandler extends DocumentAdapter implements ShorthandManager.PropertyHandler {
      public StyleMap styleMap;
      public boolean[] updatedProperties = new boolean[CSSEngine.this.getNumberOfProperties()];

      public void property(String name, LexicalUnit value, boolean important) throws CSSException {
         int i = CSSEngine.this.getPropertyIndex(name);
         if (i == -1) {
            i = CSSEngine.this.getShorthandIndex(name);
            if (i == -1) {
               return;
            }

            CSSEngine.this.shorthandManagers[i].setValues(CSSEngine.this, this, value, important);
         } else {
            if (this.styleMap.isImportant(i)) {
               return;
            }

            this.updatedProperties[i] = true;
            Value v = CSSEngine.this.valueManagers[i].createValue(value, CSSEngine.this);
            this.styleMap.putMask(i, (short)0);
            this.styleMap.putValue(i, v);
            this.styleMap.putOrigin(i, (short)Short.MIN_VALUE);
         }

      }
   }

   protected static class DocumentAdapter implements DocumentHandler {
      public void startDocument(InputSource source) {
         this.throwUnsupportedEx();
      }

      public void endDocument(InputSource source) {
         this.throwUnsupportedEx();
      }

      public void comment(String text) {
      }

      public void ignorableAtRule(String atRule) {
         this.throwUnsupportedEx();
      }

      public void namespaceDeclaration(String prefix, String uri) {
         this.throwUnsupportedEx();
      }

      public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) {
         this.throwUnsupportedEx();
      }

      public void startMedia(SACMediaList media) {
         this.throwUnsupportedEx();
      }

      public void endMedia(SACMediaList media) {
         this.throwUnsupportedEx();
      }

      public void startPage(String name, String pseudo_page) {
         this.throwUnsupportedEx();
      }

      public void endPage(String name, String pseudo_page) {
         this.throwUnsupportedEx();
      }

      public void startFontFace() {
         this.throwUnsupportedEx();
      }

      public void endFontFace() {
         this.throwUnsupportedEx();
      }

      public void startSelector(SelectorList selectors) {
         this.throwUnsupportedEx();
      }

      public void endSelector(SelectorList selectors) {
         this.throwUnsupportedEx();
      }

      public void property(String name, LexicalUnit value, boolean important) {
         this.throwUnsupportedEx();
      }

      private void throwUnsupportedEx() {
         throw new UnsupportedOperationException("you try to use an empty method in Adapter-class");
      }
   }

   protected class StyleSheetDocumentHandler extends DocumentAdapter implements ShorthandManager.PropertyHandler {
      public StyleSheet styleSheet;
      protected StyleRule styleRule;
      protected StyleDeclaration styleDeclaration;

      public void startDocument(InputSource source) throws CSSException {
      }

      public void endDocument(InputSource source) throws CSSException {
      }

      public void ignorableAtRule(String atRule) throws CSSException {
      }

      public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) throws CSSException {
         ImportRule ir = new ImportRule();
         ir.setMediaList(media);
         ir.setParent(this.styleSheet);
         ParsedURL base = CSSEngine.this.getCSSBaseURI();
         ParsedURL url;
         if (base == null) {
            url = new ParsedURL(uri);
         } else {
            url = new ParsedURL(base, uri);
         }

         ir.setURI(url);
         this.styleSheet.append(ir);
      }

      public void startMedia(SACMediaList media) throws CSSException {
         MediaRule mr = new MediaRule();
         mr.setMediaList(media);
         mr.setParent(this.styleSheet);
         this.styleSheet.append(mr);
         this.styleSheet = mr;
      }

      public void endMedia(SACMediaList media) throws CSSException {
         this.styleSheet = this.styleSheet.getParent();
      }

      public void startPage(String name, String pseudo_page) throws CSSException {
      }

      public void endPage(String name, String pseudo_page) throws CSSException {
      }

      public void startFontFace() throws CSSException {
         this.styleDeclaration = new StyleDeclaration();
      }

      public void endFontFace() throws CSSException {
         StyleMap sm = new StyleMap(CSSEngine.this.getNumberOfProperties());
         int len = this.styleDeclaration.size();

         int pidx;
         for(pidx = 0; pidx < len; ++pidx) {
            int idx = this.styleDeclaration.getIndex(pidx);
            sm.putValue(idx, this.styleDeclaration.getValue(pidx));
            sm.putImportant(idx, this.styleDeclaration.getPriority(pidx));
            sm.putOrigin(idx, (short)24576);
         }

         this.styleDeclaration = null;
         pidx = CSSEngine.this.getPropertyIndex("font-family");
         Value fontFamily = sm.getValue(pidx);
         if (fontFamily != null) {
            ParsedURL base = CSSEngine.this.getCSSBaseURI();
            CSSEngine.this.fontFaces.add(new FontFaceRule(sm, base));
         }
      }

      public void startSelector(SelectorList selectors) throws CSSException {
         this.styleRule = new StyleRule();
         this.styleRule.setSelectorList(selectors);
         this.styleDeclaration = new StyleDeclaration();
         this.styleRule.setStyleDeclaration(this.styleDeclaration);
         this.styleSheet.append(this.styleRule);
      }

      public void endSelector(SelectorList selectors) throws CSSException {
         this.styleRule = null;
         this.styleDeclaration = null;
      }

      public void property(String name, LexicalUnit value, boolean important) throws CSSException {
         int i = CSSEngine.this.getPropertyIndex(name);
         if (i == -1) {
            i = CSSEngine.this.getShorthandIndex(name);
            if (i == -1) {
               return;
            }

            CSSEngine.this.shorthandManagers[i].setValues(CSSEngine.this, this, value, important);
         } else {
            Value v = CSSEngine.this.valueManagers[i].createValue(value, CSSEngine.this);
            this.styleDeclaration.append(v, i, important);
         }

      }
   }

   protected class StyleDeclarationBuilder extends DocumentAdapter implements ShorthandManager.PropertyHandler {
      public StyleDeclaration styleDeclaration;

      public void property(String name, LexicalUnit value, boolean important) throws CSSException {
         int i = CSSEngine.this.getPropertyIndex(name);
         if (i == -1) {
            i = CSSEngine.this.getShorthandIndex(name);
            if (i == -1) {
               return;
            }

            CSSEngine.this.shorthandManagers[i].setValues(CSSEngine.this, this, value, important);
         } else {
            Value v = CSSEngine.this.valueManagers[i].createValue(value, CSSEngine.this);
            this.styleDeclaration.append(v, i, important);
         }

      }
   }

   protected class StyleDeclarationDocumentHandler extends DocumentAdapter implements ShorthandManager.PropertyHandler {
      public StyleMap styleMap;

      public void property(String name, LexicalUnit value, boolean important) throws CSSException {
         int i = CSSEngine.this.getPropertyIndex(name);
         if (i == -1) {
            i = CSSEngine.this.getShorthandIndex(name);
            if (i == -1) {
               return;
            }

            CSSEngine.this.shorthandManagers[i].setValues(CSSEngine.this, this, value, important);
         } else {
            Value v = CSSEngine.this.valueManagers[i].createValue(value, CSSEngine.this);
            CSSEngine.this.putAuthorProperty(this.styleMap, i, v, important, (short)Short.MIN_VALUE);
         }

      }
   }

   public interface MainPropertyReceiver {
      void setMainProperty(String var1, Value var2, boolean var3);
   }
}
