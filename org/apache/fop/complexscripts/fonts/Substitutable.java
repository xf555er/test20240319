package org.apache.fop.complexscripts.fonts;

import java.util.List;

public interface Substitutable {
   boolean performsSubstitution();

   CharSequence performSubstitution(CharSequence var1, String var2, String var3, List var4, boolean var5);

   CharSequence reorderCombiningMarks(CharSequence var1, int[][] var2, String var3, String var4, List var5);
}
