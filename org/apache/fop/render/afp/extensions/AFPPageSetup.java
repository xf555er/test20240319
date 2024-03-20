package org.apache.fop.render.afp.extensions;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class AFPPageSetup extends AFPExtensionAttachment {
   protected static final String ATT_VALUE = "value";
   protected static final String ATT_ENCODING = "encoding";
   protected static final String ATT_PLACEMENT = "placement";
   protected String content;
   protected String value;
   protected ExtensionPlacement placement;
   protected int encoding;
   private static final long serialVersionUID = -549941295384013190L;

   public int getEncoding() {
      return this.encoding;
   }

   public void setEncoding(int encoding) {
      this.encoding = encoding;
   }

   public AFPPageSetup(String elementName) {
      super(elementName);
      this.placement = ExtensionPlacement.DEFAULT;
      this.encoding = -1;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String source) {
      this.value = source;
   }

   public String getContent() {
      return this.content;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public ExtensionPlacement getPlacement() {
      return this.placement;
   }

   public void setPlacement(ExtensionPlacement placement) {
      if (!"no-operation".equals(this.getElementName())) {
         throw new UnsupportedOperationException("The attribute 'placement' can currently only be set for NOPs!");
      } else {
         this.placement = placement;
      }
   }

   public void toSAX(ContentHandler handler) throws SAXException {
      AttributesImpl atts = new AttributesImpl();
      if (this.name != null && this.name.length() > 0) {
         atts.addAttribute("", "name", "name", "CDATA", this.name);
      }

      if (this.value != null && this.value.length() > 0) {
         atts.addAttribute("", "value", "value", "CDATA", this.value);
      }

      if (this.placement != ExtensionPlacement.DEFAULT) {
         atts.addAttribute("", "placement", "placement", "CDATA", this.placement.getXMLValue());
      }

      if (this.encoding != -1) {
         atts.addAttribute("", "encoding", "encoding", "CDATA", String.valueOf(this.encoding));
      }

      handler.startElement("apache:fop:extensions:afp", this.elementName, this.elementName, atts);
      if (this.content != null && this.content.length() > 0) {
         char[] chars = this.content.toCharArray();
         handler.characters(chars, 0, chars.length);
      }

      handler.endElement("apache:fop:extensions:afp", this.elementName, this.elementName);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder("AFPPageSetup(");
      sb.append("element-name=").append(this.getElementName());
      sb.append(" name=").append(this.getName());
      sb.append(" value=").append(this.getValue());
      if (this.getPlacement() != ExtensionPlacement.DEFAULT) {
         sb.append(" placement=").append(this.getPlacement());
      }

      sb.append(")");
      return sb.toString();
   }
}
