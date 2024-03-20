package net.jsign.pe;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import net.jsign.ChannelUtils;
import net.jsign.DigestAlgorithm;
import net.jsign.Signable;
import net.jsign.asn1.authenticode.AuthenticodeObjectIdentifiers;
import net.jsign.asn1.authenticode.SpcAttributeTypeAndOptionalValue;
import net.jsign.asn1.authenticode.SpcIndirectDataContent;
import net.jsign.asn1.authenticode.SpcPeImageData;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.DERNull;
import net.jsign.bouncycastle.asn1.cms.Attribute;
import net.jsign.bouncycastle.asn1.cms.AttributeTable;
import net.jsign.bouncycastle.asn1.cms.ContentInfo;
import net.jsign.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x509.DigestInfo;
import net.jsign.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import net.jsign.bouncycastle.cert.X509CertificateHolder;
import net.jsign.bouncycastle.cms.CMSProcessable;
import net.jsign.bouncycastle.cms.CMSSignedData;
import net.jsign.bouncycastle.cms.SignerInformation;

public class PEFile implements Closeable, Signable {
   private final long peHeaderOffset;
   private File file;
   final SeekableByteChannel channel;
   private final ByteBuffer valueBuffer;

   public static boolean isPEFile(File file) throws IOException {
      if (file.exists() && file.isFile()) {
         try {
            PEFile peFile = new PEFile(file);
            peFile.close();
            return true;
         } catch (IOException var2) {
            if (!var2.getMessage().contains("DOS header signature not found") && !var2.getMessage().contains("PE signature not found")) {
               throw var2;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public PEFile(File file) throws IOException {
      this(Files.newByteChannel(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE));
      this.file = file;
   }

   public PEFile(SeekableByteChannel channel) throws IOException {
      this.valueBuffer = ByteBuffer.allocate(8);
      this.valueBuffer.order(ByteOrder.LITTLE_ENDIAN);
      this.channel = channel;

      try {
         this.read(0L, 0, 2);
         if (this.valueBuffer.get() == 77 && this.valueBuffer.get() == 90) {
            this.read(60L, 0, 4);
            this.peHeaderOffset = (long)this.valueBuffer.getInt() & 4294967295L;
            this.read(this.peHeaderOffset, 0, 4);
            if (this.valueBuffer.get() != 80 || this.valueBuffer.get() != 69 || this.valueBuffer.get() != 0 || this.valueBuffer.get() != 0) {
               throw new IOException("PE signature not found as expected at offset 0x" + Long.toHexString(this.peHeaderOffset));
            }
         } else {
            throw new IOException("DOS header signature not found");
         }
      } catch (IOException var3) {
         channel.close();
         throw var3;
      }
   }

   public void save() {
   }

   public synchronized void close() throws IOException {
      this.channel.close();
   }

   synchronized int read(byte[] buffer, long base, int offset) {
      try {
         this.channel.position(base + (long)offset);
         return this.channel.read(ByteBuffer.wrap(buffer));
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      }
   }

   private void read(long base, int offset, int length) {
      try {
         this.valueBuffer.limit(length);
         this.valueBuffer.clear();
         this.channel.position(base + (long)offset);
         this.channel.read(this.valueBuffer);
         this.valueBuffer.rewind();
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      }
   }

   synchronized int read(long base, int offset) {
      this.read(base, offset, 1);
      return this.valueBuffer.get();
   }

   synchronized int readWord(long base, int offset) {
      this.read(base, offset, 2);
      return this.valueBuffer.getShort() & '\uffff';
   }

   synchronized long readDWord(long base, int offset) {
      this.read(base, offset, 4);
      return (long)this.valueBuffer.getInt() & 4294967295L;
   }

   synchronized long readQWord(long base, int offset) {
      this.read(base, offset, 8);
      return this.valueBuffer.getLong();
   }

   synchronized void write(long base, byte[] data) {
      try {
         this.channel.position(base);
         this.channel.write(ByteBuffer.wrap(data));
      } catch (IOException var5) {
         throw new RuntimeException(var5);
      }
   }

   public MachineType getMachineType() {
      return MachineType.valueOf(this.readWord(this.peHeaderOffset, 4));
   }

   public int getNumberOfSections() {
      return this.readWord(this.peHeaderOffset, 6);
   }

   public Date getTimeDateStamp() {
      return new Date(1000L * this.readDWord(this.peHeaderOffset, 8));
   }

   public long getPointerToSymbolTable() {
      return this.readDWord(this.peHeaderOffset, 12);
   }

   public long getNumberOfSymbols() {
      return this.readDWord(this.peHeaderOffset, 16);
   }

   public int getSizeOfOptionalHeader() {
      return this.readWord(this.peHeaderOffset, 20);
   }

   public int getCharacteristics() {
      return this.readWord(this.peHeaderOffset, 22);
   }

   public PEFormat getFormat() {
      return PEFormat.valueOf(this.readWord(this.peHeaderOffset, 24));
   }

   public int getMajorLinkerVersion() {
      return this.read(this.peHeaderOffset, 26);
   }

   public int getMinorLinkerVersion() {
      return this.read(this.peHeaderOffset, 27);
   }

   public long getSizeOfCode() {
      return this.readDWord(this.peHeaderOffset, 28);
   }

   public long getSizeOfInitializedData() {
      return this.readDWord(this.peHeaderOffset, 32);
   }

   public long getSizeOfUninitializedData() {
      return this.readDWord(this.peHeaderOffset, 36);
   }

   public long getAddressOfEntryPoint() {
      return this.readDWord(this.peHeaderOffset, 40);
   }

   public long getBaseOfCode() {
      return this.readDWord(this.peHeaderOffset, 44);
   }

   public long getBaseOfData() {
      return PEFormat.PE32.equals(this.getFormat()) ? this.readDWord(this.peHeaderOffset, 48) : 0L;
   }

   public long getImageBase() {
      return PEFormat.PE32.equals(this.getFormat()) ? this.readDWord(this.peHeaderOffset, 52) : this.readQWord(this.peHeaderOffset, 48);
   }

   public long getSectionAlignment() {
      return this.readDWord(this.peHeaderOffset, 56);
   }

   public long getFileAlignment() {
      return this.readDWord(this.peHeaderOffset, 60);
   }

   public int getMajorOperatingSystemVersion() {
      return this.readWord(this.peHeaderOffset, 64);
   }

   public int getMinorOperatingSystemVersion() {
      return this.readWord(this.peHeaderOffset, 66);
   }

   public int getMajorImageVersion() {
      return this.readWord(this.peHeaderOffset, 68);
   }

   public int getMinorImageVersion() {
      return this.readWord(this.peHeaderOffset, 70);
   }

   public int getMajorSubsystemVersion() {
      return this.readWord(this.peHeaderOffset, 72);
   }

   public int getMinorSubsystemVersion() {
      return this.readWord(this.peHeaderOffset, 74);
   }

   public long getWin32VersionValue() {
      return this.readDWord(this.peHeaderOffset, 76);
   }

   public long getSizeOfImage() {
      return this.readDWord(this.peHeaderOffset, 80);
   }

   public long getSizeOfHeaders() {
      return this.readDWord(this.peHeaderOffset, 84);
   }

   public long getCheckSum() {
      return this.readDWord(this.peHeaderOffset, 88);
   }

   public synchronized long computeChecksum() {
      PEImageChecksum checksum = new PEImageChecksum(this.peHeaderOffset + 88L);
      ByteBuffer b = ByteBuffer.allocate(65536);

      try {
         this.channel.position(0L);

         int len;
         while((len = this.channel.read(b)) > 0) {
            b.flip();
            checksum.update(b.array(), 0, len);
         }
      } catch (IOException var4) {
         throw new RuntimeException(var4);
      }

      return checksum.getValue();
   }

   public synchronized void updateChecksum() {
      ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
      buffer.putInt((int)this.computeChecksum());
      buffer.flip();

      try {
         this.channel.position(this.peHeaderOffset + 88L);
         this.channel.write(buffer);
      } catch (IOException var3) {
         throw new RuntimeException(var3);
      }
   }

   public Subsystem getSubsystem() {
      return Subsystem.valueOf(this.readWord(this.peHeaderOffset, 92));
   }

   public int getDllCharacteristics() {
      return this.readWord(this.peHeaderOffset, 94);
   }

   public long getSizeOfStackReserve() {
      return PEFormat.PE32.equals(this.getFormat()) ? this.readDWord(this.peHeaderOffset, 96) : this.readQWord(this.peHeaderOffset, 96);
   }

   public long getSizeOfStackCommit() {
      return PEFormat.PE32.equals(this.getFormat()) ? this.readDWord(this.peHeaderOffset, 100) : this.readQWord(this.peHeaderOffset, 104);
   }

   public long getSizeOfHeapReserve() {
      return PEFormat.PE32.equals(this.getFormat()) ? this.readDWord(this.peHeaderOffset, 104) : this.readQWord(this.peHeaderOffset, 112);
   }

   public long getSizeOfHeapCommit() {
      return PEFormat.PE32.equals(this.getFormat()) ? this.readDWord(this.peHeaderOffset, 108) : this.readQWord(this.peHeaderOffset, 120);
   }

   public long getLoaderFlags() {
      return this.readDWord(this.peHeaderOffset, PEFormat.PE32.equals(this.getFormat()) ? 112 : 128);
   }

   public int getNumberOfRvaAndSizes() {
      return (int)this.readDWord(this.peHeaderOffset, PEFormat.PE32.equals(this.getFormat()) ? 116 : 132);
   }

   int getDataDirectoryOffset() {
      return (int)this.peHeaderOffset + (PEFormat.PE32.equals(this.getFormat()) ? 120 : 136);
   }

   public DataDirectory getDataDirectory(DataDirectoryType type) {
      return type.ordinal() >= this.getNumberOfRvaAndSizes() ? null : new DataDirectory(this, type.ordinal());
   }

   public synchronized void writeDataDirectory(DataDirectoryType type, byte[] data) throws IOException {
      DataDirectory directory = this.getDataDirectory(type);
      long offset;
      if (!directory.exists()) {
         offset = this.channel.size();
         this.channel.position(offset);
         this.channel.write(ByteBuffer.wrap(data));
         directory.write(offset, data.length);
      } else if (data.length == directory.getSize()) {
         this.channel.position(directory.getVirtualAddress());
         this.channel.write(ByteBuffer.wrap(data));
      } else if (data.length < directory.getSize() && type != DataDirectoryType.CERTIFICATE_TABLE) {
         directory.erase();
         this.channel.position(directory.getVirtualAddress());
         this.channel.write(ByteBuffer.wrap(data));
         directory.write(directory.getVirtualAddress(), data.length);
      } else if (directory.isTrailing()) {
         this.channel.position(directory.getVirtualAddress());
         this.channel.write(ByteBuffer.wrap(data));
         this.channel.truncate(directory.getVirtualAddress() + (long)data.length);
         directory.write(directory.getVirtualAddress(), data.length);
      } else {
         if (type == DataDirectoryType.CERTIFICATE_TABLE) {
            throw new IOException("The certificate table isn't at the end of the file and can't be moved without invalidating the signature");
         }

         directory.erase();
         offset = this.channel.size();
         this.channel.position(offset);
         this.channel.write(ByteBuffer.wrap(data));
         directory.write(offset, data.length);
      }

      this.updateChecksum();
   }

   public synchronized List getSignatures() {
      List signatures = new ArrayList();
      Iterator var2 = this.getCertificateTable().iterator();

      while(var2.hasNext()) {
         CertificateTableEntry entry = (CertificateTableEntry)var2.next();

         try {
            CMSSignedData signedData = entry.getSignature();
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

      return signatures;
   }

   public void setSignature(CMSSignedData signature) throws IOException {
      CertificateTableEntry entry = new CertificateTableEntry(signature);
      this.writeDataDirectory(DataDirectoryType.CERTIFICATE_TABLE, entry.toBytes());
   }

   private synchronized List getCertificateTable() {
      List entries = new ArrayList();
      DataDirectory certificateTable = this.getDataDirectory(DataDirectoryType.CERTIFICATE_TABLE);
      if (certificateTable != null && certificateTable.exists()) {
         long position = certificateTable.getVirtualAddress();

         try {
            entries.add(new CertificateTableEntry(this, position));
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }

      return entries;
   }

   public synchronized List getSections() {
      List sections = new ArrayList();
      int sectionTableOffset = this.getDataDirectoryOffset() + 8 * this.getNumberOfRvaAndSizes();

      for(int i = 0; i < this.getNumberOfSections(); ++i) {
         sections.add(new Section(this, sectionTableOffset + 40 * i));
      }

      return sections;
   }

   public void printInfo(OutputStream out) {
      this.printInfo(new PrintWriter(out, true));
   }

   public void printInfo(PrintWriter out) {
      if (this.file != null) {
         out.println("PE File");
         out.println("  Name:          " + this.file.getName());
         out.println("  Size:          " + this.file.length());
         out.println("  Last Modified: " + new Date(this.file.lastModified()));
         out.println();
      }

      out.println("PE Header");
      out.println("  Machine:                    " + this.getMachineType());
      out.println("  Number of sections:         " + this.getNumberOfSections());
      out.println("  Timestamp:                  " + this.getTimeDateStamp());
      out.println("  Pointer to symbol table:    0x" + Long.toHexString(this.getPointerToSymbolTable()));
      out.println("  Number of symbols:          " + this.getNumberOfSymbols());
      out.println("  Size of optional header:    " + this.getSizeOfOptionalHeader());
      out.println("  Characteristics:            0x" + Long.toBinaryString((long)this.getCharacteristics()));
      out.println();
      out.println("Optional Header");
      PEFormat format = this.getFormat();
      out.println("  PE Format:                  0x" + Integer.toHexString(format.value) + " (" + format.label + ")");
      out.println("  Linker version:             " + this.getMajorLinkerVersion() + "." + this.getMinorLinkerVersion());
      out.println("  Size of code:               " + this.getSizeOfCode());
      out.println("  Size of initialized data:   " + this.getSizeOfInitializedData());
      out.println("  Size of uninitialized data: " + this.getSizeOfUninitializedData());
      out.println("  Address of entry point:     0x" + Long.toHexString(this.getAddressOfEntryPoint()));
      out.println("  Base of code:               0x" + Long.toHexString(this.getBaseOfCode()));
      if (PEFormat.PE32.equals(this.getFormat())) {
         out.println("  Base of data:               0x" + Long.toHexString(this.getBaseOfData()));
      }

      out.println("  Image base:                 0x" + Long.toHexString(this.getImageBase()));
      out.println("  Section alignment:          " + this.getSectionAlignment());
      out.println("  File alignment:             " + this.getFileAlignment());
      out.println("  Operating system version:   " + this.getMajorOperatingSystemVersion() + "." + this.getMinorOperatingSystemVersion());
      out.println("  Image version:              " + this.getMajorImageVersion() + "." + this.getMinorImageVersion());
      out.println("  Subsystem version:          " + this.getMajorSubsystemVersion() + "." + this.getMinorSubsystemVersion());
      out.println("  Size of image:              " + this.getSizeOfImage());
      out.println("  Size of headers:            " + this.getSizeOfHeaders());
      out.println("  Checksum:                   0x" + Long.toHexString(this.getCheckSum()));
      out.println("  Checksum (computed):        0x" + Long.toHexString(this.computeChecksum()));
      out.println("  Subsystem:                  " + this.getSubsystem());
      out.println("  DLL characteristics:        0x" + Long.toBinaryString((long)this.getDllCharacteristics()));
      out.println("  Size of stack reserve:      " + this.getSizeOfStackReserve());
      out.println("  Size of stack commit:       " + this.getSizeOfStackCommit());
      out.println("  Size of heap reserve:       " + this.getSizeOfHeapReserve());
      out.println("  Size of heap commit:        " + this.getSizeOfHeapCommit());
      out.println("  Number of RVA and sizes:    " + this.getNumberOfRvaAndSizes());
      out.println();
      out.println("Data Directory");
      DataDirectoryType[] var3 = DataDirectoryType.values();
      int i = var3.length;

      for(int var5 = 0; var5 < i; ++var5) {
         DataDirectoryType type = var3[var5];
         DataDirectory entry = this.getDataDirectory(type);
         if (entry != null && entry.exists()) {
            out.printf("  %-30s 0x%08x %8d bytes%n", type, entry.getVirtualAddress(), entry.getSize());
         }
      }

      out.println();
      out.println("Sections");
      out.println("      Name     Virtual Size  Virtual Address  Raw Data Size  Raw Data Ptr  Characteristics");
      List sections = this.getSections();

      for(i = 0; i < sections.size(); ++i) {
         Section section = (Section)sections.get(i);
         out.printf("  #%d  %-8s     %8d       0x%08x       %8d    0x%08x  %s%n", i + 1, section.getName(), section.getVirtualSize(), section.getVirtualAddress(), section.getSizeOfRawData(), section.getPointerToRawData(), section.getCharacteristics());
      }

      out.println();
      List signatures = this.getSignatures();
      if (!signatures.isEmpty()) {
         out.println("Signatures");
         Iterator var16 = signatures.iterator();

         while(var16.hasNext()) {
            CMSSignedData signedData = (CMSSignedData)var16.next();
            SignerInformation signerInformation = (SignerInformation)signedData.getSignerInfos().getSigners().iterator().next();
            X509CertificateHolder certificate = (X509CertificateHolder)signedData.getCertificates().getMatches(signerInformation.getSID()).iterator().next();
            String commonName = certificate.getSubject().getRDNs(X509ObjectIdentifiers.commonName)[0].getFirst().getValue().toString();
            AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();
            boolean timestamped = unsignedAttributes != null && (unsignedAttributes.get(PKCSObjectIdentifiers.pkcs_9_at_counterSignature) != null || unsignedAttributes.get(AuthenticodeObjectIdentifiers.SPC_RFC3161_OBJID) != null);
            DigestAlgorithm algorithm = DigestAlgorithm.of(signerInformation.getDigestAlgorithmID().getAlgorithm());
            out.println("  " + commonName + "  " + (algorithm != null ? "[" + algorithm.id + "]  " : "") + (timestamped ? "(timestamped)" : ""));
         }
      }

   }

   public synchronized byte[] computeDigest(MessageDigest digest) throws IOException {
      long checksumLocation = this.peHeaderOffset + 88L;
      DataDirectory certificateTable = this.getDataDirectory(DataDirectoryType.CERTIFICATE_TABLE);
      ChannelUtils.updateDigest(this.channel, digest, 0L, checksumLocation);
      long position = checksumLocation + 4L;
      int certificateTableOffset = this.getDataDirectoryOffset() + 8 * DataDirectoryType.CERTIFICATE_TABLE.ordinal();
      ChannelUtils.updateDigest(this.channel, digest, position, (long)certificateTableOffset);
      position = (long)(certificateTableOffset + 8);
      if (certificateTable != null && certificateTable.exists()) {
         ChannelUtils.updateDigest(this.channel, digest, position, certificateTable.getVirtualAddress());
         position = certificateTable.getVirtualAddress() + (long)certificateTable.getSize();
      }

      ChannelUtils.updateDigest(this.channel, digest, position, this.channel.size());
      return digest.digest();
   }

   public byte[] computeDigest(DigestAlgorithm algorithm) throws IOException {
      return this.computeDigest(algorithm.getMessageDigest());
   }

   public ASN1Object createIndirectData(DigestAlgorithm digestAlgorithm) throws IOException {
      AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(digestAlgorithm.oid, DERNull.INSTANCE);
      DigestInfo digestInfo = new DigestInfo(algorithmIdentifier, this.computeDigest(digestAlgorithm));
      SpcAttributeTypeAndOptionalValue data = new SpcAttributeTypeAndOptionalValue(AuthenticodeObjectIdentifiers.SPC_PE_IMAGE_DATA_OBJID, new SpcPeImageData());
      return new SpcIndirectDataContent(data, digestInfo);
   }

   public synchronized void pad(int multiple) throws IOException {
      long padding = ((long)multiple - this.channel.size() % (long)multiple) % (long)multiple;
      this.channel.position(this.channel.size());
      this.channel.write(ByteBuffer.allocate((int)padding));
   }
}
