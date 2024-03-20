package org.apache.xml.serializer.dom3;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.XML11Char;
import org.apache.xml.serializer.utils.XMLChar;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.ls.LSSerializerFilter;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.LocatorImpl;

final class DOM3TreeWalker {
   private SerializationHandler fSerializer = null;
   private LocatorImpl fLocator = new LocatorImpl();
   private DOMErrorHandler fErrorHandler = null;
   private LSSerializerFilter fFilter = null;
   private LexicalHandler fLexicalHandler = null;
   private int fWhatToShowFilter;
   private String fNewLine = null;
   private Properties fDOMConfigProperties = null;
   private boolean fInEntityRef = false;
   private String fXMLVersion = null;
   private boolean fIsXMLVersion11 = false;
   private boolean fIsLevel3DOM = false;
   private int fFeatures = 0;
   boolean fNextIsRaw = false;
   private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
   private static final String XMLNS_PREFIX = "xmlns";
   private static final String XML_URI = "http://www.w3.org/XML/1998/namespace";
   private static final String XML_PREFIX = "xml";
   protected NamespaceSupport fNSBinder;
   protected NamespaceSupport fLocalNSBinder;
   private int fElementDepth = 0;
   private static final int CANONICAL = 1;
   private static final int CDATA = 2;
   private static final int CHARNORMALIZE = 4;
   private static final int COMMENTS = 8;
   private static final int DTNORMALIZE = 16;
   private static final int ELEM_CONTENT_WHITESPACE = 32;
   private static final int ENTITIES = 64;
   private static final int INFOSET = 128;
   private static final int NAMESPACES = 256;
   private static final int NAMESPACEDECLS = 512;
   private static final int NORMALIZECHARS = 1024;
   private static final int SPLITCDATA = 2048;
   private static final int VALIDATE = 4096;
   private static final int SCHEMAVALIDATE = 8192;
   private static final int WELLFORMED = 16384;
   private static final int DISCARDDEFAULT = 32768;
   private static final int PRETTY_PRINT = 65536;
   private static final int IGNORE_CHAR_DENORMALIZE = 131072;
   private static final int XMLDECL = 262144;
   private static final Hashtable s_propKeys = new Hashtable();

   DOM3TreeWalker(SerializationHandler serialHandler, DOMErrorHandler errHandler, LSSerializerFilter filter, String newLine) {
      this.fSerializer = serialHandler;
      this.fErrorHandler = errHandler;
      this.fFilter = filter;
      this.fLexicalHandler = null;
      this.fNewLine = newLine;
      this.fNSBinder = new NamespaceSupport();
      this.fLocalNSBinder = new NamespaceSupport();
      this.fDOMConfigProperties = this.fSerializer.getOutputFormat();
      this.fSerializer.setDocumentLocator(this.fLocator);
      this.initProperties(this.fDOMConfigProperties);

      try {
         this.fLocator.setSystemId(System.getProperty("user.dir") + File.separator + "dummy.xsl");
      } catch (SecurityException var6) {
      }

   }

   public void traverse(Node pos) throws SAXException {
      this.fSerializer.startDocument();
      if (pos.getNodeType() != 9) {
         Document ownerDoc = pos.getOwnerDocument();
         if (ownerDoc != null && ownerDoc.getImplementation().hasFeature("Core", "3.0")) {
            this.fIsLevel3DOM = true;
         }
      } else if (((Document)pos).getImplementation().hasFeature("Core", "3.0")) {
         this.fIsLevel3DOM = true;
      }

      if (this.fSerializer instanceof LexicalHandler) {
         this.fLexicalHandler = this.fSerializer;
      }

      if (this.fFilter != null) {
         this.fWhatToShowFilter = this.fFilter.getWhatToShow();
      }

      Node nextNode;
      label59:
      for(Node top = pos; null != pos; pos = nextNode) {
         this.startNode(pos);
         nextNode = null;
         nextNode = pos.getFirstChild();

         do {
            do {
               if (null != nextNode) {
                  continue label59;
               }

               this.endNode(pos);
               if (top.equals(pos)) {
                  continue label59;
               }

               nextNode = pos.getNextSibling();
            } while(null != nextNode);

            pos = pos.getParentNode();
         } while(null != pos && !top.equals(pos));

         if (null != pos) {
            this.endNode(pos);
         }

         nextNode = null;
      }

      this.fSerializer.endDocument();
   }

