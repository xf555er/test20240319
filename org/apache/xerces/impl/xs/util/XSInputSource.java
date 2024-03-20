package org.apache.xerces.impl.xs.util;

import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xs.XSObject;

public final class XSInputSource extends XMLInputSource {
   private SchemaGrammar[] fGrammars;
   private XSObject[] fComponents;

   public XSInputSource(SchemaGrammar[] var1) {
      super((String)null, (String)null, (String)null);
      this.fGrammars = var1;
      this.fComponents = null;
   }

   public XSInputSource(XSObject[] var1) {
      super((String)null, (String)null, (String)null);
      this.fGrammars = null;
      this.fComponents = var1;
   }

   public SchemaGrammar[] getGrammars() {
      return this.fGrammars;
   }

   public void setGrammars(SchemaGrammar[] var1) {
      this.fGrammars = var1;
   }

   public XSObject[] getComponents() {
      return this.fComponents;
   }

   public void setComponents(XSObject[] var1) {
      this.fComponents = var1;
   }
}
