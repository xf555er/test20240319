package net.jsign.bouncycastle.cms;

import java.util.Map;
import net.jsign.bouncycastle.asn1.cms.AttributeTable;

public interface CMSAttributeTableGenerator {
   AttributeTable getAttributes(Map var1) throws CMSAttributeTableGenerationException;
}