   public void traverse(Node pos, Node top) throws SAXException {
      this.fSerializer.startDocument();
      Document nextNode;
      if (pos.getNodeType() != 9) {
         nextNode = pos.getOwnerDocument();
         if (nextNode != null && nextNode.getImplementation().hasFeature("Core", "3.0")) {
            this.fIsLevel3DOM = true;
         }
      } else if (((Document)pos).getImplementation().hasFeature("Core", "3.0")) {
         this.fIsLevel3DOM = true;
      }

      if (this.fSerializer instanceof LexicalHandler) {
         this.fLexicalHandler = this.fSerializer;
      }

      if (this.fFilter != null) {
         this.fWhatToShowFilter = this.fFilter.getWhatToShow();
      }

      Node nextNode;
      label58:
      for(; null != pos; pos = nextNode) {
         this.startNode(pos);
         nextNode = null;
         nextNode = pos.getFirstChild();

         do {
            do {
               if (null != nextNode) {
                  continue label58;
               }

               this.endNode(pos);
               if (null != top && top.equals(pos)) {
                  continue label58;
               }

               nextNode = pos.getNextSibling();
            } while(null != nextNode);

            pos = pos.getParentNode();
         } while(null != pos && (null == top || !top.equals(pos)));

         nextNode = null;
      }

      this.fSerializer.endDocument();
   }

   private final void dispatachChars(Node node) throws SAXException {
      if (this.fSerializer != null) {
         this.fSerializer.characters(node);
      } else {
         String data = ((Text)node).getData();
         this.fSerializer.characters(data.toCharArray(), 0, data.length());
      }

   }

   protected void startNode(Node node) throws SAXException {
      if (node instanceof Locator) {
         Locator loc = (Locator)node;
         this.fLocator.setColumnNumber(loc.getColumnNumber());
         this.fLocator.setLineNumber(loc.getLineNumber());
         this.fLocator.setPublicId(loc.getPublicId());
         this.fLocator.setSystemId(loc.getSystemId());
      } else {
         this.fLocator.setColumnNumber(0);
         this.fLocator.setLineNumber(0);
      }

      switch (node.getNodeType()) {
         case 1:
            this.serializeElement((Element)node, true);
         case 2:
         case 6:
         case 9:
         case 11:
         default:
            break;
         case 3:
            this.serializeText((Text)node);
            break;
         case 4:
            this.serializeCDATASection((CDATASection)node);
            break;
         case 5:
            this.serializeEntityReference((EntityReference)node, true);
            break;
         case 7:
            this.serializePI((ProcessingInstruction)node);
            break;
         case 8:
            this.serializeComment((Comment)node);
            break;
         case 10:
            this.serializeDocType((DocumentType)node, true);
      }

   }

   protected void endNode(Node node) throws SAXException {
      switch (node.getNodeType()) {
         case 1:
            this.serializeElement((Element)node, false);
         case 2:
         case 3:
         case 4:
         case 6:
         case 7:
         case 8:
         case 9:
         default:
            break;
         case 5:
            this.serializeEntityReference((EntityReference)node, false);
            break;
         case 10:
            this.serializeDocType((DocumentType)node, false);
      }

   }

   protected boolean applyFilter(Node node, int nodeType) {
      if (this.fFilter != null && (this.fWhatToShowFilter & nodeType) != 0) {
         short code = this.fFilter.acceptNode(node);
         switch (code) {
            case 2:
            case 3:
               return false;
         }
      }

      return true;
   }

   protected void serializeDocType(DocumentType node, boolean bStart) throws SAXException {
      String docTypeName = node.getNodeName();
      String publicId = node.getPublicId();
      String systemId = node.getSystemId();
      String internalSubset = node.getInternalSubset();
      if (internalSubset != null && !"".equals(internalSubset)) {
         if (bStart) {
            try {
               Writer writer = this.fSerializer.getWriter();
               StringBuffer dtd = new StringBuffer();
               dtd.append("<!DOCTYPE ");
               dtd.append(docTypeName);
               if (null != publicId) {
                  dtd.append(" PUBLIC \"");
                  dtd.append(publicId);
                  dtd.append('"');
               }

               if (null != systemId) {
                  if (null == publicId) {
                     dtd.append(" SYSTEM \"");
                  } else {
                     dtd.append(" \"");
                  }

                  dtd.append(systemId);
                  dtd.append('"');
               }

               dtd.append(" [ ");
               dtd.append(this.fNewLine);
               dtd.append(internalSubset);
               dtd.append("]>");
               dtd.append(this.fNewLine);
               writer.write(dtd.toString());
               writer.flush();
            } catch (IOException var9) {
               throw new SAXException(Utils.messages.createMessage("ER_WRITING_INTERNAL_SUBSET", (Object[])null), var9);
            }
         }
      } else if (bStart) {
         if (this.fLexicalHandler != null) {
            this.fLexicalHandler.startDTD(docTypeName, publicId, systemId);
         }
      } else if (this.fLexicalHandler != null) {
         this.fLexicalHandler.endDTD();
      }

   }

   protected void serializeComment(Comment node) throws SAXException {
      if ((this.fFeatures & 8) != 0) {
         String data = node.getData();
         if ((this.fFeatures & 16384) != 0) {
            this.isCommentWellFormed(data);
         }

         if (this.fLexicalHandler != null) {
            if (!this.applyFilter(node, 128)) {
               return;
            }

            this.fLexicalHandler.comment(data.toCharArray(), 0, data.length());
         }
      }

   }

