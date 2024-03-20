package org.apache.xmlgraphics.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public interface ResourceResolver {
   Resource getResource(URI var1) throws IOException;

   OutputStream getOutputStream(URI var1) throws IOException;
}
