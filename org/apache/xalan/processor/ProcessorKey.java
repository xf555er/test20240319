package org.apache.xalan.processor;

import java.util.ArrayList;
import java.util.List;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.KeyDeclaration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class ProcessorKey extends XSLTElementProcessor {
   static final long serialVersionUID = 4285205417566822979L;

   public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
      KeyDeclaration kd = new KeyDeclaration(handler.getStylesheet(), handler.nextUid());
      kd.setDOMBackPointer(handler.getOriginatingNode());
      kd.setLocaterInfo(handler.getLocator());
      this.setPropertiesFromAttributes(handler, rawName, attributes, kd);
      handler.getStylesheet().setKey(kd);
   }

   void setPropertiesFromAttributes(StylesheetHandler handler, String rawName, Attributes attributes, ElemTemplateElement target) throws SAXException {
      XSLTElementDef def = this.getElemDef();
      List processedDefs = new ArrayList();
      int nAttrs = attributes.getLength();

      XSLTAttributeDef attrDef;
      String defVal;
      for(int i = 0; i < nAttrs; ++i) {
         String attrUri = attributes.getURI(i);
         String attrLocalName = attributes.getLocalName(i);
         attrDef = def.getAttributeDef(attrUri, attrLocalName);
         if (null == attrDef) {
            handler.error(attributes.getQName(i) + "attribute is not allowed on the " + rawName + " element!", (Exception)null);
         } else {
            defVal = attributes.getValue(i);
            if (defVal.indexOf("key(") >= 0) {
               handler.error(XSLMessages.createMessage("ER_INVALID_KEY_CALL", (Object[])null), (Exception)null);
            }

            processedDefs.add(attrDef);
            attrDef.setAttrValue(handler, attrUri, attrLocalName, attributes.getQName(i), attributes.getValue(i), target);
         }
      }

      XSLTAttributeDef[] attrDefs = def.getAttributes();
      int nAttrDefs = attrDefs.length;

      for(int i = 0; i < nAttrDefs; ++i) {
         attrDef = attrDefs[i];
         defVal = attrDef.getDefault();
         if (null != defVal && !processedDefs.contains(attrDef)) {
            attrDef.setDefAttrValue(handler, target);
         }

         if (attrDef.getRequired() && !processedDefs.contains(attrDef)) {
            handler.error(XSLMessages.createMessage("ER_REQUIRES_ATTRIB", new Object[]{rawName, attrDef.getName()}), (Exception)null);
         }
      }

   }
}
