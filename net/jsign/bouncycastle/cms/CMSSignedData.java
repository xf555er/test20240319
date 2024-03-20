package net.jsign.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1OctetString;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.BERSequence;
import net.jsign.bouncycastle.asn1.DLSet;
import net.jsign.bouncycastle.asn1.cms.ContentInfo;
import net.jsign.bouncycastle.asn1.cms.SignedData;
import net.jsign.bouncycastle.asn1.cms.SignerInfo;
import net.jsign.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import net.jsign.bouncycastle.util.Encodable;
import net.jsign.bouncycastle.util.Store;

public class CMSSignedData implements Encodable {
   private static final CMSSignedHelper HELPER;
   private static final DefaultDigestAlgorithmIdentifierFinder dgstAlgFinder;
   SignedData signedData;
   ContentInfo contentInfo;
   CMSTypedData signedContent;
   SignerInformationStore signerInfoStore;
   private Map hashes;

   private CMSSignedData(CMSSignedData var1) {
      this.signedData = var1.signedData;
      this.contentInfo = var1.contentInfo;
      this.signedContent = var1.signedContent;
      this.signerInfoStore = var1.signerInfoStore;
   }

   public CMSSignedData(byte[] var1) throws CMSException {
      this(CMSUtils.readContentInfo(var1));
   }

   public CMSSignedData(final CMSProcessable var1, ContentInfo var2) throws CMSException {
      if (var1 instanceof CMSTypedData) {
         this.signedContent = (CMSTypedData)var1;
      } else {
         this.signedContent = new CMSTypedData() {
            public ASN1ObjectIdentifier getContentType() {
               return CMSSignedData.this.signedData.getEncapContentInfo().getContentType();
            }

            public void write(OutputStream var1x) throws IOException, CMSException {
               var1.write(var1x);
            }

            public Object getContent() {
               return var1.getContent();
            }
         };
      }

      this.contentInfo = var2;
      this.signedData = this.getSignedData();
   }

   public CMSSignedData(ContentInfo var1) throws CMSException {
      this.contentInfo = var1;
      this.signedData = this.getSignedData();
      ASN1Encodable var2 = this.signedData.getEncapContentInfo().getContent();
      if (var2 != null) {
         if (var2 instanceof ASN1OctetString) {
            this.signedContent = new CMSProcessableByteArray(this.signedData.getEncapContentInfo().getContentType(), ((ASN1OctetString)var2).getOctets());
         } else {
            this.signedContent = new PKCS7ProcessableObject(this.signedData.getEncapContentInfo().getContentType(), var2);
         }
      } else {
         this.signedContent = null;
      }

   }

   private SignedData getSignedData() throws CMSException {
      try {
         return SignedData.getInstance(this.contentInfo.getContent());
      } catch (ClassCastException var2) {
         throw new CMSException("Malformed content.", var2);
      } catch (IllegalArgumentException var3) {
         throw new CMSException("Malformed content.", var3);
      }
   }

   public SignerInformationStore getSignerInfos() {
      if (this.signerInfoStore == null) {
         ASN1Set var1 = this.signedData.getSignerInfos();
         ArrayList var2 = new ArrayList();

         for(int var3 = 0; var3 != var1.size(); ++var3) {
            SignerInfo var4 = SignerInfo.getInstance(var1.getObjectAt(var3));
            ASN1ObjectIdentifier var5 = this.signedData.getEncapContentInfo().getContentType();
            if (this.hashes == null) {
               var2.add(new SignerInformation(var4, var5, this.signedContent, (byte[])null));
            } else {
               Object var6 = this.hashes.keySet().iterator().next();
               byte[] var7 = var6 instanceof String ? (byte[])((byte[])this.hashes.get(var4.getDigestAlgorithm().getAlgorithm().getId())) : (byte[])((byte[])this.hashes.get(var4.getDigestAlgorithm().getAlgorithm()));
               var2.add(new SignerInformation(var4, var5, (CMSProcessable)null, var7));
            }
         }

         this.signerInfoStore = new SignerInformationStore(var2);
      }

      return this.signerInfoStore;
   }

   public Store getCertificates() {
      return HELPER.getCertificates(this.signedData.getCertificates());
   }

   public String getSignedContentTypeOID() {
      return this.signedData.getEncapContentInfo().getContentType().getId();
   }

   public CMSTypedData getSignedContent() {
      return this.signedContent;
   }

   public ContentInfo toASN1Structure() {
      return this.contentInfo;
   }

   public byte[] getEncoded() throws IOException {
      return this.contentInfo.getEncoded();
   }

   public static CMSSignedData replaceSigners(CMSSignedData var0, SignerInformationStore var1) {
      CMSSignedData var2 = new CMSSignedData(var0);
      var2.signerInfoStore = var1;
      HashSet var3 = new HashSet();
      ASN1EncodableVector var4 = new ASN1EncodableVector();
      Iterator var5 = var1.getSigners().iterator();

      while(var5.hasNext()) {
         SignerInformation var6 = (SignerInformation)var5.next();
         CMSUtils.addDigestAlgs(var3, var6, dgstAlgFinder);
         var4.add(var6.toASN1Structure());
      }

      ASN1Set var10 = CMSUtils.convertToBERSet(var3);
      DLSet var7 = new DLSet(var4);
      ASN1Sequence var8 = (ASN1Sequence)var0.signedData.toASN1Primitive();
      var4 = new ASN1EncodableVector();
      var4.add(var8.getObjectAt(0));
      var4.add(var10);

      for(int var9 = 2; var9 != var8.size() - 1; ++var9) {
         var4.add(var8.getObjectAt(var9));
      }

      var4.add(var7);
      var2.signedData = SignedData.getInstance(new BERSequence(var4));
      var2.contentInfo = new ContentInfo(var2.contentInfo.getContentType(), var2.signedData);
      return var2;
   }

   static {
      HELPER = CMSSignedHelper.INSTANCE;
      dgstAlgFinder = new DefaultDigestAlgorithmIdentifierFinder();
   }
}
