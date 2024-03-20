package net.jsign.bouncycastle.asn1.x500;

public interface X500NameStyle {
   boolean areEqual(X500Name var1, X500Name var2);

   int calculateHashCode(X500Name var1);

   String toString(X500Name var1);
}
