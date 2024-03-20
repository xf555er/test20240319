package net.jsign.bouncycastle.tsp;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.tsp.TSTInfo;

public class TimeStampTokenInfo {
   TSTInfo tstInfo;
   Date genTime;

   TimeStampTokenInfo(TSTInfo var1) throws TSPException, IOException {
      this.tstInfo = var1;

      try {
         this.genTime = var1.getGenTime().getDate();
      } catch (ParseException var3) {
         throw new TSPException("unable to parse genTime field");
      }
   }

   public ASN1ObjectIdentifier getPolicy() {
      return this.tstInfo.getPolicy();
   }

   public BigInteger getNonce() {
      return this.tstInfo.getNonce() != null ? this.tstInfo.getNonce().getValue() : null;
   }

   public ASN1ObjectIdentifier getMessageImprintAlgOID() {
      return this.tstInfo.getMessageImprint().getHashAlgorithm().getAlgorithm();
   }

   public byte[] getMessageImprintDigest() {
      return this.tstInfo.getMessageImprint().getHashedMessage();
   }
}
