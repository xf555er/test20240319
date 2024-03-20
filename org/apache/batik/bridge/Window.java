package org.apache.batik.bridge;

import org.apache.batik.script.Interpreter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface Window extends org.apache.batik.w3c.dom.Window {
   Object setInterval(String var1, long var2);

   Object setInterval(Runnable var1, long var2);

   void clearInterval(Object var1);

   Object setTimeout(String var1, long var2);

   Object setTimeout(Runnable var1, long var2);

   void clearTimeout(Object var1);

   Node parseXML(String var1, Document var2);

   String printNode(Node var1);

   void getURL(String var1, URLResponseHandler var2);

   void getURL(String var1, URLResponseHandler var2, String var3);

   void postURL(String var1, String var2, URLResponseHandler var3);

   void postURL(String var1, String var2, URLResponseHandler var3, String var4);

   void postURL(String var1, String var2, URLResponseHandler var3, String var4, String var5);

   void alert(String var1);

   boolean confirm(String var1);

   String prompt(String var1);

   String prompt(String var1, String var2);

   BridgeContext getBridgeContext();

   Interpreter getInterpreter();

   public interface URLResponseHandler {
      void getURLDone(boolean var1, String var2, String var3);
   }
}
