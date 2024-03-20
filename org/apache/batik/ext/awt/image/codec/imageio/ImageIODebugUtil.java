package org.apache.batik.ext.awt.image.codec.imageio;

import javax.imageio.metadata.IIOMetadata;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;

public class ImageIODebugUtil {
   public static void dumpMetadata(IIOMetadata meta) {
      String format = meta.getNativeMetadataFormatName();
      Node node = meta.getAsTree(format);
      dumpNode(node);
   }

   public static void dumpNode(Node node) {
      try {
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer t = tf.newTransformer();
         Source src = new DOMSource(node);
         Result res = new StreamResult(System.out);
         t.transform(src, res);
         System.out.println();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }
}
