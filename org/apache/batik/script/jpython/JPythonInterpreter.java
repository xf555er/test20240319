package org.apache.batik.script.jpython;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterException;
import org.python.core.PyException;
import org.python.util.PythonInterpreter;

public class JPythonInterpreter implements Interpreter {
   private PythonInterpreter interpreter = null;

   public JPythonInterpreter() {
      this.interpreter = new PythonInterpreter();
   }

   public String[] getMimeTypes() {
      return JPythonInterpreterFactory.JPYTHON_MIMETYPES;
   }

   public Object evaluate(Reader scriptreader) throws IOException {
      return this.evaluate(scriptreader, "");
   }

   public Object evaluate(Reader scriptreader, String description) throws IOException {
      StringBuffer sbuffer = new StringBuffer();
      char[] buffer = new char[1024];
      int val = false;

      int val;
      while((val = scriptreader.read(buffer)) != -1) {
         sbuffer.append(buffer, 0, val);
      }

      String str = sbuffer.toString();
      return this.evaluate(str);
   }

   public Object evaluate(String script) {
      try {
         this.interpreter.exec(script);
         return null;
      } catch (PyException var3) {
         throw new InterpreterException(var3, var3.getMessage(), -1, -1);
      } catch (RuntimeException var4) {
         throw new InterpreterException(var4, var4.getMessage(), -1, -1);
      }
   }

   public void dispose() {
   }

   public void bindObject(String name, Object object) {
      this.interpreter.set(name, object);
   }

   public void setOut(Writer out) {
      this.interpreter.setOut(out);
   }

   public Locale getLocale() {
      return null;
   }

   public void setLocale(Locale locale) {
   }

   public String formatMessage(String key, Object[] args) {
      return null;
   }
}
