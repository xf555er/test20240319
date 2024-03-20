package org.apache.fop.fonts.truetype;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xmlgraphics.util.uri.DataURLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SVGGlyphData {
   protected long svgDocOffset;
   protected long svgDocLength;
   private String svg;
   public float scale = 1.0F;

   public void setSVG(String svg) {
      this.svg = svg;
   }

   public String getDataURL(int height) {
      try {
         String modifiedSVG = this.updateTransform(this.svg, height);
         return DataURLUtil.createDataURL(new ByteArrayInputStream(modifiedSVG.getBytes("UTF-8")), "image/svg");
      } catch (TransformerException | SAXException | ParserConfigurationException | IOException var3) {
         throw new RuntimeException(var3);
      }
   }

   private String updateTransform(String svg, int height) throws IOException, ParserConfigurationException, SAXException, TransformerException {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      InputSource inputSource = new InputSource();
      inputSource.setCharacterStream(new StringReader(svg));
      Document doc = documentBuilder.parse(inputSource);
      NodeList nodes = doc.getElementsByTagName("g");
      Element gElement = (Element)nodes.item(0);
      if (gElement != null) {
         String transform = gElement.getAttribute("transform");
         if (transform.contains("scale(")) {
            String scaleStr = transform.split("scale\\(")[1].split("\\)")[0].trim();
            this.scale = Float.parseFloat(scaleStr);
            gElement.removeAttribute("transform");
         } else {
            gElement.setAttribute("transform", "translate(0," + height + ")");
         }
      } else {
         Element svgElement = (Element)doc.getElementsByTagName("svg").item(0);
         svgElement.setAttribute("viewBox", "0 0 1000 " + height);
         gElement = doc.createElement("g");
         gElement.setAttribute("transform", "translate(0," + height + ")");
         NodeList paths = doc.getElementsByTagName("path");

         for(int i = 0; i < paths.getLength(); ++i) {
            Node path = paths.item(i);
            if (i == 0) {
               path.getParentNode().insertBefore(gElement, path);
            }

            gElement.appendChild(path);
         }
      }

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(doc);
      transformer.transform(source, result);
      return result.getWriter().toString();
   }
}
