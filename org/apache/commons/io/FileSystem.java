package org.apache.commons.io;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public enum FileSystem {
   GENERIC(false, false, Integer.MAX_VALUE, Integer.MAX_VALUE, new char[]{'\u0000'}, new String[0], false),
   LINUX(true, true, 255, 4096, new char[]{'\u0000', '/'}, new String[0], false),
   MAC_OSX(true, true, 255, 1024, new char[]{'\u0000', '/', ':'}, new String[0], false),
   WINDOWS(false, true, 255, 32000, new char[]{'\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r', '\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001a', '\u001b', '\u001c', '\u001d', '\u001e', '\u001f', '"', '*', '/', ':', '<', '>', '?', '\\', '|'}, new String[]{"AUX", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "CON", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", "NUL", "PRN"}, true);

   private static final boolean IS_OS_LINUX = getOsMatchesName("Linux");
   private static final boolean IS_OS_MAC = getOsMatchesName("Mac");
   private static final String OS_NAME_WINDOWS_PREFIX = "Windows";
   private static final boolean IS_OS_WINDOWS = getOsMatchesName("Windows");
   private final boolean casePreserving;
   private final boolean caseSensitive;
   private final char[] illegalFileNameChars;
   private final int maxFileNameLength;
   private final int maxPathLength;
   private final String[] reservedFileNames;
   private final boolean supportsDriveLetter;

   public static FileSystem getCurrent() {
      if (IS_OS_LINUX) {
         return LINUX;
      } else if (IS_OS_MAC) {
         return MAC_OSX;
      } else {
         return IS_OS_WINDOWS ? WINDOWS : GENERIC;
      }
   }

   private static boolean getOsMatchesName(String osNamePrefix) {
      return isOsNameMatch(getSystemProperty("os.name"), osNamePrefix);
   }

   private static String getSystemProperty(String property) {
      try {
         return System.getProperty(property);
      } catch (SecurityException var2) {
         System.err.println("Caught a SecurityException reading the system property '" + property + "'; the SystemUtils property value will default to null.");
         return null;
      }
   }

   private static boolean isOsNameMatch(String osName, String osNamePrefix) {
      return osName == null ? false : osName.toUpperCase(Locale.ROOT).startsWith(osNamePrefix.toUpperCase(Locale.ROOT));
   }

   private FileSystem(boolean caseSensitive, boolean casePreserving, int maxFileLength, int maxPathLength, char[] illegalFileNameChars, String[] reservedFileNames, boolean supportsDriveLetter) {
      this.maxFileNameLength = maxFileLength;
      this.maxPathLength = maxPathLength;
      this.illegalFileNameChars = (char[])Objects.requireNonNull(illegalFileNameChars, "illegalFileNameChars");
      this.reservedFileNames = (String[])Objects.requireNonNull(reservedFileNames, "reservedFileNames");
      this.caseSensitive = caseSensitive;
      this.casePreserving = casePreserving;
      this.supportsDriveLetter = supportsDriveLetter;
   }

   public char[] getIllegalFileNameChars() {
      return (char[])this.illegalFileNameChars.clone();
   }

   public int getMaxFileNameLength() {
      return this.maxFileNameLength;
   }

   public int getMaxPathLength() {
      return this.maxPathLength;
   }

   public String[] getReservedFileNames() {
      return (String[])this.reservedFileNames.clone();
   }

   public boolean isCasePreserving() {
      return this.casePreserving;
   }

   public boolean isCaseSensitive() {
      return this.caseSensitive;
   }

   private boolean isIllegalFileNameChar(char c) {
      return Arrays.binarySearch(this.illegalFileNameChars, c) >= 0;
   }

   public boolean isLegalFileName(CharSequence candidate) {
      if (candidate != null && candidate.length() != 0 && candidate.length() <= this.maxFileNameLength) {
         if (this.isReservedFileName(candidate)) {
            return false;
         } else {
            for(int i = 0; i < candidate.length(); ++i) {
               if (this.isIllegalFileNameChar(candidate.charAt(i))) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean isReservedFileName(CharSequence candidate) {
      return Arrays.binarySearch(this.reservedFileNames, candidate) >= 0;
   }

   public boolean supportsDriveLetter() {
      return this.supportsDriveLetter;
   }

   public String toLegalFileName(String candidate, char replacement) {
      if (this.isIllegalFileNameChar(replacement)) {
         throw new IllegalArgumentException(String.format("The replacement character '%s' cannot be one of the %s illegal characters: %s", replacement == 0 ? "\\0" : replacement, this.name(), Arrays.toString(this.illegalFileNameChars)));
      } else {
         String truncated = candidate.length() > this.maxFileNameLength ? candidate.substring(0, this.maxFileNameLength) : candidate;
         boolean changed = false;
         char[] charArray = truncated.toCharArray();

         for(int i = 0; i < charArray.length; ++i) {
            if (this.isIllegalFileNameChar(charArray[i])) {
               charArray[i] = replacement;
               changed = true;
            }
         }

         return changed ? String.valueOf(charArray) : truncated;
      }
   }
}
