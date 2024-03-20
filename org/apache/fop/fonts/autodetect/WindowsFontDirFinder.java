package org.apache.fop.fonts.autodetect;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class WindowsFontDirFinder implements FontDirFinder {
   private String getWinDir(String osName) throws IOException {
      Process process = null;
      Runtime runtime = Runtime.getRuntime();
      if (osName.startsWith("Windows 9")) {
         process = runtime.exec("command.com /c echo %windir%");
      } else {
         process = runtime.exec("cmd.exe /c echo %windir%");
      }

      InputStreamReader isr = null;
      BufferedReader bufferedReader = null;
      String dir = "";

      try {
         isr = new InputStreamReader(process.getInputStream());
         bufferedReader = new BufferedReader(isr);
         dir = bufferedReader.readLine();
      } finally {
         IOUtils.closeQuietly((Reader)bufferedReader);
         IOUtils.closeQuietly((Reader)isr);
      }

      return dir;
   }

   public List find() {
      List fontDirList = new ArrayList();
      String windir = null;

      try {
         windir = System.getProperty("env.windir");
      } catch (SecurityException var9) {
      }

      String osName = System.getProperty("os.name");
      if (windir == null) {
         try {
            windir = this.getWinDir(osName);
         } catch (IOException var8) {
         }
      }

      File osFontsDir = null;
      File psFontsDir = null;
      if (windir != null) {
         if (windir.endsWith("/")) {
            windir = windir.substring(0, windir.length() - 1);
         }

         osFontsDir = new File(windir + File.separator + "FONTS");
         if (osFontsDir.exists() && osFontsDir.canRead()) {
            fontDirList.add(osFontsDir);
         }

         psFontsDir = new File(windir.substring(0, 2) + File.separator + "PSFONTS");
         if (psFontsDir.exists() && psFontsDir.canRead()) {
            fontDirList.add(psFontsDir);
         }
      } else {
         String windowsDirName = osName.endsWith("NT") ? "WINNT" : "WINDOWS";

         char driveLetter;
         for(driveLetter = 'C'; driveLetter <= 'E'; ++driveLetter) {
            osFontsDir = new File(driveLetter + ":" + File.separator + windowsDirName + File.separator + "FONTS");
            if (osFontsDir.exists() && osFontsDir.canRead()) {
               fontDirList.add(osFontsDir);
               break;
            }
         }

         for(driveLetter = 'C'; driveLetter <= 'E'; ++driveLetter) {
            psFontsDir = new File(driveLetter + ":" + File.separator + "PSFONTS");
            if (psFontsDir.exists() && psFontsDir.canRead()) {
               fontDirList.add(psFontsDir);
               break;
            }
         }
      }

      return fontDirList;
   }
}
