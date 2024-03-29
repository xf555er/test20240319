package org.apache.commons.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/** @deprecated */
@Deprecated
public class FileSystemUtils {
   private static final FileSystemUtils INSTANCE = new FileSystemUtils();
   private static final int INIT_PROBLEM = -1;
   private static final int OTHER = 0;
   private static final int WINDOWS = 1;
   private static final int UNIX = 2;
   private static final int POSIX_UNIX = 3;
   private static final int OS;
   private static final String DF;

   /** @deprecated */
   @Deprecated
   public static long freeSpace(String path) throws IOException {
      return INSTANCE.freeSpaceOS(path, OS, false, Duration.ofMillis(-1L));
   }

   /** @deprecated */
   @Deprecated
   public static long freeSpaceKb(String path) throws IOException {
      return freeSpaceKb(path, -1L);
   }

   /** @deprecated */
   @Deprecated
   public static long freeSpaceKb(String path, long timeout) throws IOException {
      return INSTANCE.freeSpaceOS(path, OS, true, Duration.ofMillis(timeout));
   }

   /** @deprecated */
   @Deprecated
   public static long freeSpaceKb() throws IOException {
      return freeSpaceKb(-1L);
   }

   /** @deprecated */
   @Deprecated
   public static long freeSpaceKb(long timeout) throws IOException {
      return freeSpaceKb((new File(".")).getAbsolutePath(), timeout);
   }

   long freeSpaceOS(String path, int os, boolean kb, Duration timeout) throws IOException {
      if (path == null) {
         throw new IllegalArgumentException("Path must not be null");
      } else {
         switch (os) {
            case 0:
               throw new IllegalStateException("Unsupported operating system");
            case 1:
               return kb ? this.freeSpaceWindows(path, timeout) / 1024L : this.freeSpaceWindows(path, timeout);
            case 2:
               return this.freeSpaceUnix(path, kb, false, timeout);
            case 3:
               return this.freeSpaceUnix(path, kb, true, timeout);
            default:
               throw new IllegalStateException("Exception caught when determining operating system");
         }
      }
   }

   long freeSpaceWindows(String path, Duration timeout) throws IOException {
      String normPath = FilenameUtils.normalize(path, false);
      if (normPath == null) {
         throw new IllegalArgumentException(path);
      } else {
         if (!normPath.isEmpty() && normPath.charAt(0) != '"') {
            normPath = "\"" + normPath + "\"";
         }

         String[] cmdAttribs = new String[]{"cmd.exe", "/C", "dir /a /-c " + normPath};
         List lines = this.performCommand(cmdAttribs, Integer.MAX_VALUE, timeout);

         for(int i = lines.size() - 1; i >= 0; --i) {
            String line = (String)lines.get(i);
            if (!line.isEmpty()) {
               return this.parseDir(line, normPath);
            }
         }

         throw new IOException("Command line 'dir /-c' did not return any info for path '" + normPath + "'");
      }
   }

   long parseDir(String line, String path) throws IOException {
      int bytesStart = 0;
      int bytesEnd = 0;

      int j;
      char c;
      for(j = line.length() - 1; j >= 0; --j) {
         c = line.charAt(j);
         if (Character.isDigit(c)) {
            bytesEnd = j + 1;
            break;
         }
      }

      while(j >= 0) {
         c = line.charAt(j);
         if (!Character.isDigit(c) && c != ',' && c != '.') {
            bytesStart = j + 1;
            break;
         }

         --j;
      }

      if (j < 0) {
         throw new IOException("Command line 'dir /-c' did not return valid info for path '" + path + "'");
      } else {
         StringBuilder buf = new StringBuilder(line.substring(bytesStart, bytesEnd));

         for(int k = 0; k < buf.length(); ++k) {
            if (buf.charAt(k) == ',' || buf.charAt(k) == '.') {
               buf.deleteCharAt(k--);
            }
         }

         return this.parseBytes(buf.toString(), path);
      }
   }

