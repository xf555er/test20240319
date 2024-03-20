package org.apache.fop.pdf;

import java.util.LinkedHashSet;
import java.util.Set;

public class PDFResourceContext extends PDFDictionary {
   private Set xObjects = new LinkedHashSet();
   private Set patterns = new LinkedHashSet();
   private Set shadings = new LinkedHashSet();
   private Set gstates = new LinkedHashSet();

   public PDFResourceContext(PDFResources resources) {
      this.put("Resources", resources);
      resources.addContext(this);
   }

   public void addXObject(PDFXObject xObject) {
      this.xObjects.add(xObject);
   }

   public Set getXObjects() {
      return this.xObjects;
   }

   public PDFResources getPDFResources() {
      return (PDFResources)this.get("Resources");
   }

   public void addAnnotation(PDFObject annot) {
      PDFAnnotList annotList = this.getAnnotations();
      if (annotList == null) {
         annotList = this.getDocument().getFactory().makeAnnotList();
         this.put("Annots", annotList);
      }

      annotList.addAnnot(annot);
   }

   public PDFAnnotList getAnnotations() {
      return (PDFAnnotList)this.get("Annots");
   }

   public void addGState(PDFGState gstate) {
      this.gstates.add(gstate);
   }

   public Set getGStates() {
      return this.gstates;
   }

   public void addShading(PDFShading shading) {
      this.shadings.add(shading);
   }

   public Set getShadings() {
      return this.shadings;
   }

   public Set getPatterns() {
      return this.patterns;
   }

   public void addPattern(PDFPattern pattern) {
      this.patterns.add(pattern);
   }
}