   protected void serializeElement(Element node, boolean bStart) throws SAXException {
      if (bStart) {
         ++this.fElementDepth;
         if ((this.fFeatures & 16384) != 0) {
            this.isElementWellFormed(node);
         }

         if (!this.applyFilter(node, 1)) {
            return;
         }

         if ((this.fFeatures & 256) != 0) {
            this.fNSBinder.pushContext();
            this.fLocalNSBinder.reset();
            this.recordLocalNSDecl(node);
            this.fixupElementNS(node);
         }

         this.fSerializer.startElement(node.getNamespaceURI(), node.getLocalName(), node.getNodeName());
         this.serializeAttList(node);
      } else {
         --this.fElementDepth;
         if (!this.applyFilter(node, 1)) {
            return;
         }

         this.fSerializer.endElement(node.getNamespaceURI(), node.getLocalName(), node.getNodeName());
         if ((this.fFeatures & 256) != 0) {
            this.fNSBinder.popContext();
         }
      }

   }

   protected void serializeAttList(Element node) throws SAXException {
      NamedNodeMap atts = node.getAttributes();
      int nAttrs = atts.getLength();

      for(int i = 0; i < nAttrs; ++i) {
         Node attr = atts.item(i);
         String localName = attr.getLocalName();
         String attrName = attr.getNodeName();
         String attrPrefix = attr.getPrefix() == null ? "" : attr.getPrefix();
         String attrValue = attr.getNodeValue();
         String type = null;
         if (this.fIsLevel3DOM) {
            type = ((Attr)attr).getSchemaTypeInfo().getTypeName();
         }

         type = type == null ? "CDATA" : type;
         String attrNS = attr.getNamespaceURI();
         if (attrNS != null && attrNS.length() == 0) {
            attrNS = null;
            attrName = attr.getLocalName();
         }

         boolean isSpecified = ((Attr)attr).getSpecified();
         boolean addAttr = true;
         boolean applyFilter = false;
         boolean xmlnsAttr = attrName.equals("xmlns") || attrName.startsWith("xmlns:");
         if ((this.fFeatures & 16384) != 0) {
            this.isAttributeWellFormed(attr);
         }

         String prefix;
         if ((this.fFeatures & 256) != 0 && !xmlnsAttr) {
            String declAttrPrefix;
            if (attrNS == null) {
               if (localName == null) {
                  declAttrPrefix = Utils.messages.createMessage("ER_NULL_LOCAL_ELEMENT_NAME", new Object[]{attrName});
                  if (this.fErrorHandler != null) {
                     this.fErrorHandler.handleError(new DOMErrorImpl((short)2, declAttrPrefix, "ER_NULL_LOCAL_ELEMENT_NAME", (Exception)null, (Object)null, (DOMLocatorImpl)null));
                  }
               }
            } else {
               attrPrefix = attrPrefix == null ? "" : attrPrefix;
               declAttrPrefix = this.fNSBinder.getPrefix(attrNS);
               prefix = this.fNSBinder.getURI(attrPrefix);
               if ("".equals(attrPrefix) || "".equals(declAttrPrefix) || !attrPrefix.equals(declAttrPrefix)) {
                  if (declAttrPrefix != null && !"".equals(declAttrPrefix)) {
                     if (declAttrPrefix.length() > 0) {
                        attrName = declAttrPrefix + ":" + localName;
                     } else {
                        attrName = localName;
                     }
                  } else if (attrPrefix != null && !"".equals(attrPrefix) && prefix == null) {
                     if ((this.fFeatures & 512) != 0) {
                        this.fSerializer.addAttribute("http://www.w3.org/2000/xmlns/", attrPrefix, "xmlns:" + attrPrefix, "CDATA", attrNS);
                        this.fNSBinder.declarePrefix(attrPrefix, attrNS);
                        this.fLocalNSBinder.declarePrefix(attrPrefix, attrNS);
                     }
                  } else {
                     int counter = 1;

                     for(attrPrefix = "NS" + counter++; this.fLocalNSBinder.getURI(attrPrefix) != null; attrPrefix = "NS" + counter++) {
                     }

                     attrName = attrPrefix + ":" + localName;
                     if ((this.fFeatures & 512) != 0) {
                        this.fSerializer.addAttribute("http://www.w3.org/2000/xmlns/", attrPrefix, "xmlns:" + attrPrefix, "CDATA", attrNS);
                        this.fNSBinder.declarePrefix(attrPrefix, attrNS);
                        this.fLocalNSBinder.declarePrefix(attrPrefix, attrNS);
                     }
                  }
               }
            }
         }

         if (((this.fFeatures & '耀') == 0 || !isSpecified) && (this.fFeatures & '耀') != 0) {
            addAttr = false;
         } else {
            applyFilter = true;
         }

         int index;
         if (applyFilter && this.fFilter != null && (this.fFilter.getWhatToShow() & 2) != 0 && !xmlnsAttr) {
            index = this.fFilter.acceptNode(attr);
            switch (index) {
               case 2:
               case 3:
                  addAttr = false;
            }
         }

         if (addAttr && xmlnsAttr) {
            if ((this.fFeatures & 512) != 0 && localName != null && !"".equals(localName)) {
               this.fSerializer.addAttribute(attrNS, localName, attrName, type, attrValue);
            }
         } else if (addAttr && !xmlnsAttr) {
            if ((this.fFeatures & 512) != 0 && attrNS != null) {
               this.fSerializer.addAttribute(attrNS, localName, attrName, type, attrValue);
            } else {
               this.fSerializer.addAttribute("", localName, attrName, type, attrValue);
            }
         }

         if (xmlnsAttr && (this.fFeatures & 512) != 0) {
            prefix = (index = attrName.indexOf(":")) < 0 ? "" : attrName.substring(index + 1);
            if (!"".equals(prefix)) {
               this.fSerializer.namespaceAfterStartElement(prefix, attrValue);
            }
         }
      }

   }

