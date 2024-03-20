package net.jsign.bouncycastle.tsp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.cms.Attribute;
import net.jsign.bouncycastle.asn1.cms.AttributeTable;
import net.jsign.bouncycastle.asn1.cms.ContentInfo;
import net.jsign.bouncycastle.asn1.ess.ESSCertID;
import net.jsign.bouncycastle.asn1.ess.ESSCertIDv2;
import net.jsign.bouncycastle.asn1.ess.SigningCertificate;
import net.jsign.bouncycastle.asn1.ess.SigningCertificateV2;
import net.jsign.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import net.jsign.bouncycastle.asn1.tsp.TSTInfo;
import net.jsign.bouncycastle.cms.CMSException;
import net.jsign.bouncycastle.cms.CMSSignedData;
import net.jsign.bouncycastle.cms.CMSTypedData;
import net.jsign.bouncycastle.cms.SignerInformation;

public class TimeStampToken {
   CMSSignedData tsToken;
   SignerInformation tsaSignerInfo;
   TimeStampTokenInfo tstInfo;
   CertID certID;

   public TimeStampToken(ContentInfo var1) throws TSPException, IOException {
      this(getSignedData(var1));
   }

   private static CMSSignedData getSignedData(ContentInfo var0) throws TSPException {
      try {
         return new CMSSignedData(var0);
      } catch (CMSException var2) {
         throw new TSPException("TSP parsing error: " + var2.getMessage(), var2.getCause());
      }
   }

   public TimeStampToken(CMSSignedData var1) throws TSPException, IOException {
      this.tsToken = var1;
      if (!this.tsToken.getSignedContentTypeOID().equals(PKCSObjectIdentifiers.id_ct_TSTInfo.getId())) {
         throw new TSPValidationException("ContentInfo object not for a time stamp.");
      } else {
         Collection var2 = this.tsToken.getSignerInfos().getSigners();
         if (var2.size() != 1) {
            throw new IllegalArgumentException("Time-stamp token signed by " + var2.size() + " signers, but it must contain just the TSA signature.");
         } else {
            this.tsaSignerInfo = (SignerInformation)var2.iterator().next();

            try {
               CMSTypedData var3 = this.tsToken.getSignedContent();
               ByteArrayOutputStream var4 = new ByteArrayOutputStream();
               var3.write(var4);
               this.tstInfo = new TimeStampTokenInfo(TSTInfo.getInstance(ASN1Primitive.fromByteArray(var4.toByteArray())));
               Attribute var5 = this.tsaSignerInfo.getSignedAttributes().get(PKCSObjectIdentifiers.id_aa_signingCertificate);
               if (var5 != null) {
                  SigningCertificate var6 = SigningCertificate.getInstance(var5.getAttrValues().getObjectAt(0));
                  this.certID = new CertID(ESSCertID.getInstance(var6.getCerts()[0]));
               } else {
                  var5 = this.tsaSignerInfo.getSignedAttributes().get(PKCSObjectIdentifiers.id_aa_signingCertificateV2);
                  if (var5 == null) {
                     throw new TSPValidationException("no signing certificate attribute found, time stamp invalid.");
                  }

                  SigningCertificateV2 var8 = SigningCertificateV2.getInstance(var5.getAttrValues().getObjectAt(0));
                  this.certID = new CertID(ESSCertIDv2.getInstance(var8.getCerts()[0]));
               }

            } catch (CMSException var7) {
               throw new TSPException(var7.getMessage(), var7.getUnderlyingException());
            }
         }
      }
   }

   public TimeStampTokenInfo getTimeStampInfo() {
      return this.tstInfo;
   }

   public AttributeTable getSignedAttributes() {
      return this.tsaSignerInfo.getSignedAttributes();
   }

   public CMSSignedData toCMSSignedData() {
      return this.tsToken;
   }

   private class CertID {
      private ESSCertID certID;
      private ESSCertIDv2 certIDv2;

      CertID(ESSCertID var2) {
         this.certID = var2;
         this.certIDv2 = null;
      }

      CertID(ESSCertIDv2 var2) {
         this.certIDv2 = var2;
         this.certID = null;
      }
   }
}
