package net.jsign.script;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import net.jsign.asn1.authenticode.SpcSipInfo;
import net.jsign.asn1.authenticode.SpcUuid;
import net.jsign.bouncycastle.asn1.ASN1Object;

public class PowerShellXMLScript extends SignableScript {
   public PowerShellXMLScript() {
   }

   public PowerShellXMLScript(File file) throws IOException {
      super(file);
   }

   public PowerShellXMLScript(File file, Charset encoding) throws IOException {
      super(file, encoding);
   }

   boolean isByteOrderMarkSigned() {
      return true;
   }

   boolean isUTF8AutoDetected() {
      return false;
   }

   String getSignatureStart() {
      return "<!-- SIG # Begin signature block -->";
   }

   String getSignatureEnd() {
      return "<!-- SIG # End signature block -->";
   }

   String getLineCommentStart() {
      return "<!-- ";
   }

   String getLineCommentEnd() {
      return " -->";
   }

   ASN1Object getSpcSipInfo() {
      return new SpcSipInfo(65536, new SpcUuid("1FCC3B60-594B-084E-B724-D2C6297EF351"));
   }
}
