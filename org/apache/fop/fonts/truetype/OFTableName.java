package org.apache.fop.fonts.truetype;

public final class OFTableName {
   public static final OFTableName TABLE_DIRECTORY = new OFTableName("tableDirectory");
   public static final OFTableName BASE = new OFTableName("BASE");
   public static final OFTableName CFF = new OFTableName("CFF ");
   public static final OFTableName EBDT = new OFTableName("EBDT");
   public static final OFTableName EBLC = new OFTableName("EBLC");
   public static final OFTableName EBSC = new OFTableName("EBSC");
   public static final OFTableName FFTM = new OFTableName("FFTM");
   public static final OFTableName GDEF = new OFTableName("GDEF");
   public static final OFTableName GPOS = new OFTableName("GPOS");
   public static final OFTableName GSUB = new OFTableName("GSUB");
   public static final OFTableName LTSH = new OFTableName("LTSH");
   public static final OFTableName OS2 = new OFTableName("OS/2");
   public static final OFTableName PCLT = new OFTableName("PCLT");
   public static final OFTableName VDMX = new OFTableName("VDMX");
   public static final OFTableName CMAP = new OFTableName("cmap");
   public static final OFTableName CVT = new OFTableName("cvt ");
   public static final OFTableName FPGM = new OFTableName("fpgm");
   public static final OFTableName GASP = new OFTableName("gasp");
   public static final OFTableName GLYF = new OFTableName("glyf");
   public static final OFTableName HDMX = new OFTableName("hdmx");
   public static final OFTableName HEAD = new OFTableName("head");
   public static final OFTableName HHEA = new OFTableName("hhea");
   public static final OFTableName HMTX = new OFTableName("hmtx");
   public static final OFTableName KERN = new OFTableName("kern");
   public static final OFTableName LOCA = new OFTableName("loca");
   public static final OFTableName MAXP = new OFTableName("maxp");
   public static final OFTableName NAME = new OFTableName("name");
   public static final OFTableName POST = new OFTableName("post");
   public static final OFTableName PREP = new OFTableName("prep");
   public static final OFTableName VHEA = new OFTableName("vhea");
   public static final OFTableName VMTX = new OFTableName("vmtx");
   public static final OFTableName SVG = new OFTableName("SVG ");
   private final String name;

   private OFTableName(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public static OFTableName getValue(String tableName) {
      if (tableName != null) {
         return new OFTableName(tableName);
      } else {
         throw new IllegalArgumentException("A TrueType font table name must not be null");
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof OFTableName)) {
         return false;
      } else {
         OFTableName to = (OFTableName)o;
         return this.name.equals(to.getName());
      }
   }

   public String toString() {
      return this.name;
   }
}
