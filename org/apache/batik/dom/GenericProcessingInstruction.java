package org.apache.batik.dom;

import org.w3c.dom.Node;

public class GenericProcessingInstruction extends AbstractProcessingInstruction {
   protected String target;
   protected boolean readonly;

   protected GenericProcessingInstruction() {
   }

   public GenericProcessingInstruction(String target, String data, AbstractDocument owner) {
      this.ownerDocument = owner;
      this.setTarget(target);
      this.setData(data);
   }

   public void setNodeName(String v) {
      this.setTarget(v);
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   public String getTarget() {
      return this.target;
   }

   public void setTarget(String v) {
      this.target = v;
   }

   protected Node export(Node n, AbstractDocument d) {
      GenericProcessingInstruction p = (GenericProcessingInstruction)super.export(n, d);
      p.setTarget(this.getTarget());
      return p;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      GenericProcessingInstruction p = (GenericProcessingInstruction)super.deepExport(n, d);
      p.setTarget(this.getTarget());
      return p;
   }

   protected Node copyInto(Node n) {
      GenericProcessingInstruction p = (GenericProcessingInstruction)super.copyInto(n);
      p.setTarget(this.getTarget());
      return p;
   }

   protected Node deepCopyInto(Node n) {
      GenericProcessingInstruction p = (GenericProcessingInstruction)super.deepCopyInto(n);
      p.setTarget(this.getTarget());
      return p;
   }

   protected Node newNode() {
      return new GenericProcessingInstruction();
   }
}
