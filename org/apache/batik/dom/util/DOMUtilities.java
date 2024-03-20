package org.apache.batik.dom.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.batik.constants.XMLConstants;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMUtilities extends XMLUtilities implements XMLConstants {
   protected static final String[] LOCK_STRINGS = new String[]{"", "CapsLock", "NumLock", "NumLock CapsLock", "Scroll", "Scroll CapsLock", "Scroll NumLock", "Scroll NumLock CapsLock", "KanaMode", "KanaMode CapsLock", "KanaMode NumLock", "KanaMode NumLock CapsLock", "KanaMode Scroll", "KanaMode Scroll CapsLock", "KanaMode Scroll NumLock", "KanaMode Scroll NumLock CapsLock"};
   protected static final String[] MODIFIER_STRINGS = new String[]{"", "Shift", "Control", "Control Shift", "Meta", "Meta Shift", "Control Meta", "Control Meta Shift", "Alt", "Alt Shift", "Alt Control", "Alt Control Shift", "Alt Meta", "Alt Meta Shift", "Alt Control Meta", "Alt Control Meta Shift", "AltGraph", "AltGraph Shift", "AltGraph Control", "AltGraph Control Shift", "AltGraph Meta", "AltGraph Meta Shift", "AltGraph Control Meta", "AltGraph Control Meta Shift", "Alt AltGraph", "Alt AltGraph Shift", "Alt AltGraph Control", "Alt AltGraph Control Shift", "Alt AltGraph Meta", "Alt AltGraph Meta Shift", "Alt AltGraph Control Meta", "Alt AltGraph Control Meta Shift"};

   protected DOMUtilities() {
   }

   public static void writeDocument(Document doc, Writer w) throws IOException {
      AbstractDocument d = (AbstractDocument)doc;
      if (doc.getDocumentElement() == null) {
         throw new IOException("No document element");
      } else {
         NSMap m = DOMUtilities.NSMap.create();

         for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            writeNode(n, w, m, "1.1".equals(d.getXmlVersion()));
         }

      }
   }

   protected static void writeNode(Node n, Writer w, NSMap m, boolean isXML11) throws IOException {
      String tagName;
      int i;
      int len;
      String ns;
      int len;
      String prefix;
      switch (n.getNodeType()) {
         case 1:
            if (n.hasAttributes()) {
               NamedNodeMap attr = n.getAttributes();
               len = attr.getLength();

               for(i = 0; i < len; ++i) {
                  Attr a = (Attr)attr.item(i);
                  String name = a.getNodeName();
                  if (name.startsWith("xmlns")) {
                     if (name.length() == 5) {
                        m = m.declare("", a.getNodeValue());
                     } else {
                        String prefix = name.substring(6);
                        m = m.declare(prefix, a.getNodeValue());
                     }
                  }
               }
            }

            w.write(60);
            ns = n.getNamespaceURI();
            if (ns == null) {
               tagName = n.getNodeName();
               w.write(tagName);
               if (!"".equals(m.getNamespace(""))) {
                  w.write(" xmlns=\"\"");
                  m = m.declare("", "");
               }
            } else {
               prefix = n.getPrefix();
               if (prefix == null) {
                  prefix = "";
               }

               if (ns.equals(m.getNamespace(prefix))) {
                  tagName = n.getNodeName();
                  w.write(tagName);
               } else {
                  prefix = m.getPrefixForElement(ns);
                  if (prefix == null) {
                     prefix = m.getNewPrefix();
                     tagName = prefix + ':' + n.getLocalName();
                     w.write(tagName + " xmlns:" + prefix + "=\"" + contentToString(ns, isXML11) + '"');
                     m = m.declare(prefix, ns);
                  } else {
                     if (prefix.equals("")) {
                        tagName = n.getLocalName();
                     } else {
                        tagName = prefix + ':' + n.getLocalName();
                     }

                     w.write(tagName);
                  }
               }
            }

            if (n.hasAttributes()) {
               NamedNodeMap attr = n.getAttributes();
               len = attr.getLength();

               for(int i = 0; i < len; ++i) {
                  Attr a = (Attr)attr.item(i);
                  String name = a.getNodeName();
                  String prefix = a.getPrefix();
                  String ans = a.getNamespaceURI();
                  if (ans != null && !"xmlns".equals(prefix) && !name.equals("xmlns") && (prefix != null && !ans.equals(m.getNamespace(prefix)) || prefix == null)) {
                     prefix = m.getPrefixForAttr(ans);
                     if (prefix == null) {
                        prefix = m.getNewPrefix();
                        m = m.declare(prefix, ans);
                        w.write(" xmlns:" + prefix + "=\"" + contentToString(ans, isXML11) + '"');
                     }

                     name = prefix + ':' + a.getLocalName();
                  }

                  w.write(' ' + name + "=\"" + contentToString(a.getNodeValue(), isXML11) + '"');
               }
            }

            Node c = n.getFirstChild();
            if (c != null) {
               w.write(62);

               do {
                  writeNode(c, w, m, isXML11);
                  c = c.getNextSibling();
               } while(c != null);

               w.write("</" + tagName + '>');
            } else {
               w.write("/>");
            }
            break;
         case 2:
         case 6:
         case 9:
         default:
            throw new IOException("Unknown DOM node type " + n.getNodeType());
         case 3:
            w.write(contentToString(n.getNodeValue(), isXML11));
            break;
         case 4:
            ns = n.getNodeValue();
            if (ns.indexOf("]]>") != -1) {
               throw new IOException("Unserializable CDATA section node");
            }

            w.write("<![CDATA[" + assertValidCharacters(ns, isXML11) + "]]>");
            break;
         case 5:
            w.write('&' + n.getNodeName() + ';');
            break;
         case 7:
            ns = n.getNodeName();
            tagName = n.getNodeValue();
            if (ns.equalsIgnoreCase("xml") || ns.indexOf(58) != -1 || tagName.indexOf("?>") != -1) {
               throw new IOException("Unserializable processing instruction node");
            }

            w.write("<?" + ns + ' ' + tagName + "?>");
            break;
         case 8:
            w.write("<!--");
            ns = n.getNodeValue();
            len = ns.length();
            if (len != 0 && ns.charAt(len - 1) == '-' || ns.indexOf("--") != -1) {
               throw new IOException("Unserializable comment node");
            }

            w.write(ns);
            w.write("-->");
            break;
         case 10:
            DocumentType dt = (DocumentType)n;
            w.write("<!DOCTYPE " + n.getOwnerDocument().getDocumentElement().getNodeName());
            tagName = dt.getPublicId();
            if (tagName != null) {
               i = getUsableQuote(tagName);
               if (i == 0) {
                  throw new IOException("Unserializable DOCTYPE node");
               }

               w.write(" PUBLIC " + i + tagName + i);
            }

            prefix = dt.getSystemId();
            if (prefix != null) {
               len = getUsableQuote(prefix);
               if (len == 0) {
                  throw new IOException("Unserializable DOCTYPE node");
               }

               if (tagName == null) {
                  w.write(" SYSTEM");
               }

               w.write(" " + len + prefix + len);
            }

            String subset = dt.getInternalSubset();
            if (subset != null) {
               w.write('[' + subset + ']');
            }

            w.write(62);
      }

   }

   public static void writeNode(Node n, Writer w) throws IOException {
      if (n.getNodeType() == 9) {
         writeDocument((Document)n, w);
      } else {
         AbstractDocument d = (AbstractDocument)n.getOwnerDocument();
         writeNode(n, w, DOMUtilities.NSMap.create(), d == null ? false : "1.1".equals(d.getXmlVersion()));
      }

   }

   private static char getUsableQuote(String s) {
      char ret = 0;

      for(int i = s.length() - 1; i >= 0; --i) {
         char c = s.charAt(i);
         if (c == '"') {
            if (ret != 0) {
               return '\u0000';
            }

            ret = '\'';
         } else if (c == '\'') {
            if (ret != 0) {
               return '\u0000';
            }

            ret = '"';
         }
      }

      return ret == 0 ? '"' : ret;
   }

   public static String getXML(Node n) {
      Writer writer = new StringWriter();

      try {
         writeNode(n, writer);
         writer.close();
      } catch (IOException var3) {
         return "";
      }

      return writer.toString();
   }

   protected static String assertValidCharacters(String s, boolean isXML11) throws IOException {
      int len = s.length();

      for(int i = 0; i < len; ++i) {
         char c = s.charAt(i);
         if (!isXML11 && !isXMLCharacter(c) || isXML11 && !isXML11Character(c)) {
            throw new IOException("Invalid character");
         }
      }

      return s;
   }

   public static String contentToString(String s, boolean isXML11) throws IOException {
      StringBuffer result = new StringBuffer(s.length());
      int len = s.length();

      for(int i = 0; i < len; ++i) {
         char c = s.charAt(i);
         if (!isXML11 && !isXMLCharacter(c) || isXML11 && !isXML11Character(c)) {
            throw new IOException("Invalid character");
         }

         switch (c) {
            case '"':
               result.append("&quot;");
               break;
            case '&':
               result.append("&amp;");
               break;
            case '\'':
               result.append("&apos;");
               break;
            case '<':
               result.append("&lt;");
               break;
            case '>':
               result.append("&gt;");
               break;
            default:
               result.append(c);
         }
      }

      return result.toString();
   }

   public static int getChildIndex(Node child, Node parent) {
      return child != null && child.getParentNode() == parent && child.getParentNode() != null ? getChildIndex(child) : -1;
   }

   public static int getChildIndex(Node child) {
      NodeList children = child.getParentNode().getChildNodes();

      for(int i = 0; i < children.getLength(); ++i) {
         Node currentChild = children.item(i);
         if (currentChild == child) {
            return i;
         }
      }

      return -1;
   }

   public static boolean isAnyNodeAncestorOf(ArrayList ancestorNodes, Node node) {
      int n = ancestorNodes.size();
      Iterator var3 = ancestorNodes.iterator();

      Node ancestor;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         Object ancestorNode = var3.next();
         ancestor = (Node)ancestorNode;
      } while(!isAncestorOf(ancestor, node));

      return true;
   }

   public static boolean isAncestorOf(Node node, Node descendant) {
      if (node != null && descendant != null) {
         for(Node currentNode = descendant.getParentNode(); currentNode != null; currentNode = currentNode.getParentNode()) {
            if (currentNode == node) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isParentOf(Node node, Node parentNode) {
      return node != null && parentNode != null && node.getParentNode() == parentNode;
   }

   public static boolean canAppend(Node node, Node parentNode) {
      return node != null && parentNode != null && node != parentNode && !isAncestorOf(node, parentNode);
   }

   public static boolean canAppendAny(ArrayList children, Node parentNode) {
      if (!canHaveChildren(parentNode)) {
         return false;
      } else {
         int n = children.size();
         Iterator var3 = children.iterator();

         Node child;
         do {
            if (!var3.hasNext()) {
               return false;
            }

            Object aChildren = var3.next();
            child = (Node)aChildren;
         } while(!canAppend(child, parentNode));

         return true;
      }
   }

   public static boolean canHaveChildren(Node parentNode) {
      if (parentNode == null) {
         return false;
      } else {
         switch (parentNode.getNodeType()) {
            case 3:
            case 4:
            case 7:
            case 8:
            case 9:
               return false;
            case 5:
            case 6:
            default:
               return true;
         }
      }
   }

   public static Node parseXML(String text, Document doc, String uri, Map prefixes, String wrapperElementName, SAXDocumentFactory documentFactory) {
      String wrapperElementPrefix = "";
      String wrapperElementSuffix = "";
      if (wrapperElementName != null) {
         wrapperElementPrefix = "<" + wrapperElementName;
         if (prefixes != null) {
            wrapperElementPrefix = wrapperElementPrefix + " ";

            String currentKey;
            String currentValue;
            for(Iterator var8 = prefixes.entrySet().iterator(); var8.hasNext(); wrapperElementPrefix = wrapperElementPrefix + currentKey + "=\"" + currentValue + "\" ") {
               Object o = var8.next();
               Map.Entry e = (Map.Entry)o;
               currentKey = (String)e.getKey();
               currentValue = (String)e.getValue();
            }
         }

         wrapperElementPrefix = wrapperElementPrefix + ">";
         wrapperElementSuffix = wrapperElementSuffix + "</" + wrapperElementName + '>';
      }

      if (wrapperElementPrefix.trim().length() == 0 && wrapperElementSuffix.trim().length() == 0) {
         try {
            Document d = documentFactory.createDocument(uri, (Reader)(new StringReader(text)));
            if (doc == null) {
               return d;
            }

            Node result = doc.createDocumentFragment();
            result.appendChild(doc.importNode(d.getDocumentElement(), true));
            return result;
         } catch (Exception var14) {
         }
      }

      StringBuffer sb = new StringBuffer(wrapperElementPrefix.length() + text.length() + wrapperElementSuffix.length());
      sb.append(wrapperElementPrefix);
      sb.append(text);
      sb.append(wrapperElementSuffix);
      String newText = sb.toString();

      try {
         Document d = documentFactory.createDocument(uri, (Reader)(new StringReader(newText)));
         if (doc == null) {
            return d;
         }

         for(Node node = d.getDocumentElement().getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1) {
               node = doc.importNode(node, true);
               Node result = doc.createDocumentFragment();
               result.appendChild(node);
               return result;
            }
         }
      } catch (Exception var13) {
      }

      return null;
   }

   public static Document deepCloneDocument(Document doc, DOMImplementation impl) {
      Element root = doc.getDocumentElement();
      Document result = impl.createDocument(root.getNamespaceURI(), root.getNodeName(), (DocumentType)null);
      Element rroot = result.getDocumentElement();
      boolean before = true;

      for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n != root) {
            if (n.getNodeType() != 10) {
               if (before) {
                  result.insertBefore(result.importNode(n, true), rroot);
               } else {
                  result.appendChild(result.importNode(n, true));
               }
            }
         } else {
            before = false;
            if (root.hasAttributes()) {
               NamedNodeMap attr = root.getAttributes();
               int len = attr.getLength();

               for(int i = 0; i < len; ++i) {
                  rroot.setAttributeNode((Attr)result.importNode(attr.item(i), true));
               }
            }

            for(Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
               rroot.appendChild(result.importNode(c, true));
            }
         }
      }

      return result;
   }

   public static boolean isValidName(String s) {
      int len = s.length();
      if (len == 0) {
         return false;
      } else {
         char c = s.charAt(0);
         int d = c / 32;
         int m = c % 32;
         if ((NAME_FIRST_CHARACTER[d] & 1 << m) == 0) {
            return false;
         } else {
            for(int i = 1; i < len; ++i) {
               c = s.charAt(i);
               d = c / 32;
               m = c % 32;
               if ((NAME_CHARACTER[d] & 1 << m) == 0) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public static boolean isValidName11(String s) {
      int len = s.length();
      if (len == 0) {
         return false;
      } else {
         char c = s.charAt(0);
         int d = c / 32;
         int m = c % 32;
         if ((NAME11_FIRST_CHARACTER[d] & 1 << m) == 0) {
            return false;
         } else {
            for(int i = 1; i < len; ++i) {
               c = s.charAt(i);
               d = c / 32;
               m = c % 32;
               if ((NAME11_CHARACTER[d] & 1 << m) == 0) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public static boolean isValidPrefix(String s) {
      return s.indexOf(58) == -1;
   }

   public static String getPrefix(String s) {
      int i = s.indexOf(58);
      return i != -1 && i != s.length() - 1 ? s.substring(0, i) : null;
   }

   public static String getLocalName(String s) {
      int i = s.indexOf(58);
      return i != -1 && i != s.length() - 1 ? s.substring(i + 1) : s;
   }

   public static void parseStyleSheetPIData(String data, HashMap table) {
      char c;
      int i;
      for(i = 0; i < data.length(); ++i) {
         c = data.charAt(i);
         if (!XMLUtilities.isXMLSpace(c)) {
            break;
         }
      }

      while(i < data.length()) {
         c = data.charAt(i);
         int d = c / 32;
         int m = c % 32;
         if ((NAME_FIRST_CHARACTER[d] & 1 << m) == 0) {
            throw new DOMException((short)5, "Wrong name initial:  " + c);
         }

         StringBuffer ident = new StringBuffer();
         ident.append(c);

         while(true) {
            ++i;
            if (i >= data.length()) {
               break;
            }

            c = data.charAt(i);
            d = c / 32;
            m = c % 32;
            if ((NAME_CHARACTER[d] & 1 << m) == 0) {
               break;
            }

            ident.append(c);
         }

         if (i >= data.length()) {
            throw new DOMException((short)12, "Wrong xml-stylesheet data: " + data);
         }

         while(i < data.length()) {
            c = data.charAt(i);
            if (!XMLUtilities.isXMLSpace(c)) {
               break;
            }

            ++i;
         }

         if (i >= data.length()) {
            throw new DOMException((short)12, "Wrong xml-stylesheet data: " + data);
         }

         if (data.charAt(i) != '=') {
            throw new DOMException((short)12, "Wrong xml-stylesheet data: " + data);
         }

         ++i;

         while(i < data.length()) {
            c = data.charAt(i);
            if (!XMLUtilities.isXMLSpace(c)) {
               break;
            }

            ++i;
         }

         if (i >= data.length()) {
            throw new DOMException((short)12, "Wrong xml-stylesheet data: " + data);
         }

         c = data.charAt(i);
         ++i;
         StringBuffer value = new StringBuffer();
         if (c == '\'') {
            while(i < data.length()) {
               c = data.charAt(i);
               if (c == '\'') {
                  break;
               }

               value.append(c);
               ++i;
            }

            if (i >= data.length()) {
               throw new DOMException((short)12, "Wrong xml-stylesheet data: " + data);
            }
         } else {
            if (c != '"') {
               throw new DOMException((short)12, "Wrong xml-stylesheet data: " + data);
            }

            while(i < data.length()) {
               c = data.charAt(i);
               if (c == '"') {
                  break;
               }

               value.append(c);
               ++i;
            }

            if (i >= data.length()) {
               throw new DOMException((short)12, "Wrong xml-stylesheet data: " + data);
            }
         }

         table.put(ident.toString().intern(), value.toString());
         ++i;

         while(i < data.length()) {
            c = data.charAt(i);
            if (!XMLUtilities.isXMLSpace(c)) {
               break;
            }

            ++i;
         }
      }

   }

   public static String getModifiersList(int lockState, int modifiersEx) {
      if ((modifiersEx & 8192) != 0) {
         modifiersEx = 16 | modifiersEx >> 6 & 15;
      } else {
         modifiersEx = modifiersEx >> 6 & 15;
      }

      String s = LOCK_STRINGS[lockState & 15];
      return s.length() != 0 ? s + ' ' + MODIFIER_STRINGS[modifiersEx] : MODIFIER_STRINGS[modifiersEx];
   }

   public static boolean isAttributeSpecifiedNS(Element e, String namespaceURI, String localName) {
      Attr a = e.getAttributeNodeNS(namespaceURI, localName);
      return a != null && a.getSpecified();
   }

   private static final class NSMap {
      private String prefix;
      private String ns;
      private NSMap next;
      private int nextPrefixNumber;

      public static NSMap create() {
         return (new NSMap()).declare("xml", "http://www.w3.org/XML/1998/namespace").declare("xmlns", "http://www.w3.org/2000/xmlns/");
      }

      public NSMap declare(String prefix, String ns) {
         NSMap m = new NSMap();
         m.prefix = prefix;
         m.ns = ns;
         m.next = this;
         m.nextPrefixNumber = this.nextPrefixNumber;
         return m;
      }

      public String getNewPrefix() {
         String prefix;
         do {
            prefix = "a" + this.nextPrefixNumber++;
         } while(this.getNamespace(prefix) != null);

         return prefix;
      }

      public String getNamespace(String prefix) {
         for(NSMap m = this; m.next != null; m = m.next) {
            if (m.prefix.equals(prefix)) {
               return m.ns;
            }
         }

         return null;
      }

      public String getPrefixForElement(String ns) {
         for(NSMap m = this; m.next != null; m = m.next) {
            if (ns.equals(m.ns)) {
               return m.prefix;
            }
         }

         return null;
      }

      public String getPrefixForAttr(String ns) {
         for(NSMap m = this; m.next != null; m = m.next) {
            if (ns.equals(m.ns) && !m.prefix.equals("")) {
               return m.prefix;
            }
         }

         return null;
      }
   }
}
