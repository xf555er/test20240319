package common;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringStack {
   protected String string;
   protected String delimeter;

   public StringStack(String var1) {
      this(var1, " ");
   }

   public StringStack(String var1, String var2) {
      this.string = var1;
      this.delimeter = var2;
   }

   public List toList() {
      LinkedList var1 = new LinkedList();
      StringStack var2 = new StringStack(this.string, this.delimeter);

      while(!var2.isEmpty()) {
         var1.add(var2.shift());
      }

      return var1;
   }

   public void push(String var1) {
      if (this.string.length() > 0) {
         this.string = this.string + this.delimeter + var1;
      } else {
         this.string = var1;
      }

   }

   public int length() {
      return this.string.length();
   }

   public boolean isEmpty() {
      return this.string.length() == 0;
   }

   public String peekFirst() {
      if (this.string.indexOf(this.delimeter) > -1) {
         String var1 = this.string.substring(0, this.string.indexOf(this.delimeter));
         return var1;
      } else {
         return this.string;
      }
   }

   public String shift() {
      String var1;
      if (this.string.indexOf(this.delimeter) > -1) {
         var1 = this.string.substring(0, this.string.indexOf(this.delimeter));
         if (var1.length() >= this.string.length()) {
            this.string = "";
            return var1;
         } else {
            this.string = this.string.substring(var1.length() + 1, this.string.length());
            return var1;
         }
      } else {
         var1 = this.string;
         this.string = "";
         return var1;
      }
   }

   public String shiftStringCanBeSingleOrDoubleQuoted() {
      Matcher var1 = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'").matcher(this.string);
      if (var1.find()) {
         Boolean var5 = false;
         String var3 = "";
         if (var1.group(1) != null) {
            var5 = true;
            var3 = var1.group(1);
         } else if (var1.group(2) != null) {
            var5 = true;
            var3 = var1.group(2);
         } else {
            var5 = false;
            var3 = var1.group();
         }

         int var4 = var3.length();
         if (var5) {
            var4 += 2;
         }

         if (var4 >= this.string.length()) {
            this.string = "";
            return var3;
         } else {
            this.string = this.string.substring(var4 + 1, this.string.length());
            return var3;
         }
      } else {
         String var2 = this.string;
         this.string = "";
         return var2;
      }
   }

   public String pop() {
      int var1 = this.string.lastIndexOf(this.delimeter);
      String var2;
      if (var1 > -1) {
         var2 = this.string.substring(var1 + 1, this.string.length());
         this.string = this.string.substring(0, var1);
         return var2;
      } else {
         var2 = this.string;
         this.string = "";
         return var2;
      }
   }

   public String toString() {
      return this.string;
   }

   public void setDelimeter(String var1) {
      this.delimeter = var1;
   }
}
