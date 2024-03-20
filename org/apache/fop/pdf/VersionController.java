package org.apache.fop.pdf;

public abstract class VersionController {
   private Version version;

   private VersionController(Version version) {
      this.version = version;
   }

   public Version getPDFVersion() {
      return this.version;
   }

   public abstract void setPDFVersion(Version var1);

   abstract void addTableHeaderScopeAttribute(PDFStructElem var1, StandardStructureAttributes.Table.Scope var2);

   public String toString() {
      return this.version.toString();
   }

   public static VersionController getFixedVersionController(Version version) {
      if (version.compareTo(Version.V1_4) < 0) {
         throw new IllegalArgumentException("The PDF version cannot be set below version 1.4");
      } else {
         return new FixedVersion(version);
      }
   }

   public static VersionController getDynamicVersionController(Version initialVersion, PDFDocument doc) {
      return new DynamicVersion(initialVersion, doc);
   }

   // $FF: synthetic method
   VersionController(Version x0, Object x1) {
      this(x0);
   }

   private static final class DynamicVersion extends VersionController {
      private PDFDocument doc;

      private DynamicVersion(Version version, PDFDocument doc) {
         super(version, null);
         this.doc = doc;
      }

      public void setPDFVersion(Version version) {
         if (super.version.compareTo(version) < 0) {
            super.version = version;
            this.doc.getRoot().setVersion(version);
         }

      }

      void addTableHeaderScopeAttribute(PDFStructElem th, StandardStructureAttributes.Table.Scope scope) {
         this.setPDFVersion(Version.V1_5);
         StandardStructureAttributes.Table.Scope.addScopeAttribute(th, scope);
      }

      // $FF: synthetic method
      DynamicVersion(Version x0, PDFDocument x1, Object x2) {
         this(x0, x1);
      }
   }

   private static final class FixedVersion extends VersionController {
      private FixedVersion(Version version) {
         super(version, null);
      }

      public void setPDFVersion(Version version) {
         if (super.version.compareTo(version) != 0) {
            throw new IllegalStateException("Cannot change the version of this PDF document.");
         }
      }

      void addTableHeaderScopeAttribute(PDFStructElem th, StandardStructureAttributes.Table.Scope scope) {
         if (super.version.compareTo(Version.V1_4) > 0) {
            StandardStructureAttributes.Table.Scope.addScopeAttribute(th, scope);
         }

      }

      // $FF: synthetic method
      FixedVersion(Version x0, Object x1) {
         this(x0);
      }
   }
}
