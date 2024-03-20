package org.apache.batik.dom;

import org.w3c.dom.Entity;
import org.w3c.dom.Node;

public abstract class AbstractEntity extends AbstractParentNode implements Entity {
   protected String nodeName;
   protected String publicId;
   protected String systemId;

   public short getNodeType() {
      return 6;
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

   public String getNotationName() {
      return this.getNodeName();
   }

   public void setNotationName(String name) {
      this.setNodeName(name);
   }

   public String getInputEncoding() {
      return null;
   }

   public String getXmlEncoding() {
      return null;
   }

   public String getXmlVersion() {
      return null;
   }

   protected Node export(Node n, AbstractDocument d) {
      super.export(n, d);
      AbstractEntity ae = (AbstractEntity)n;
      ae.nodeName = this.nodeName;
      ae.publicId = this.publicId;
      ae.systemId = this.systemId;
      return n;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      super.deepExport(n, d);
      AbstractEntity ae = (AbstractEntity)n;
      ae.nodeName = this.nodeName;
      ae.publicId = this.publicId;
      ae.systemId = this.systemId;
      return n;
   }

   protected Node copyInto(Node n) {
      super.copyInto(n);
      AbstractEntity ae = (AbstractEntity)n;
      ae.nodeName = this.nodeName;
      ae.publicId = this.publicId;
      ae.systemId = this.systemId;
      return n;
   }

   protected Node deepCopyInto(Node n) {
      super.deepCopyInto(n);
      AbstractEntity ae = (AbstractEntity)n;
      ae.nodeName = this.nodeName;
      ae.publicId = this.publicId;
      ae.systemId = this.systemId;
      return n;
   }

   protected void checkChildType(Node n, boolean replace) {
      switch (n.getNodeType()) {
         case 1:
         case 3:
         case 4:
         case 5:
         case 7:
         case 8:
         case 11:
            return;
         case 2:
         case 6:
         case 9:
         case 10:
         default:
            throw this.createDOMException((short)3, "child.type", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName(), Integer.valueOf(n.getNodeType()), n.getNodeName()});
      }
   }
}
