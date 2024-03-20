package org.apache.fop.render.intermediate.extensions;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.xmlgraphics.util.XMLizable;

public abstract class AbstractAction implements XMLizable {
   private String id;
   private StructureTreeElement structureTreeElement;

   public void setID(String id) {
      this.id = id;
   }

   public String getID() {
      return this.id;
   }

   public void setStructureTreeElement(StructureTreeElement structureTreeElement) {
      this.structureTreeElement = structureTreeElement;
   }

   public StructureTreeElement getStructureTreeElement() {
      return this.structureTreeElement;
   }

   public boolean hasID() {
      return this.id != null;
   }

   public abstract boolean isSame(AbstractAction var1);

   public boolean isComplete() {
      return true;
   }

   public String getIDPrefix() {
      return null;
   }
}
