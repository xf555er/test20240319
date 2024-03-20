package org.apache.fop.complexscripts.fonts;

public interface Positionable {
   boolean performsPositioning();

   int[][] performPositioning(CharSequence var1, String var2, String var3, int var4);

   int[][] performPositioning(CharSequence var1, String var2, String var3);
}
