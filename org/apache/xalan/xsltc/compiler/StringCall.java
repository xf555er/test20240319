package org.apache.xalan.xsltc.compiler;

import java.util.Vector;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;

final class StringCall extends FunctionCall {
   public StringCall(QName fname, Vector arguments) {
      super(fname, arguments);
   }

   public Type typeCheck(SymbolTable stable) throws TypeCheckError {
      int argc = this.argumentCount();
      if (argc > 1) {
         ErrorMsg err = new ErrorMsg("ILLEGAL_ARG_ERR", this);
         throw new TypeCheckError(err);
      } else {
         if (argc > 0) {
            this.argument().typeCheck(stable);
         }

         return this._type = Type.String;
      }
   }

   public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
      InstructionList il = methodGen.getInstructionList();
      Type targ;
      if (this.argumentCount() == 0) {
         il.append(methodGen.loadContextNode());
         targ = Type.Node;
      } else {
         Expression arg = this.argument();
         arg.translate(classGen, methodGen);
         arg.startIterator(classGen, methodGen);
         targ = arg.getType();
      }

      if (!targ.identicalTo(Type.String)) {
         targ.translateTo(classGen, methodGen, Type.String);
      }

   }
}
