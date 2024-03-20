package org.apache.commons.io.input;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;

public class CharacterSetFilterReader extends AbstractCharacterFilterReader {
   private static IntPredicate toIntPredicate(Set skip) {
      if (skip == null) {
         return SKIP_NONE;
      } else {
         Set unmodifiableSet = Collections.unmodifiableSet(skip);
         return (c) -> {
            return unmodifiableSet.contains(c);
         };
      }
   }

   public CharacterSetFilterReader(Reader reader, Integer... skip) {
      this(reader, (Set)(new HashSet(Arrays.asList(skip))));
   }

   public CharacterSetFilterReader(Reader reader, Set skip) {
      super(reader, toIntPredicate(skip));
   }
}
