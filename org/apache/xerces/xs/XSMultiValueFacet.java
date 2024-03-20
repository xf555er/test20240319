package org.apache.xerces.xs;

import org.apache.xerces.xs.datatypes.ObjectList;

public interface XSMultiValueFacet extends XSObject {
   short getFacetKind();

   StringList getLexicalFacetValues();

   ObjectList getEnumerationValues();

   XSObjectList getAnnotations();
}
