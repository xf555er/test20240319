package org.apache.batik.dom;

import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class GenericText extends AbstractText {
   protected boolean readonly;

   protected GenericText() {
   }

   public GenericText(String value, AbstractDocument owner) {
      this.ownerDocument = owner;
      this.setNodeValue(value);
   }

   public String getNodeName() {
      return "#text";
   }

   public short getNodeType() {
      return 3;
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected Text createTextNode(String text) {
      return this.getOwnerDocument().createTextNode(text);
   }

   protected Node newNode() {
      return new GenericText();
   }
}
