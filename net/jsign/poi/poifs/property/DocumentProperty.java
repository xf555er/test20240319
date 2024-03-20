package net.jsign.poi.poifs.property;

import net.jsign.poi.poifs.filesystem.POIFSDocument;

public class DocumentProperty extends Property {
   private POIFSDocument _document = null;

   public DocumentProperty(String name, int size) {
      this.setName(name);
      this.setSize(size);
      this.setNodeColor((byte)1);
      this.setPropertyType((byte)2);
   }

   protected DocumentProperty(int index, byte[] array, int offset) {
      super(index, array, offset);
   }

   public void setDocument(POIFSDocument doc) {
      this._document = doc;
   }

   public POIFSDocument getDocument() {
      return this._document;
   }

   public boolean isDirectory() {
      return false;
   }

   protected void preWrite() {
   }

   public void updateSize(int size) {
      this.setSize(size);
   }
}
