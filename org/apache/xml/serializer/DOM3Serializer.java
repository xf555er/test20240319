package org.apache.xml.serializer;

import java.io.IOException;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializerFilter;

public interface DOM3Serializer {
   void serializeDOM3(Node var1) throws IOException;

   void setErrorHandler(DOMErrorHandler var1);

   DOMErrorHandler getErrorHandler();

   void setNodeFilter(LSSerializerFilter var1);

   LSSerializerFilter getNodeFilter();

   void setNewLine(char[] var1);
}
