package net.jsign.script;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import net.jsign.asn1.authenticode.SpcSipInfo;
import net.jsign.asn1.authenticode.SpcUuid;
import net.jsign.bouncycastle.asn1.ASN1Object;

public class JScript extends WSHScript {
   public JScript() {
   }

   public JScript(File file) throws IOException {
      super(file);
   }

   public JScript(File file, Charset encoding) throws IOException {
      super(file, encoding);
   }

   boolean isUTF8AutoDetected() {
      return false;
   }

   String getSignatureStart() {
      return "// SIG // Begin signature block";
   }

   String getSignatureEnd() {
      return "// SIG // End signature block";
   }

   String getLineCommentStart() {
      return "// SIG // ";
   }

   String getLineCommentEnd() {
      return "";
   }

   ASN1Object getSpcSipInfo() {
      return new SpcSipInfo(1, new SpcUuid("10E0C906-CE38-D411-A2A3-00104BD35090"));
   }
}
