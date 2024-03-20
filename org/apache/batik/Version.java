package org.apache.batik;

public final class Version {
   public static String getVersion() {
      Package pkg = Version.class.getPackage();
      String version = null;
      if (pkg != null) {
         version = pkg.getImplementationVersion();
      }

      String headURL = "$HeadURL: https://svn.apache.org/repos/asf/xmlgraphics/batik/branches/maven/batik-util/src/main/java/org/apache/batik/Version.java $";
      String prefix = "$HeadURL: ";
      String suffix = "/sources/org/apache/batik/Version.java $";
      if (headURL.startsWith(prefix) && headURL.endsWith(suffix)) {
         headURL = headURL.substring(prefix.length(), headURL.length() - suffix.length());
         if (!headURL.endsWith("/trunk")) {
            int index1 = headURL.lastIndexOf(47);
            int index2 = headURL.lastIndexOf(47, index1 - 1);
            String name = headURL.substring(index1 + 1);
            String type = headURL.substring(index2 + 1, index1);
            String tagPrefix = "batik-";
            if (type.equals("tags") && name.startsWith(tagPrefix)) {
               version = name.substring(tagPrefix.length()).replace('_', '.');
            } else if (type.equals("branches")) {
               version = version + "; " + name;
            }
         }
      }

      if (version == null) {
         version = "development version";
      }

      return version;
   }
}
