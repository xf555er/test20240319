package org.apache.fop.fo.properties;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.CompareUtil;

public class URIProperty extends Property {
   private URI resolvedURI;

   protected URIProperty(URI uri) {
      this.resolvedURI = uri;
   }

   private URIProperty(String uri, boolean resolve) {
      if (resolve && uri != null && !"".equals(uri)) {
         this.resolvedURI = URI.create(uri);
      } else {
         this.setSpecifiedValue(uri);
      }

   }

   public String getString() {
      return this.resolvedURI == null ? this.getSpecifiedValue() : this.resolvedURI.toString();
   }

   public String toString() {
      return this.getString();
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      result = 31 * result + CompareUtil.getHashCode(this.getSpecifiedValue());
      result = 31 * result + CompareUtil.getHashCode(this.resolvedURI);
      return result;
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (!(obj instanceof URIProperty)) {
         return false;
      } else {
         URIProperty other = (URIProperty)obj;
         return CompareUtil.equal(this.getSpecifiedValue(), other.getSpecifiedValue()) && CompareUtil.equal(this.resolvedURI, other.resolvedURI);
      }
   }

   // $FF: synthetic method
   URIProperty(String x0, boolean x1, Object x2) {
      this(x0, x1);
   }

   public static class Maker extends PropertyMaker {
      public Maker(int propId) {
         super(propId);
      }

      public Property make(PropertyList propertyList, String value, FObj fo) throws PropertyException {
         Property p = null;
         if (value.matches("(?s)^(url\\(('|\")?)?data:.*$")) {
            p = new URIProperty(value, false);
         } else {
            try {
               URI specifiedURI = new URI(URISpecification.escapeURI(value));
               URIProperty xmlBase = (URIProperty)propertyList.get(274, true, false);
               if (xmlBase == null) {
                  if (this.propId == 274) {
                     p = new URIProperty(specifiedURI);
                     p.setSpecifiedValue(value);
                  } else {
                     p = new URIProperty(value, false);
                  }
               } else {
                  p = new URIProperty(xmlBase.resolvedURI.resolve(specifiedURI));
                  p.setSpecifiedValue(value);
               }
            } catch (URISyntaxException var7) {
               throw new PropertyException("Invalid URI specified");
            }
         }

         return p;
      }
   }
}
