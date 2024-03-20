package org.apache.fop.render.pdf.extensions;

import java.util.HashMap;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FONode;

public class PDFElementMapping extends ElementMapping {
   public static final String NAMESPACE = "http://xmlgraphics.apache.org/fop/extensions/pdf";

   public PDFElementMapping() {
      this.namespaceURI = "http://xmlgraphics.apache.org/fop/extensions/pdf";
   }

   protected void initialize() {
      if (this.foObjs == null) {
         this.foObjs = new HashMap();
         this.foObjs.put(PDFDictionaryType.Action.elementName(), new PDFActionElementMaker());
         this.foObjs.put(PDFObjectType.Array.elementName(), new PDFArrayElementMaker());
         this.foObjs.put(PDFObjectType.Boolean.elementName(), new PDFCollectionEntryElementMaker(PDFObjectType.Boolean));
         this.foObjs.put(PDFDictionaryType.Catalog.elementName(), new PDFCatalogElementMaker());
         this.foObjs.put(PDFDictionaryType.Dictionary.elementName(), new PDFDictionaryElementMaker());
         this.foObjs.put("embedded-file", new PDFEmbeddedFileElementMaker());
         this.foObjs.put(PDFObjectType.Name.elementName(), new PDFCollectionEntryElementMaker(PDFObjectType.Name));
         this.foObjs.put(PDFObjectType.Number.elementName(), new PDFCollectionEntryElementMaker(PDFObjectType.Number));
         this.foObjs.put(PDFDictionaryType.Navigator.elementName(), new PDFNavigatorElementMaker());
         this.foObjs.put(PDFDictionaryType.Layer.elementName(), new PDFLayerElementMaker());
         this.foObjs.put(PDFDictionaryType.Page.elementName(), new PDFPageElementMaker());
         this.foObjs.put(PDFObjectType.Reference.elementName(), new PDFReferenceElementMaker());
         this.foObjs.put(PDFObjectType.String.elementName(), new PDFCollectionEntryElementMaker(PDFObjectType.String));
         this.foObjs.put(PDFDictionaryType.Info.elementName(), new PDFDocumentInformationElementMaker());
         this.foObjs.put(PDFDictionaryType.VT.elementName(), new PDFVTElementMaker());
         this.foObjs.put(PDFDictionaryType.PagePiece.elementName(), new PDFPagePieceElementMaker());
      }

   }

   static class PDFPagePieceElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFPagePieceElement(parent);
      }
   }

   static class PDFVTElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFVTElement(parent);
      }
   }

   static class PDFReferenceElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFReferenceElement(parent);
      }
   }

   static class PDFCollectionEntryElementMaker extends ElementMapping.Maker {
      private PDFObjectType entryType;

      PDFCollectionEntryElementMaker(PDFObjectType entryType) {
         this.entryType = entryType;
      }

      public FONode make(FONode parent) {
         return new PDFCollectionEntryElement(parent, this.entryType);
      }
   }

   static class PDFPageElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFPageElement(parent);
      }
   }

   static class PDFNavigatorElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFNavigatorElement(parent);
      }
   }

   static class PDFLayerElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFLayerElement(parent);
      }
   }

   static class PDFEmbeddedFileElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFEmbeddedFileElement(parent);
      }
   }

   static class PDFDictionaryElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFDictionaryElement(parent, PDFDictionaryType.Dictionary);
      }
   }

   static class PDFDocumentInformationElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFDocumentInformationElement(parent);
      }
   }

   static class PDFCatalogElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFCatalogElement(parent);
      }
   }

   static class PDFArrayElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFArrayElement(parent);
      }
   }

   static class PDFActionElementMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new PDFActionElement(parent);
      }
   }
}
