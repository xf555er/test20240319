package net.jsign.script;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import net.jsign.asn1.authenticode.SpcSipInfo;
import net.jsign.asn1.authenticode.SpcUuid;
import net.jsign.bouncycastle.asn1.ASN1Object;

public class VBScript extends WSHScript {
   public VBScript() {
   }

   public VBScript(File file) throws IOException {
      super(file);
   }

   public VBScript(File file, Charset encoding) throws IOException {
      super(file, encoding);
   }

   boolean isUTF8AutoDetected() {
      return false;
   }

   String getSignatureStart() {
      return "'' SIG '' Begin signature block";
   }

   String getSignatureEnd() {
      return "'' SIG '' End signature block";
   }

   String getLineCommentStart() {
      return "'' SIG '' ";
   }

   String getLineCommentEnd() {
      return "";
   }

   ASN1Object getSpcSipInfo() {
      return new SpcSipInfo(1, new SpcUuid("4EF02916-9927-B54D-8FE5-ACE10F17EBAB"));
   }
}
