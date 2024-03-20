package org.apache.fop.pdf;

public class PDFJavaScriptLaunchAction extends PDFAction {
   private String script;

   public PDFJavaScriptLaunchAction(String script) {
      this.script = script;
   }

   public String getAction() {
      return this.referencePDF();
   }

   public String toPDFString() {
      StringBuffer sb = new StringBuffer(64);
      sb.append("<<\n/S /JavaScript\n/JS ");
      sb.append(this.encodeScript(this.script));
      sb.append("\n>>");
      return sb.toString();
   }

   protected boolean contentEquals(PDFObject obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && obj instanceof PDFJavaScriptLaunchAction) {
         PDFJavaScriptLaunchAction launch = (PDFJavaScriptLaunchAction)obj;
         return launch.script.equals(this.script);
      } else {
         return false;
      }
   }
}
