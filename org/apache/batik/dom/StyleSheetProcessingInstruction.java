package org.apache.batik.dom;

import java.util.HashMap;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.stylesheets.LinkStyle;
import org.w3c.dom.stylesheets.StyleSheet;

public class StyleSheetProcessingInstruction extends AbstractProcessingInstruction implements LinkStyle {
   protected boolean readonly;
   protected transient StyleSheet sheet;
   protected StyleSheetFactory factory;
   protected transient HashMap pseudoAttributes;

   protected StyleSheetProcessingInstruction() {
   }

   public StyleSheetProcessingInstruction(String data, AbstractDocument owner, StyleSheetFactory f) {
      this.ownerDocument = owner;
      this.setData(data);
      this.factory = f;
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   public void setNodeName(String v) {
   }

   public String getTarget() {
      return "xml-stylesheet";
   }

   public StyleSheet getSheet() {
      if (this.sheet == null) {
         this.sheet = this.factory.createStyleSheet(this, this.getPseudoAttributes());
      }

      return this.sheet;
   }

   public HashMap getPseudoAttributes() {
      if (this.pseudoAttributes == null) {
         this.pseudoAttributes = new HashMap();
         this.pseudoAttributes.put("alternate", "no");
         this.pseudoAttributes.put("media", "all");
         DOMUtilities.parseStyleSheetPIData(this.data, this.pseudoAttributes);
      }

      return this.pseudoAttributes;
   }

   public void setData(String data) throws DOMException {
      super.setData(data);
      this.sheet = null;
      this.pseudoAttributes = null;
   }

   protected Node newNode() {
      return new StyleSheetProcessingInstruction();
   }
}
