package org.apache.batik.anim.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.MissingResourceException;
import java.util.Properties;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGDocumentFactory;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.util.MimeTypeConstants;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SAXSVGDocumentFactory extends SAXDocumentFactory implements SVGDocumentFactory {
   public static final Object LOCK = new Object();
   public static final String KEY_PUBLIC_IDS = "publicIds";
   public static final String KEY_SKIPPABLE_PUBLIC_IDS = "skippablePublicIds";
   public static final String KEY_SKIP_DTD = "skipDTD";
   public static final String KEY_SYSTEM_ID = "systemId.";
   protected static final String DTDIDS = "org.apache.batik.anim.dom.resources.dtdids";
   protected static final String HTTP_CHARSET = "charset";
   protected static String dtdids;
   protected static String skippable_dtdids;
   protected static String skip_dtd;
   protected static Properties dtdProps;

   public SAXSVGDocumentFactory(String parser) {
      super(SVGDOMImplementation.getDOMImplementation(), parser);
   }

   public SAXSVGDocumentFactory(String parser, boolean dd) {
      super(SVGDOMImplementation.getDOMImplementation(), parser, dd);
   }

   public SVGDocument createSVGDocument(String uri) throws IOException {
      return (SVGDocument)this.createDocument(uri);
   }

   public SVGDocument createSVGDocument(String uri, InputStream inp) throws IOException {
      return (SVGDocument)this.createDocument(uri, inp);
   }

   public SVGDocument createSVGDocument(String uri, Reader r) throws IOException {
      return (SVGDocument)this.createDocument(uri, r);
   }

   public Document createDocument(String uri) throws IOException {
      ParsedURL purl = new ParsedURL(uri);
      InputStream is = purl.openStream(MimeTypeConstants.MIME_TYPES_SVG_LIST.iterator());
      uri = purl.getPostConnectionURL();
      InputSource isrc = new InputSource(is);
      String contentType = purl.getContentType();
      int cindex = -1;
      if (contentType != null) {
         contentType = contentType.toLowerCase();
         cindex = contentType.indexOf("charset");
      }

      String charset = null;
      if (cindex != -1) {
         int i = cindex + "charset".length();
         int eqIdx = contentType.indexOf(61, i);
         if (eqIdx != -1) {
            ++eqIdx;
            int idx = contentType.indexOf(44, eqIdx);
            int semiIdx = contentType.indexOf(59, eqIdx);
            if (semiIdx != -1 && (semiIdx < idx || idx == -1)) {
               idx = semiIdx;
            }

            if (idx != -1) {
               charset = contentType.substring(eqIdx, idx);
            } else {
               charset = contentType.substring(eqIdx);
            }

            charset = charset.trim();
            isrc.setEncoding(charset);
         }
      }

      isrc.setSystemId(uri);
      SVGOMDocument doc = (SVGOMDocument)super.createDocument("http://www.w3.org/2000/svg", "svg", uri, isrc);
      doc.setParsedURL(new ParsedURL(uri));
      doc.setDocumentInputEncoding(charset);
      doc.setXmlStandalone(this.isStandalone);
      doc.setXmlVersion(this.xmlVersion);
      return doc;
   }

   public Document createDocument(String uri, InputStream inp) throws IOException {
      InputSource is = new InputSource(inp);
      is.setSystemId(uri);

      try {
         Document doc = super.createDocument("http://www.w3.org/2000/svg", "svg", uri, is);
         if (uri != null) {
            ((SVGOMDocument)doc).setParsedURL(new ParsedURL(uri));
         }

         AbstractDocument d = (AbstractDocument)doc;
         d.setDocumentURI(uri);
         d.setXmlStandalone(this.isStandalone);
         d.setXmlVersion(this.xmlVersion);
         return doc;
      } catch (MalformedURLException var6) {
         throw new IOException(var6.getMessage());
      }
   }

   public Document createDocument(String uri, Reader r) throws IOException {
      InputSource is = new InputSource(r);
      is.setSystemId(uri);

      try {
         Document doc = super.createDocument("http://www.w3.org/2000/svg", "svg", uri, is);
         if (uri != null) {
            ((SVGOMDocument)doc).setParsedURL(new ParsedURL(uri));
         }

         AbstractDocument d = (AbstractDocument)doc;
         d.setDocumentURI(uri);
         d.setXmlStandalone(this.isStandalone);
         d.setXmlVersion(this.xmlVersion);
         return doc;
      } catch (MalformedURLException var6) {
         throw new IOException(var6.getMessage());
      }
   }

   public Document createDocument(String ns, String root, String uri) throws IOException {
      if ("http://www.w3.org/2000/svg".equals(ns) && "svg".equals(root)) {
         return this.createDocument(uri);
      } else {
         throw new RuntimeException("Bad root element");
      }
   }

   public Document createDocument(String ns, String root, String uri, InputStream is) throws IOException {
      if ("http://www.w3.org/2000/svg".equals(ns) && "svg".equals(root)) {
         return this.createDocument(uri, is);
      } else {
         throw new RuntimeException("Bad root element");
      }
   }

   public Document createDocument(String ns, String root, String uri, Reader r) throws IOException {
      if ("http://www.w3.org/2000/svg".equals(ns) && "svg".equals(root)) {
         return this.createDocument(uri, r);
      } else {
         throw new RuntimeException("Bad root element");
      }
   }

   public DOMImplementation getDOMImplementation(String ver) {
      if (ver != null && ver.length() != 0 && !ver.equals("1.0") && !ver.equals("1.1")) {
         if (ver.equals("1.2")) {
            return SVG12DOMImplementation.getDOMImplementation();
         } else {
            throw new RuntimeException("Unsupport SVG version '" + ver + "'");
         }
      } else {
         return SVGDOMImplementation.getDOMImplementation();
      }
   }

   public void startDocument() throws SAXException {
      super.startDocument();
   }

   public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
      try {
         synchronized(LOCK) {
            if (dtdProps == null) {
               dtdProps = new Properties();

               try {
                  Class cls = SAXSVGDocumentFactory.class;
                  InputStream is = cls.getResourceAsStream("resources/dtdids.properties");
                  dtdProps.load(is);
               } catch (IOException var7) {
                  throw new SAXException(var7);
               }
            }

            if (dtdids == null) {
               dtdids = dtdProps.getProperty("publicIds");
            }

            if (skippable_dtdids == null) {
               skippable_dtdids = dtdProps.getProperty("skippablePublicIds");
            }

            if (skip_dtd == null) {
               skip_dtd = dtdProps.getProperty("skipDTD");
            }
         }

         if (publicId == null) {
            return null;
         } else if (!this.isValidating && skippable_dtdids.indexOf(publicId) != -1) {
            return new InputSource(new StringReader(skip_dtd));
         } else {
            if (dtdids.indexOf(publicId) != -1) {
               String localSystemId = dtdProps.getProperty("systemId." + publicId.replace(' ', '_'));
               if (localSystemId != null && !"".equals(localSystemId)) {
                  return new InputSource(this.getClass().getResource(localSystemId).toString());
               }
            }

            return null;
         }
      } catch (MissingResourceException var9) {
         throw new SAXException(var9);
      }
   }
}
