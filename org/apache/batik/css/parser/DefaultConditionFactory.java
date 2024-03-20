package org.apache.batik.css.parser;

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionFactory;
import org.w3c.css.sac.ContentCondition;
import org.w3c.css.sac.LangCondition;
import org.w3c.css.sac.NegativeCondition;
import org.w3c.css.sac.PositionalCondition;

public class DefaultConditionFactory implements ConditionFactory {
   public static final ConditionFactory INSTANCE = new DefaultConditionFactory();

   protected DefaultConditionFactory() {
   }

   public CombinatorCondition createAndCondition(Condition first, Condition second) throws CSSException {
      return new DefaultAndCondition(first, second);
   }

   public CombinatorCondition createOrCondition(Condition first, Condition second) throws CSSException {
      throw new CSSException("Not implemented in CSS2");
   }

   public NegativeCondition createNegativeCondition(Condition condition) throws CSSException {
      throw new CSSException("Not implemented in CSS2");
   }

   public PositionalCondition createPositionalCondition(int position, boolean typeNode, boolean type) throws CSSException {
      throw new CSSException("Not implemented in CSS2");
   }

   public AttributeCondition createAttributeCondition(String localName, String namespaceURI, boolean specified, String value) throws CSSException {
      return new DefaultAttributeCondition(localName, namespaceURI, specified, value);
   }

   public AttributeCondition createIdCondition(String value) throws CSSException {
      return new DefaultIdCondition(value);
   }

   public LangCondition createLangCondition(String lang) throws CSSException {
      return new DefaultLangCondition(lang);
   }

   public AttributeCondition createOneOfAttributeCondition(String localName, String nsURI, boolean specified, String value) throws CSSException {
      return new DefaultOneOfAttributeCondition(localName, nsURI, specified, value);
   }

   public AttributeCondition createBeginHyphenAttributeCondition(String localName, String namespaceURI, boolean specified, String value) throws CSSException {
      return new DefaultBeginHyphenAttributeCondition(localName, namespaceURI, specified, value);
   }

   public AttributeCondition createClassCondition(String namespaceURI, String value) throws CSSException {
      return new DefaultClassCondition(namespaceURI, value);
   }

   public AttributeCondition createPseudoClassCondition(String namespaceURI, String value) throws CSSException {
      return new DefaultPseudoClassCondition(namespaceURI, value);
   }

   public Condition createOnlyChildCondition() throws CSSException {
      throw new CSSException("Not implemented in CSS2");
   }

   public Condition createOnlyTypeCondition() throws CSSException {
      throw new CSSException("Not implemented in CSS2");
   }

   public ContentCondition createContentCondition(String data) throws CSSException {
      throw new CSSException("Not implemented in CSS2");
   }
}
