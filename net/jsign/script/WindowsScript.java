package net.jsign.script;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import net.jsign.asn1.authenticode.SpcSipInfo;
import net.jsign.asn1.authenticode.SpcUuid;
import net.jsign.bouncycastle.asn1.ASN1Object;

public class WindowsScript extends WSHScript {
   public WindowsScript() {
   }

   public WindowsScript(File file) throws IOException {
      super(file);
   }

   public WindowsScript(File file, Charset encoding) throws IOException {
      super(file, encoding);
   }

   String getSignatureStart() {
      return "<signature>";
   }

   String getSignatureEnd() {
      return "</signature>";
   }

   String getLineCommentStart() {
      return "** SIG ** ";
   }

   String getLineCommentEnd() {
      return "";
   }

   protected int getSignatureInsertionPoint(String content) {
      return content.lastIndexOf("</job>");
   }

   ASN1Object getSpcSipInfo() {
      return new SpcSipInfo(1, new SpcUuid("7005611A-CE38-D411-A2A3-00104BD35090"));
   }
}
