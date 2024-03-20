package org.apache.xmlgraphics.image.loader.impl.imageio;

import javax.imageio.metadata.IIOMetadata;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class ImageIOUtil {
   public static final Object IMAGEIO_METADATA = IIOMetadata.class;

   private ImageIOUtil() {
   }

   public static void extractResolution(IIOMetadata iiometa, ImageSize size) {
      if (iiometa != null && iiometa.isStandardMetadataFormatSupported()) {
         Element metanode = (Element)iiometa.getAsTree("javax_imageio_1.0");
         Element dim = getChild(metanode, "Dimension");
         if (dim != null) {
            double dpiHorz = size.getDpiHorizontal();
            double dpiVert = size.getDpiVertical();
            Element child = getChild(dim, "HorizontalPixelSize");
            float value;
            if (child != null) {
               value = Float.parseFloat(child.getAttribute("value"));
               if (value != 0.0F && !Float.isInfinite(value)) {
                  dpiHorz = (double)(25.4F / value);
               }
            }

            child = getChild(dim, "VerticalPixelSize");
            if (child != null) {
               value = Float.parseFloat(child.getAttribute("value"));
               if (value != 0.0F && !Float.isInfinite(value)) {
                  dpiVert = (double)(25.4F / value);
               }
            }

            size.setResolution(dpiHorz, dpiVert);
            size.calcSizeFromPixels();
         }
      }

   }

   public static Element getChild(Element el, String name) {
      NodeList nodes = el.getElementsByTagName(name);
      return nodes.getLength() > 0 ? (Element)nodes.item(0) : null;
   }

   public static void dumpMetadataToSystemOut(IIOMetadata iiometa) {
      String[] metanames = iiometa.getMetadataFormatNames();
      String[] var2 = metanames;
      int var3 = metanames.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String metaname = var2[var4];
         System.out.println("--->" + metaname);
         dumpNodeToSystemOut(iiometa.getAsTree(metaname));
      }

   }

   private static void dumpNodeToSystemOut(Node node) {
      Transformer trans = null;

      try {
         trans = TransformerFactory.newInstance().newTransformer();
         trans.setOutputProperty("omit-xml-declaration", "yes");
         trans.setOutputProperty("indent", "yes");
         Source src = new DOMSource(node);
         Result res = new StreamResult(System.out);
         trans.transform(src, res);
      } catch (TransformerConfigurationException var4) {
         var4.printStackTrace();
      } catch (TransformerException var5) {
         var5.printStackTrace();
      }

   }
}
