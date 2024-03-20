package org.apache.batik.dom;

import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class GenericDocumentType extends AbstractChildNode implements DocumentType {
   protected String qualifiedName;
   protected String publicId;
   protected String systemId;

   public GenericDocumentType(String qualifiedName, String publicId, String systemId) {
      this.qualifiedName = qualifiedName;
      this.publicId = publicId;
      this.systemId = systemId;
   }

   public String getNodeName() {
      return this.qualifiedName;
   }

   public short getNodeType() {
      return 10;
   }

   public boolean isReadonly() {
      return true;
   }

   public void setReadonly(boolean ro) {
   }

   public String getName() {
      return this.qualifiedName;
   }

   public NamedNodeMap getEntities() {
      return null;
   }

   public NamedNodeMap getNotations() {
      return null;
   }

   public String getPublicId() {
      return this.publicId;
   }

   public String getSystemId() {
      return this.systemId;
   }

   public String getInternalSubset() {
      return null;
   }

   protected Node newNode() {
      return new GenericDocumentType(this.qualifiedName, this.publicId, this.systemId);
   }
}
