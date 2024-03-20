package net.jsign.mscab;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
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
import net.jsign.bouncycastle.asn1.ASN1InputStream;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.DERNull;
import net.jsign.bouncycastle.asn1.cms.Attribute;
import net.jsign.bouncycastle.asn1.cms.AttributeTable;
import net.jsign.bouncycastle.asn1.cms.ContentInfo;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x509.DigestInfo;
import net.jsign.bouncycastle.cms.CMSException;
import net.jsign.bouncycastle.cms.CMSProcessable;
import net.jsign.bouncycastle.cms.CMSSignedData;
import net.jsign.bouncycastle.cms.SignerInformation;

public class MSCabinetFile implements Closeable, Signable {
   private final CFHeader header;
   private final SeekableByteChannel channel;

   public static boolean isMSCabinetFile(File file) throws IOException {
      if (file.exists() && file.isFile()) {
         try {
            MSCabinetFile cabFile = new MSCabinetFile(file);
            cabFile.close();
            return true;
         } catch (IOException var2) {
            if (!var2.getMessage().contains("MSCabinet header signature not found") && !var2.getMessage().contains("MSCabinet file too short")) {
               throw var2;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public MSCabinetFile(File file) throws IOException {
      this(Files.newByteChannel(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE));
   }

   public MSCabinetFile(SeekableByteChannel channel) throws IOException {
      this.header = new CFHeader();
      this.channel = channel;
      channel.position(0L);
      this.header.read(channel);
      if (this.header.csumHeader != 0L) {
         throw new IOException("MSCabinet file is corrupt: invalid reserved field in the header");
      } else {
         if (this.header.isReservePresent()) {
            if (this.header.cbCFHeader != 20) {
               throw new IOException("MSCabinet file is corrupt: cabinet reserved area size is " + this.header.cbCFHeader + " instead of " + 20);
            }

            CABSignature cabsig = this.header.getSignature();
            if (cabsig.header != 1048576) {
               throw new IOException("MSCabinet file is corrupt: signature header is " + cabsig.header);
            }

            if (cabsig.offset < channel.size() && cabsig.offset + cabsig.length > channel.size() || cabsig.offset > channel.size()) {
               throw new IOException("MSCabinet file is corrupt: signature data (offset=" + cabsig.offset + ", size=" + cabsig.length + ") after the end of the file");
            }
         }

      }
   }

   public void close() throws IOException {
      this.channel.close();
   }

   public synchronized byte[] computeDigest(MessageDigest digest) throws IOException {
      CFHeader modifiedHeader = new CFHeader(this.header);
      if (!this.header.isReservePresent()) {
         modifiedHeader.cbCFHeader = 20;
         modifiedHeader.cbCabinet += 24L;
         modifiedHeader.coffFiles += 24L;
         modifiedHeader.flags |= 4;
         CABSignature cabsig = new CABSignature();
         cabsig.offset = (long)((int)modifiedHeader.cbCabinet);
         modifiedHeader.abReserved = cabsig.array();
      }

      modifiedHeader.headerDigestUpdate(digest);
      this.channel.position((long)this.header.getHeaderSize());
      if (this.header.hasPreviousCabinet()) {
         digest.update(ChannelUtils.readNullTerminatedString(this.channel));
         digest.update(ChannelUtils.readNullTerminatedString(this.channel));
      }

      if (this.header.hasNextCabinet()) {
         digest.update(ChannelUtils.readNullTerminatedString(this.channel));
         digest.update(ChannelUtils.readNullTerminatedString(this.channel));
      }

      for(int i = 0; i < this.header.cFolders; ++i) {
         CFFolder folder = CFFolder.read(this.channel);
         if (!this.header.isReservePresent()) {
            folder.coffCabStart += 24L;
         }

         folder.digest(digest);
      }

      long endPosition = this.header.hasSignature() ? this.header.getSignature().offset : this.channel.size();
      ChannelUtils.updateDigest(this.channel, digest, this.channel.position(), endPosition);
      return digest.digest();
   }

   public ASN1Object createIndirectData(DigestAlgorithm digestAlgorithm) throws IOException {
      AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(digestAlgorithm.oid, DERNull.INSTANCE);
      DigestInfo digestInfo = new DigestInfo(algorithmIdentifier, this.computeDigest(digestAlgorithm.getMessageDigest()));
      SpcAttributeTypeAndOptionalValue data = new SpcAttributeTypeAndOptionalValue(AuthenticodeObjectIdentifiers.SPC_CAB_DATA_OBJID, new SpcPeImageData());
      return new SpcIndirectDataContent(data, digestInfo);
   }

   public synchronized List getSignatures() throws IOException {
      List signatures = new ArrayList();

      try {
         CABSignature cabsig = this.header.getSignature();
         if (cabsig != null && cabsig.offset > 0L) {
            byte[] buffer = new byte[(int)cabsig.length];
            this.channel.position(cabsig.offset);
            this.channel.read(ByteBuffer.wrap(buffer));
            CMSSignedData signedData = new CMSSignedData((CMSProcessable)null, ContentInfo.getInstance((new ASN1InputStream(buffer)).readObject()));
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
         }

         return signatures;
      } catch (CMSException var10) {
         throw new IOException(var10);
      }
   }

   public synchronized void setSignature(CMSSignedData signature) throws IOException {
      byte[] content = signature.toASN1Structure().getEncoded("DER");
      int shift = 0;
      if (!this.header.isReservePresent()) {
         shift = 24;
         ChannelUtils.insert(this.channel, 36L, new byte[shift]);
         this.header.cbCFHeader = 20;
         CFHeader var10000 = this.header;
         var10000.cbCabinet += (long)shift;
         var10000 = this.header;
         var10000.coffFiles += (long)shift;
         var10000 = this.header;
         var10000.flags |= 4;
         this.header.abReserved = new byte[20];
      }

      CABSignature cabsig = new CABSignature(this.header.abReserved);
      cabsig.header = 1048576;
      cabsig.offset = (long)((int)this.header.cbCabinet);
      cabsig.length = (long)content.length;
      this.header.abReserved = cabsig.array();
      this.channel.position(0L);
      ByteBuffer buffer = ByteBuffer.allocate(this.header.getHeaderSize()).order(ByteOrder.LITTLE_ENDIAN);
      this.header.write(buffer);
      buffer.flip();
      this.channel.write(buffer);
      if (this.header.hasPreviousCabinet()) {
         ChannelUtils.readNullTerminatedString(this.channel);
         ChannelUtils.readNullTerminatedString(this.channel);
      }

      if (this.header.hasNextCabinet()) {
         ChannelUtils.readNullTerminatedString(this.channel);
         ChannelUtils.readNullTerminatedString(this.channel);
      }

      for(int i = 0; i < this.header.cFolders; ++i) {
         long position = this.channel.position();
         CFFolder folder = CFFolder.read(this.channel);
         folder.coffCabStart += (long)shift;
         this.channel.position(position);
         folder.write(this.channel);
      }

      this.channel.position(cabsig.offset);
      this.channel.write(ByteBuffer.wrap(content));
      if (this.channel.position() < this.channel.size()) {
         this.channel.truncate(this.channel.position());
      }

   }

   public void save() {
   }
}