   long freeSpaceUnix(String path, boolean kb, boolean posix, Duration timeout) throws IOException {
      if (path.isEmpty()) {
         throw new IllegalArgumentException("Path must not be empty");
      } else {
         String flags = "-";
         if (kb) {
            flags = flags + "k";
         }

         if (posix) {
            flags = flags + "P";
         }

         String[] cmdAttribs = flags.length() > 1 ? new String[]{DF, flags, path} : new String[]{DF, path};
         List lines = this.performCommand(cmdAttribs, 3, timeout);
         if (lines.size() < 2) {
            throw new IOException("Command line '" + DF + "' did not return info as expected for path '" + path + "'- response was " + lines);
         } else {
            String line2 = (String)lines.get(1);
            StringTokenizer tok = new StringTokenizer(line2, " ");
            String line3;
            if (tok.countTokens() < 4) {
               if (tok.countTokens() != 1 || lines.size() < 3) {
                  throw new IOException("Command line '" + DF + "' did not return data as expected for path '" + path + "'- check path is valid");
               }

               line3 = (String)lines.get(2);
               tok = new StringTokenizer(line3, " ");
            } else {
               tok.nextToken();
            }

            tok.nextToken();
            tok.nextToken();
            line3 = tok.nextToken();
            return this.parseBytes(line3, path);
         }
      }
   }

   long parseBytes(String freeSpace, String path) throws IOException {
      try {
         long bytes = Long.parseLong(freeSpace);
         if (bytes < 0L) {
            throw new IOException("Command line '" + DF + "' did not find free space in response for path '" + path + "'- check path is valid");
         } else {
            return bytes;
         }
      } catch (NumberFormatException var5) {
         throw new IOException("Command line '" + DF + "' did not return numeric data as expected for path '" + path + "'- check path is valid", var5);
      }
   }

   List performCommand(String[] cmdAttribs, int max, Duration timeout) throws IOException {
      List lines = new ArrayList(20);
      Process proc = null;
      InputStream in = null;
      OutputStream out = null;
      InputStream err = null;
      BufferedReader inr = null;

      ArrayList var12;
      try {
         Thread monitor = ThreadMonitor.start(timeout);
         proc = this.openProcess(cmdAttribs);
         in = proc.getInputStream();
         out = proc.getOutputStream();
         err = proc.getErrorStream();
         inr = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));

         for(String line = inr.readLine(); line != null && lines.size() < max; line = inr.readLine()) {
            line = line.toLowerCase(Locale.ENGLISH).trim();
            lines.add(line);
         }

         proc.waitFor();
         ThreadMonitor.stop(monitor);
         if (proc.exitValue() != 0) {
            throw new IOException("Command line returned OS error code '" + proc.exitValue() + "' for command " + Arrays.asList(cmdAttribs));
         }

         if (lines.isEmpty()) {
            throw new IOException("Command line did not return any info for command " + Arrays.asList(cmdAttribs));
         }

         inr.close();
         inr = null;
         in.close();
         in = null;
         if (out != null) {
            out.close();
            out = null;
         }

         if (err != null) {
            err.close();
            err = null;
         }

         var12 = lines;
      } catch (InterruptedException var16) {
         throw new IOException("Command line threw an InterruptedException for command " + Arrays.asList(cmdAttribs) + " timeout=" + timeout, var16);
      } finally {
         IOUtils.closeQuietly(in);
         IOUtils.closeQuietly(out);
         IOUtils.closeQuietly(err);
         IOUtils.closeQuietly((Reader)inr);
         if (proc != null) {
            proc.destroy();
         }

      }

      return var12;
   }

   Process openProcess(String[] cmdAttribs) throws IOException {
      return Runtime.getRuntime().exec(cmdAttribs);
   }

   static {
      int os = 0;
      String dfPath = "df";

      try {
         String osName = System.getProperty("os.name");
         if (osName == null) {
            throw new IOException("os.name not found");
         }

         osName = osName.toLowerCase(Locale.ENGLISH);
         if (osName.contains("windows")) {
            os = 1;
         } else if (!osName.contains("linux") && !osName.contains("mpe/ix") && !osName.contains("freebsd") && !osName.contains("openbsd") && !osName.contains("irix") && !osName.contains("digital unix") && !osName.contains("unix") && !osName.contains("mac os x")) {
            if (!osName.contains("sun os") && !osName.contains("sunos") && !osName.contains("solaris")) {
               if (osName.contains("hp-ux") || osName.contains("aix")) {
                  os = 3;
               }
            } else {
               os = 3;
               dfPath = "/usr/xpg4/bin/df";
            }
         } else {
            os = 2;
         }
      } catch (Exception var3) {
         os = -1;
      }

      OS = os;
      DF = dfPath;
   }
}
