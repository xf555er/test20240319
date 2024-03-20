package org.apache.xmlgraphics.util;

import javax.imageio.metadata.IIOMetadata;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;

public final class ImageIODebugUtil {
   private ImageIODebugUtil() {
   }

   public static void dumpMetadata(IIOMetadata meta, boolean nativeFormat) {
      String format;
      if (nativeFormat) {
         format = meta.getNativeMetadataFormatName();
      } else {
         format = "javax_imageio_1.0";
      }

      Node node = meta.getAsTree(format);
      dumpNode(node);
   }

   public static void dumpNode(Node node) {
      try {
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer t = tf.newTransformer();
         t.setOutputProperty("omit-xml-declaration", "yes");
         Source src = new DOMSource(node);
         Result res = new StreamResult(System.out);
         t.transform(src, res);
      } catch (TransformerConfigurationException var5) {
         var5.printStackTrace();
      } catch (TransformerException var6) {
         var6.printStackTrace();
      }

   }
}
