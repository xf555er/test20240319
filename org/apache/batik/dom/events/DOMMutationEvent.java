package org.apache.batik.dom.events;

import org.w3c.dom.Node;
import org.w3c.dom.events.MutationEvent;

public class DOMMutationEvent extends AbstractEvent implements MutationEvent {
   private Node relatedNode;
   private String prevValue;
   private String newValue;
   private String attrName;
   private short attrChange;

   public Node getRelatedNode() {
      return this.relatedNode;
   }

   public String getPrevValue() {
      return this.prevValue;
   }

   public String getNewValue() {
      return this.newValue;
   }

   public String getAttrName() {
      return this.attrName;
   }

   public short getAttrChange() {
      return this.attrChange;
   }

   public void initMutationEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg, Node relatedNodeArg, String prevValueArg, String newValueArg, String attrNameArg, short attrChangeArg) {
      this.initEvent(typeArg, canBubbleArg, cancelableArg);
      this.relatedNode = relatedNodeArg;
      this.prevValue = prevValueArg;
      this.newValue = newValueArg;
      this.attrName = attrNameArg;
      this.attrChange = attrChangeArg;
   }

   public void initMutationEventNS(String namespaceURIArg, String typeArg, boolean canBubbleArg, boolean cancelableArg, Node relatedNodeArg, String prevValueArg, String newValueArg, String attrNameArg, short attrChangeArg) {
      this.initEventNS(namespaceURIArg, typeArg, canBubbleArg, cancelableArg);
      this.relatedNode = relatedNodeArg;
      this.prevValue = prevValueArg;
      this.newValue = newValueArg;
      this.attrName = attrNameArg;
      this.attrChange = attrChangeArg;
   }
}
