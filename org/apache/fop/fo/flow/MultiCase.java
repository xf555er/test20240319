package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.xml.sax.Locator;

public class MultiCase extends FObj {
   private int startingState;
   private String caseName;
   private String caseTitle;

   public MultiCase(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.startingState = pList.get(234).getEnum();
      this.caseName = pList.get(61).getString();
      this.caseTitle = pList.get(62).getString();
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         if (!this.isBlockOrInlineItem(nsURI, localName) || "marker".equals(localName)) {
            this.invalidChildError(loc, nsURI, localName);
         }

         if (!"multi-toggle".equals(localName)) {
            FONode.validateChildNode(this.getParent().getParent(), loc, nsURI, localName);
         }
      }

   }

   public void endOfNode() throws FOPException {
      if (this.firstChild == null) {
         this.missingChildElementError("(#PCDATA|%inline;|%block)*");
      }

   }

   public int getStartingState() {
      return this.startingState;
   }

   public String getLocalName() {
      return "multi-case";
   }

   public int getNameId() {
      return 45;
   }

   public String getCaseName() {
      return this.caseName;
   }

   public String getCaseTitle() {
      return this.caseTitle;
   }
}
