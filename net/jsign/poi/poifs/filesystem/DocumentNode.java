package net.jsign.poi.poifs.filesystem;

import net.jsign.poi.poifs.property.DocumentProperty;

public class DocumentNode extends EntryNode implements DocumentEntry {
   private POIFSDocument _document;

   DocumentNode(DocumentProperty property, DirectoryNode parent) {
      super(property, parent);
      this._document = property.getDocument();
   }

   public int getSize() {
      return this.getProperty().getSize();
   }
}
