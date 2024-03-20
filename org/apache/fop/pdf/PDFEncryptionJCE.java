package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class PDFEncryptionJCE extends PDFObject implements PDFEncryption {
   private final MessageDigest digest;
   private SecureRandom random;
   private byte[] encryptionKey;
   private String encryptionDictionary;
   private boolean useAlgorithm31a;
   private boolean encryptMetadata = true;
   private Version pdfVersion;
   private static byte[] ivZero = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

   private PDFEncryptionJCE(PDFObjectNumber objectNumber, PDFEncryptionParams params, PDFDocument pdf) {
      this.pdfVersion = Version.V1_4;
      this.setObjectNumber(objectNumber);

      try {
         if (params.getEncryptionLengthInBits() == 256) {
            this.digest = MessageDigest.getInstance("SHA-256");
         } else {
            this.digest = MessageDigest.getInstance("MD5");
         }
      } catch (NoSuchAlgorithmException var5) {
         throw new UnsupportedOperationException(var5.getMessage());
      }

      this.setDocument(pdf);
      EncryptionInitializer encryptionInitializer = new EncryptionInitializer(params);
      encryptionInitializer.init();
      this.useAlgorithm31a = encryptionInitializer.isVersion5Revision5Algorithm();
   }

   public static PDFEncryption make(PDFObjectNumber objectNumber, PDFEncryptionParams params, PDFDocument pdf) {
      return new PDFEncryptionJCE(objectNumber, params, pdf);
   }

   public byte[] encrypt(byte[] data, PDFObject refObj) {
      PDFObject o;
      for(o = refObj; o != null && !o.hasObjectNumber(); o = o.getParent()) {
      }

      if (o == null && !this.useAlgorithm31a) {
         throw new IllegalStateException("No object number could be obtained for a PDF object");
      } else {
         byte[] key;
         if (this.useAlgorithm31a) {
            key = new byte[16];
            this.random.nextBytes(key);
            byte[] encryptedData = encryptWithKey(this.encryptionKey, data, false, key);
            byte[] storedData = new byte[encryptedData.length + 16];
            System.arraycopy(key, 0, storedData, 0, 16);
            System.arraycopy(encryptedData, 0, storedData, 16, encryptedData.length);
            return storedData;
         } else {
            key = this.createEncryptionKey(o.getObjectNumber().getNumber(), o.getGeneration());
            return encryptWithKey(key, data);
         }
      }
   }

   public void applyFilter(AbstractPDFStream stream) {
      if (this.encryptMetadata || !(stream instanceof PDFMetadata)) {
         stream.getFilterList().addFilter((PDFFilter)(new EncryptionFilter(stream.getObjectNumber(), stream.getGeneration())));
      }
   }

   public byte[] toPDF() {
      assert this.encryptionDictionary != null;

      return encode(this.encryptionDictionary);
   }

   public String getTrailerEntry() {
      return "/Encrypt " + this.getObjectNumber() + " " + this.getGeneration() + " R\n";
   }

   private static byte[] encryptWithKey(byte[] key, byte[] data) {
      try {
         Cipher c = initCipher(key);
         return c.doFinal(data);
      } catch (IllegalBlockSizeException var3) {
         throw new IllegalStateException(var3.getMessage());
      } catch (BadPaddingException var4) {
         throw new IllegalStateException(var4.getMessage());
      }
   }

   private static byte[] encryptWithKey(byte[] key, byte[] data, boolean noPadding, byte[] iv) {
      try {
         Cipher c = initCipher(key, noPadding, iv);
         return c.doFinal(data);
      } catch (IllegalBlockSizeException var5) {
         throw new IllegalStateException(var5.getMessage());
      } catch (BadPaddingException var6) {
         throw new IllegalStateException(var6.getMessage());
      }
   }

   private static Cipher initCipher(byte[] key) {
      try {
         SecretKeySpec keyspec = new SecretKeySpec(key, "RC4");
         Cipher cipher = Cipher.getInstance("RC4");
         cipher.init(1, keyspec);
         return cipher;
      } catch (InvalidKeyException var3) {
         throw new IllegalStateException(var3);
      } catch (NoSuchAlgorithmException var4) {
         throw new UnsupportedOperationException(var4);
      } catch (NoSuchPaddingException var5) {
         throw new UnsupportedOperationException(var5);
      }
   }

   private static Cipher initCipher(byte[] key, boolean noPadding, byte[] iv) {
      try {
         SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
         IvParameterSpec ivspec = new IvParameterSpec(iv);
         Cipher cipher = noPadding ? Cipher.getInstance("AES/CBC/NoPadding") : Cipher.getInstance("AES/CBC/PKCS5Padding");
         cipher.init(1, skeySpec, ivspec);
         return cipher;
      } catch (InvalidKeyException var6) {
         throw new IllegalStateException(var6);
      } catch (NoSuchAlgorithmException var7) {
         throw new UnsupportedOperationException(var7);
      } catch (NoSuchPaddingException var8) {
         throw new UnsupportedOperationException(var8);
      } catch (InvalidAlgorithmParameterException var9) {
         throw new UnsupportedOperationException(var9);
      }
   }

   private byte[] createEncryptionKey(int objectNumber, int generationNumber) {
      byte[] md5Input = this.prepareMD5Input(objectNumber, generationNumber);
      this.digest.reset();
      byte[] hash = this.digest.digest(md5Input);
      int keyLength = Math.min(16, md5Input.length);
      byte[] key = new byte[keyLength];
      System.arraycopy(hash, 0, key, 0, keyLength);
      return key;
   }

   private byte[] prepareMD5Input(int objectNumber, int generationNumber) {
      byte[] md5Input = new byte[this.encryptionKey.length + 5];
      System.arraycopy(this.encryptionKey, 0, md5Input, 0, this.encryptionKey.length);
      int i = this.encryptionKey.length;
      md5Input[i++] = (byte)(objectNumber >>> 0);
      md5Input[i++] = (byte)(objectNumber >>> 8);
      md5Input[i++] = (byte)(objectNumber >>> 16);
      md5Input[i++] = (byte)(generationNumber >>> 0);
      md5Input[i++] = (byte)(generationNumber >>> 8);
      return md5Input;
   }

   public Version getPDFVersion() {
      return this.pdfVersion;
   }

   private class EncryptionFilter extends PDFFilter {
      private PDFObjectNumber streamNumber;
      private int streamGeneration;

      EncryptionFilter(PDFObjectNumber streamNumber, int streamGeneration) {
         this.streamNumber = streamNumber;
         this.streamGeneration = streamGeneration;
      }

      public String getName() {
         return "";
      }

      public PDFObject getDecodeParms() {
         return null;
      }

      public OutputStream applyFilter(OutputStream out) throws IOException {
         byte[] key;
         Cipher cipher;
         if (PDFEncryptionJCE.this.useAlgorithm31a) {
            key = new byte[16];
            PDFEncryptionJCE.this.random.nextBytes(key);
            cipher = PDFEncryptionJCE.initCipher(PDFEncryptionJCE.this.encryptionKey, false, key);
            out.write(key);
            out.flush();
            return new CipherOutputStream(out, cipher);
         } else {
            key = PDFEncryptionJCE.this.createEncryptionKey(this.streamNumber.getNumber(), this.streamGeneration);
            cipher = PDFEncryptionJCE.initCipher(key);
            return new CipherOutputStream(out, cipher);
         }
      }
   }

   private class Rev5Engine extends InitializationEngine {
      private byte[] userValidationSalt = new byte[8];
      private byte[] userKeySalt = new byte[8];
      private byte[] ownerValidationSalt = new byte[8];
      private byte[] ownerKeySalt = new byte[8];
      private byte[] ueValue;
      private byte[] oeValue;
      private final boolean encryptMetadata;

      Rev5Engine(EncryptionSettings encryptionSettings) {
         super(encryptionSettings);
         this.encryptMetadata = encryptionSettings.encryptMetadata;
      }

      void run() {
         super.run();
         PDFEncryptionJCE.this.random = new SecureRandom();
         this.createEncryptionKey();
         this.computeUValue();
         this.computeOValue();
         this.computeUEValue();
         this.computeOEValue();
      }

      protected String getEncryptionDictionaryPart() {
         String encryptionDictionaryPart = super.getEncryptionDictionaryPart();
         encryptionDictionaryPart = encryptionDictionaryPart + "/OE " + PDFText.toHex(this.oeValue) + "\n/UE " + PDFText.toHex(this.ueValue) + "\n/Perms " + PDFText.toHex(this.computePermsValue(this.permissions)) + "\n/EncryptMetadata " + this.encryptMetadata + "\n/CF <</StdCF <</AuthEvent /DocOpen /CFM /AESV3 /Length 32>>>>\n/StmF /StdCF /StrF /StdCF\n";
         return encryptionDictionaryPart;
      }

      protected void computeUValue() {
         byte[] userBytes = new byte[16];
         PDFEncryptionJCE.this.random.nextBytes(userBytes);
         System.arraycopy(userBytes, 0, this.userValidationSalt, 0, 8);
         System.arraycopy(userBytes, 8, this.userKeySalt, 0, 8);
         PDFEncryptionJCE.this.digest.reset();
         byte[] prepared = this.preparedUserPassword;
         byte[] concatenated = new byte[prepared.length + 8];
         System.arraycopy(prepared, 0, concatenated, 0, prepared.length);
         System.arraycopy(this.userValidationSalt, 0, concatenated, prepared.length, 8);
         PDFEncryptionJCE.this.digest.update(concatenated);
         byte[] sha256 = PDFEncryptionJCE.this.digest.digest();
         this.uValue = new byte[48];
         System.arraycopy(sha256, 0, this.uValue, 0, 32);
         System.arraycopy(this.userValidationSalt, 0, this.uValue, 32, 8);
         System.arraycopy(this.userKeySalt, 0, this.uValue, 40, 8);
      }

      protected void computeOValue() {
         byte[] ownerBytes = new byte[16];
         PDFEncryptionJCE.this.random.nextBytes(ownerBytes);
         System.arraycopy(ownerBytes, 0, this.ownerValidationSalt, 0, 8);
         System.arraycopy(ownerBytes, 8, this.ownerKeySalt, 0, 8);
         PDFEncryptionJCE.this.digest.reset();
         byte[] prepared = this.preparedOwnerPassword;
         byte[] concatenated = new byte[prepared.length + 56];
         System.arraycopy(prepared, 0, concatenated, 0, prepared.length);
         System.arraycopy(this.ownerValidationSalt, 0, concatenated, prepared.length, 8);
         System.arraycopy(this.uValue, 0, concatenated, prepared.length + 8, 48);
         PDFEncryptionJCE.this.digest.update(concatenated);
         byte[] sha256 = PDFEncryptionJCE.this.digest.digest();
         this.oValue = new byte[48];
         System.arraycopy(sha256, 0, this.oValue, 0, 32);
         System.arraycopy(this.ownerValidationSalt, 0, this.oValue, 32, 8);
         System.arraycopy(this.ownerKeySalt, 0, this.oValue, 40, 8);
      }

      protected void createEncryptionKey() {
         PDFEncryptionJCE.this.encryptionKey = new byte[this.encryptionLengthInBytes];
         PDFEncryptionJCE.this.random.nextBytes(PDFEncryptionJCE.this.encryptionKey);
      }

      protected byte[] preparePassword(String password) {
         try {
            byte[] passwordBytes = password.getBytes("UTF-8");
            byte[] preparedPassword;
            if (passwordBytes.length > 127) {
               preparedPassword = new byte[127];
               System.arraycopy(passwordBytes, 0, preparedPassword, 0, 127);
            } else {
               preparedPassword = new byte[passwordBytes.length];
               System.arraycopy(passwordBytes, 0, preparedPassword, 0, passwordBytes.length);
            }

            return preparedPassword;
         } catch (UnsupportedEncodingException var5) {
            throw new UnsupportedOperationException(var5.getMessage());
         }
      }

      private void computeUEValue() {
         PDFEncryptionJCE.this.digest.reset();
         byte[] prepared = this.preparedUserPassword;
         byte[] concatenated = new byte[prepared.length + 8];
         System.arraycopy(prepared, 0, concatenated, 0, prepared.length);
         System.arraycopy(this.userKeySalt, 0, concatenated, prepared.length, 8);
         PDFEncryptionJCE.this.digest.update(concatenated);
         byte[] ueEncryptionKey = PDFEncryptionJCE.this.digest.digest();
         this.ueValue = PDFEncryptionJCE.encryptWithKey(ueEncryptionKey, PDFEncryptionJCE.this.encryptionKey, true, PDFEncryptionJCE.ivZero);
      }

      private void computeOEValue() {
         PDFEncryptionJCE.this.digest.reset();
         byte[] prepared = this.preparedOwnerPassword;
         byte[] concatenated = new byte[prepared.length + 56];
         System.arraycopy(prepared, 0, concatenated, 0, prepared.length);
         System.arraycopy(this.ownerKeySalt, 0, concatenated, prepared.length, 8);
         System.arraycopy(this.uValue, 0, concatenated, prepared.length + 8, 48);
         PDFEncryptionJCE.this.digest.update(concatenated);
         byte[] oeEncryptionKey = PDFEncryptionJCE.this.digest.digest();
         this.oeValue = PDFEncryptionJCE.encryptWithKey(oeEncryptionKey, PDFEncryptionJCE.this.encryptionKey, true, PDFEncryptionJCE.ivZero);
      }

      public byte[] computePermsValue(int permissions) {
         byte[] perms = new byte[16];
         long extendedPermissions = -4294967296L | (long)permissions;

         for(int k = 0; k < 8; ++k) {
            perms[k] = (byte)((int)(extendedPermissions & 255L));
            extendedPermissions >>= 8;
         }

         if (this.encryptMetadata) {
            perms[8] = 84;
         } else {
            perms[8] = 70;
         }

         perms[9] = 97;
         perms[10] = 100;
         perms[11] = 98;
         byte[] randomBytes = new byte[4];
         PDFEncryptionJCE.this.random.nextBytes(randomBytes);
         System.arraycopy(randomBytes, 0, perms, 12, 4);
         byte[] encryptedPerms = PDFEncryptionJCE.encryptWithKey(PDFEncryptionJCE.this.encryptionKey, perms, true, PDFEncryptionJCE.ivZero);
         return encryptedPerms;
      }
   }

   private class Rev3Engine extends RevBefore5Engine {
      Rev3Engine(EncryptionSettings encryptionSettings) {
         super(encryptionSettings);
      }

      protected byte[] computeOValueStep3(byte[] hash) {
         for(int i = 0; i < 50; ++i) {
            hash = PDFEncryptionJCE.this.digest.digest(hash);
         }

         return hash;
      }

      protected byte[] computeOValueStep7(byte[] key, byte[] encryptionResult) {
         return this.xorKeyAndEncrypt19Times(key, encryptionResult);
      }

      protected byte[] createEncryptionKeyStep6(byte[] hash) {
         for(int i = 0; i < 50; ++i) {
            PDFEncryptionJCE.this.digest.update(hash, 0, this.encryptionLengthInBytes);
            hash = PDFEncryptionJCE.this.digest.digest();
         }

         return hash;
      }

      protected void computeUValue() {
         PDFEncryptionJCE.this.digest.reset();
         PDFEncryptionJCE.this.digest.update(this.padding);
         PDFEncryptionJCE.this.digest.update(PDFEncryptionJCE.this.getDocumentSafely().getFileIDGenerator().getOriginalFileID());
         byte[] encryptionResult = PDFEncryptionJCE.encryptWithKey(PDFEncryptionJCE.this.encryptionKey, PDFEncryptionJCE.this.digest.digest());
         encryptionResult = this.xorKeyAndEncrypt19Times(PDFEncryptionJCE.this.encryptionKey, encryptionResult);
         this.uValue = new byte[32];
         System.arraycopy(encryptionResult, 0, this.uValue, 0, 16);
         Arrays.fill(this.uValue, 16, 32, (byte)0);
      }

      private byte[] xorKeyAndEncrypt19Times(byte[] key, byte[] input) {
         byte[] result = input;
         byte[] encryptionKey = new byte[key.length];

         for(int i = 1; i <= 19; ++i) {
            for(int j = 0; j < key.length; ++j) {
               encryptionKey[j] = (byte)(key[j] ^ i);
            }

            result = PDFEncryptionJCE.encryptWithKey(encryptionKey, result);
         }

         return result;
      }
   }

   private class Rev2Engine extends RevBefore5Engine {
      Rev2Engine(EncryptionSettings encryptionSettings) {
         super(encryptionSettings);
      }

      protected byte[] computeOValueStep3(byte[] hash) {
         return hash;
      }

      protected byte[] computeOValueStep7(byte[] key, byte[] encryptionResult) {
         return encryptionResult;
      }

      protected byte[] createEncryptionKeyStep6(byte[] hash) {
         return hash;
      }

      protected void computeUValue() {
         this.uValue = PDFEncryptionJCE.encryptWithKey(PDFEncryptionJCE.this.encryptionKey, this.padding);
      }
   }

   private abstract class RevBefore5Engine extends InitializationEngine {
      protected final byte[] padding = new byte[]{40, -65, 78, 94, 78, 117, -118, 65, 100, 0, 78, 86, -1, -6, 1, 8, 46, 46, 0, -74, -48, 104, 62, -128, 47, 12, -87, -2, 100, 83, 105, 122};

      RevBefore5Engine(EncryptionSettings encryptionSettings) {
         super(encryptionSettings);
      }

      protected void computeOValue() {
         byte[] md5Input = this.preparedOwnerPassword;
         PDFEncryptionJCE.this.digest.reset();
         byte[] hash = PDFEncryptionJCE.this.digest.digest(md5Input);
         hash = this.computeOValueStep3(hash);
         byte[] key = new byte[this.encryptionLengthInBytes];
         System.arraycopy(hash, 0, key, 0, this.encryptionLengthInBytes);
         byte[] encryptionResult = PDFEncryptionJCE.encryptWithKey(key, this.preparedUserPassword);
         this.oValue = this.computeOValueStep7(key, encryptionResult);
      }

      protected void createEncryptionKey() {
         PDFEncryptionJCE.this.digest.reset();
         PDFEncryptionJCE.this.digest.update(this.preparedUserPassword);
         PDFEncryptionJCE.this.digest.update(this.oValue);
         PDFEncryptionJCE.this.digest.update((byte)(this.permissions >>> 0));
         PDFEncryptionJCE.this.digest.update((byte)(this.permissions >>> 8));
         PDFEncryptionJCE.this.digest.update((byte)(this.permissions >>> 16));
         PDFEncryptionJCE.this.digest.update((byte)(this.permissions >>> 24));
         PDFEncryptionJCE.this.digest.update(PDFEncryptionJCE.this.getDocumentSafely().getFileIDGenerator().getOriginalFileID());
         byte[] hash = PDFEncryptionJCE.this.digest.digest();
         hash = this.createEncryptionKeyStep6(hash);
         PDFEncryptionJCE.this.encryptionKey = new byte[this.encryptionLengthInBytes];
         System.arraycopy(hash, 0, PDFEncryptionJCE.this.encryptionKey, 0, this.encryptionLengthInBytes);
      }

      protected byte[] preparePassword(String password) {
         int finalLength = 32;
         byte[] preparedPassword = new byte[finalLength];

         try {
            byte[] passwordBytes = password.getBytes("UTF-8");
            if (passwordBytes.length >= finalLength) {
               System.arraycopy(passwordBytes, 0, preparedPassword, 0, finalLength);
            } else {
               System.arraycopy(passwordBytes, 0, preparedPassword, 0, passwordBytes.length);
               System.arraycopy(this.padding, 0, preparedPassword, passwordBytes.length, finalLength - passwordBytes.length);
            }

            return preparedPassword;
         } catch (UnsupportedEncodingException var5) {
            throw new UnsupportedOperationException(var5);
         }
      }

      void run() {
         super.run();
         this.computeOValue();
         this.createEncryptionKey();
         this.computeUValue();
      }

      protected abstract byte[] computeOValueStep3(byte[] var1);

      protected abstract byte[] computeOValueStep7(byte[] var1, byte[] var2);

      protected abstract byte[] createEncryptionKeyStep6(byte[] var1);
   }

   private abstract class InitializationEngine {
      protected final int encryptionLengthInBytes;
      protected final int permissions;
      private final String userPassword;
      private final String ownerPassword;
      protected byte[] oValue;
      protected byte[] uValue;
      protected byte[] preparedUserPassword;
      protected byte[] preparedOwnerPassword;

      InitializationEngine(EncryptionSettings encryptionSettings) {
         this.encryptionLengthInBytes = encryptionSettings.encryptionLength / 8;
         this.permissions = encryptionSettings.permissions;
         this.userPassword = encryptionSettings.userPassword;
         this.ownerPassword = encryptionSettings.ownerPassword;
      }

      void run() {
         this.preparedUserPassword = this.preparePassword(this.userPassword);
         if (this.ownerPassword != null && this.ownerPassword.length() != 0) {
            this.preparedOwnerPassword = this.preparePassword(this.ownerPassword);
         } else {
            this.preparedOwnerPassword = this.preparedUserPassword;
         }

      }

      protected String getEncryptionDictionaryPart() {
         String encryptionDictionaryPart = "/O " + PDFText.toHex(this.oValue) + "\n/U " + PDFText.toHex(this.uValue) + "\n";
         return encryptionDictionaryPart;
      }

      protected abstract void computeOValue();

      protected abstract void computeUValue();

      protected abstract void createEncryptionKey();

      protected abstract byte[] preparePassword(String var1);
   }

   private static final class EncryptionSettings {
      final int encryptionLength;
      final int permissions;
      final String userPassword;
      final String ownerPassword;
      final boolean encryptMetadata;

      EncryptionSettings(int encryptionLength, int permissions, String userPassword, String ownerPassword, boolean encryptMetadata) {
         this.encryptionLength = encryptionLength;
         this.permissions = permissions;
         this.userPassword = userPassword;
         this.ownerPassword = ownerPassword;
         this.encryptMetadata = encryptMetadata;
      }
   }

   private static enum Permission {
      PRINT(3),
      EDIT_CONTENT(4),
      COPY_CONTENT(5),
      EDIT_ANNOTATIONS(6),
      FILL_IN_FORMS(9),
      ACCESS_CONTENT(10),
      ASSEMBLE_DOCUMENT(11),
      PRINT_HQ(12);

      private final int mask;

      private Permission(int bit) {
         this.mask = 1 << bit - 1;
      }

      private int removeFrom(int permissions) {
         return permissions - this.mask;
      }

      static int computePermissions(PDFEncryptionParams encryptionParams) {
         int permissions = -4;
         if (!encryptionParams.isAllowPrint()) {
            permissions = PRINT.removeFrom(permissions);
         }

         if (!encryptionParams.isAllowCopyContent()) {
            permissions = COPY_CONTENT.removeFrom(permissions);
         }

         if (!encryptionParams.isAllowEditContent()) {
            permissions = EDIT_CONTENT.removeFrom(permissions);
         }

         if (!encryptionParams.isAllowEditAnnotations()) {
            permissions = EDIT_ANNOTATIONS.removeFrom(permissions);
         }

         if (!encryptionParams.isAllowFillInForms()) {
            permissions = FILL_IN_FORMS.removeFrom(permissions);
         }

         if (!encryptionParams.isAllowAccessContent()) {
            permissions = ACCESS_CONTENT.removeFrom(permissions);
         }

         if (!encryptionParams.isAllowAssembleDocument()) {
            permissions = ASSEMBLE_DOCUMENT.removeFrom(permissions);
         }

         if (!encryptionParams.isAllowPrintHq()) {
            permissions = PRINT_HQ.removeFrom(permissions);
         }

         return permissions;
      }
   }

   private class EncryptionInitializer {
      private final PDFEncryptionParams encryptionParams;
      private int encryptionLength;
      private int version;
      private int revision;

      EncryptionInitializer(PDFEncryptionParams params) {
         this.encryptionParams = new PDFEncryptionParams(params);
      }

      void init() {
         this.encryptionLength = this.encryptionParams.getEncryptionLengthInBits();
         this.determineEncryptionAlgorithm();
         int permissions = PDFEncryptionJCE.Permission.computePermissions(this.encryptionParams);
         EncryptionSettings encryptionSettings = new EncryptionSettings(this.encryptionLength, permissions, this.encryptionParams.getUserPassword(), this.encryptionParams.getOwnerPassword(), this.encryptionParams.encryptMetadata());
         InitializationEngine initializationEngine = this.createEngine(encryptionSettings);
         initializationEngine.run();
         PDFEncryptionJCE.this.encryptionDictionary = this.createEncryptionDictionary(permissions, initializationEngine);
         PDFEncryptionJCE.this.encryptMetadata = this.encryptionParams.encryptMetadata();
      }

      private InitializationEngine createEngine(EncryptionSettings encryptionSettings) {
         if (this.revision == 5) {
            return PDFEncryptionJCE.this.new Rev5Engine(encryptionSettings);
         } else {
            return (InitializationEngine)(this.revision == 2 ? PDFEncryptionJCE.this.new Rev2Engine(encryptionSettings) : PDFEncryptionJCE.this.new Rev3Engine(encryptionSettings));
         }
      }

      private void determineEncryptionAlgorithm() {
         if (this.isVersion5Revision5Algorithm()) {
            this.version = 5;
            this.revision = 5;
            PDFEncryptionJCE.this.pdfVersion = Version.V1_7;
         } else if (this.isVersion1Revision2Algorithm()) {
            this.version = 1;
            this.revision = 2;
         } else {
            this.version = 2;
            this.revision = 3;
         }

      }

      private boolean isVersion1Revision2Algorithm() {
         return this.encryptionLength == 40 && this.encryptionParams.isAllowFillInForms() && this.encryptionParams.isAllowAccessContent() && this.encryptionParams.isAllowAssembleDocument() && this.encryptionParams.isAllowPrintHq();
      }

      private boolean isVersion5Revision5Algorithm() {
         return this.encryptionLength == 256;
      }

      private String createEncryptionDictionary(int permissions, InitializationEngine engine) {
         String encryptionDict = "<<\n/Filter /Standard\n/V " + this.version + "\n/R " + this.revision + "\n/Length " + this.encryptionLength + "\n/P " + permissions + "\n" + engine.getEncryptionDictionaryPart() + ">>";
         return encryptionDict;
      }
   }
}
