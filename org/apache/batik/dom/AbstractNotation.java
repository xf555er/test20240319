package org.apache.batik.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Notation;

public abstract class AbstractNotation extends AbstractNode implements Notation {
   protected String nodeName;
   protected String publicId;
   protected String systemId;

   public short getNodeType() {
      return 12;
   }

   public void setNodeName(String v) {
      this.nodeName = v;
   }

   public String getNodeName() {
      return this.nodeName;
   }

   public String getPublicId() {
      return this.publicId;
   }

   public void setPublicId(String id) {
      this.publicId = id;
   }

   public String getSystemId() {
      return this.systemId;
   }

   public void setSystemId(String id) {
      this.systemId = id;
   }

   public void setTextContent(String s) throws DOMException {
   }

   protected Node export(Node n, AbstractDocument d) {
      super.export(n, d);
      AbstractNotation an = (AbstractNotation)n;
      an.nodeName = this.nodeName;
      an.publicId = this.publicId;
      an.systemId = this.systemId;
      return n;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      super.deepExport(n, d);
      AbstractNotation an = (AbstractNotation)n;
      an.nodeName = this.nodeName;
      an.publicId = this.publicId;
      an.systemId = this.systemId;
      return n;
   }

   protected Node copyInto(Node n) {
      super.copyInto(n);
      AbstractNotation an = (AbstractNotation)n;
      an.nodeName = this.nodeName;
      an.publicId = this.publicId;
      an.systemId = this.systemId;
      return n;
   }

   protected Node deepCopyInto(Node n) {
      super.deepCopyInto(n);
      AbstractNotation an = (AbstractNotation)n;
      an.nodeName = this.nodeName;
      an.publicId = this.publicId;
      an.systemId = this.systemId;
      return n;
   }
}
