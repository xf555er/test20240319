package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Strings;

public class ASN1GeneralizedTime extends ASN1Primitive {
   protected byte[] time;

   public static ASN1GeneralizedTime getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof ASN1GeneralizedTime)) {
         if (var0 instanceof byte[]) {
            try {
               return (ASN1GeneralizedTime)fromByteArray((byte[])((byte[])var0));
            } catch (Exception var2) {
               throw new IllegalArgumentException("encoding error in getInstance: " + var2.toString());
            }
         } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + var0.getClass().getName());
         }
      } else {
         return (ASN1GeneralizedTime)var0;
      }
   }

   public ASN1GeneralizedTime(String var1) {
      this.time = Strings.toByteArray(var1);

      try {
         this.getDate();
      } catch (ParseException var3) {
         throw new IllegalArgumentException("invalid date string: " + var3.getMessage());
      }
   }

   ASN1GeneralizedTime(byte[] var1) {
      if (var1.length < 4) {
         throw new IllegalArgumentException("GeneralizedTime string too short");
      } else {
         this.time = var1;
         if (!this.isDigit(0) || !this.isDigit(1) || !this.isDigit(2) || !this.isDigit(3)) {
            throw new IllegalArgumentException("illegal characters in GeneralizedTime string");
         }
      }
   }

   public String getTime() {
      String var1 = Strings.fromByteArray(this.time);
      if (var1.charAt(var1.length() - 1) == 'Z') {
         return var1.substring(0, var1.length() - 1) + "GMT+00:00";
      } else {
         int var2 = var1.length() - 6;
         char var3 = var1.charAt(var2);
         if ((var3 == '-' || var3 == '+') && var1.indexOf("GMT") == var2 - 3) {
            return var1;
         } else {
            var2 = var1.length() - 5;
            var3 = var1.charAt(var2);
            if (var3 != '-' && var3 != '+') {
               var2 = var1.length() - 3;
               var3 = var1.charAt(var2);
               return var3 != '-' && var3 != '+' ? var1 + this.calculateGMTOffset(var1) : var1.substring(0, var2) + "GMT" + var1.substring(var2) + ":00";
            } else {
               return var1.substring(0, var2) + "GMT" + var1.substring(var2, var2 + 3) + ":" + var1.substring(var2 + 3);
            }
         }
      }
   }

   private String calculateGMTOffset(String var1) {
      String var2 = "+";
      TimeZone var3 = TimeZone.getDefault();
      int var4 = var3.getRawOffset();
      if (var4 < 0) {
         var2 = "-";
         var4 = -var4;
      }

      int var5 = var4 / 3600000;
      int var6 = (var4 - var5 * 60 * 60 * 1000) / '\uea60';

      try {
         if (var3.useDaylightTime()) {
            if (this.hasFractionalSeconds()) {
               var1 = this.pruneFractionalSeconds(var1);
            }

            SimpleDateFormat var7 = this.calculateGMTDateFormat();
            if (var3.inDaylightTime(var7.parse(var1 + "GMT" + var2 + this.convert(var5) + ":" + this.convert(var6)))) {
               var5 += var2.equals("+") ? 1 : -1;
            }
         }
      } catch (ParseException var8) {
      }

      return "GMT" + var2 + this.convert(var5) + ":" + this.convert(var6);
   }

   private SimpleDateFormat calculateGMTDateFormat() {
      SimpleDateFormat var1;
      if (this.hasFractionalSeconds()) {
         var1 = new SimpleDateFormat("yyyyMMddHHmmss.SSSz");
      } else if (this.hasSeconds()) {
         var1 = new SimpleDateFormat("yyyyMMddHHmmssz");
      } else if (this.hasMinutes()) {
         var1 = new SimpleDateFormat("yyyyMMddHHmmz");
      } else {
         var1 = new SimpleDateFormat("yyyyMMddHHz");
      }

      var1.setTimeZone(new SimpleTimeZone(0, "Z"));
      return var1;
   }

   private String pruneFractionalSeconds(String var1) {
      String var2 = var1.substring(14);

      int var3;
      for(var3 = 1; var3 < var2.length(); ++var3) {
         char var4 = var2.charAt(var3);
         if ('0' > var4 || var4 > '9') {
            break;
         }
      }

      if (var3 - 1 > 3) {
         var2 = var2.substring(0, 4) + var2.substring(var3);
         var1 = var1.substring(0, 14) + var2;
      } else if (var3 - 1 == 1) {
         var2 = var2.substring(0, var3) + "00" + var2.substring(var3);
         var1 = var1.substring(0, 14) + var2;
      } else if (var3 - 1 == 2) {
         var2 = var2.substring(0, var3) + "0" + var2.substring(var3);
         var1 = var1.substring(0, 14) + var2;
      }

      return var1;
   }

   private String convert(int var1) {
      return var1 < 10 ? "0" + var1 : Integer.toString(var1);
   }

   public Date getDate() throws ParseException {
      String var2 = Strings.fromByteArray(this.time);
      String var3 = var2;
      SimpleDateFormat var1;
      if (var2.endsWith("Z")) {
         if (this.hasFractionalSeconds()) {
            var1 = new SimpleDateFormat("yyyyMMddHHmmss.SSS'Z'");
         } else if (this.hasSeconds()) {
            var1 = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
         } else if (this.hasMinutes()) {
            var1 = new SimpleDateFormat("yyyyMMddHHmm'Z'");
         } else {
            var1 = new SimpleDateFormat("yyyyMMddHH'Z'");
         }

         var1.setTimeZone(new SimpleTimeZone(0, "Z"));
      } else if (var2.indexOf(45) <= 0 && var2.indexOf(43) <= 0) {
         if (this.hasFractionalSeconds()) {
            var1 = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
         } else if (this.hasSeconds()) {
            var1 = new SimpleDateFormat("yyyyMMddHHmmss");
         } else if (this.hasMinutes()) {
            var1 = new SimpleDateFormat("yyyyMMddHHmm");
         } else {
            var1 = new SimpleDateFormat("yyyyMMddHH");
         }

         var1.setTimeZone(new SimpleTimeZone(0, TimeZone.getDefault().getID()));
      } else {
         var3 = this.getTime();
         var1 = this.calculateGMTDateFormat();
      }

      if (this.hasFractionalSeconds()) {
         var3 = this.pruneFractionalSeconds(var3);
      }

      return DateUtil.epochAdjust(var1.parse(var3));
   }

   protected boolean hasFractionalSeconds() {
      for(int var1 = 0; var1 != this.time.length; ++var1) {
         if (this.time[var1] == 46 && var1 == 14) {
            return true;
         }
      }

      return false;
   }

   protected boolean hasSeconds() {
      return this.isDigit(12) && this.isDigit(13);
   }

   protected boolean hasMinutes() {
      return this.isDigit(10) && this.isDigit(11);
   }

   private boolean isDigit(int var1) {
      return this.time.length > var1 && this.time[var1] >= 48 && this.time[var1] <= 57;
   }

   boolean isConstructed() {
      return false;
   }

   int encodedLength() {
      int var1 = this.time.length;
      return 1 + StreamUtil.calculateBodyLength(var1) + var1;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeEncoded(var2, 24, this.time);
   }

   ASN1Primitive toDERObject() {
      return new DERGeneralizedTime(this.time);
   }

   ASN1Primitive toDLObject() {
      return new DERGeneralizedTime(this.time);
   }

   boolean asn1Equals(ASN1Primitive var1) {
      return !(var1 instanceof ASN1GeneralizedTime) ? false : Arrays.areEqual(this.time, ((ASN1GeneralizedTime)var1).time);
   }

   public int hashCode() {
      return Arrays.hashCode(this.time);
   }
}
