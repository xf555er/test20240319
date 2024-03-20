package net.jsign.poi.poifs.filesystem;

import net.jsign.poi.poifs.property.Property;

public abstract class EntryNode implements Entry {
   private Property _property;
   private DirectoryNode _parent;

   protected EntryNode(Property property, DirectoryNode parent) {
      this._property = property;
      this._parent = parent;
   }

   protected Property getProperty() {
      return this._property;
   }

   public String getName() {
      return this._property.getName();
   }

   public DirectoryEntry getParent() {
      return this._parent;
   }
}
