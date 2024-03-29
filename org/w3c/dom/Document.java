package org.w3c.dom;

public interface Document extends Node {
   DocumentType getDoctype();

   DOMImplementation getImplementation();

   Element getDocumentElement();

   Element createElement(String var1) throws DOMException;

   DocumentFragment createDocumentFragment();

   Text createTextNode(String var1);

   Comment createComment(String var1);

   CDATASection createCDATASection(String var1) throws DOMException;

   ProcessingInstruction createProcessingInstruction(String var1, String var2) throws DOMException;

   Attr createAttribute(String var1) throws DOMException;

   EntityReference createEntityReference(String var1) throws DOMException;

   NodeList getElementsByTagName(String var1);

   Node importNode(Node var1, boolean var2) throws DOMException;

   Element createElementNS(String var1, String var2) throws DOMException;

   Attr createAttributeNS(String var1, String var2) throws DOMException;

   NodeList getElementsByTagNameNS(String var1, String var2);

   Element getElementById(String var1);
}
