package net.jsign.bouncycastle.tsp;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.tsp.TimeStampReq;
import net.jsign.bouncycastle.asn1.x509.Extensions;

public class TimeStampRequest {
   private static Set EMPTY_SET = Collections.unmodifiableSet(new HashSet());
   private TimeStampReq req;
   private Extensions extensions;

   public TimeStampRequest(TimeStampReq var1) {
      this.req = var1;
      this.extensions = var1.getExtensions();
   }

   public ASN1ObjectIdentifier getMessageImprintAlgOID() {
      return this.req.getMessageImprint().getHashAlgorithm().getAlgorithm();
   }

   public byte[] getMessageImprintDigest() {
      return this.req.getMessageImprint().getHashedMessage();
   }

   public ASN1ObjectIdentifier getReqPolicy() {
      return this.req.getReqPolicy() != null ? this.req.getReqPolicy() : null;
   }

   public BigInteger getNonce() {
      return this.req.getNonce() != null ? this.req.getNonce().getValue() : null;
   }

   public byte[] getEncoded() throws IOException {
      return this.req.getEncoded();
   }
}
