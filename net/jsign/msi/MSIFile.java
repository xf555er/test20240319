package net.jsign.msi;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.jsign.DigestAlgorithm;
import net.jsign.Signable;
import net.jsign.asn1.authenticode.AuthenticodeObjectIdentifiers;
import net.jsign.asn1.authenticode.SpcAttributeTypeAndOptionalValue;
import net.jsign.asn1.authenticode.SpcIndirectDataContent;
import net.jsign.asn1.authenticode.SpcSipInfo;
import net.jsign.asn1.authenticode.SpcUuid;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1InputStream;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.DERNull;
import net.jsign.bouncycastle.asn1.cms.Attribute;
import net.jsign.bouncycastle.asn1.cms.AttributeTable;
import net.jsign.bouncycastle.asn1.cms.ContentInfo;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x509.DigestInfo;
import net.jsign.bouncycastle.cms.CMSProcessable;
import net.jsign.bouncycastle.cms.CMSSignedData;
import net.jsign.bouncycastle.cms.SignerInformation;
import net.jsign.poi.poifs.filesystem.DocumentEntry;
import net.jsign.poi.poifs.filesystem.DocumentInputStream;
import net.jsign.poi.poifs.filesystem.POIFSDocument;
import net.jsign.poi.poifs.filesystem.POIFSFileSystem;
import net.jsign.poi.poifs.property.DirectoryProperty;
import net.jsign.poi.poifs.property.DocumentProperty;
import net.jsign.poi.poifs.property.Property;
import net.jsign.poi.util.IOUtils;

public class MSIFile implements Closeable, Signable {
   private static final long MSI_HEADER = -3400479537158350111L;
   private static final String DIGITAL_SIGNATURE_ENTRY_NAME = "\u0005DigitalSignature";
   private static final String MSI_DIGITAL_SIGNATURE_EX_ENTRY_NAME = "\u0005MsiDigitalSignatureEx";
   private final POIFSFileSystem fs;
   private SeekableByteChannel channel;

   public static boolean isMSIFile(File file) throws IOException {
      DataInputStream in = new DataInputStream(new FileInputStream(file));
      Throwable var2 = null;

      boolean var3;
      try {
         var3 = in.readLong() == -3400479537158350111L;
      } catch (Throwable var12) {
         var2 = var12;
         throw var12;
      } finally {
         if (in != null) {
            if (var2 != null) {
               try {
                  in.close();
               } catch (Throwable var11) {
                  var2.addSuppressed(var11);
               }
            } else {
               in.close();
            }
         }

      }

      return var3;
   }

   public MSIFile(File file) throws IOException {
      this.fs = new POIFSFileSystem(file, false);
   }

   public MSIFile(SeekableByteChannel channel) throws IOException {
      this.channel = channel;
      InputStream in = new FilterInputStream(Channels.newInputStream(channel)) {
         public void close() {
         }
      };
      this.fs = new POIFSFileSystem(in);
   }

   public void close() throws IOException {
      this.fs.close();
      if (this.channel != null) {
         this.channel.close();
      }

   }

   public boolean hasExtendedSignature() {
      try {
         this.fs.getRoot().getEntry("\u0005MsiDigitalSignatureEx");
         return true;
      } catch (FileNotFoundException var2) {
         return false;
      }
   }

   private List getSortedProperties() {
      List entries = new ArrayList();
      this.append(this.fs.getPropertyTable().getRoot(), entries);
      return entries;
   }

   private void append(DirectoryProperty node, List entries) {
      Map sortedEntries = new TreeMap();
      Iterator var4 = node.iterator();

      Property property;
      while(var4.hasNext()) {
         property = (Property)var4.next();
         sortedEntries.put(new MSIStreamName(property.getName()), property);
      }

      var4 = sortedEntries.values().iterator();

      while(var4.hasNext()) {
         property = (Property)var4.next();
         if (!property.isDirectory()) {
            entries.add(property);
         } else {
            this.append((DirectoryProperty)property, entries);
         }
      }

   }

