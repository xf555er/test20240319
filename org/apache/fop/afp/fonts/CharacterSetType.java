package org.apache.fop.afp.fonts;

public enum CharacterSetType {
   DOUBLE_BYTE {
      CharactersetEncoder getEncoder(String encoding) {
         return new CharactersetEncoder.DefaultEncoder(encoding, true);
      }
   },
   DOUBLE_BYTE_LINE_DATA {
      CharactersetEncoder getEncoder(String encoding) {
         return new CharactersetEncoder.EbcdicDoubleByteLineDataEncoder(encoding);
      }
   },
   SINGLE_BYTE {
      CharactersetEncoder getEncoder(String encoding) {
         return new CharactersetEncoder.DefaultEncoder(encoding, false);
      }
   };

   private CharacterSetType() {
   }

   abstract CharactersetEncoder getEncoder(String var1);

   // $FF: synthetic method
   CharacterSetType(Object x2) {
      this();
   }
}
