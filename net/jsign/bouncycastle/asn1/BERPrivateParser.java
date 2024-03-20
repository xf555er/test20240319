package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public class BERPrivateParser implements ASN1PrivateParser {
   private final int tag;
   private final ASN1StreamParser parser;

   BERPrivateParser(int var1, ASN1StreamParser var2) {
      this.tag = var1;
      this.parser = var2;
   }

   public ASN1Primitive getLoadedObject() throws IOException {
      return new BERPrivate(this.tag, this.parser.readVector());
   }

   public ASN1Primitive toASN1Primitive() {
      try {
         return this.getLoadedObject();
      } catch (IOException var2) {
         throw new ASN1ParsingException(var2.getMessage(), var2);
      }
   }
}