   public byte[] computeDigest(MessageDigest digest) {
      Iterator var2 = this.getSortedProperties().iterator();

      while(true) {
         Property property;
         String name;
         do {
            do {
               if (!var2.hasNext()) {
                  byte[] classId = new byte[16];
                  this.fs.getRoot().getStorageClsid().write(classId, 0);
                  digest.update(classId);
                  return digest.digest();
               }

               property = (Property)var2.next();
               name = (new MSIStreamName(property.getName())).decode();
            } while(name.equals("\u0005DigitalSignature"));
         } while(name.equals("\u0005MsiDigitalSignatureEx"));

         POIFSDocument document = new POIFSDocument((DocumentProperty)property, this.fs);
         long remaining = (long)document.getSize();

         int size;
         for(Iterator var8 = document.iterator(); var8.hasNext(); remaining -= (long)size) {
            ByteBuffer buffer = (ByteBuffer)var8.next();
            size = buffer.remaining();
            buffer.limit(buffer.position() + (int)Math.min(remaining, (long)size));
            digest.update(buffer);
         }
      }
   }

   public ASN1Object createIndirectData(DigestAlgorithm digestAlgorithm) {
      AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(digestAlgorithm.oid, DERNull.INSTANCE);
      DigestInfo digestInfo = new DigestInfo(algorithmIdentifier, this.computeDigest(digestAlgorithm.getMessageDigest()));
      SpcUuid uuid = new SpcUuid("F1100C00-0000-0000-C000-000000000046");
      SpcAttributeTypeAndOptionalValue data = new SpcAttributeTypeAndOptionalValue(AuthenticodeObjectIdentifiers.SPC_SIPINFO_OBJID, new SpcSipInfo(1, uuid));
      return new SpcIndirectDataContent(data, digestInfo);
   }

   public List getSignatures() throws IOException {
      List signatures = new ArrayList();

      try {
         DocumentEntry digitalSignature = (DocumentEntry)this.fs.getRoot().getEntry("\u0005DigitalSignature");
         if (digitalSignature != null) {
            byte[] signatureBytes = IOUtils.toByteArray(new DocumentInputStream(digitalSignature));

            try {
               CMSSignedData signedData = new CMSSignedData((CMSProcessable)null, ContentInfo.getInstance((new ASN1InputStream(signatureBytes)).readObject()));
               signatures.add(signedData);
               SignerInformation signerInformation = (SignerInformation)signedData.getSignerInfos().getSigners().iterator().next();
               AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();
               if (unsignedAttributes != null) {
                  Attribute nestedSignatures = unsignedAttributes.get(AuthenticodeObjectIdentifiers.SPC_NESTED_SIGNATURE_OBJID);
                  if (nestedSignatures != null) {
                     Iterator var8 = nestedSignatures.getAttrValues().iterator();

                     while(var8.hasNext()) {
                        ASN1Encodable nestedSignature = (ASN1Encodable)var8.next();
                        signatures.add(new CMSSignedData((CMSProcessable)null, ContentInfo.getInstance(nestedSignature)));
                     }
                  }
               }
            } catch (UnsupportedOperationException var10) {
            } catch (Exception var11) {
               var11.printStackTrace();
            }
         }
      } catch (FileNotFoundException var12) {
      }

      return signatures;
   }

   public void setSignature(CMSSignedData signature) throws IOException {
      byte[] signatureBytes = signature.toASN1Structure().getEncoded("DER");
      this.fs.getRoot().createOrUpdateDocument("\u0005DigitalSignature", new ByteArrayInputStream(signatureBytes));
   }

   public void save() throws IOException {
      if (this.channel == null) {
         this.fs.writeFilesystem();
      } else {
         this.channel.position(0L);
         this.fs.writeFilesystem(Channels.newOutputStream(this.channel));
         this.channel.truncate(this.channel.position());
      }

   }
}