   protected void serializePI(ProcessingInstruction node) throws SAXException {
      String name = node.getNodeName();
      if ((this.fFeatures & 16384) != 0) {
         this.isPIWellFormed(node);
      }

      if (this.applyFilter(node, 64)) {
         if (name.equals("xslt-next-is-raw")) {
            this.fNextIsRaw = true;
         } else {
            this.fSerializer.processingInstruction(name, node.getData());
         }

      }
   }

   protected void serializeCDATASection(CDATASection node) throws SAXException {
      if ((this.fFeatures & 16384) != 0) {
         this.isCDATASectionWellFormed(node);
      }

      if ((this.fFeatures & 2) != 0) {
         String nodeValue = node.getNodeValue();
         int endIndex = nodeValue.indexOf("]]>");
         String msg;
         if ((this.fFeatures & 2048) != 0) {
            if (endIndex >= 0) {
               String relatedData = nodeValue.substring(0, endIndex + 2);
               msg = Utils.messages.createMessage("cdata-sections-splitted", (Object[])null);
               if (this.fErrorHandler != null) {
                  this.fErrorHandler.handleError(new DOMErrorImpl((short)1, msg, "cdata-sections-splitted", (Exception)null, relatedData, (DOMLocatorImpl)null));
               }
            }
         } else if (endIndex >= 0) {
            nodeValue.substring(0, endIndex + 2);
            msg = Utils.messages.createMessage("cdata-sections-splitted", (Object[])null);
            if (this.fErrorHandler != null) {
               this.fErrorHandler.handleError(new DOMErrorImpl((short)2, msg, "cdata-sections-splitted"));
            }

            return;
         }

         if (!this.applyFilter(node, 8)) {
            return;
         }

         if (this.fLexicalHandler != null) {
            this.fLexicalHandler.startCDATA();
         }

         this.dispatachChars(node);
         if (this.fLexicalHandler != null) {
            this.fLexicalHandler.endCDATA();
         }
      } else {
         this.dispatachChars(node);
      }

   }

   protected void serializeText(Text node) throws SAXException {
      if (this.fNextIsRaw) {
         this.fNextIsRaw = false;
         this.fSerializer.processingInstruction("javax.xml.transform.disable-output-escaping", "");
         this.dispatachChars(node);
         this.fSerializer.processingInstruction("javax.xml.transform.enable-output-escaping", "");
      } else {
         boolean bDispatch = false;
         if ((this.fFeatures & 16384) != 0) {
            this.isTextWellFormed(node);
         }

         boolean isElementContentWhitespace = false;
         if (this.fIsLevel3DOM) {
            isElementContentWhitespace = node.isElementContentWhitespace();
         }

         if (isElementContentWhitespace) {
            if ((this.fFeatures & 32) != 0) {
               bDispatch = true;
            }
         } else {
            bDispatch = true;
         }

         if (!this.applyFilter(node, 4)) {
            return;
         }

         if (bDispatch) {
            this.dispatachChars(node);
         }
      }

   }

   protected void serializeEntityReference(EntityReference node, boolean bStart) throws SAXException {
      if (bStart) {
         if ((this.fFeatures & 64) != 0) {
            if ((this.fFeatures & 16384) != 0) {
               this.isEntityReferneceWellFormed(node);
            }

            if ((this.fFeatures & 256) != 0) {
               this.checkUnboundPrefixInEntRef(node);
            }
         }

         if (this.fLexicalHandler != null) {
            this.fLexicalHandler.startEntity(node.getNodeName());
         }
      } else if (this.fLexicalHandler != null) {
         this.fLexicalHandler.endEntity(node.getNodeName());
      }

   }

   protected boolean isXMLName(String s, boolean xml11Version) {
      if (s == null) {
         return false;
      } else {
         return !xml11Version ? XMLChar.isValidName(s) : XML11Char.isXML11ValidName(s);
      }
   }

