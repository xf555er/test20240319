package net.jsign.asn1.authenticode;

import java.util.Map;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.cms.AttributeTable;
import net.jsign.bouncycastle.cms.CMSAttributeTableGenerationException;
import net.jsign.bouncycastle.cms.CMSAttributeTableGenerator;

public class FilteredAttributeTableGenerator implements CMSAttributeTableGenerator {
   private final CMSAttributeTableGenerator delegate;
   private final ASN1ObjectIdentifier[] removedAttributes;

   public FilteredAttributeTableGenerator(CMSAttributeTableGenerator delegate, ASN1ObjectIdentifier... removedAttributes) {
      this.delegate = delegate;
      this.removedAttributes = removedAttributes;
   }

   public AttributeTable getAttributes(Map parameters) throws CMSAttributeTableGenerationException {
      AttributeTable attributes = this.delegate.getAttributes(parameters);
      ASN1ObjectIdentifier[] var3 = this.removedAttributes;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ASN1ObjectIdentifier identifier = var3[var5];
         attributes = attributes.remove(identifier);
      }

      return attributes;
   }
}
