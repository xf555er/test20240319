package net.jsign.bouncycastle.asn1.x509;

import java.text.ParseException;
import java.util.Date;
import net.jsign.bouncycastle.asn1.ASN1Choice;
import net.jsign.bouncycastle.asn1.ASN1GeneralizedTime;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1UTCTime;

public class Time extends ASN1Object implements ASN1Choice {
   ASN1Primitive time;

   public Time(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1UTCTime) && !(var1 instanceof ASN1GeneralizedTime)) {
         throw new IllegalArgumentException("unknown object passed to Time");
      } else {
         this.time = var1;
      }
   }

   public static Time getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof Time)) {
         if (var0 instanceof ASN1UTCTime) {
            return new Time((ASN1UTCTime)var0);
         } else if (var0 instanceof ASN1GeneralizedTime) {
            return new Time((ASN1GeneralizedTime)var0);
         } else {
            throw new IllegalArgumentException("unknown object in factory: " + var0.getClass().getName());
         }
      } else {
         return (Time)var0;
      }
   }

   public String getTime() {
      return this.time instanceof ASN1UTCTime ? ((ASN1UTCTime)this.time).getAdjustedTime() : ((ASN1GeneralizedTime)this.time).getTime();
   }

   public Date getDate() {
      try {
         return this.time instanceof ASN1UTCTime ? ((ASN1UTCTime)this.time).getAdjustedDate() : ((ASN1GeneralizedTime)this.time).getDate();
      } catch (ParseException var2) {
         throw new IllegalStateException("invalid date string: " + var2.getMessage());
      }
   }

   public ASN1Primitive toASN1Primitive() {
      return this.time;
   }

   public String toString() {
      return this.getTime();
   }
}
