package org.apache.commons.io.file;

import java.nio.file.SimpleFileVisitor;

public abstract class SimplePathVisitor extends SimpleFileVisitor implements PathVisitor {
   protected SimplePathVisitor() {
   }
}
