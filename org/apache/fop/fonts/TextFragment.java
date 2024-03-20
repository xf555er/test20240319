package org.apache.fop.fonts;

import java.text.CharacterIterator;

public interface TextFragment {
   CharacterIterator getIterator();

   int getBeginIndex();

   int getEndIndex();

   String getScript();

   String getLanguage();

   int getBidiLevel();

   char charAt(int var1);

   CharSequence subSequence(int var1, int var2);
}
