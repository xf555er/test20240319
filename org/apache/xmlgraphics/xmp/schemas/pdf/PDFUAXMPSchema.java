package org.apache.xmlgraphics.xmp.schemas.pdf;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSchema;
import org.apache.xmlgraphics.xmp.merge.MergeRuleSet;

public class PDFUAXMPSchema extends XMPSchema {
   public static final String NAMESPACE = "http://www.aiim.org/pdfua/ns/id/";
   private static MergeRuleSet mergeRuleSet = new MergeRuleSet();

   public PDFUAXMPSchema() {
      super("http://www.aiim.org/pdfua/ns/id/", "pdfuaid");
   }

   public static PDFUAAdapter getAdapter(Metadata meta) {
      return new PDFUAAdapter(meta, "http://www.aiim.org/pdfua/ns/id/");
   }

   public MergeRuleSet getDefaultMergeRuleSet() {
      return mergeRuleSet;
   }
}
