package org.apache.commons.io;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilenameUtils {
   private static final String[] EMPTY_STRING_ARRAY = new String[0];
   private static final String EMPTY_STRING = "";
   private static final int NOT_FOUND = -1;
   public static final char EXTENSION_SEPARATOR = '.';
   public static final String EXTENSION_SEPARATOR_STR = Character.toString('.');
   private static final char UNIX_SEPARATOR = '/';
   private static final char WINDOWS_SEPARATOR = '\\';
   private static final char SYSTEM_SEPARATOR;
   private static final char OTHER_SEPARATOR;
   private static final Pattern IPV4_PATTERN;
   private static final int IPV4_MAX_OCTET_VALUE = 255;
   private static final int IPV6_MAX_HEX_GROUPS = 8;
   private static final int IPV6_MAX_HEX_DIGITS_PER_GROUP = 4;
   private static final int MAX_UNSIGNED_SHORT = 65535;
   private static final int BASE_16 = 16;
   private static final Pattern REG_NAME_PART_PATTERN;

   static boolean isSystemWindows() {
      return SYSTEM_SEPARATOR == '\\';
   }

   private static boolean isSeparator(char ch) {
      return ch == '/' || ch == '\\';
   }

   public static String normalize(String fileName) {
      return doNormalize(fileName, SYSTEM_SEPARATOR, true);
   }

   public static String normalize(String fileName, boolean unixSeparator) {
      char separator = unixSeparator ? 47 : 92;
      return doNormalize(fileName, (char)separator, true);
   }

   public static String normalizeNoEndSeparator(String fileName) {
      return doNormalize(fileName, SYSTEM_SEPARATOR, false);
   }

   public static String normalizeNoEndSeparator(String fileName, boolean unixSeparator) {
      char separator = unixSeparator ? 47 : 92;
      return doNormalize(fileName, (char)separator, false);
   }

   private static String doNormalize(String fileName, char separator, boolean keepSeparator) {
      if (fileName == null) {
         return null;
      } else {
         requireNonNullChars(fileName);
         int size = fileName.length();
         if (size == 0) {
            return fileName;
         } else {
            int prefix = getPrefixLength(fileName);
            if (prefix < 0) {
               return null;
            } else {
               char[] array = new char[size + 2];
               fileName.getChars(0, fileName.length(), array, 0);
               char otherSeparator = separator == SYSTEM_SEPARATOR ? OTHER_SEPARATOR : SYSTEM_SEPARATOR;

               for(int i = 0; i < array.length; ++i) {
                  if (array[i] == otherSeparator) {
                     array[i] = separator;
                  }
               }

               boolean lastIsDirectory = true;
               if (array[size - 1] != separator) {
                  array[size++] = separator;
                  lastIsDirectory = false;
               }

               int i;
               for(i = prefix != 0 ? prefix : 1; i < size; ++i) {
                  if (array[i] == separator && array[i - 1] == separator) {
                     System.arraycopy(array, i, array, i - 1, size - i);
                     --size;
                     --i;
                  }
               }

               for(i = prefix + 1; i < size; ++i) {
                  if (array[i] == separator && array[i - 1] == '.' && (i == prefix + 1 || array[i - 2] == separator)) {
                     if (i == size - 1) {
                        lastIsDirectory = true;
                     }

                     System.arraycopy(array, i + 1, array, i - 1, size - i);
                     size -= 2;
                     --i;
                  }
               }

               label114:
               for(i = prefix + 2; i < size; ++i) {
                  if (array[i] == separator && array[i - 1] == '.' && array[i - 2] == '.' && (i == prefix + 2 || array[i - 3] == separator)) {
                     if (i == prefix + 2) {
                        return null;
                     }

                     if (i == size - 1) {
                        lastIsDirectory = true;
                     }

                     for(int j = i - 4; j >= prefix; --j) {
                        if (array[j] == separator) {
                           System.arraycopy(array, i + 1, array, j + 1, size - i);
                           size -= i - j;
                           i = j + 1;
                           continue label114;
                        }
                     }

                     System.arraycopy(array, i + 1, array, prefix, size - i);
                     size -= i + 1 - prefix;
                     i = prefix + 1;
                  }
               }

               if (size <= 0) {
                  return "";
               } else if (size <= prefix) {
                  return new String(array, 0, size);
               } else if (lastIsDirectory && keepSeparator) {
                  return new String(array, 0, size);
               } else {
                  return new String(array, 0, size - 1);
               }
            }
         }
      }
   }

   public static String concat(String basePath, String fullFileNameToAdd) {
      int prefix = getPrefixLength(fullFileNameToAdd);
      if (prefix < 0) {
         return null;
      } else if (prefix > 0) {
         return normalize(fullFileNameToAdd);
      } else if (basePath == null) {
         return null;
      } else {
         int len = basePath.length();
         if (len == 0) {
            return normalize(fullFileNameToAdd);
         } else {
            char ch = basePath.charAt(len - 1);
            return isSeparator(ch) ? normalize(basePath + fullFileNameToAdd) : normalize(basePath + '/' + fullFileNameToAdd);
         }
      }
   }

   public static boolean directoryContains(String canonicalParent, String canonicalChild) {
      Objects.requireNonNull(canonicalParent, "canonicalParent");
      if (canonicalChild == null) {
         return false;
      } else {
         return IOCase.SYSTEM.checkEquals(canonicalParent, canonicalChild) ? false : IOCase.SYSTEM.checkStartsWith(canonicalChild, canonicalParent);
      }
   }

   public static String separatorsToUnix(String path) {
      return path != null && path.indexOf(92) != -1 ? path.replace('\\', '/') : path;
   }

   public static String separatorsToWindows(String path) {
      return path != null && path.indexOf(47) != -1 ? path.replace('/', '\\') : path;
   }

   public static String separatorsToSystem(String path) {
      if (path == null) {
         return null;
      } else {
         return isSystemWindows() ? separatorsToWindows(path) : separatorsToUnix(path);
      }
   }

   public static int getPrefixLength(String fileName) {
      if (fileName == null) {
         return -1;
      } else {
         int len = fileName.length();
         if (len == 0) {
            return 0;
         } else {
            char ch0 = fileName.charAt(0);
            if (ch0 == ':') {
               return -1;
            } else if (len == 1) {
               if (ch0 == '~') {
                  return 2;
               } else {
                  return isSeparator(ch0) ? 1 : 0;
               }
            } else {
               int ch1;
               int posUnix;
               if (ch0 == '~') {
                  ch1 = fileName.indexOf(47, 1);
                  posUnix = fileName.indexOf(92, 1);
                  if (ch1 == -1 && posUnix == -1) {
                     return len + 1;
                  } else {
                     ch1 = ch1 == -1 ? posUnix : ch1;
                     posUnix = posUnix == -1 ? ch1 : posUnix;
                     return Math.min(ch1, posUnix) + 1;
                  }
               } else {
                  ch1 = fileName.charAt(1);
                  if (ch1 == 58) {
                     ch0 = Character.toUpperCase(ch0);
                     if (ch0 >= 'A' && ch0 <= 'Z') {
                        if (len == 2 && !FileSystem.getCurrent().supportsDriveLetter()) {
                           return 0;
                        } else {
                           return len != 2 && isSeparator(fileName.charAt(2)) ? 3 : 2;
                        }
                     } else {
                        return ch0 == '/' ? 1 : -1;
                     }
                  } else if (isSeparator(ch0) && isSeparator((char)ch1)) {
                     posUnix = fileName.indexOf(47, 2);
                     int posWin = fileName.indexOf(92, 2);
                     if ((posUnix != -1 || posWin != -1) && posUnix != 2 && posWin != 2) {
                        posUnix = posUnix == -1 ? posWin : posUnix;
                        posWin = posWin == -1 ? posUnix : posWin;
                        int pos = Math.min(posUnix, posWin) + 1;
                        String hostnamePart = fileName.substring(2, pos - 1);
                        return isValidHostName(hostnamePart) ? pos : -1;
                     } else {
                        return -1;
                     }
                  } else {
                     return isSeparator(ch0) ? 1 : 0;
                  }
               }
            }
         }
      }
   }

   public static int indexOfLastSeparator(String fileName) {
      if (fileName == null) {
         return -1;
      } else {
         int lastUnixPos = fileName.lastIndexOf(47);
         int lastWindowsPos = fileName.lastIndexOf(92);
         return Math.max(lastUnixPos, lastWindowsPos);
      }
   }

   public static int indexOfExtension(String fileName) throws IllegalArgumentException {
      if (fileName == null) {
         return -1;
      } else {
         int offset;
         if (isSystemWindows()) {
            offset = fileName.indexOf(58, getAdsCriticalOffset(fileName));
            if (offset != -1) {
               throw new IllegalArgumentException("NTFS ADS separator (':') in file name is forbidden.");
            }
         }

         offset = fileName.lastIndexOf(46);
         int lastSeparator = indexOfLastSeparator(fileName);
         return lastSeparator > offset ? -1 : offset;
      }
   }

   public static String getPrefix(String fileName) {
      if (fileName == null) {
         return null;
      } else {
         int len = getPrefixLength(fileName);
         if (len < 0) {
            return null;
         } else if (len > fileName.length()) {
            requireNonNullChars(fileName + '/');
            return fileName + '/';
         } else {
            String path = fileName.substring(0, len);
            requireNonNullChars(path);
            return path;
         }
      }
   }

   public static String getPath(String fileName) {
      return doGetPath(fileName, 1);
   }

   public static String getPathNoEndSeparator(String fileName) {
      return doGetPath(fileName, 0);
   }

   private static String doGetPath(String fileName, int separatorAdd) {
      if (fileName == null) {
         return null;
      } else {
         int prefix = getPrefixLength(fileName);
         if (prefix < 0) {
            return null;
         } else {
            int index = indexOfLastSeparator(fileName);
            int endIndex = index + separatorAdd;
            if (prefix < fileName.length() && index >= 0 && prefix < endIndex) {
               String path = fileName.substring(prefix, endIndex);
               requireNonNullChars(path);
               return path;
            } else {
               return "";
            }
         }
      }
   }

   public static String getFullPath(String fileName) {
      return doGetFullPath(fileName, true);
   }

   public static String getFullPathNoEndSeparator(String fileName) {
      return doGetFullPath(fileName, false);
   }

   private static String doGetFullPath(String fileName, boolean includeSeparator) {
      if (fileName == null) {
         return null;
      } else {
         int prefix = getPrefixLength(fileName);
         if (prefix < 0) {
            return null;
         } else if (prefix >= fileName.length()) {
            return includeSeparator ? getPrefix(fileName) : fileName;
         } else {
            int index = indexOfLastSeparator(fileName);
            if (index < 0) {
               return fileName.substring(0, prefix);
            } else {
               int end = index + (includeSeparator ? 1 : 0);
               if (end == 0) {
                  ++end;
               }

               return fileName.substring(0, end);
            }
         }
      }
   }

   public static String getName(String fileName) {
      if (fileName == null) {
         return null;
      } else {
         requireNonNullChars(fileName);
         int index = indexOfLastSeparator(fileName);
         return fileName.substring(index + 1);
      }
   }

   private static void requireNonNullChars(String path) {
      if (path.indexOf(0) >= 0) {
         throw new IllegalArgumentException("Null byte present in file/path name. There are no known legitimate use cases for such data, but several injection attacks may use it");
      }
   }

   public static String getBaseName(String fileName) {
      return removeExtension(getName(fileName));
   }

   public static String getExtension(String fileName) throws IllegalArgumentException {
      if (fileName == null) {
         return null;
      } else {
         int index = indexOfExtension(fileName);
         return index == -1 ? "" : fileName.substring(index + 1);
      }
   }

   private static int getAdsCriticalOffset(String fileName) {
      int offset1 = fileName.lastIndexOf(SYSTEM_SEPARATOR);
      int offset2 = fileName.lastIndexOf(OTHER_SEPARATOR);
      if (offset1 == -1) {
         return offset2 == -1 ? 0 : offset2 + 1;
      } else {
         return offset2 == -1 ? offset1 + 1 : Math.max(offset1, offset2) + 1;
      }
   }

   public static String removeExtension(String fileName) {
      if (fileName == null) {
         return null;
      } else {
         requireNonNullChars(fileName);
         int index = indexOfExtension(fileName);
         return index == -1 ? fileName : fileName.substring(0, index);
      }
   }

   public static boolean equals(String fileName1, String fileName2) {
      return equals(fileName1, fileName2, false, IOCase.SENSITIVE);
   }

   public static boolean equalsOnSystem(String fileName1, String fileName2) {
      return equals(fileName1, fileName2, false, IOCase.SYSTEM);
   }

   public static boolean equalsNormalized(String fileName1, String fileName2) {
      return equals(fileName1, fileName2, true, IOCase.SENSITIVE);
   }

   public static boolean equalsNormalizedOnSystem(String fileName1, String fileName2) {
      return equals(fileName1, fileName2, true, IOCase.SYSTEM);
   }

   public static boolean equals(String fileName1, String fileName2, boolean normalized, IOCase caseSensitivity) {
      if (fileName1 != null && fileName2 != null) {
         if (normalized) {
            fileName1 = normalize(fileName1);
            if (fileName1 == null) {
               return false;
            }

            fileName2 = normalize(fileName2);
            if (fileName2 == null) {
               return false;
            }
         }

         if (caseSensitivity == null) {
            caseSensitivity = IOCase.SENSITIVE;
         }

         return caseSensitivity.checkEquals(fileName1, fileName2);
      } else {
         return fileName1 == null && fileName2 == null;
      }
   }

   public static boolean isExtension(String fileName, String extension) {
      if (fileName == null) {
         return false;
      } else {
         requireNonNullChars(fileName);
         if (extension != null && !extension.isEmpty()) {
            String fileExt = getExtension(fileName);
            return fileExt.equals(extension);
         } else {
            return indexOfExtension(fileName) == -1;
         }
      }
   }

   public static boolean isExtension(String fileName, String... extensions) {
      if (fileName == null) {
         return false;
      } else {
         requireNonNullChars(fileName);
         if (extensions != null && extensions.length != 0) {
            String fileExt = getExtension(fileName);
            String[] var3 = extensions;
            int var4 = extensions.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               String extension = var3[var5];
               if (fileExt.equals(extension)) {
                  return true;
               }
            }

            return false;
         } else {
            return indexOfExtension(fileName) == -1;
         }
      }
   }

   public static boolean isExtension(String fileName, Collection extensions) {
      if (fileName == null) {
         return false;
      } else {
         requireNonNullChars(fileName);
         if (extensions != null && !extensions.isEmpty()) {
            String fileExt = getExtension(fileName);
            Iterator var3 = extensions.iterator();

            String extension;
            do {
               if (!var3.hasNext()) {
                  return false;
               }

               extension = (String)var3.next();
            } while(!fileExt.equals(extension));

            return true;
         } else {
            return indexOfExtension(fileName) == -1;
         }
      }
   }

   public static boolean wildcardMatch(String fileName, String wildcardMatcher) {
      return wildcardMatch(fileName, wildcardMatcher, IOCase.SENSITIVE);
   }

   public static boolean wildcardMatchOnSystem(String fileName, String wildcardMatcher) {
      return wildcardMatch(fileName, wildcardMatcher, IOCase.SYSTEM);
   }

   public static boolean wildcardMatch(String fileName, String wildcardMatcher, IOCase caseSensitivity) {
      if (fileName == null && wildcardMatcher == null) {
         return true;
      } else if (fileName != null && wildcardMatcher != null) {
         if (caseSensitivity == null) {
            caseSensitivity = IOCase.SENSITIVE;
         }

         String[] wcs = splitOnTokens(wildcardMatcher);
         boolean anyChars = false;
         int textIdx = 0;
         int wcsIdx = 0;
         Deque backtrack = new ArrayDeque(wcs.length);

         do {
            if (!backtrack.isEmpty()) {
               int[] array = (int[])backtrack.pop();
               wcsIdx = array[0];
               textIdx = array[1];
               anyChars = true;
            }

            for(; wcsIdx < wcs.length; ++wcsIdx) {
               if (wcs[wcsIdx].equals("?")) {
                  ++textIdx;
                  if (textIdx > fileName.length()) {
                     break;
                  }

                  anyChars = false;
               } else if (wcs[wcsIdx].equals("*")) {
                  anyChars = true;
                  if (wcsIdx == wcs.length - 1) {
                     textIdx = fileName.length();
                  }
               } else {
                  if (anyChars) {
                     textIdx = caseSensitivity.checkIndexOf(fileName, textIdx, wcs[wcsIdx]);
                     if (textIdx == -1) {
                        break;
                     }

                     int repeat = caseSensitivity.checkIndexOf(fileName, textIdx + 1, wcs[wcsIdx]);
                     if (repeat >= 0) {
                        backtrack.push(new int[]{wcsIdx, repeat});
                     }
                  } else if (!caseSensitivity.checkRegionMatches(fileName, textIdx, wcs[wcsIdx])) {
                     break;
                  }

                  textIdx += wcs[wcsIdx].length();
                  anyChars = false;
               }
            }

            if (wcsIdx == wcs.length && textIdx == fileName.length()) {
               return true;
            }
         } while(!backtrack.isEmpty());

         return false;
      } else {
         return false;
      }
   }

   static String[] splitOnTokens(String text) {
      if (text.indexOf(63) == -1 && text.indexOf(42) == -1) {
         return new String[]{text};
      } else {
         char[] array = text.toCharArray();
         ArrayList list = new ArrayList();
         StringBuilder buffer = new StringBuilder();
         char prevChar = 0;
         char[] var5 = array;
         int var6 = array.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            char ch = var5[var7];
            if (ch != '?' && ch != '*') {
               buffer.append(ch);
            } else {
               if (buffer.length() != 0) {
                  list.add(buffer.toString());
                  buffer.setLength(0);
               }

               if (ch == '?') {
                  list.add("?");
               } else if (prevChar != '*') {
                  list.add("*");
               }
            }

            prevChar = ch;
         }

         if (buffer.length() != 0) {
            list.add(buffer.toString());
         }

         return (String[])list.toArray(EMPTY_STRING_ARRAY);
      }
   }

   private static boolean isValidHostName(String name) {
      return isIPv6Address(name) || isRFC3986HostName(name);
   }

   private static boolean isIPv4Address(String name) {
      Matcher m = IPV4_PATTERN.matcher(name);
      if (m.matches() && m.groupCount() == 4) {
         for(int i = 1; i <= 4; ++i) {
            String ipSegment = m.group(i);
            int iIpSegment = Integer.parseInt(ipSegment);
            if (iIpSegment > 255) {
               return false;
            }

            if (ipSegment.length() > 1 && ipSegment.startsWith("0")) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean isIPv6Address(String inet6Address) {
      boolean containsCompressedZeroes = inet6Address.contains("::");
      if (containsCompressedZeroes && inet6Address.indexOf("::") != inet6Address.lastIndexOf("::")) {
         return false;
      } else if ((!inet6Address.startsWith(":") || inet6Address.startsWith("::")) && (!inet6Address.endsWith(":") || inet6Address.endsWith("::"))) {
         String[] octets = inet6Address.split(":");
         if (containsCompressedZeroes) {
            List octetList = new ArrayList(Arrays.asList(octets));
            if (inet6Address.endsWith("::")) {
               octetList.add("");
            } else if (inet6Address.startsWith("::") && !octetList.isEmpty()) {
               octetList.remove(0);
            }

            octets = (String[])octetList.toArray(EMPTY_STRING_ARRAY);
         }

         if (octets.length > 8) {
            return false;
         } else {
            int validOctets = 0;
            int emptyOctets = 0;

            for(int index = 0; index < octets.length; ++index) {
               String octet = octets[index];
               if (octet.isEmpty()) {
                  ++emptyOctets;
                  if (emptyOctets > 1) {
                     return false;
                  }
               } else {
                  emptyOctets = 0;
                  if (index == octets.length - 1 && octet.contains(".")) {
                     if (!isIPv4Address(octet)) {
                        return false;
                     }

                     validOctets += 2;
                     continue;
                  }

                  if (octet.length() > 4) {
                     return false;
                  }

                  int octetInt;
                  try {
                     octetInt = Integer.parseInt(octet, 16);
                  } catch (NumberFormatException var9) {
                     return false;
                  }

                  if (octetInt < 0 || octetInt > 65535) {
                     return false;
                  }
               }

               ++validOctets;
            }

            return validOctets <= 8 && (validOctets >= 8 || containsCompressedZeroes);
         }
      } else {
         return false;
      }
   }

   private static boolean isRFC3986HostName(String name) {
      String[] parts = name.split("\\.", -1);

      for(int i = 0; i < parts.length; ++i) {
         if (parts[i].isEmpty()) {
            return i == parts.length - 1;
         }

         if (!REG_NAME_PART_PATTERN.matcher(parts[i]).matches()) {
            return false;
         }
      }

      return true;
   }

   static {
      SYSTEM_SEPARATOR = File.separatorChar;
      if (isSystemWindows()) {
         OTHER_SEPARATOR = '/';
      } else {
         OTHER_SEPARATOR = '\\';
      }

      IPV4_PATTERN = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
      REG_NAME_PART_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9-]*$");
   }
}
