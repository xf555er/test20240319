package net.jsign.poi;

import java.io.File;

public class EmptyFileException extends IllegalArgumentException {
   public EmptyFileException() {
      super("The supplied file was empty (zero bytes long)");
   }

   public EmptyFileException(File file) {
      super(file.exists() ? "The supplied file '" + file.getAbsolutePath() + "' was empty (zero bytes long)" : "The file '" + file.getAbsolutePath() + "' does not exist");
   }
}
