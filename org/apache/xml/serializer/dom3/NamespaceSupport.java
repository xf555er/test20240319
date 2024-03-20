package org.apache.xml.serializer.dom3;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class NamespaceSupport {
   static final String PREFIX_XML = "xml".intern();
   static final String PREFIX_XMLNS = "xmlns".intern();
   public static final String XML_URI = "http://www.w3.org/XML/1998/namespace".intern();
   public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/".intern();
   protected String[] fNamespace = new String[32];
   protected int fNamespaceSize;
   protected int[] fContext = new int[8];
   protected int fCurrentContext;
   protected String[] fPrefixes = new String[16];

   public void reset() {
      this.fNamespaceSize = 0;
      this.fCurrentContext = 0;
      this.fContext[this.fCurrentContext] = this.fNamespaceSize;
      this.fNamespace[this.fNamespaceSize++] = PREFIX_XML;
      this.fNamespace[this.fNamespaceSize++] = XML_URI;
      this.fNamespace[this.fNamespaceSize++] = PREFIX_XMLNS;
      this.fNamespace[this.fNamespaceSize++] = XMLNS_URI;
      ++this.fCurrentContext;
   }

   public void pushContext() {
      if (this.fCurrentContext + 1 == this.fContext.length) {
         int[] contextarray = new int[this.fContext.length * 2];
         System.arraycopy(this.fContext, 0, contextarray, 0, this.fContext.length);
         this.fContext = contextarray;
      }

      this.fContext[++this.fCurrentContext] = this.fNamespaceSize;
   }

   public void popContext() {
      this.fNamespaceSize = this.fContext[this.fCurrentContext--];
   }

   public boolean declarePrefix(String prefix, String uri) {
      if (prefix != PREFIX_XML && prefix != PREFIX_XMLNS) {
         for(int i = this.fNamespaceSize; i > this.fContext[this.fCurrentContext]; i -= 2) {
            if (this.fNamespace[i - 2].equals(prefix)) {
               this.fNamespace[i - 1] = uri;
               return true;
            }
         }

         if (this.fNamespaceSize == this.fNamespace.length) {
            String[] namespacearray = new String[this.fNamespaceSize * 2];
            System.arraycopy(this.fNamespace, 0, namespacearray, 0, this.fNamespaceSize);
            this.fNamespace = namespacearray;
         }

         this.fNamespace[this.fNamespaceSize++] = prefix;
         this.fNamespace[this.fNamespaceSize++] = uri;
         return true;
      } else {
         return false;
      }
   }

   public String getURI(String prefix) {
      for(int i = this.fNamespaceSize; i > 0; i -= 2) {
         if (this.fNamespace[i - 2].equals(prefix)) {
            return this.fNamespace[i - 1];
         }
      }

      return null;
   }

   public String getPrefix(String uri) {
      for(int i = this.fNamespaceSize; i > 0; i -= 2) {
         if (this.fNamespace[i - 1].equals(uri) && this.getURI(this.fNamespace[i - 2]).equals(uri)) {
            return this.fNamespace[i - 2];
         }
      }

      return null;
   }

   public int getDeclaredPrefixCount() {
      return (this.fNamespaceSize - this.fContext[this.fCurrentContext]) / 2;
   }

   public String getDeclaredPrefixAt(int index) {
      return this.fNamespace[this.fContext[this.fCurrentContext] + index * 2];
   }

   public Enumeration getAllPrefixes() {
      int count = 0;
      String[] prefix;
      if (this.fPrefixes.length < this.fNamespace.length / 2) {
         prefix = new String[this.fNamespaceSize];
         this.fPrefixes = prefix;
      }

      prefix = null;
      boolean unique = true;

      for(int i = 2; i < this.fNamespaceSize - 2; i += 2) {
         String prefix = this.fNamespace[i + 2];

         for(int k = 0; k < count; ++k) {
            if (this.fPrefixes[k] == prefix) {
               unique = false;
               break;
            }
         }

         if (unique) {
            this.fPrefixes[count++] = prefix;
         }

         unique = true;
      }

      return new Prefixes(this.fPrefixes, count);
   }

   protected final class Prefixes implements Enumeration {
      private String[] prefixes;
      private int counter = 0;
      private int size = 0;

      public Prefixes(String[] prefixes, int size) {
         this.prefixes = prefixes;
         this.size = size;
      }

      public boolean hasMoreElements() {
         return this.counter < this.size;
      }

      public Object nextElement() {
         if (this.counter < this.size) {
            return NamespaceSupport.this.fPrefixes[this.counter++];
         } else {
            throw new NoSuchElementException("Illegal access to Namespace prefixes enumeration.");
         }
      }

      public String toString() {
         StringBuffer buf = new StringBuffer();

         for(int i = 0; i < this.size; ++i) {
            buf.append(this.prefixes[i]);
            buf.append(" ");
         }

         return buf.toString();
      }
   }
}
