package org.apache.batik.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public abstract class AbstractProcessingInstruction extends AbstractChildNode implements ProcessingInstruction {
   protected String data;

   public String getNodeName() {
      return this.getTarget();
   }

   public short getNodeType() {
      return 7;
   }

   public String getNodeValue() throws DOMException {
      return this.getData();
   }

   public void setNodeValue(String nodeValue) throws DOMException {
      this.setData(nodeValue);
   }

   public String getData() {
      return this.data;
   }

   public void setData(String data) throws DOMException {
      if (this.isReadonly()) {
         throw this.createDOMException((short)7, "readonly.node", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName()});
      } else {
         String val = this.data;
         this.data = data;
         this.fireDOMCharacterDataModifiedEvent(val, this.data);
         if (this.getParentNode() != null) {
            ((AbstractParentNode)this.getParentNode()).fireDOMSubtreeModifiedEvent();
         }

      }
   }

   public String getTextContent() {
      return this.getNodeValue();
   }

   protected Node export(Node n, AbstractDocument d) {
      AbstractProcessingInstruction p = (AbstractProcessingInstruction)super.export(n, d);
      p.data = this.data;
      return p;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      AbstractProcessingInstruction p = (AbstractProcessingInstruction)super.deepExport(n, d);
      p.data = this.data;
      return p;
   }

   protected Node copyInto(Node n) {
      AbstractProcessingInstruction p = (AbstractProcessingInstruction)super.copyInto(n);
      p.data = this.data;
      return p;
   }

   protected Node deepCopyInto(Node n) {
      AbstractProcessingInstruction p = (AbstractProcessingInstruction)super.deepCopyInto(n);
      p.data = this.data;
      return p;
   }
}
