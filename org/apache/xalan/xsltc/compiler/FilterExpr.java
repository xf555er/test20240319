package org.apache.xalan.xsltc.compiler;

import java.util.Vector;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.NEW;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.NodeSetType;
import org.apache.xalan.xsltc.compiler.util.ReferenceType;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;

class FilterExpr extends Expression {
   private Expression _primary;
   private final Vector _predicates;

   public FilterExpr(Expression primary, Vector predicates) {
      this._primary = primary;
      this._predicates = predicates;
      primary.setParent(this);
   }

   protected Expression getExpr() {
      return this._primary instanceof CastExpr ? ((CastExpr)this._primary).getExpr() : this._primary;
   }

   public void setParser(Parser parser) {
      super.setParser(parser);
      this._primary.setParser(parser);
      if (this._predicates != null) {
         int n = this._predicates.size();

         for(int i = 0; i < n; ++i) {
            Expression exp = (Expression)this._predicates.elementAt(i);
            exp.setParser(parser);
            exp.setParent(this);
         }
      }

   }

   public String toString() {
      return "filter-expr(" + this._primary + ", " + this._predicates + ")";
   }

   public Type typeCheck(SymbolTable stable) throws TypeCheckError {
      Type ptype = this._primary.typeCheck(stable);
      boolean canOptimize = this._primary instanceof KeyCall;
      if (!(ptype instanceof NodeSetType)) {
         if (!(ptype instanceof ReferenceType)) {
            throw new TypeCheckError(this);
         }

         this._primary = new CastExpr(this._primary, Type.NodeSet);
      }

      int n = this._predicates.size();

      for(int i = 0; i < n; ++i) {
         Predicate pred = (Predicate)this._predicates.elementAt(i);
         if (!canOptimize) {
            pred.dontOptimize();
         }

         pred.typeCheck(stable);
      }

      return this._type = Type.NodeSet;
   }

   public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
      if (this._predicates.size() > 0) {
         this.translatePredicates(classGen, methodGen);
      } else {
         this._primary.translate(classGen, methodGen);
      }

   }

   public void translatePredicates(ClassGenerator classGen, MethodGenerator methodGen) {
      ConstantPoolGen cpg = classGen.getConstantPool();
      InstructionList il = methodGen.getInstructionList();
      if (this._predicates.size() == 0) {
         this.translate(classGen, methodGen);
      } else {
         Predicate predicate = (Predicate)this._predicates.lastElement();
         this._predicates.remove(predicate);
         this.translatePredicates(classGen, methodGen);
         int nthIteratorIdx;
         LocalVariableGen iteratorTemp;
         LocalVariableGen predicateValueTemp;
         if (predicate.isNthPositionFilter()) {
            nthIteratorIdx = cpg.addMethodref("org.apache.xalan.xsltc.dom.NthIterator", "<init>", "(Lorg/apache/xml/dtm/DTMAxisIterator;I)V");
            iteratorTemp = methodGen.addLocalVariable("filter_expr_tmp1", Util.getJCRefType("Lorg/apache/xml/dtm/DTMAxisIterator;"), (InstructionHandle)null, (InstructionHandle)null);
            iteratorTemp.setStart(il.append((org.apache.bcel.generic.Instruction)(new ASTORE(iteratorTemp.getIndex()))));
            predicate.translate(classGen, methodGen);
            predicateValueTemp = methodGen.addLocalVariable("filter_expr_tmp2", Util.getJCRefType("I"), (InstructionHandle)null, (InstructionHandle)null);
            predicateValueTemp.setStart(il.append((org.apache.bcel.generic.Instruction)(new ISTORE(predicateValueTemp.getIndex()))));
            il.append((org.apache.bcel.generic.Instruction)(new NEW(cpg.addClass("org.apache.xalan.xsltc.dom.NthIterator"))));
            il.append((org.apache.bcel.generic.Instruction)DUP);
            iteratorTemp.setEnd(il.append((org.apache.bcel.generic.Instruction)(new ALOAD(iteratorTemp.getIndex()))));
            predicateValueTemp.setEnd(il.append((org.apache.bcel.generic.Instruction)(new ILOAD(predicateValueTemp.getIndex()))));
            il.append((org.apache.bcel.generic.Instruction)(new INVOKESPECIAL(nthIteratorIdx)));
         } else {
            nthIteratorIdx = cpg.addMethodref("org.apache.xalan.xsltc.dom.CurrentNodeListIterator", "<init>", "(Lorg/apache/xml/dtm/DTMAxisIterator;ZLorg/apache/xalan/xsltc/dom/CurrentNodeListFilter;ILorg/apache/xalan/xsltc/runtime/AbstractTranslet;)V");
            iteratorTemp = methodGen.addLocalVariable("filter_expr_tmp1", Util.getJCRefType("Lorg/apache/xml/dtm/DTMAxisIterator;"), (InstructionHandle)null, (InstructionHandle)null);
            iteratorTemp.setStart(il.append((org.apache.bcel.generic.Instruction)(new ASTORE(iteratorTemp.getIndex()))));
            predicate.translate(classGen, methodGen);
            predicateValueTemp = methodGen.addLocalVariable("filter_expr_tmp2", Util.getJCRefType("Lorg/apache/xalan/xsltc/dom/CurrentNodeListFilter;"), (InstructionHandle)null, (InstructionHandle)null);
            predicateValueTemp.setStart(il.append((org.apache.bcel.generic.Instruction)(new ASTORE(predicateValueTemp.getIndex()))));
            il.append((org.apache.bcel.generic.Instruction)(new NEW(cpg.addClass("org.apache.xalan.xsltc.dom.CurrentNodeListIterator"))));
            il.append((org.apache.bcel.generic.Instruction)DUP);
            iteratorTemp.setEnd(il.append((org.apache.bcel.generic.Instruction)(new ALOAD(iteratorTemp.getIndex()))));
            il.append(ICONST_1);
            predicateValueTemp.setEnd(il.append((org.apache.bcel.generic.Instruction)(new ALOAD(predicateValueTemp.getIndex()))));
            il.append(methodGen.loadCurrentNode());
            il.append(classGen.loadTranslet());
            il.append((org.apache.bcel.generic.Instruction)(new INVOKESPECIAL(nthIteratorIdx)));
         }
      }

   }
}
