package org.apache.xml.serializer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class NamespaceMappings {
   private int count = 0;
   private Hashtable m_namespaces = new Hashtable();
   private Stack m_nodeStack = new Stack();
   private static final String EMPTYSTRING = "";
   private static final String XML_PREFIX = "xml";

   public NamespaceMappings() {
      this.initNamespaces();
   }

   private void initNamespaces() {
      MappingRecord nn = new MappingRecord("", "", -1);
      Stack stack = this.createPrefixStack("");
      stack.push(nn);
      nn = new MappingRecord("xml", "http://www.w3.org/XML/1998/namespace", -1);
      stack = this.createPrefixStack("xml");
      stack.push(nn);
   }

   public String lookupNamespace(String prefix) {
      String uri = null;
      Stack stack = this.getPrefixStack(prefix);
      if (stack != null && !stack.isEmpty()) {
         uri = ((MappingRecord)stack.peek()).m_uri;
      }

      if (uri == null) {
         uri = "";
      }

      return uri;
   }

   MappingRecord getMappingFromPrefix(String prefix) {
      Stack stack = (Stack)this.m_namespaces.get(prefix);
      return stack != null && !stack.isEmpty() ? (MappingRecord)stack.peek() : null;
   }

   public String lookupPrefix(String uri) {
      String foundPrefix = null;
      Enumeration prefixes = this.m_namespaces.keys();

      while(prefixes.hasMoreElements()) {
         String prefix = (String)prefixes.nextElement();
         String uri2 = this.lookupNamespace(prefix);
         if (uri2 != null && uri2.equals(uri)) {
            foundPrefix = prefix;
            break;
         }
      }

      return foundPrefix;
   }

   MappingRecord getMappingFromURI(String uri) {
      MappingRecord foundMap = null;
      Enumeration prefixes = this.m_namespaces.keys();

      while(prefixes.hasMoreElements()) {
         String prefix = (String)prefixes.nextElement();
         MappingRecord map2 = this.getMappingFromPrefix(prefix);
         if (map2 != null && map2.m_uri.equals(uri)) {
            foundMap = map2;
            break;
         }
      }

      return foundMap;
   }

   boolean popNamespace(String prefix) {
      if (prefix.startsWith("xml")) {
         return false;
      } else {
         Stack stack;
         if ((stack = this.getPrefixStack(prefix)) != null) {
            stack.pop();
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean pushNamespace(String prefix, String uri, int elemDepth) {
      if (prefix.startsWith("xml")) {
         return false;
      } else {
         Stack stack;
         if ((stack = (Stack)this.m_namespaces.get(prefix)) == null) {
            this.m_namespaces.put(prefix, stack = new Stack());
         }

         MappingRecord mr;
         if (!stack.empty()) {
            mr = (MappingRecord)stack.peek();
            if (uri.equals(mr.m_uri) || elemDepth == mr.m_declarationDepth) {
               return false;
            }
         }

         mr = new MappingRecord(prefix, uri, elemDepth);
         stack.push(mr);
         this.m_nodeStack.push(mr);
         return true;
      }
   }

   void popNamespaces(int elemDepth, ContentHandler saxHandler) {
      while(!this.m_nodeStack.isEmpty()) {
         MappingRecord map = (MappingRecord)((MappingRecord)this.m_nodeStack.peek());
         int depth = map.m_declarationDepth;
         if (elemDepth < 1 || map.m_declarationDepth < elemDepth) {
            return;
         }

         MappingRecord nm1 = (MappingRecord)this.m_nodeStack.pop();
         String prefix = map.m_prefix;
         Stack prefixStack = this.getPrefixStack(prefix);
         MappingRecord nm2 = (MappingRecord)prefixStack.peek();
         if (nm1 == nm2) {
            prefixStack.pop();
            if (saxHandler != null) {
               try {
                  saxHandler.endPrefixMapping(prefix);
               } catch (SAXException var10) {
               }
            }
         }
      }

   }

   public String generateNextPrefix() {
      return "ns" + this.count++;
   }

   public Object clone() throws CloneNotSupportedException {
      NamespaceMappings clone = new NamespaceMappings();
      clone.m_nodeStack = (Stack)this.m_nodeStack.clone();
      clone.count = this.count;
      clone.m_namespaces = (Hashtable)this.m_namespaces.clone();
      clone.count = this.count;
      return clone;
   }

   final void reset() {
      this.count = 0;
      this.m_namespaces.clear();
      this.m_nodeStack.clear();
      this.initNamespaces();
   }

   private Stack getPrefixStack(String prefix) {
      Stack fs = (Stack)this.m_namespaces.get(prefix);
      return fs;
   }

   private Stack createPrefixStack(String prefix) {
      Stack fs = new Stack();
      this.m_namespaces.put(prefix, fs);
      return fs;
   }

   public String[] lookupAllPrefixes(String uri) {
      ArrayList foundPrefixes = new ArrayList();
      Enumeration prefixes = this.m_namespaces.keys();

      while(prefixes.hasMoreElements()) {
         String prefix = (String)prefixes.nextElement();
         String uri2 = this.lookupNamespace(prefix);
         if (uri2 != null && uri2.equals(uri)) {
            foundPrefixes.add(prefix);
         }
      }

      String[] prefixArray = new String[foundPrefixes.size()];
      foundPrefixes.toArray(prefixArray);
      return prefixArray;
   }

   private class Stack {
      private int top = -1;
      private int max = 20;
      Object[] m_stack;

      public Object clone() throws CloneNotSupportedException {
         Stack clone = NamespaceMappings.this.new Stack();
         clone.max = this.max;
         clone.top = this.top;
         clone.m_stack = new Object[clone.max];

         for(int i = 0; i <= this.top; ++i) {
            clone.m_stack[i] = this.m_stack[i];
         }

         return clone;
      }

      public Stack() {
         this.m_stack = new Object[this.max];
      }

      public Object push(Object o) {
         ++this.top;
         if (this.max <= this.top) {
            int newMax = 2 * this.max + 1;
            Object[] newArray = new Object[newMax];
            System.arraycopy(this.m_stack, 0, newArray, 0, this.max);
            this.max = newMax;
            this.m_stack = newArray;
         }

         this.m_stack[this.top] = o;
         return o;
      }

      public Object pop() {
         Object o;
         if (0 <= this.top) {
            o = this.m_stack[this.top];
            --this.top;
         } else {
            o = null;
         }

         return o;
      }

      public Object peek() {
         Object o;
         if (0 <= this.top) {
            o = this.m_stack[this.top];
         } else {
            o = null;
         }

         return o;
      }

      public Object peek(int idx) {
         return this.m_stack[idx];
      }

      public boolean isEmpty() {
         return this.top < 0;
      }

      public boolean empty() {
         return this.top < 0;
      }

      public void clear() {
         for(int i = 0; i <= this.top; ++i) {
            this.m_stack[i] = null;
         }

         this.top = -1;
      }

      public Object getElement(int index) {
         return this.m_stack[index];
      }
   }

   static class MappingRecord {
      final String m_prefix;
      final String m_uri;
      final int m_declarationDepth;

      MappingRecord(String prefix, String uri, int depth) {
         this.m_prefix = prefix;
         this.m_uri = uri == null ? "" : uri;
         this.m_declarationDepth = depth;
      }
   }
}
