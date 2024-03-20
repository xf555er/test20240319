package javax.xml.transform.dom;

import javax.xml.transform.Result;
import org.w3c.dom.Node;

public class DOMResult implements Result {
   public static final String FEATURE = "http://javax.xml.transform.dom.DOMResult/feature";
   private Node node;
   private String systemId;

   public DOMResult() {
   }

   public DOMResult(Node var1) {
      this.setNode(var1);
   }

   public DOMResult(Node var1, String var2) {
      this.setNode(var1);
      this.setSystemId(var2);
   }

   public void setNode(Node var1) {
      this.node = var1;
   }

   public Node getNode() {
      return this.node;
   }

   public void setSystemId(String var1) {
      this.systemId = var1;
   }

   public String getSystemId() {
      return this.systemId;
   }
}
