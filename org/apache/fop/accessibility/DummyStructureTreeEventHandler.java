package org.apache.fop.accessibility;

import java.util.Locale;
import org.xml.sax.Attributes;

public final class DummyStructureTreeEventHandler implements StructureTreeEventHandler {
   public static final StructureTreeEventHandler INSTANCE = new DummyStructureTreeEventHandler();

   private DummyStructureTreeEventHandler() {
   }

   public void startPageSequence(Locale locale, String role) {
   }

   public void endPageSequence() {
   }

   public StructureTreeElement startNode(String name, Attributes attributes, StructureTreeElement parent) {
      return null;
   }

   public void endNode(String name) {
   }

   public StructureTreeElement startImageNode(String name, Attributes attributes, StructureTreeElement parent) {
      return null;
   }

   public StructureTreeElement startReferencedNode(String name, Attributes attributes, StructureTreeElement parent) {
      return null;
   }
}
