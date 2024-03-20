package org.apache.batik.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ClassFileUtilities {
   public static final byte CONSTANT_UTF8_INFO = 1;
   public static final byte CONSTANT_INTEGER_INFO = 3;
   public static final byte CONSTANT_FLOAT_INFO = 4;
   public static final byte CONSTANT_LONG_INFO = 5;
   public static final byte CONSTANT_DOUBLE_INFO = 6;
   public static final byte CONSTANT_CLASS_INFO = 7;
   public static final byte CONSTANT_STRING_INFO = 8;
   public static final byte CONSTANT_FIELDREF_INFO = 9;
   public static final byte CONSTANT_METHODREF_INFO = 10;
   public static final byte CONSTANT_INTERFACEMETHODREF_INFO = 11;
   public static final byte CONSTANT_NAMEANDTYPE_INFO = 12;

   protected ClassFileUtilities() {
   }

   public static void main(String[] args) {
      boolean showFiles = false;
      if (args.length == 1 && args[0].equals("-f")) {
         showFiles = true;
      } else if (args.length != 0) {
         System.err.println("usage: org.apache.batik.util.ClassFileUtilities [-f]");
         System.err.println();
         System.err.println("  -f    list files that cause each jar file dependency");
         System.exit(1);
      }

      File cwd = new File(".");
      File buildDir = null;
      String[] cwdFiles = cwd.list();
      String[] var5 = cwdFiles;
      int var6 = cwdFiles.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         String cwdFile = var5[var7];
         if (cwdFile.startsWith("batik-")) {
            buildDir = new File(cwdFile);
            if (buildDir.isDirectory()) {
               break;
            }

            buildDir = null;
         }
      }

      if (buildDir != null && buildDir.isDirectory()) {
         try {
            Map cs = new HashMap();
            Map js = new HashMap();
            collectJars(buildDir, js, cs);
            Set classpath = new HashSet();
            Iterator i = js.values().iterator();

            while(i.hasNext()) {
               classpath.add(((Jar)i.next()).jarFile);
            }

            i = cs.values().iterator();

            ClassFile fromFile;
            Iterator var11;
            Object file;
            ClassFile fromFile;
            while(i.hasNext()) {
               fromFile = (ClassFile)i.next();
               Set result = getClassDependencies((InputStream)fromFile.getInputStream(), classpath, false);
               var11 = result.iterator();

               while(var11.hasNext()) {
                  file = var11.next();
                  fromFile = (ClassFile)cs.get(file);
                  if (fromFile != fromFile && fromFile != null) {
                     fromFile.deps.add(fromFile);
                  }
               }
            }

            i = cs.values().iterator();

            Jar fromJar;
            while(i.hasNext()) {
               fromFile = (ClassFile)i.next();
               Iterator var23 = fromFile.deps.iterator();

               while(var23.hasNext()) {
                  Object dep = var23.next();
                  ClassFile toFile = (ClassFile)dep;
                  fromJar = fromFile.jar;
                  Jar toJar = toFile.jar;
                  if (!fromFile.name.equals(toFile.name) && toJar != fromJar && !fromJar.files.contains(toFile.name)) {
                     Integer n = (Integer)fromJar.deps.get(toJar);
                     if (n == null) {
                        fromJar.deps.put(toJar, 1);
                     } else {
                        fromJar.deps.put(toJar, n + 1);
                     }
                  }
               }
            }

            List triples = new ArrayList(10);
            i = js.values().iterator();

            while(i.hasNext()) {
               Jar fromJar = (Jar)i.next();
               var11 = fromJar.deps.keySet().iterator();

               while(var11.hasNext()) {
                  file = var11.next();
                  fromJar = (Jar)file;
                  Triple t = new Triple();
                  t.from = fromJar;
                  t.to = fromJar;
                  t.count = (Integer)fromJar.deps.get(fromJar);
                  triples.add(t);
               }
            }

            Collections.sort(triples);
            i = triples.iterator();

            while(true) {
               Triple t;
               do {
                  if (!i.hasNext()) {
                     return;
                  }

                  t = (Triple)i.next();
                  System.out.println(t.count + "," + t.from.name + "," + t.to.name);
               } while(!showFiles);

               var11 = t.from.files.iterator();

               while(var11.hasNext()) {
                  file = var11.next();
                  fromFile = (ClassFile)file;
                  Iterator var30 = fromFile.deps.iterator();

                  while(var30.hasNext()) {
                     Object dep = var30.next();
                     ClassFile toFile = (ClassFile)dep;
                     if (toFile.jar == t.to && !t.from.files.contains(toFile.name)) {
                        System.out.println("\t" + fromFile.name + " --> " + toFile.name);
                     }
                  }
               }
            }
         } catch (IOException var17) {
            var17.printStackTrace();
         }
      } else {
         System.out.println("Directory 'batik-xxx' not found in current directory!");
      }
   }

   private static void collectJars(File dir, Map jars, Map classFiles) throws IOException {
      File[] files = dir.listFiles();
      File[] var4 = files;
      int var5 = files.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         File file = var4[var6];
         String n = file.getName();
         if (n.endsWith(".jar") && file.isFile()) {
            Jar j = new Jar();
            j.name = file.getPath();
            j.file = file;
            j.jarFile = new JarFile(file);
            jars.put(j.name, j);
            Enumeration entries = j.jarFile.entries();

            while(entries.hasMoreElements()) {
               ZipEntry ze = (ZipEntry)entries.nextElement();
               String name = ze.getName();
               if (name.endsWith(".class")) {
                  ClassFile cf = new ClassFile();
                  cf.name = name;
                  cf.jar = j;
                  classFiles.put(j.name + '!' + cf.name, cf);
                  j.files.add(cf);
               }
            }
         } else if (file.isDirectory()) {
            collectJars(file, jars, classFiles);
         }
      }

   }

   public static Set getClassDependencies(String path, Set classpath, boolean rec) throws IOException {
      return getClassDependencies((InputStream)(new FileInputStream(path)), classpath, rec);
   }

   public static Set getClassDependencies(InputStream is, Set classpath, boolean rec) throws IOException {
      Set result = new HashSet();
      Set done = new HashSet();
      computeClassDependencies(is, classpath, done, result, rec);
      return result;
   }

   private static void computeClassDependencies(InputStream is, Set classpath, Set done, Set result, boolean rec) throws IOException {
      Iterator var5 = getClassDependencies(is).iterator();

      while(true) {
         String s;
         do {
            if (!var5.hasNext()) {
               return;
            }

            Object o = var5.next();
            s = (String)o;
         } while(done.contains(s));

         done.add(s);
         Iterator var8 = classpath.iterator();

         while(var8.hasNext()) {
            Object aClasspath = var8.next();
            InputStream depis = null;
            String path = null;
            if (aClasspath instanceof JarFile) {
               JarFile jarFile = (JarFile)aClasspath;
               String classFileName = s + ".class";
               ZipEntry ze = jarFile.getEntry(classFileName);
               if (ze != null) {
                  path = jarFile.getName() + '!' + classFileName;
                  depis = jarFile.getInputStream(ze);
               }
            } else {
               path = (String)aClasspath + '/' + s + ".class";
               File f = new File(path);
               if (f.isFile()) {
                  depis = new FileInputStream(f);
               }
            }

            if (depis != null) {
               result.add(path);
               if (rec) {
                  computeClassDependencies((InputStream)depis, classpath, done, result, rec);
               }
            }
         }
      }
   }

   public static Set getClassDependencies(InputStream is) throws IOException {
      DataInputStream dis = new DataInputStream(is);
      if (dis.readInt() != -889275714) {
         throw new IOException("Invalid classfile");
      } else {
         dis.readInt();
         int len = dis.readShort();
         String[] strs = new String[len];
         Set classes = new HashSet();
         Set desc = new HashSet();

         for(int i = 1; i < len; ++i) {
            int constCode = dis.readByte() & 255;
            switch (constCode) {
               case 1:
                  strs[i] = dis.readUTF();
                  break;
               case 2:
               default:
                  throw new RuntimeException("unexpected data in constant-pool:" + constCode);
               case 3:
               case 4:
               case 9:
               case 10:
               case 11:
                  dis.readInt();
                  break;
               case 5:
               case 6:
                  dis.readLong();
                  ++i;
                  break;
               case 7:
                  classes.add(dis.readShort() & '\uffff');
                  break;
               case 8:
                  dis.readShort();
                  break;
               case 12:
                  dis.readShort();
                  desc.add(dis.readShort() & '\uffff');
            }
         }

         Set result = new HashSet();
         Iterator it = classes.iterator();

         while(it.hasNext()) {
            result.add(strs[(Integer)it.next()]);
         }

         it = desc.iterator();

         while(it.hasNext()) {
            result.addAll(getDescriptorClasses(strs[(Integer)it.next()]));
         }

         return result;
      }
   }

   protected static Set getDescriptorClasses(String desc) {
      Set result = new HashSet();
      int i = 0;
      char c = desc.charAt(i);
      StringBuffer sb;
      switch (c) {
         case '(':
            while(true) {
               ++i;
               c = desc.charAt(i);
               switch (c) {
                  case ')':
                     ++i;
                     c = desc.charAt(i);
                     switch (c) {
                        case 'V':
                        default:
                           return result;
                        case '[':
                           do {
                              ++i;
                              c = desc.charAt(i);
                           } while(c == '[');

                           if (c != 'L') {
                              return result;
                           }
                        case 'L':
                           ++i;
                           c = desc.charAt(i);

                           for(sb = new StringBuffer(); c != ';'; c = desc.charAt(i)) {
                              sb.append(c);
                              ++i;
                           }

                           result.add(sb.toString());
                           return result;
                     }
                  case '[':
                     do {
                        ++i;
                        c = desc.charAt(i);
                     } while(c == '[');

                     if (c != 'L') {
                        break;
                     }
                  case 'L':
                     ++i;
                     c = desc.charAt(i);

                     for(sb = new StringBuffer(); c != ';'; c = desc.charAt(i)) {
                        sb.append(c);
                        ++i;
                     }

                     result.add(sb.toString());
               }
            }
         case '[':
            do {
               ++i;
               c = desc.charAt(i);
            } while(c == '[');

            if (c != 'L') {
               break;
            }
         case 'L':
            ++i;
            c = desc.charAt(i);

            for(sb = new StringBuffer(); c != ';'; c = desc.charAt(i)) {
               sb.append(c);
               ++i;
            }

            result.add(sb.toString());
      }

      return result;
   }

   protected static class Triple implements Comparable {
      public Jar from;
      public Jar to;
      public int count;

      public int compareTo(Object o) {
         return ((Triple)o).count - this.count;
      }
   }

   protected static class Jar {
      public String name;
      public File file;
      public JarFile jarFile;
      public Map deps = new HashMap();
      public Set files = new HashSet();
   }

   protected static class ClassFile {
      public String name;
      public List deps = new ArrayList(10);
      public Jar jar;

      public InputStream getInputStream() throws IOException {
         return this.jar.jarFile.getInputStream(this.jar.jarFile.getEntry(this.name));
      }
   }
}
