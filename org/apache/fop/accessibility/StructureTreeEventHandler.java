package org.apache.fop.accessibility;

import java.util.Locale;
import org.xml.sax.Attributes;

public interface StructureTreeEventHandler {
   void startPageSequence(Locale var1, String var2);

   StructureTreeElement startNode(String var1, Attributes var2, StructureTreeElement var3);

   void endNode(String var1);

   StructureTreeElement startImageNode(String var1, Attributes var2, StructureTreeElement var3);

   StructureTreeElement startReferencedNode(String var1, Attributes var2, StructureTreeElement var3);

   void endPageSequence();
}
