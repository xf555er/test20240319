package net.jsign.bouncycastle.pqc.crypto.lms;

import java.io.IOException;
import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;
import net.jsign.bouncycastle.util.Encodable;

public abstract class LMSKeyParameters extends AsymmetricKeyParameter implements Encodable {
   protected LMSKeyParameters(boolean var1) {
      super(var1);
   }

   public abstract byte[] getEncoded() throws IOException;
}
