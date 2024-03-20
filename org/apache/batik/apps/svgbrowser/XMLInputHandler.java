package org.apache.batik.apps.svgbrowser;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.svg.SVGDocument;

public class XMLInputHandler implements SquiggleInputHandler {
   public static final String[] XVG_MIME_TYPES = new String[]{"image/xml+xsl+svg"};
   public static final String[] XVG_FILE_EXTENSIONS = new String[]{".xml", ".xsl"};
   public static final String ERROR_NO_XML_STYLESHEET_PROCESSING_INSTRUCTION = "XMLInputHandler.error.no.xml.stylesheet.processing.instruction";
   public static final String ERROR_TRANSFORM_OUTPUT_NOT_SVG = "XMLInputHandler.error.transform.output.not.svg";
   public static final String ERROR_TRANSFORM_PRODUCED_NO_CONTENT = "XMLInputHandler.error.transform.produced.no.content";
   public static final String ERROR_TRANSFORM_OUTPUT_WRONG_NS = "XMLInputHandler.error.transform.output.wrong.ns";
   public static final String ERROR_RESULT_GENERATED_EXCEPTION = "XMLInputHandler.error.result.generated.exception";
   public static final String XSL_PROCESSING_INSTRUCTION_TYPE = "text/xsl";
   public static final String PSEUDO_ATTRIBUTE_TYPE = "type";
   public static final String PSEUDO_ATTRIBUTE_HREF = "href";

   public String[] getHandledMimeTypes() {
      return XVG_MIME_TYPES;
   }

   public String[] getHandledExtensions() {
      return XVG_FILE_EXTENSIONS;
   }

   public String getDescription() {
      return "";
   }

   public boolean accept(File f) {
      return f.isFile() && this.accept(f.getPath());
   }

   public boolean accept(ParsedURL purl) {
      if (purl == null) {
         return false;
      } else {
         String path = purl.getPath();
         return this.accept(path);
      }
   }

   public boolean accept(String path) {
      if (path == null) {
         return false;
      } else {
         String[] var2 = XVG_FILE_EXTENSIONS;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String XVG_FILE_EXTENSION = var2[var4];
            if (path.endsWith(XVG_FILE_EXTENSION)) {
               return true;
            }
         }

         return false;
      }
   }

   public void handle(ParsedURL purl, JSVGViewerFrame svgViewerFrame) throws Exception {
      String uri = purl.toString();
      TransformerFactory tFactory = TransformerFactory.newInstance();
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setValidating(false);
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document inDoc = db.parse(uri);
      String xslStyleSheetURI = this.extractXSLProcessingInstruction(inDoc);
      if (xslStyleSheetURI == null) {
         xslStyleSheetURI = uri;
      }

      ParsedURL parsedXSLStyleSheetURI = new ParsedURL(uri, xslStyleSheetURI);
      Transformer transformer = tFactory.newTransformer(new StreamSource(parsedXSLStyleSheetURI.toString()));
      transformer.setURIResolver(new DocumentURIResolver(parsedXSLStyleSheetURI.toString()));
      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      transformer.transform(new DOMSource(inDoc), result);
      sw.flush();
      sw.close();
      String parser = XMLResourceDescriptor.getXMLParserClassName();
      SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
      SVGDocument outDoc = null;

      try {
         outDoc = f.createSVGDocument(uri, (Reader)(new StringReader(sw.toString())));
      } catch (Exception var17) {
         System.err.println("======================================");
         System.err.println(sw.toString());
         System.err.println("======================================");
         throw new IllegalArgumentException(Resources.getString("XMLInputHandler.error.result.generated.exception"));
      }

      svgViewerFrame.getJSVGCanvas().setSVGDocument(outDoc);
      svgViewerFrame.setSVGDocument(outDoc, uri, outDoc.getTitle());
   }

   protected void checkAndPatch(Document doc) {
      Element root = doc.getDocumentElement();
      Node realRoot = root.getFirstChild();
      String svgNS = "http://www.w3.org/2000/svg";
      if (realRoot == null) {
         throw new IllegalArgumentException(Resources.getString("XMLInputHandler.error.transform.produced.no.content"));
      } else if (realRoot.getNodeType() == 1 && "svg".equals(realRoot.getLocalName())) {
         if (!svgNS.equals(realRoot.getNamespaceURI())) {
            throw new IllegalArgumentException(Resources.getString("XMLInputHandler.error.transform.output.wrong.ns"));
         } else {
            for(Node child = realRoot.getFirstChild(); child != null; child = realRoot.getFirstChild()) {
               root.appendChild(child);
            }

            NamedNodeMap attrs = realRoot.getAttributes();
            int n = attrs.getLength();

            for(int i = 0; i < n; ++i) {
               root.setAttributeNode((Attr)attrs.item(i));
            }

            root.removeChild(realRoot);
         }
      } else {
         throw new IllegalArgumentException(Resources.getString("XMLInputHandler.error.transform.output.not.svg"));
      }
   }

   protected String extractXSLProcessingInstruction(Document doc) {
      for(Node child = doc.getFirstChild(); child != null; child = child.getNextSibling()) {
         if (child.getNodeType() == 7) {
            ProcessingInstruction pi = (ProcessingInstruction)child;
            HashMap table = new HashMap();
            DOMUtilities.parseStyleSheetPIData(pi.getData(), table);
            Object type = table.get("type");
            if ("text/xsl".equals(type)) {
               Object href = table.get("href");
               if (href != null) {
                  return href.toString();
               }

               return null;
            }
         }
      }

      return null;
   }

   public static class DocumentURIResolver implements URIResolver {
      String documentURI;

      public DocumentURIResolver(String documentURI) {
         this.documentURI = documentURI;
      }

      public Source resolve(String href, String base) {
         if (base == null || "".equals(base)) {
            base = this.documentURI;
         }

         ParsedURL purl = new ParsedURL(base, href);
         return new StreamSource(purl.toString());
      }
   }
}
