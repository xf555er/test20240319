package org.apache.batik.script;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.util.Service;
import org.w3c.dom.Document;

public class InterpreterPool {
   public static final String BIND_NAME_DOCUMENT = "document";
   protected static Map defaultFactories = new HashMap(7);
   protected Map factories = new HashMap(7);

   public InterpreterPool() {
      this.factories.putAll(defaultFactories);
   }

   public Interpreter createInterpreter(Document document, String language) {
      return this.createInterpreter(document, language, (ImportInfo)null);
   }

   public Interpreter createInterpreter(Document document, String language, ImportInfo imports) {
      InterpreterFactory factory = (InterpreterFactory)this.factories.get(language);
      if (factory == null) {
         return null;
      } else {
         if (imports == null) {
            imports = ImportInfo.getImports();
         }

         Interpreter interpreter = null;
         SVGOMDocument svgDoc = (SVGOMDocument)document;
         URL url = null;

         try {
            url = new URL(svgDoc.getDocumentURI());
         } catch (MalformedURLException var9) {
         }

         interpreter = factory.createInterpreter(url, svgDoc.isSVG12(), imports);
         if (interpreter == null) {
            return null;
         } else {
            if (document != null) {
               interpreter.bindObject("document", document);
            }

            return interpreter;
         }
      }
   }

   public void putInterpreterFactory(String language, InterpreterFactory factory) {
      this.factories.put(language, factory);
   }

   public void removeInterpreterFactory(String language) {
      this.factories.remove(language);
   }

   static {
      Iterator iter = Service.providers(InterpreterFactory.class);

      while(iter.hasNext()) {
         InterpreterFactory factory = null;
         factory = (InterpreterFactory)iter.next();
         String[] mimeTypes = factory.getMimeTypes();
         String[] var3 = mimeTypes;
         int var4 = mimeTypes.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String mimeType = var3[var5];
            defaultFactories.put(mimeType, factory);
         }
      }

   }
}
