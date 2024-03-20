package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.util.LanguageTags;

public class PDFStructElem extends StructureHierarchyMember implements StructureTreeElement, CompressedObject, Serializable {
   private static final long serialVersionUID = -3055241807589202532L;
   private StructureType structureType;
   protected PDFStructElem parentElement;
   protected List kids;
   private List attributes;

   public PDFStructElem() {
   }

   public PDFStructElem(PDFObject parent, StructureType structureType) {
      this(parent);
      this.structureType = structureType;
      this.put("S", structureType.getName());
      this.setParent(parent);
   }

   private PDFStructElem(PDFObject parent) {
      if (parent instanceof PDFStructElem) {
         this.parentElement = (PDFStructElem)parent;
      }

   }

   public PDFStructElem getParentStructElem() {
      return this.parentElement;
   }

   public void setParent(PDFObject parent) {
      if (parent != null && parent.hasObjectNumber()) {
         this.put("P", new PDFReference(parent));
      }

   }

   public void addKid(PDFObject kid) {
      if (this.kids == null) {
         this.kids = new ArrayList();
      }

      this.kids.add(kid);
   }

   public void setMCIDKid(int mcid) {
      this.put("K", mcid);
   }

   public void setPage(PDFPage page) {
      this.put("Pg", page);
   }

   public StructureType getStructureType() {
      return this.structureType;
   }

   private void setLanguage(String language) {
      this.put("Lang", language);
   }

   public void setLanguage(Locale language) {
      this.setLanguage(LanguageTags.toLanguageTag(language));
   }

   public String getLanguage() {
      return (String)this.get("Lang");
   }

   protected void writeDictionary(OutputStream out, StringBuilder textBuffer) throws IOException {
      this.attachKids();
      this.attachAttributes();
      super.writeDictionary(out, textBuffer);
   }

   private void attachAttributes() {
      if (this.attributes != null) {
         if (this.attributes.size() == 1) {
            this.put("A", this.attributes.get(0));
         } else {
            PDFArray array = new PDFArray(this.attributes);
            this.put("A", array);
         }
      }

   }

   public void addKidInSpecificOrder(int position, PDFStructElem kid) {
      if (this.kids == null) {
         this.addKid(kid);
      } else if (this.kids.size() - 1 < position) {
         this.kids.add(kid);
      } else if (this.kids.get(position) == null) {
         this.kids.set(position, kid);
      } else if (!this.kids.contains(kid)) {
         this.kids.add(position, kid);
      }

   }

   protected boolean attachKids() {
      List validKids = new ArrayList();
      if (this.kids != null) {
         Iterator var2 = this.kids.iterator();

         while(var2.hasNext()) {
            PDFObject kid = (PDFObject)var2.next();
            if (kid instanceof Placeholder) {
               if (((Placeholder)kid).attachKids()) {
                  validKids.add(kid);
               }
            } else {
               validKids.add(kid);
            }
         }
      }

      boolean kidsAttached = !validKids.isEmpty();
      if (kidsAttached) {
         PDFArray array = new PDFArray();
         Iterator var4 = validKids.iterator();

         while(var4.hasNext()) {
            PDFObject ob = (PDFObject)var4.next();
            array.add(ob);
         }

         this.put("K", array);
      }

      return kidsAttached;
   }

   public void setTableAttributeColSpan(int colSpan) {
      this.setTableAttributeRowColumnSpan("ColSpan", colSpan);
   }

   public void setTableAttributeRowSpan(int rowSpan) {
      this.setTableAttributeRowColumnSpan("RowSpan", rowSpan);
   }

   private void setTableAttributeRowColumnSpan(String typeSpan, int span) {
      PDFDictionary attribute = new PDFDictionary();
      attribute.put("O", StandardStructureAttributes.Table.NAME);
      attribute.put(typeSpan, span);
      if (this.attributes == null) {
         this.attributes = new ArrayList(2);
      }

      this.attributes.add(attribute);
   }

   public List getKids() {
      return this.kids;
   }

   public int output(OutputStream stream) throws IOException {
      if (this.getDocument() != null && this.getDocument().getProfile().getPDFUAMode().isEnabled()) {
         if (this.entries.containsKey("Alt") && "".equals(this.get("Alt"))) {
            this.put("Alt", "No alternate text specified");
         } else if (this.kids != null) {
            Iterator var2 = this.kids.iterator();

            while(var2.hasNext()) {
               PDFObject kid = (PDFObject)var2.next();
               if (kid instanceof PDFStructElem && !(kid instanceof Placeholder) && this.structureType.toString().equals("P") && this.isBSLE(((PDFStructElem)kid).getStructureType().toString())) {
                  this.structureType = StandardStructureTypes.Grouping.DIV;
                  this.put("S", StandardStructureTypes.Grouping.DIV.getName());
                  break;
               }
            }
         }
      }

      return super.output(stream);
   }

   private boolean isBSLE(String type) {
      String[] blseValues = new String[]{"Table", "L", "P"};
      return Arrays.asList(blseValues).contains(type);
   }

   // $FF: synthetic method
   PDFStructElem(PDFObject x0, Object x1) {
      this(x0);
   }

   public static class Placeholder extends PDFStructElem {
      private static final long serialVersionUID = -2397980642558372068L;

      public void outputInline(OutputStream out, StringBuilder textBuffer) throws IOException {
         if (this.kids != null) {
            assert this.kids.size() > 0;

            for(int i = 0; i < this.kids.size(); ++i) {
               if (i > 0) {
                  textBuffer.append(' ');
               }

               Object obj = this.kids.get(i);
               if (obj instanceof PDFStructElem) {
                  ((PDFStructElem)obj).setParent(this.parentElement);
               }

               this.formatObject(obj, out, textBuffer);
            }
         }

      }

      public Placeholder(PDFObject parent) {
         super(parent, (<undefinedtype>)null);
      }
   }
}
