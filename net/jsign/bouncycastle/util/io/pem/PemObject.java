package net.jsign.bouncycastle.util.io.pem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PemObject {
   private static final List EMPTY_LIST = Collections.unmodifiableList(new ArrayList());
   private String type;
   private List headers;
   private byte[] content;

   public PemObject(String var1, List var2, byte[] var3) {
      this.type = var1;
      this.headers = Collections.unmodifiableList(var2);
      this.content = var3;
   }

   public String getType() {
      return this.type;
   }

   public List getHeaders() {
      return this.headers;
   }

   public byte[] getContent() {
      return this.content;
   }
}
