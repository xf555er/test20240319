package net.jsign;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.List;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.cms.CMSSignedData;
import net.jsign.mscab.MSCabinetFile;
import net.jsign.msi.MSIFile;
import net.jsign.pe.PEFile;
import net.jsign.script.JScript;
import net.jsign.script.PowerShellScript;
import net.jsign.script.PowerShellXMLScript;
import net.jsign.script.VBScript;
import net.jsign.script.WindowsScript;

public interface Signable {
   byte[] computeDigest(MessageDigest var1) throws IOException;

   ASN1Object createIndirectData(DigestAlgorithm var1) throws IOException;

   List getSignatures() throws IOException;

   void setSignature(CMSSignedData var1) throws IOException;

   void save() throws IOException;

   static Signable of(File file) throws IOException {
      return of(file, (Charset)null);
   }

   static Signable of(File file, Charset encoding) throws IOException {
      if (PEFile.isPEFile(file)) {
         return new PEFile(file);
      } else if (MSIFile.isMSIFile(file)) {
         return new MSIFile(file);
      } else if (MSCabinetFile.isMSCabinetFile(file)) {
         return new MSCabinetFile(file);
      } else if (!file.getName().endsWith(".ps1") && !file.getName().endsWith(".psd1") && !file.getName().endsWith(".psm1")) {
         if (file.getName().endsWith(".ps1xml")) {
            return new PowerShellXMLScript(file, encoding);
         } else if (!file.getName().endsWith(".vbs") && !file.getName().endsWith(".vbe")) {
            if (!file.getName().endsWith(".js") && !file.getName().endsWith(".jse")) {
               if (file.getName().endsWith(".wsf")) {
                  return new WindowsScript(file, encoding);
               } else {
                  throw new UnsupportedOperationException("Unsupported file: " + file);
               }
            } else {
               return new JScript(file, encoding);
            }
         } else {
            return new VBScript(file, encoding);
         }
      } else {
         return new PowerShellScript(file, encoding);
      }
   }
}
