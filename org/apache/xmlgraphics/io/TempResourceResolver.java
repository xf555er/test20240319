package org.apache.xmlgraphics.io;

import java.io.IOException;
import java.io.OutputStream;

public interface TempResourceResolver {
   Resource getResource(String var1) throws IOException;

   OutputStream getOutputStream(String var1) throws IOException;
}