   protected boolean isValidQName(String prefix, String local, boolean xml11Version) {
      if (local == null) {
         return false;
      } else {
         boolean validNCName = false;
         if (!xml11Version) {
            validNCName = (prefix == null || XMLChar.isValidNCName(prefix)) && XMLChar.isValidNCName(local);
         } else {
            validNCName = (prefix == null || XML11Char.isXML11ValidNCName(prefix)) && XML11Char.isXML11ValidNCName(local);
         }

         return validNCName;
      }
   }

   protected boolean isWFXMLChar(String chardata, Character refInvalidChar) {
      if (chardata != null && chardata.length() != 0) {
         char[] dataarray = chardata.toCharArray();
         int datalength = dataarray.length;
         int i;
         char ch;
         char ch2;
         if (!this.fIsXMLVersion11) {
            label78: {
               i = 0;

               while(i < datalength) {
                  if (XMLChar.isInvalid(dataarray[i++])) {
                     ch = dataarray[i - 1];
                     if (!XMLChar.isHighSurrogate(ch) || i >= datalength) {
                        break label78;
                     }

                     ch2 = dataarray[i++];
                     if (!XMLChar.isLowSurrogate(ch2) || !XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                        break label78;
                     }
                  }
               }

               return true;
            }
         } else {
            i = 0;

            do {
               do {
                  if (i >= datalength) {
                     return true;
                  }
               } while(!XML11Char.isXML11Invalid(dataarray[i++]));

               ch = dataarray[i - 1];
               if (!XMLChar.isHighSurrogate(ch) || i >= datalength) {
                  break;
               }

               ch2 = dataarray[i++];
            } while(XMLChar.isLowSurrogate(ch2) && XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2)));

            new Character(ch);
            return false;
         }

