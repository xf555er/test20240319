package net.jsign.commons.io.function;

import java.io.IOException;

@FunctionalInterface
public interface IOFunction {
   Object apply(Object var1) throws IOException;
}
