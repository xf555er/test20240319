package org.apache.batik.bridge;

import java.net.URL;
import org.apache.batik.script.ImportInfo;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterFactory;

public class RhinoInterpreterFactory implements InterpreterFactory {
   public static final String[] RHINO_MIMETYPES = new String[]{"application/ecmascript", "application/javascript", "text/ecmascript", "text/javascript"};

   public String[] getMimeTypes() {
      return RHINO_MIMETYPES;
   }

   public Interpreter createInterpreter(URL documentURL, boolean svg12) {
      return this.createInterpreter(documentURL, svg12, (ImportInfo)null);
   }

   public Interpreter createInterpreter(URL documentURL, boolean svg12, ImportInfo imports) {
      return (Interpreter)(svg12 ? new SVG12RhinoInterpreter(documentURL, imports) : new RhinoInterpreter(documentURL, imports));
   }
}