         new Character(ch);
         return false;
      } else {
         return true;
      }
   }

   protected Character isWFXMLChar(String chardata) {
      if (chardata != null && chardata.length() != 0) {
         char[] dataarray = chardata.toCharArray();
         int datalength = dataarray.length;
         Character refInvalidChar;
         int i;
         char ch;
         char ch2;
         if (!this.fIsXMLVersion11) {
            label78: {
               i = 0;

               while(i < datalength) {
                  if (XMLChar.isInvalid(dataarray[i++])) {
                     ch = dataarray[i - 1];
                     if (!XMLChar.isHighSurrogate(ch) || i >= datalength) {
                        break label78;
                     }

                     ch2 = dataarray[i++];
                     if (!XMLChar.isLowSurrogate(ch2) || !XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                        break label78;
                     }
                  }
               }

               return null;
            }
         } else {
            i = 0;

            do {
               do {
                  if (i >= datalength) {
                     return null;
                  }
               } while(!XML11Char.isXML11Invalid(dataarray[i++]));

               ch = dataarray[i - 1];
               if (!XMLChar.isHighSurrogate(ch) || i >= datalength) {
                  break;
               }

               ch2 = dataarray[i++];
            } while(XMLChar.isLowSurrogate(ch2) && XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2)));

            refInvalidChar = new Character(ch);
            return refInvalidChar;
         }

         refInvalidChar = new Character(ch);
         return refInvalidChar;
      } else {
         return null;
      }
   }

   protected void isCommentWellFormed(String data) {
      if (data != null && data.length() != 0) {
         char[] dataarray = data.toCharArray();
         int datalength = dataarray.length;
         int i;
         char c;
         String msg;
         char c2;
         if (this.fIsXMLVersion11) {
            i = 0;

            while(true) {
               label90:
               do {
                  while(i < datalength) {
                     c = dataarray[i++];
                     if (XML11Char.isXML11Invalid(c)) {
                        if (!XMLChar.isHighSurrogate(c) || i >= datalength) {
                           break label90;
                        }

                        c2 = dataarray[i++];
                        continue label90;
                     }

                     if (c == '-' && i < datalength && dataarray[i] == '-') {
                        msg = Utils.messages.createMessage("ER_WF_DASH_IN_COMMENT", (Object[])null);
                        if (this.fErrorHandler != null) {
                           this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character", (Exception)null, (Object)null, (DOMLocatorImpl)null));
                        }
                     }
                  }

                  return;
               } while(XMLChar.isLowSurrogate(c2) && XMLChar.isSupplemental(XMLChar.supplemental(c, c2)));

               msg = Utils.messages.createMessage("ER_WF_INVALID_CHARACTER_IN_COMMENT", new Object[]{new Character(c)});
               if (this.fErrorHandler != null) {
                  this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character", (Exception)null, (Object)null, (DOMLocatorImpl)null));
               }
            }
         } else {
            i = 0;

            while(true) {
               label70:
               do {
                  while(i < datalength) {
                     c = dataarray[i++];
                     if (XMLChar.isInvalid(c)) {
                        if (!XMLChar.isHighSurrogate(c) || i >= datalength) {
                           break label70;
                        }

                        c2 = dataarray[i++];
                        continue label70;
                     }

                     if (c == '-' && i < datalength && dataarray[i] == '-') {
                        msg = Utils.messages.createMessage("ER_WF_DASH_IN_COMMENT", (Object[])null);
                        if (this.fErrorHandler != null) {
                           this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character", (Exception)null, (Object)null, (DOMLocatorImpl)null));
                        }
                     }
                  }

                  return;
               } while(XMLChar.isLowSurrogate(c2) && XMLChar.isSupplemental(XMLChar.supplemental(c, c2)));

               msg = Utils.messages.createMessage("ER_WF_INVALID_CHARACTER_IN_COMMENT", new Object[]{new Character(c)});
               if (this.fErrorHandler != null) {
                  this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character", (Exception)null, (Object)null, (DOMLocatorImpl)null));
               }
            }
         }
      }
   }

   protected void isElementWellFormed(Node node) {
      boolean isNameWF = false;
      if ((this.fFeatures & 256) != 0) {
         isNameWF = this.isValidQName(node.getPrefix(), node.getLocalName(), this.fIsXMLVersion11);
      } else {
         isNameWF = this.isXMLName(node.getNodeName(), this.fIsXMLVersion11);
      }

      if (!isNameWF) {
         String msg = Utils.messages.createMessage("wf-invalid-character-in-node-name", new Object[]{"Element", node.getNodeName()});
         if (this.fErrorHandler != null) {
            this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character-in-node-name", (Exception)null, (Object)null, (DOMLocatorImpl)null));
         }
      }

   }

   protected void isAttributeWellFormed(Node node) {
      boolean isNameWF = false;
      if ((this.fFeatures & 256) != 0) {
         isNameWF = this.isValidQName(node.getPrefix(), node.getLocalName(), this.fIsXMLVersion11);
      } else {
         isNameWF = this.isXMLName(node.getNodeName(), this.fIsXMLVersion11);
      }

      String msg;
      if (!isNameWF) {
         msg = Utils.messages.createMessage("wf-invalid-character-in-node-name", new Object[]{"Attr", node.getNodeName()});
         if (this.fErrorHandler != null) {
            this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character-in-node-name", (Exception)null, (Object)null, (DOMLocatorImpl)null));
         }
      }

      msg = node.getNodeValue();
      if (msg.indexOf(60) >= 0) {
         String msg = Utils.messages.createMessage("ER_WF_LT_IN_ATTVAL", new Object[]{((Attr)node).getOwnerElement().getNodeName(), node.getNodeName()});
         if (this.fErrorHandler != null) {
            this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "ER_WF_LT_IN_ATTVAL", (Exception)null, (Object)null, (DOMLocatorImpl)null));
         }
      }

      NodeList children = node.getChildNodes();

      for(int i = 0; i < children.getLength(); ++i) {
         Node child = children.item(i);
         if (child != null) {
            switch (child.getNodeType()) {
               case 3:
                  this.isTextWellFormed((Text)child);
                  break;
               case 5:
                  this.isEntityReferneceWellFormed((EntityReference)child);
            }
         }
      }

   }

   protected void isPIWellFormed(ProcessingInstruction node) {
      if (!this.isXMLName(node.getNodeName(), this.fIsXMLVersion11)) {
         String msg = Utils.messages.createMessage("wf-invalid-character-in-node-name", new Object[]{"ProcessingInstruction", node.getTarget()});
         if (this.fErrorHandler != null) {
            this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character-in-node-name", (Exception)null, (Object)null, (DOMLocatorImpl)null));
         }
      }

      Character invalidChar = this.isWFXMLChar(node.getData());
      if (invalidChar != null) {
         String msg = Utils.messages.createMessage("ER_WF_INVALID_CHARACTER_IN_PI", new Object[]{Integer.toHexString(Character.getNumericValue(invalidChar))});
         if (this.fErrorHandler != null) {
            this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character", (Exception)null, (Object)null, (DOMLocatorImpl)null));
         }
      }

   }

   protected void isCDATASectionWellFormed(CDATASection node) {
      Character invalidChar = this.isWFXMLChar(node.getData());
      if (invalidChar != null) {
         String msg = Utils.messages.createMessage("ER_WF_INVALID_CHARACTER_IN_CDATA", new Object[]{Integer.toHexString(Character.getNumericValue(invalidChar))});
         if (this.fErrorHandler != null) {
            this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character", (Exception)null, (Object)null, (DOMLocatorImpl)null));
         }
      }

   }

   protected void isTextWellFormed(Text node) {
      Character invalidChar = this.isWFXMLChar(node.getData());
      if (invalidChar != null) {
         String msg = Utils.messages.createMessage("ER_WF_INVALID_CHARACTER_IN_TEXT", new Object[]{Integer.toHexString(Character.getNumericValue(invalidChar))});
         if (this.fErrorHandler != null) {
            this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character", (Exception)null, (Object)null, (DOMLocatorImpl)null));
         }
      }

   }

   protected void isEntityReferneceWellFormed(EntityReference node) {
      if (!this.isXMLName(node.getNodeName(), this.fIsXMLVersion11)) {
         String msg = Utils.messages.createMessage("wf-invalid-character-in-node-name", new Object[]{"EntityReference", node.getNodeName()});
         if (this.fErrorHandler != null) {
            this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "wf-invalid-character-in-node-name", (Exception)null, (Object)null, (DOMLocatorImpl)null));
         }
      }

      Node parent = node.getParentNode();
      DocumentType docType = node.getOwnerDocument().getDoctype();
      if (docType != null) {
         NamedNodeMap entities = docType.getEntities();

         for(int i = 0; i < entities.getLength(); ++i) {
            Entity ent = (Entity)entities.item(i);
            String nodeName = node.getNodeName() == null ? "" : node.getNodeName();
            String nodeNamespaceURI = node.getNamespaceURI() == null ? "" : node.getNamespaceURI();
            String entName = ent.getNodeName() == null ? "" : ent.getNodeName();
            String entNamespaceURI = ent.getNamespaceURI() == null ? "" : ent.getNamespaceURI();
            String msg;
            if (parent.getNodeType() == 1 && entNamespaceURI.equals(nodeNamespaceURI) && entName.equals(nodeName) && ent.getNotationName() != null) {
               msg = Utils.messages.createMessage("ER_WF_REF_TO_UNPARSED_ENT", new Object[]{node.getNodeName()});
               if (this.fErrorHandler != null) {
                  this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "ER_WF_REF_TO_UNPARSED_ENT", (Exception)null, (Object)null, (DOMLocatorImpl)null));
               }
            }

            if (parent.getNodeType() == 2 && entNamespaceURI.equals(nodeNamespaceURI) && entName.equals(nodeName) && (ent.getPublicId() != null || ent.getSystemId() != null || ent.getNotationName() != null)) {
               msg = Utils.messages.createMessage("ER_WF_REF_TO_EXTERNAL_ENT", new Object[]{node.getNodeName()});
               if (this.fErrorHandler != null) {
                  this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "ER_WF_REF_TO_EXTERNAL_ENT", (Exception)null, (Object)null, (DOMLocatorImpl)null));
               }
            }
         }
      }

   }

   protected void checkUnboundPrefixInEntRef(Node node) {
      Node next;
      for(Node child = node.getFirstChild(); child != null; child = next) {
         next = child.getNextSibling();
         if (child.getNodeType() == 1) {
            String prefix = child.getPrefix();
            if (prefix != null && this.fNSBinder.getURI(prefix) == null) {
               String msg = Utils.messages.createMessage("unbound-prefix-in-entity-reference", new Object[]{node.getNodeName(), child.getNodeName(), prefix});
               if (this.fErrorHandler != null) {
                  this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "unbound-prefix-in-entity-reference", (Exception)null, (Object)null, (DOMLocatorImpl)null));
               }
            }

            NamedNodeMap attrs = child.getAttributes();

            for(int i = 0; i < attrs.getLength(); ++i) {
               String attrPrefix = attrs.item(i).getPrefix();
               if (attrPrefix != null && this.fNSBinder.getURI(attrPrefix) == null) {
                  String msg = Utils.messages.createMessage("unbound-prefix-in-entity-reference", new Object[]{node.getNodeName(), child.getNodeName(), attrs.item(i)});
                  if (this.fErrorHandler != null) {
                     this.fErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "unbound-prefix-in-entity-reference", (Exception)null, (Object)null, (DOMLocatorImpl)null));
                  }
               }
            }
         }

         if (child.hasChildNodes()) {
            this.checkUnboundPrefixInEntRef(child);
         }
      }

   }

   protected void recordLocalNSDecl(Node node) {
      NamedNodeMap atts = ((Element)node).getAttributes();
      int length = atts.getLength();

      for(int i = 0; i < length; ++i) {
         Node attr = atts.item(i);
         String localName = attr.getLocalName();
         String attrPrefix = attr.getPrefix();
         String attrValue = attr.getNodeValue();
         String attrNS = attr.getNamespaceURI();
         localName = localName != null && !"xmlns".equals(localName) ? localName : "";
         attrPrefix = attrPrefix == null ? "" : attrPrefix;
         attrValue = attrValue == null ? "" : attrValue;
         attrNS = attrNS == null ? "" : attrNS;
         if ("http://www.w3.org/2000/xmlns/".equals(attrNS)) {
            if ("http://www.w3.org/2000/xmlns/".equals(attrValue)) {
               String msg = Utils.messages.createMessage("ER_NS_PREFIX_CANNOT_BE_BOUND", new Object[]{attrPrefix, "http://www.w3.org/2000/xmlns/"});
               if (this.fErrorHandler != null) {
                  this.fErrorHandler.handleError(new DOMErrorImpl((short)2, msg, "ER_NS_PREFIX_CANNOT_BE_BOUND", (Exception)null, (Object)null, (DOMLocatorImpl)null));
               }
            } else if ("xmlns".equals(attrPrefix)) {
               if (attrValue.length() != 0) {
                  this.fNSBinder.declarePrefix(localName, attrValue);
               }
            } else {
               this.fNSBinder.declarePrefix("", attrValue);
            }
         }
      }

   }

   protected void fixupElementNS(Node node) throws SAXException {
      String namespaceURI = ((Element)node).getNamespaceURI();
      String prefix = ((Element)node).getPrefix();
      String localName = ((Element)node).getLocalName();
      String msg;
      if (namespaceURI != null) {
         prefix = prefix == null ? "" : prefix;
         msg = this.fNSBinder.getURI(prefix);
         if (msg == null || !msg.equals(namespaceURI)) {
            if ((this.fFeatures & 512) != 0) {
               if (!"".equals(prefix) && !"".equals(namespaceURI)) {
                  ((Element)node).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, namespaceURI);
               } else {
                  ((Element)node).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", namespaceURI);
               }
            }

            this.fLocalNSBinder.declarePrefix(prefix, namespaceURI);
            this.fNSBinder.declarePrefix(prefix, namespaceURI);
         }
      } else if (localName != null && !"".equals(localName)) {
         namespaceURI = this.fNSBinder.getURI("");
         if (namespaceURI != null && namespaceURI.length() > 0) {
            ((Element)node).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "");
            this.fLocalNSBinder.declarePrefix("", "");
            this.fNSBinder.declarePrefix("", "");
         }
      } else {
         msg = Utils.messages.createMessage("ER_NULL_LOCAL_ELEMENT_NAME", new Object[]{node.getNodeName()});
         if (this.fErrorHandler != null) {
            this.fErrorHandler.handleError(new DOMErrorImpl((short)2, msg, "ER_NULL_LOCAL_ELEMENT_NAME", (Exception)null, (Object)null, (DOMLocatorImpl)null));
         }
      }

   }

   protected void initProperties(Properties properties) {
      Enumeration keys = properties.keys();

      while(keys.hasMoreElements()) {
         String key = (String)keys.nextElement();
         Object iobj = s_propKeys.get(key);
         if (iobj != null) {
            if (iobj instanceof Integer) {
               int BITFLAG = (Integer)iobj;
               if (properties.getProperty(key).endsWith("yes")) {
                  this.fFeatures |= BITFLAG;
               } else {
                  this.fFeatures &= ~BITFLAG;
               }
            } else if ("{http://www.w3.org/TR/DOM-Level-3-LS}format-pretty-print".equals(key)) {
               if (properties.getProperty(key).endsWith("yes")) {
                  this.fSerializer.setIndent(true);
                  this.fSerializer.setIndentAmount(3);
               } else {
                  this.fSerializer.setIndent(false);
               }
            } else if ("omit-xml-declaration".equals(key)) {
               if (properties.getProperty(key).endsWith("yes")) {
                  this.fSerializer.setOmitXMLDeclaration(true);
               } else {
                  this.fSerializer.setOmitXMLDeclaration(false);
               }
            } else {
               String encoding;
               if ("{http://xml.apache.org/xerces-2j}xml-version".equals(key)) {
                  encoding = properties.getProperty(key);
                  if ("1.1".equals(encoding)) {
                     this.fIsXMLVersion11 = true;
                     this.fSerializer.setVersion(encoding);
                  } else {
                     this.fSerializer.setVersion("1.0");
                  }
               } else if ("encoding".equals(key)) {
                  encoding = properties.getProperty(key);
                  if (encoding != null) {
                     this.fSerializer.setEncoding(encoding);
                  }
               } else if ("{http://xml.apache.org/xerces-2j}entities".equals(key)) {
                  if (properties.getProperty(key).endsWith("yes")) {
                     this.fSerializer.setDTDEntityExpansion(false);
                  } else {
                     this.fSerializer.setDTDEntityExpansion(true);
                  }
               }
            }
         }
      }

      if (this.fNewLine != null) {
         this.fSerializer.setOutputProperty("{http://xml.apache.org/xalan}line-separator", this.fNewLine);
      }

   }

   static {
      int i = 2;
      Integer val = new Integer(i);
      s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", val);
      int i1 = 8;
      val = new Integer(i1);
      s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}comments", val);
      int i2 = 32;
      val = new Integer(i2);
      s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", val);
      int i3 = 64;
      val = new Integer(i3);
      s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}entities", val);
      int i4 = 256;
      val = new Integer(i4);
      s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", val);
      int i5 = 512;
      val = new Integer(i5);
      s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", val);
      int i6 = 2048;
      val = new Integer(i6);
      s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}split-cdata-sections", val);
      int i7 = 16384;
      val = new Integer(i7);
      s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", val);
      int i8 = '耀';
      val = new Integer(i8);
      s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}discard-default-content", val);
      s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}format-pretty-print", "");
      s_propKeys.put("omit-xml-declaration", "");
      s_propKeys.put("{http://xml.apache.org/xerces-2j}xml-version", "");
      s_propKeys.put("encoding", "");
      s_propKeys.put("{http://xml.apache.org/xerces-2j}entities", "");
   }
}
