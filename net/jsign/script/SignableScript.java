package net.jsign.script;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.jsign.DigestAlgorithm;
import net.jsign.Signable;
import net.jsign.asn1.authenticode.AuthenticodeObjectIdentifiers;
import net.jsign.asn1.authenticode.SpcAttributeTypeAndOptionalValue;
import net.jsign.asn1.authenticode.SpcIndirectDataContent;
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
import net.jsign.commons.io.ByteOrderMark;
import net.jsign.commons.io.IOUtils;
import net.jsign.commons.io.input.BOMInputStream;

abstract class SignableScript implements Signable {
   private File file;
   private String content;
   private Charset encoding;
   private byte[] bom;

   public SignableScript() {
      this.encoding = StandardCharsets.UTF_8;
   }

   public SignableScript(File file) throws IOException {
      this(file, StandardCharsets.UTF_8);
   }

   public SignableScript(File file, Charset encoding) throws IOException {
      this.file = file;
      this.encoding = encoding != null ? encoding : StandardCharsets.UTF_8;
      ByteOrderMark[] supportedBOMs = new ByteOrderMark[]{ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE};
      BOMInputStream in = new BOMInputStream(new BufferedInputStream(new FileInputStream(file)), this.isByteOrderMarkSigned(), supportedBOMs);
      Throwable var5 = null;

      try {
         if (in.hasBOM()) {
            this.encoding = Charset.forName(in.getBOMCharsetName());
            if (!this.isByteOrderMarkSigned()) {
               this.bom = in.getBOM().getBytes();
            }
         } else if (StandardCharsets.UTF_8.equals(encoding) && !this.isUTF8AutoDetected()) {
            this.encoding = StandardCharsets.ISO_8859_1;
         }

         this.setContent(new String(IOUtils.toByteArray(in), this.encoding));
      } catch (Throwable var14) {
         var5 = var14;
         throw var14;
      } finally {
         if (in != null) {
            if (var5 != null) {
               try {
                  in.close();
               } catch (Throwable var13) {
                  var5.addSuppressed(var13);
               }
            } else {
               in.close();
            }
         }

      }

   }

   abstract boolean isByteOrderMarkSigned();

   boolean isUTF8AutoDetected() {
      return true;
   }

   public String getContent() {
      return this.content;
   }

   public void setContent(String content) {
      this.content = content;
   }

   abstract String getSignatureStart();

   abstract String getSignatureEnd();

   abstract String getLineCommentStart();

   abstract String getLineCommentEnd();

   abstract ASN1Object getSpcSipInfo();

   private Pattern getSignatureBlockPattern() {
      return Pattern.compile("(?s)\\r\\n" + this.getSignatureStart() + "\\r\\n(?<signatureBlock>.*)" + this.getSignatureEnd() + "\\r\\n");
   }

   private Pattern getSignatureBlockRemovalPattern() {
      return Pattern.compile("(?s)\\r?\\n" + this.getSignatureStart() + "\\r?\\n.*" + this.getSignatureEnd() + "\\r?\\n");
   }

   public List getSignatures() {
      List signatures = new ArrayList();

      try {
         CMSSignedData signedData = this.decodeSignatureBlock();
         if (signedData != null) {
            signatures.add(signedData);
            SignerInformation signerInformation = (SignerInformation)signedData.getSignerInfos().getSigners().iterator().next();
            AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();
            if (unsignedAttributes != null) {
               Attribute nestedSignatures = unsignedAttributes.get(AuthenticodeObjectIdentifiers.SPC_NESTED_SIGNATURE_OBJID);
               if (nestedSignatures != null) {
                  Iterator var6 = nestedSignatures.getAttrValues().iterator();

                  while(var6.hasNext()) {
                     ASN1Encodable nestedSignature = (ASN1Encodable)var6.next();
                     signatures.add(new CMSSignedData((CMSProcessable)null, ContentInfo.getInstance(nestedSignature)));
                  }
               }
            }
         }
      } catch (UnsupportedOperationException var8) {
      } catch (Exception var9) {
         var9.printStackTrace();
      }

      return signatures;
   }

