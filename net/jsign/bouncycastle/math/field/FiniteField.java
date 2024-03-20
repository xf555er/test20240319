package net.jsign.bouncycastle.math.field;

import java.math.BigInteger;

public interface FiniteField {
   BigInteger getCharacteristic();

   int getDimension();
}
