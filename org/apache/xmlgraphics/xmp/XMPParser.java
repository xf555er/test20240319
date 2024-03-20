package org.apache.xmlgraphics.xmp;

import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

public final class XMPParser {
   private XMPParser() {
   }

   public static Metadata parseXMP(URL url) throws TransformerException {
      return parseXMP((Source)(new StreamSource(url.toExternalForm())));
   }

   public static Metadata parseXMP(Source src) throws TransformerException {
      TransformerFactory tFactory = TransformerFactory.newInstance();
      tFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
      tFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");
      Transformer transformer = tFactory.newTransformer();
      XMPHandler handler = createXMPHandler();
      SAXResult res = new SAXResult(handler);
      transformer.transform(src, res);
      return handler.getMetadata();
   }

   public static XMPHandler createXMPHandler() {
      return new XMPHandler();
   }
}
