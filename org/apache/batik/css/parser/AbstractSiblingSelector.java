package org.apache.batik.css.parser;

import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;

public abstract class AbstractSiblingSelector implements SiblingSelector {
   protected short nodeType;
   protected Selector selector;
   protected SimpleSelector simpleSelector;

   protected AbstractSiblingSelector(short type, Selector sel, SimpleSelector simple) {
      this.nodeType = type;
      this.selector = sel;
      this.simpleSelector = simple;
   }

   public short getNodeType() {
      return this.nodeType;
   }

   public Selector getSelector() {
      return this.selector;
   }

   public SimpleSelector getSiblingSelector() {
      return this.simpleSelector;
   }
}
