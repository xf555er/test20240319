package org.apache.fop.area;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.xmlgraphics.util.QName;

public abstract class AreaTreeObject implements Cloneable {
   protected Map foreignAttributes;
   protected List extensionAttachments;

   public Object clone() throws CloneNotSupportedException {
      AreaTreeObject ato = (AreaTreeObject)super.clone();
      if (this.foreignAttributes != null) {
         ato.foreignAttributes = (Map)((HashMap)this.foreignAttributes).clone();
      }

      if (this.extensionAttachments != null) {
         ato.extensionAttachments = (List)((ArrayList)this.extensionAttachments).clone();
      }

      return ato;
   }

   public void setForeignAttribute(QName name, String value) {
      if (this.foreignAttributes == null) {
         this.foreignAttributes = new HashMap();
      }

      this.foreignAttributes.put(name, value);
   }

   public void setForeignAttributes(Map atts) {
      if (atts != null && atts.size() != 0) {
         Iterator var2 = atts.entrySet().iterator();

         while(var2.hasNext()) {
            Map.Entry e = (Map.Entry)var2.next();
            this.setForeignAttribute((QName)e.getKey(), (String)e.getValue());
         }

      }
   }

   public String getForeignAttributeValue(QName name) {
      return this.foreignAttributes != null ? (String)this.foreignAttributes.get(name) : null;
   }

   public Map getForeignAttributes() {
      return this.foreignAttributes != null ? Collections.unmodifiableMap(this.foreignAttributes) : Collections.emptyMap();
   }

   private void prepareExtensionAttachmentContainer() {
      if (this.extensionAttachments == null) {
         this.extensionAttachments = new ArrayList();
      }

   }

   public void addExtensionAttachment(ExtensionAttachment attachment) {
      this.prepareExtensionAttachmentContainer();
      this.extensionAttachments.add(attachment);
   }

   public void setExtensionAttachments(List extensionAttachments) {
      this.prepareExtensionAttachmentContainer();
      this.extensionAttachments.addAll(extensionAttachments);
   }

   public List getExtensionAttachments() {
      return this.extensionAttachments != null ? Collections.unmodifiableList(this.extensionAttachments) : Collections.emptyList();
   }

   public boolean hasExtensionAttachments() {
      return this.extensionAttachments != null && !this.extensionAttachments.isEmpty();
   }
}
