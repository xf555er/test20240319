package net.jsign.poi.poifs.filesystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.jsign.poi.hpsf.ClassID;
import net.jsign.poi.poifs.property.DirectoryProperty;
import net.jsign.poi.poifs.property.DocumentProperty;
import net.jsign.poi.poifs.property.Property;

public class DirectoryNode extends EntryNode implements Iterable, DirectoryEntry {
   private final Map _byname = new HashMap();
   private final ArrayList _entries = new ArrayList();
   private final POIFSFileSystem _filesystem;
   private final POIFSDocumentPath _path;

   DirectoryNode(DirectoryProperty property, POIFSFileSystem filesystem, DirectoryNode parent) {
      super(property, parent);
      this._filesystem = filesystem;
      if (parent == null) {
         this._path = new POIFSDocumentPath();
      } else {
         this._path = new POIFSDocumentPath(parent._path, new String[]{property.getName()});
      }

      Iterator iter = property.getChildren();

      while(iter.hasNext()) {
         Property child = (Property)iter.next();
         Object childNode;
         if (child.isDirectory()) {
            DirectoryProperty childDir = (DirectoryProperty)child;
            childNode = new DirectoryNode(childDir, this._filesystem, this);
         } else {
            childNode = new DocumentNode((DocumentProperty)child, this);
         }

         this._entries.add(childNode);
         this._byname.put(((Entry)childNode).getName(), childNode);
      }

   }

   public POIFSFileSystem getFileSystem() {
      return this._filesystem;
   }

   DocumentEntry createDocument(POIFSDocument document) throws IOException {
      DocumentProperty property = document.getDocumentProperty();
      DocumentNode rval = new DocumentNode(property, this);
      ((DirectoryProperty)this.getProperty()).addChild(property);
      this._filesystem.addDocument(document);
      this._entries.add(rval);
      this._byname.put(property.getName(), rval);
      return rval;
   }

   public Iterator getEntries() {
      return this._entries.iterator();
   }

   public boolean hasEntry(String name) {
      return name != null && this._byname.containsKey(name);
   }

   public Entry getEntry(String name) throws FileNotFoundException {
      Entry rval = null;
      if (name != null) {
         rval = (Entry)this._byname.get(name);
      }

      if (rval == null) {
         if (this._byname.containsKey("Workbook")) {
            throw new IllegalArgumentException("The document is really a XLS file");
         } else if (this._byname.containsKey("PowerPoint Document")) {
            throw new IllegalArgumentException("The document is really a PPT file");
         } else if (this._byname.containsKey("VisioDocument")) {
            throw new IllegalArgumentException("The document is really a VSD file");
         } else {
            throw new FileNotFoundException("no such entry: \"" + name + "\", had: " + this._byname.keySet());
         }
      } else {
         return rval;
      }
   }

   public DocumentEntry createDocument(String name, InputStream stream) throws IOException {
      return this.createDocument(new POIFSDocument(name, this._filesystem, stream));
   }

   public DocumentEntry createOrUpdateDocument(String name, InputStream stream) throws IOException {
      if (!this.hasEntry(name)) {
         return this.createDocument(name, stream);
      } else {
         DocumentNode existing = (DocumentNode)this.getEntry(name);
         POIFSDocument nDoc = new POIFSDocument(existing);
         nDoc.replaceContents(stream);
         return existing;
      }
   }

   public ClassID getStorageClsid() {
      return this.getProperty().getStorageClsid();
   }

   public Iterator iterator() {
      return this.getEntries();
   }
}
