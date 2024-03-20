package net.jsign.bouncycastle.asn1.x509;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.DERSequence;

public class Extensions extends ASN1Object {
   private Hashtable extensions = new Hashtable();
   private Vector ordering = new Vector();

   public static Extensions getInstance(ASN1TaggedObject var0, boolean var1) {
      return getInstance(ASN1Sequence.getInstance(var0, var1));
   }

   public static Extensions getInstance(Object var0) {
      if (var0 instanceof Extensions) {
         return (Extensions)var0;
      } else {
         return var0 != null ? new Extensions(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   private Extensions(ASN1Sequence var1) {
      Enumeration var2 = var1.getObjects();

      while(var2.hasMoreElements()) {
         Extension var3 = Extension.getInstance(var2.nextElement());
         if (this.extensions.containsKey(var3.getExtnId())) {
            throw new IllegalArgumentException("repeated extension found: " + var3.getExtnId());
         }

         this.extensions.put(var3.getExtnId(), var3);
         this.ordering.addElement(var3.getExtnId());
      }

   }

   public Extensions(Extension[] var1) {
      for(int var2 = 0; var2 != var1.length; ++var2) {
         Extension var3 = var1[var2];
         this.ordering.addElement(var3.getExtnId());
         this.extensions.put(var3.getExtnId(), var3);
      }

   }

   public Extension getExtension(ASN1ObjectIdentifier var1) {
      return (Extension)this.extensions.get(var1);
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(this.ordering.size());
      Enumeration var2 = this.ordering.elements();

      while(var2.hasMoreElements()) {
         ASN1ObjectIdentifier var3 = (ASN1ObjectIdentifier)var2.nextElement();
         Extension var4 = (Extension)this.extensions.get(var3);
         var1.add(var4);
      }

      return new DERSequence(var1);
   }
}
