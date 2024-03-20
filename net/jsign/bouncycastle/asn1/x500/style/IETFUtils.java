package net.jsign.bouncycastle.asn1.x500.style;

import java.io.IOException;
import java.util.Hashtable;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1String;
import net.jsign.bouncycastle.asn1.DERUniversalString;
import net.jsign.bouncycastle.asn1.x500.AttributeTypeAndValue;
import net.jsign.bouncycastle.asn1.x500.RDN;
import net.jsign.bouncycastle.util.Strings;
import net.jsign.bouncycastle.util.encoders.Hex;

public class IETFUtils {
   public static void appendRDN(StringBuffer var0, RDN var1, Hashtable var2) {
      if (var1.isMultiValued()) {
         AttributeTypeAndValue[] var3 = var1.getTypesAndValues();
         boolean var4 = true;

         for(int var5 = 0; var5 != var3.length; ++var5) {
            if (var4) {
               var4 = false;
            } else {
               var0.append('+');
            }

            appendTypeAndValue(var0, var3[var5], var2);
         }
      } else if (var1.getFirst() != null) {
         appendTypeAndValue(var0, var1.getFirst(), var2);
      }

   }

   public static void appendTypeAndValue(StringBuffer var0, AttributeTypeAndValue var1, Hashtable var2) {
      String var3 = (String)var2.get(var1.getType());
      if (var3 != null) {
         var0.append(var3);
      } else {
         var0.append(var1.getType().getId());
      }

      var0.append('=');
      var0.append(valueToString(var1.getValue()));
   }

   public static String valueToString(ASN1Encodable var0) {
      StringBuffer var1 = new StringBuffer();
      if (var0 instanceof ASN1String && !(var0 instanceof DERUniversalString)) {
         String var2 = ((ASN1String)var0).getString();
         if (var2.length() > 0 && var2.charAt(0) == '#') {
            var1.append('\\');
         }

         var1.append(var2);
      } else {
         try {
            var1.append('#');
            var1.append(Hex.toHexString(var0.toASN1Primitive().getEncoded("DER")));
         } catch (IOException var6) {
            throw new IllegalArgumentException("Other value has no encoded form");
         }
      }

      int var7 = var1.length();
      int var3 = 0;
      if (var1.length() >= 2 && var1.charAt(0) == '\\' && var1.charAt(1) == '#') {
         var3 += 2;
      }

      while(var3 != var7) {
         switch (var1.charAt(var3)) {
            case '"':
            case '+':
            case ',':
            case ';':
            case '<':
            case '=':
            case '>':
            case '\\':
               var1.insert(var3, "\\");
               var3 += 2;
               ++var7;
               break;
            default:
               ++var3;
         }
      }

      int var4 = 0;
      if (var1.length() > 0) {
         while(var1.length() > var4 && var1.charAt(var4) == ' ') {
            var1.insert(var4, "\\");
            var4 += 2;
         }
      }

      for(int var5 = var1.length() - 1; var5 >= 0 && var1.charAt(var5) == ' '; --var5) {
         var1.insert(var5, '\\');
      }

      return var1.toString();
   }

   public static String canonicalize(String var0) {
      if (var0.length() > 0 && var0.charAt(0) == '#') {
         ASN1Primitive var1 = decodeObject(var0);
         if (var1 instanceof ASN1String) {
            var0 = ((ASN1String)var1).getString();
         }
      }

      var0 = Strings.toLowerCase(var0);
      int var6 = var0.length();
      if (var6 < 2) {
         return var0;
      } else {
         int var2 = 0;

         int var3;
         for(var3 = var6 - 1; var2 < var3 && var0.charAt(var2) == '\\' && var0.charAt(var2 + 1) == ' '; var2 += 2) {
         }

         int var4 = var3;

         for(int var5 = var2 + 1; var4 > var5 && var0.charAt(var4 - 1) == '\\' && var0.charAt(var4) == ' '; var4 -= 2) {
         }

         if (var2 > 0 || var4 < var3) {
            var0 = var0.substring(var2, var4 + 1);
         }

         return stripInternalSpaces(var0);
      }
   }

   public static String canonicalString(ASN1Encodable var0) {
      return canonicalize(valueToString(var0));
   }

   private static ASN1Primitive decodeObject(String var0) {
      try {
         return ASN1Primitive.fromByteArray(Hex.decodeStrict(var0, 1, var0.length() - 1));
      } catch (IOException var2) {
         throw new IllegalStateException("unknown encoding in name: " + var2);
      }
   }

   public static String stripInternalSpaces(String var0) {
      if (var0.indexOf("  ") < 0) {
         return var0;
      } else {
         StringBuffer var1 = new StringBuffer();
         char var2 = var0.charAt(0);
         var1.append(var2);

         for(int var3 = 1; var3 < var0.length(); ++var3) {
            char var4 = var0.charAt(var3);
            if (var2 != ' ' || var4 != ' ') {
               var1.append(var4);
               var2 = var4;
            }
         }

         return var1.toString();
      }
   }

   public static boolean rDNAreEqual(RDN var0, RDN var1) {
      if (var0.size() != var1.size()) {
         return false;
      } else {
         AttributeTypeAndValue[] var2 = var0.getTypesAndValues();
         AttributeTypeAndValue[] var3 = var1.getTypesAndValues();
         if (var2.length != var3.length) {
            return false;
         } else {
            for(int var4 = 0; var4 != var2.length; ++var4) {
               if (!atvAreEqual(var2[var4], var3[var4])) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private static boolean atvAreEqual(AttributeTypeAndValue var0, AttributeTypeAndValue var1) {
      if (var0 == var1) {
         return true;
      } else if (null != var0 && null != var1) {
         ASN1ObjectIdentifier var2 = var0.getType();
         ASN1ObjectIdentifier var3 = var1.getType();
         if (!var2.equals(var3)) {
            return false;
         } else {
            String var4 = canonicalString(var0.getValue());
            String var5 = canonicalString(var1.getValue());
            return var4.equals(var5);
         }
      } else {
         return false;
      }
   }
}
