package org.apache.batik.anim.timing;

public class Trace {
   private static int level;
   private static boolean enabled = false;

   public static void enter(Object o, String fn, Object[] args) {
      if (enabled) {
         System.err.print("LOG\t");

         int i;
         for(i = 0; i < level; ++i) {
            System.err.print("  ");
         }

         if (fn == null) {
            System.err.print("new " + o.getClass().getName() + "(");
         } else {
            System.err.print(o + "." + fn + "(");
         }

         if (args != null) {
            System.err.print(args[0]);

            for(i = 1; i < args.length; ++i) {
               System.err.print(", " + args[i]);
            }
         }

         System.err.println(")");
      }

      ++level;
   }

   public static void exit() {
      --level;
   }

   public static void print(String s) {
      if (enabled) {
         System.err.print("LOG\t");

         for(int i = 0; i < level; ++i) {
            System.err.print("  ");
         }

         System.err.println(s);
      }

   }
}
