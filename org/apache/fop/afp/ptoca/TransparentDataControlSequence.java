package org.apache.fop.afp.ptoca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.afp.fonts.CharactersetEncoder;

final class TransparentDataControlSequence implements Iterable {
   private static final int MAX_SBCS_TRN_SIZE = 253;
   private static final int MAX_DBCS_TRN_SIZE = 252;
   private final List trns;

   public TransparentDataControlSequence(CharactersetEncoder.EncodedChars encChars) {
      int maxTrnLength = encChars.isDBCS() ? 252 : 253;
      int numTransData = encChars.getLength() / maxTrnLength;
      int currIndex = 0;
      List trns = new ArrayList();

      int left;
      for(left = 0; left < numTransData; ++left) {
         trns.add(new TransparentData(currIndex, maxTrnLength, encChars));
         currIndex += maxTrnLength;
      }

      left = encChars.getLength() - currIndex;
      trns.add(new TransparentData(currIndex, left, encChars));
      this.trns = Collections.unmodifiableList(trns);
   }

   public Iterator iterator() {
      return this.trns.iterator();
   }

   static final class TransparentData {
      private final int offset;
      private final int length;
      private final CharactersetEncoder.EncodedChars encodedChars;

      private TransparentData(int offset, int length, CharactersetEncoder.EncodedChars encChars) {
         this.offset = offset;
         this.length = length;
         this.encodedChars = encChars;
      }

      void writeTo(OutputStream outStream) throws IOException {
         this.encodedChars.writeTo(outStream, this.offset, this.length);
      }

      // $FF: synthetic method
      TransparentData(int x0, int x1, CharactersetEncoder.EncodedChars x2, Object x3) {
         this(x0, x1, x2);
      }
   }
}
