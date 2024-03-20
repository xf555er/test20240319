package org.apache.fop.hyphenation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SerializeHyphPattern {
   private boolean errorDump;

   public void setErrorDump(boolean errorDump) {
      this.errorDump = errorDump;
   }

   public void serializeDir(File sourceDir, File targetDir) {
      String extension = ".xml";
      String[] sourceFiles = sourceDir.list(new FilenameFilter() {
         public boolean accept(File dir, String name) {
            return name.endsWith(".xml");
         }
      });
      if (sourceFiles != null) {
         String[] var5 = sourceFiles;
         int var6 = sourceFiles.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String sourceFile = var5[var7];
            File infile = new File(sourceDir, sourceFile);
            String outfilename = sourceFile.substring(0, sourceFile.length() - ".xml".length()) + ".hyp";
            File outfile = new File(targetDir, outfilename);
            this.serializeFile(infile, outfile);
         }
      }

   }

   private void serializeFile(File infile, File outfile) {
      boolean startProcess = this.rebuild(infile, outfile);
      if (startProcess) {
         HyphenationTree hTree = this.buildPatternFile(infile);

         try {
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outfile)));
            out.writeObject(hTree);
            out.close();
         } catch (IOException var6) {
            System.err.println("Can't write compiled pattern file: " + outfile);
            System.err.println(var6);
         }
      }

   }

   private HyphenationTree buildPatternFile(File infile) {
      System.out.println("Processing " + infile);
      HyphenationTree hTree = new HyphenationTree();

      try {
         hTree.loadPatterns(infile.toString());
         if (this.errorDump) {
            System.out.println("Stats: ");
            hTree.printStats();
         }
      } catch (HyphenationException var4) {
         System.err.println("Can't load patterns from xml file " + infile + " - Maybe hyphenation.dtd is missing?");
         if (this.errorDump) {
            System.err.println(var4.toString());
         }
      }

      return hTree;
   }

   private boolean rebuild(File infile, File outfile) {
      if (outfile.exists()) {
         return outfile.lastModified() < infile.lastModified();
      } else {
         return true;
      }
   }

   public static void main(String[] args) {
      SerializeHyphPattern ser = new SerializeHyphPattern();
      ser.serializeDir(new File(args[0]), new File(args[1]));
   }
}
