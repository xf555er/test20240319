package javax.xml.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class DocumentBuilder {
   protected DocumentBuilder() {
   }

   public Document parse(InputStream var1) throws SAXException, IOException {
      if (var1 == null) {
         throw new IllegalArgumentException("InputStream cannot be null");
      } else {
         InputSource var2 = new InputSource(var1);
         return this.parse(var2);
      }
   }

   public Document parse(InputStream var1, String var2) throws SAXException, IOException {
      if (var1 == null) {
         throw new IllegalArgumentException("InputStream cannot be null");
      } else {
         InputSource var3 = new InputSource(var1);
         var3.setSystemId(var2);
         return this.parse(var3);
      }
   }

   public Document parse(String var1) throws SAXException, IOException {
      if (var1 == null) {
         throw new IllegalArgumentException("URI cannot be null");
      } else {
         InputSource var2 = new InputSource(var1);
         return this.parse(var2);
      }
   }

   public Document parse(File var1) throws SAXException, IOException {
      if (var1 == null) {
         throw new IllegalArgumentException("File cannot be null");
      } else {
         String var2 = "file:" + var1.getAbsolutePath();
         if (File.separatorChar == '\\') {
            var2 = var2.replace('\\', '/');
         }

         InputSource var3 = new InputSource(var2);
         return this.parse(var3);
      }
   }

   public abstract Document parse(InputSource var1) throws SAXException, IOException;

   public abstract boolean isNamespaceAware();

   public abstract boolean isValidating();

   public abstract void setEntityResolver(EntityResolver var1);

   public abstract void setErrorHandler(ErrorHandler var1);

   public abstract Document newDocument();

   public abstract DOMImplementation getDOMImplementation();
}
