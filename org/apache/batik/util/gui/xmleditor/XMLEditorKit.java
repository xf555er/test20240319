package org.apache.batik.util.gui.xmleditor;

import java.awt.Font;
import javax.swing.JEditorPane;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class XMLEditorKit extends DefaultEditorKit {
   public static final String XML_MIME_TYPE = "text/xml";
   protected XMLContext context;
   protected ViewFactory factory;

   public XMLEditorKit() {
      this((XMLContext)null);
   }

   public XMLEditorKit(XMLContext context) {
      this.factory = null;
      this.factory = new XMLViewFactory();
      if (context == null) {
         this.context = new XMLContext();
      } else {
         this.context = context;
      }

   }

   public XMLContext getStylePreferences() {
      return this.context;
   }

   public void install(JEditorPane c) {
      super.install(c);
      Object obj = this.context.getSyntaxFont("default");
      if (obj != null) {
         c.setFont((Font)obj);
      }

   }

   public String getContentType() {
      return "text/xml";
   }

   public Object clone() {
      XMLEditorKit kit = new XMLEditorKit();
      kit.context = this.context;
      return kit;
   }

   public Document createDefaultDocument() {
      XMLDocument doc = new XMLDocument(this.context);
      return doc;
   }

   public ViewFactory getViewFactory() {
      return this.factory;
   }

   protected class XMLViewFactory implements ViewFactory {
      public View create(Element elem) {
         return new XMLView(XMLEditorKit.this.context, elem);
      }
   }
}
