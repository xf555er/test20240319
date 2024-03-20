package net.jsign.bouncycastle.tsp;

import java.math.BigInteger;
import net.jsign.bouncycastle.asn1.ASN1Boolean;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.tsp.MessageImprint;
import net.jsign.bouncycastle.asn1.tsp.TimeStampReq;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x509.Extensions;
import net.jsign.bouncycastle.asn1.x509.ExtensionsGenerator;
import net.jsign.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;

public class TimeStampRequestGenerator {
   private static final DefaultDigestAlgorithmIdentifierFinder dgstAlgFinder = new DefaultDigestAlgorithmIdentifierFinder();
   private ASN1ObjectIdentifier reqPolicy;
   private ASN1Boolean certReq;
   private ExtensionsGenerator extGenerator = new ExtensionsGenerator();

   public void setCertReq(boolean var1) {
      this.certReq = ASN1Boolean.getInstance(var1);
   }

   public TimeStampRequest generate(ASN1ObjectIdentifier var1, byte[] var2) {
      return this.generate(dgstAlgFinder.find(var1), var2);
   }

   public TimeStampRequest generate(AlgorithmIdentifier var1, byte[] var2) {
      return this.generate(var1, var2, (BigInteger)null);
   }

   public TimeStampRequest generate(AlgorithmIdentifier var1, byte[] var2, BigInteger var3) {
      if (var1 == null) {
         throw new IllegalArgumentException("digest algorithm not specified");
      } else {
         MessageImprint var4 = new MessageImprint(var1, var2);
         Extensions var5 = null;
         if (!this.extGenerator.isEmpty()) {
            var5 = this.extGenerator.generate();
         }

         return var3 != null ? new TimeStampRequest(new TimeStampReq(var4, this.reqPolicy, new ASN1Integer(var3), this.certReq, var5)) : new TimeStampRequest(new TimeStampReq(var4, this.reqPolicy, (ASN1Integer)null, this.certReq, var5));
      }
   }
}
