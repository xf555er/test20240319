package net.jsign.poi;

public abstract class UnsupportedFileFormatException extends IllegalArgumentException {
   protected UnsupportedFileFormatException(String s) {
      super(s);
   }
}
