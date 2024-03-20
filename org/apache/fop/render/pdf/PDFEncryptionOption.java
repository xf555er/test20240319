package org.apache.fop.render.pdf;

import org.apache.fop.render.RendererConfigOption;

public enum PDFEncryptionOption implements RendererConfigOption {
   ENCRYPTION_LENGTH("encryption-length", 128),
   NO_PRINTHQ("noprinthq", 40),
   NO_ASSEMBLEDOC("noassembledoc", false),
   NO_ACCESSCONTENT("noaccesscontent", false),
   NO_FILLINFORMS("nofillinforms", false),
   NO_ANNOTATIONS("noannotations", false),
   NO_PRINT("noprint", false),
   NO_COPY_CONTENT("nocopy", false),
   NO_EDIT_CONTENT("noedit", false),
   USER_PASSWORD("user-password", ""),
   OWNER_PASSWORD("owner-password", ""),
   ENCRYPT_METADATA("encrypt-metadata", true);

   public static final String ENCRYPTION_PARAMS = "encryption-params";
   private final String name;
   private final Object defaultValue;

   private PDFEncryptionOption(String name, Object defaultValue) {
      this.name = name;
      this.defaultValue = defaultValue;
   }

   private PDFEncryptionOption(String name) {
      this(name, (Object)null);
   }

   public String getName() {
      return this.name;
   }

   public Object getDefaultValue() {
      return this.defaultValue;
   }
}
