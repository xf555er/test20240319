package org.apache.xalan.xsltc.compiler;

import java.util.Vector;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;

final class NotCall extends FunctionCall {
   public NotCall(QName fname, Vector arguments) {
      super(fname, arguments);
   }

   public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
      InstructionList il = methodGen.getInstructionList();
      this.argument().translate(classGen, methodGen);
      il.append(ICONST_1);
      il.append((org.apache.bcel.generic.Instruction)IXOR);
   }

   public void translateDesynthesized(ClassGenerator classGen, MethodGenerator methodGen) {
      InstructionList il = methodGen.getInstructionList();
      Expression exp = this.argument();
      exp.translateDesynthesized(classGen, methodGen);
      BranchHandle gotoh = il.append((BranchInstruction)(new GOTO((InstructionHandle)null)));
      this._trueList = exp._falseList;
      this._falseList = exp._trueList;
      this._falseList.add(gotoh);
   }
}
