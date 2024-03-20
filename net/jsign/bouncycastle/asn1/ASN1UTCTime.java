package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Strings;

public class ASN1UTCTime extends ASN1Primitive {
   private byte[] time;

   public ASN1UTCTime(String var1) {
      this.time = Strings.toByteArray(var1);

      try {
         this.getDate();
      } catch (ParseException var3) {
         throw new IllegalArgumentException("invalid date string: " + var3.getMessage());
      }
   }

   ASN1UTCTime(byte[] var1) {
      if (var1.length < 2) {
         throw new IllegalArgumentException("UTCTime string too short");
      } else {
         this.time = var1;
         if (!this.isDigit(0) || !this.isDigit(1)) {
            throw new IllegalArgumentException("illegal characters in UTCTime string");
         }
      }
   }

   public Date getDate() throws ParseException {
      SimpleDateFormat var1 = new SimpleDateFormat("yyMMddHHmmssz");
      return DateUtil.epochAdjust(var1.parse(this.getTime()));
   }

   public Date getAdjustedDate() throws ParseException {
      SimpleDateFormat var1 = new SimpleDateFormat("yyyyMMddHHmmssz");
      var1.setTimeZone(new SimpleTimeZone(0, "Z"));
      return DateUtil.epochAdjust(var1.parse(this.getAdjustedTime()));
   }

   public String getTime() {
      String var1 = Strings.fromByteArray(this.time);
      if (var1.indexOf(45) < 0 && var1.indexOf(43) < 0) {
         return var1.length() == 11 ? var1.substring(0, 10) + "00GMT+00:00" : var1.substring(0, 12) + "GMT+00:00";
      } else {
         int var2 = var1.indexOf(45);
         if (var2 < 0) {
            var2 = var1.indexOf(43);
         }

         String var3 = var1;
         if (var2 == var1.length() - 3) {
            var3 = var1 + "00";
         }

         return var2 == 10 ? var3.substring(0, 10) + "00GMT" + var3.substring(10, 13) + ":" + var3.substring(13, 15) : var3.substring(0, 12) + "GMT" + var3.substring(12, 15) + ":" + var3.substring(15, 17);
      }
   }

   public String getAdjustedTime() {
      String var1 = this.getTime();
      return var1.charAt(0) < '5' ? "20" + var1 : "19" + var1;
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
      var1.writeEncoded(var2, 23, this.time);
   }

   boolean asn1Equals(ASN1Primitive var1) {
      return !(var1 instanceof ASN1UTCTime) ? false : Arrays.areEqual(this.time, ((ASN1UTCTime)var1).time);
   }

   public int hashCode() {
      return Arrays.hashCode(this.time);
   }

   public String toString() {
      return Strings.fromByteArray(this.time);
   }
}
