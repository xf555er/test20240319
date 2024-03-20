package org.apache.batik.dom;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class GenericCDATASection extends AbstractText implements CDATASection {
   protected boolean readonly;

   protected GenericCDATASection() {
   }

   public GenericCDATASection(String value, AbstractDocument owner) {
      this.ownerDocument = owner;
      this.setNodeValue(value);
   }

   public String getNodeName() {
      return "#cdata-section";
   }

   public short getNodeType() {
      return 4;
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected Text createTextNode(String text) {
      return this.getOwnerDocument().createCDATASection(text);
   }

   protected Node newNode() {
      return new GenericCDATASection();
   }
}
