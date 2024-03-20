package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public class BERTaggedObjectParser implements ASN1TaggedObjectParser {
   private boolean _constructed;
   private int _tagNumber;
   private ASN1StreamParser _parser;

   BERTaggedObjectParser(boolean var1, int var2, ASN1StreamParser var3) {
      this._constructed = var1;
      this._tagNumber = var2;
      this._parser = var3;
   }

   public ASN1Primitive getLoadedObject() throws IOException {
      return this._parser.readTaggedObject(this._constructed, this._tagNumber);
   }

   public ASN1Primitive toASN1Primitive() {
      try {
         return this.getLoadedObject();
      } catch (IOException var2) {
         throw new ASN1ParsingException(var2.getMessage());
      }
   }
}