   private String getSignatureBlock() {
      Matcher matcher = this.getSignatureBlockPattern().matcher(this.getContent());
      return !matcher.find() ? null : matcher.group("signatureBlock");
   }

   private CMSSignedData decodeSignatureBlock() throws CMSException {
      String signatureBlock = this.getSignatureBlock();
      if (signatureBlock == null) {
         return null;
      } else {
         signatureBlock = signatureBlock.replace(this.getLineCommentStart(), "");
         signatureBlock = signatureBlock.replace(this.getLineCommentEnd(), "");
         signatureBlock = signatureBlock.replaceAll("[\r\n]", "");
         byte[] signatureBytes = Base64.getDecoder().decode(signatureBlock);

         try {
            return new CMSSignedData((CMSProcessable)null, ContentInfo.getInstance((new ASN1InputStream(signatureBytes)).readObject()));
         } catch (IOException var4) {
            throw new IllegalArgumentException("Failed to construct ContentInfo from byte[]: ", var4);
         }
      }
   }

   public void setSignature(CMSSignedData signature) throws IOException {
      String content = this.getContentWithoutSignatureBlock();
      int pos = this.getSignatureInsertionPoint(content);
      this.content = content.substring(0, pos) + this.createSignatureBlock(signature) + content.substring(pos);
   }

   private String createSignatureBlock(CMSSignedData signature) throws IOException {
      byte[] signatureBytes = signature.toASN1Structure().getEncoded("DER");
      String signatureBlob = Base64.getEncoder().encodeToString(signatureBytes);
      StringBuilder signatureBlock = new StringBuilder();
      signatureBlock.append("\r\n");
      signatureBlock.append(this.getSignatureStart() + "\r\n");
      int start = 0;

      for(int blobLength = signatureBlob.length(); start < blobLength; start += 64) {
         signatureBlock.append(this.getLineCommentStart());
         signatureBlock.append(signatureBlob, start, Math.min(blobLength, start + 64));
         signatureBlock.append(this.getLineCommentEnd());
         signatureBlock.append("\r\n");
      }

      signatureBlock.append(this.getSignatureEnd() + "\r\n");
      return signatureBlock.toString();
   }

   protected int getSignatureInsertionPoint(String content) {
      return content.length();
   }

   protected String getContentWithoutSignatureBlock() {
      return this.getSignatureBlockRemovalPattern().matcher(this.getContent()).replaceFirst("");
   }

   public byte[] computeDigest(MessageDigest digest) {
      digest.update(this.getContentWithoutSignatureBlock().getBytes(StandardCharsets.UTF_16LE));
      return digest.digest();
   }

   public ASN1Object createIndirectData(DigestAlgorithm digestAlgorithm) {
      AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(digestAlgorithm.oid, DERNull.INSTANCE);
      DigestInfo digestInfo = new DigestInfo(algorithmIdentifier, this.computeDigest(digestAlgorithm.getMessageDigest()));
      SpcAttributeTypeAndOptionalValue data = new SpcAttributeTypeAndOptionalValue(AuthenticodeObjectIdentifiers.SPC_SIPINFO_OBJID, this.getSpcSipInfo());
      return new SpcIndirectDataContent(data, digestInfo);
   }

   public void save() throws IOException {
      if (this.file != null) {
         this.save(this.file);
      }

   }

   public void save(File file) throws IOException {
      FileOutputStream out = new FileOutputStream(file);
      Throwable var3 = null;

      try {
         if (this.bom != null) {
            out.write(this.bom);
         }

         out.write(this.getContent().getBytes(this.encoding));
         out.flush();
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (out != null) {
            if (var3 != null) {
               try {
                  out.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               out.close();
            }
         }

      }

   }
}
