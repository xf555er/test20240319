package org.apache.commons.io.function;

import java.io.IOException;

@FunctionalInterface
public interface IOSupplier {
   Object get() throws IOException;
}
