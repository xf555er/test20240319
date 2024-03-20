package net.jsign.asn1.authenticode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1InputStream;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.operator.DigestCalculator;
import net.jsign.bouncycastle.operator.DigestCalculatorProvider;
import net.jsign.bouncycastle.operator.OperatorCreationException;
import net.jsign.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

public class AuthenticodeDigestCalculatorProvider implements DigestCalculatorProvider {
   public DigestCalculator get(final AlgorithmIdentifier digestAlgorithmIdentifier) throws OperatorCreationException {
      final DigestCalculator delegate = (new JcaDigestCalculatorProviderBuilder()).build().get(digestAlgorithmIdentifier);
      return new DigestCalculator() {
         private final ByteArrayOutputStream out = new ByteArrayOutputStream();

         public AlgorithmIdentifier getAlgorithmIdentifier() {
            return digestAlgorithmIdentifier;
         }

         public OutputStream getOutputStream() {
            return this.out;
         }

         public byte[] getDigest() {
            try {
               ASN1InputStream in = new ASN1InputStream(this.out.toByteArray());
               ASN1Sequence sequence = (ASN1Sequence)in.readObject();
               Iterator var3 = sequence.iterator();

               while(var3.hasNext()) {
                  ASN1Encodable element = (ASN1Encodable)var3.next();
                  delegate.getOutputStream().write(element.toASN1Primitive().getEncoded());
               }
            } catch (IOException var5) {
               throw new RuntimeException(var5);
            }

            return delegate.getDigest();
         }
      };
   }
}
