package org.apache.batik.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class GenericElement extends AbstractElement {
   protected String nodeName;
   protected boolean readonly;

   protected GenericElement() {
   }

   public GenericElement(String name, AbstractDocument owner) throws DOMException {
      super(name, owner);
      this.nodeName = name;
   }

   public void setNodeName(String v) {
      this.nodeName = v;
   }

   public String getNodeName() {
      return this.nodeName;
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected Node export(Node n, AbstractDocument d) {
      super.export(n, d);
      GenericElement ge = (GenericElement)n;
      ge.nodeName = this.nodeName;
      return n;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      super.deepExport(n, d);
      GenericElement ge = (GenericElement)n;
      ge.nodeName = this.nodeName;
      return n;
   }

   protected Node copyInto(Node n) {
      GenericElement ge = (GenericElement)super.copyInto(n);
      ge.nodeName = this.nodeName;
      return n;
   }

   protected Node deepCopyInto(Node n) {
      GenericElement ge = (GenericElement)super.deepCopyInto(n);
      ge.nodeName = this.nodeName;
      return n;
   }

   protected Node newNode() {
      return new GenericElement();
   }
}
