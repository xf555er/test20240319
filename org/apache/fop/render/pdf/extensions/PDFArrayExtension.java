package org.apache.fop.render.pdf.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDFArrayExtension extends PDFCollectionExtension {
   private static final long serialVersionUID = -1L;
   private Map properties = new HashMap();
   private List entries = new ArrayList();

   PDFArrayExtension() {
      super(PDFObjectType.Array);
   }

   public void setValue(Object value) {
      throw new UnsupportedOperationException();
   }

   public Object getValue() {
      return this.getEntries();
   }

   public void setProperty(String name, String value) {
      this.properties.put(name, value);
   }

   public String getProperty(String name) {
      return (String)this.properties.get(name);
   }

   public void addEntry(PDFCollectionEntryExtension entry) {
      if (entry.getKey() != null) {
         throw new IllegalArgumentException();
      } else {
         this.entries.add(entry);
      }
   }

   public List getEntries() {
      return this.entries;
   }

   public PDFCollectionEntryExtension getLastEntry() {
      return this.entries.size() > 0 ? (PDFCollectionEntryExtension)this.entries.get(this.entries.size() - 1) : null;
   }

   public String getElementName() {
      return PDFObjectType.Array.elementName();
   }
}
