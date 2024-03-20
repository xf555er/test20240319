package org.apache.fop.render.pdf.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PDFDictionaryExtension extends PDFCollectionExtension {
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_PAGE_NUMBERS = "page-numbers";
   private static final long serialVersionUID = -1L;
   private PDFDictionaryType dictionaryType;
   private Map properties;
   private List entries;

   PDFDictionaryExtension() {
      this(PDFDictionaryType.Dictionary);
   }

   PDFDictionaryExtension(PDFDictionaryType dictionaryType) {
      super(PDFObjectType.Dictionary);
      this.dictionaryType = dictionaryType;
      this.properties = new HashMap();
      this.entries = new ArrayList();
   }

   public void setValue(Object value) {
      throw new UnsupportedOperationException();
   }

   public Object getValue() {
      return this.getEntries();
   }

   public PDFDictionaryType getDictionaryType() {
      return this.dictionaryType;
   }

   public void setProperty(String name, String value) {
      this.properties.put(name, value);
   }

   public String getProperty(String name) {
      return (String)this.properties.get(name);
   }

   public void addEntry(PDFCollectionEntryExtension entry) {
      if (entry.getKey() != null && entry.getKey().length() != 0) {
         this.entries.add(entry);
      } else {
         throw new IllegalArgumentException("pdf:dictionary key is empty");
      }
   }

   public List getEntries() {
      return this.entries;
   }

   public PDFCollectionEntryExtension findEntry(String key) {
      Iterator var2 = this.entries.iterator();

      PDFCollectionEntryExtension entry;
      String entryKey;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         entry = (PDFCollectionEntryExtension)var2.next();
         entryKey = entry.getKey();
      } while(entryKey == null || !entryKey.equals(key));

      return entry;
   }

   public Object findEntryValue(String key) {
      Iterator var2 = this.entries.iterator();

      PDFCollectionEntryExtension entry;
      String entryKey;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         entry = (PDFCollectionEntryExtension)var2.next();
         entryKey = entry.getKey();
      } while(entryKey == null || !entryKey.equals(key));

      return entry.getValue();
   }

   public PDFCollectionEntryExtension getLastEntry() {
      return this.entries.size() > 0 ? (PDFCollectionEntryExtension)this.entries.get(this.entries.size() - 1) : null;
   }

   public boolean usesIDAttribute() {
      return this.dictionaryType.usesIDAttribute();
   }

   public String getElementName() {
      return this.dictionaryType.elementName();
   }
}
