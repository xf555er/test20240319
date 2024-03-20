package org.apache.fop.render.intermediate.extensions;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.render.intermediate.IFDocumentNavigationHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.PageIndexContext;
import org.apache.fop.util.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DocumentNavigationHandler extends DefaultHandler implements DocumentNavigationExtensionConstants {
   protected static final Log log = LogFactory.getLog(DocumentNavigationHandler.class);
   private StringBuffer content = new StringBuffer();
   private Stack objectStack = new Stack();
   private IFDocumentNavigationHandler navHandler;
   private StructureTreeElement structureTreeElement;
   private Map structureTreeElements;

   public DocumentNavigationHandler(IFDocumentNavigationHandler navHandler, Map structureTreeElements) {
      this.navHandler = navHandler;

      assert structureTreeElements != null;

      this.structureTreeElements = structureTreeElements;
   }

   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      boolean handled = false;
      if ("http://xmlgraphics.apache.org/fop/intermediate/document-navigation".equals(uri)) {
         if (BOOKMARK_TREE.getLocalName().equals(localName)) {
            if (!this.objectStack.isEmpty()) {
               throw new SAXException(localName + " must be the root element!");
            }

            BookmarkTree bookmarkTree = new BookmarkTree();
            this.objectStack.push(bookmarkTree);
         } else {
            String gotoURI;
            String id;
            if (BOOKMARK.getLocalName().equals(localName)) {
               id = attributes.getValue("title");
               gotoURI = attributes.getValue("starting-state");
               boolean show = !"hide".equals(gotoURI);
               Bookmark b = new Bookmark(id, show, (AbstractAction)null);
               Object o = this.objectStack.peek();
               if (o instanceof AbstractAction) {
                  AbstractAction action = (AbstractAction)this.objectStack.pop();
                  o = this.objectStack.peek();
                  ((Bookmark)o).setAction(action);
               }

               if (o instanceof BookmarkTree) {
                  ((BookmarkTree)o).addBookmark(b);
               } else {
                  ((Bookmark)o).addChildBookmark(b);
               }

               this.objectStack.push(b);
            } else if (NAMED_DESTINATION.getLocalName().equals(localName)) {
               if (!this.objectStack.isEmpty()) {
                  throw new SAXException(localName + " must be the root element!");
               }

               id = attributes.getValue("name");
               NamedDestination dest = new NamedDestination(id, (AbstractAction)null);
               this.objectStack.push(dest);
            } else if (LINK.getLocalName().equals(localName)) {
               if (!this.objectStack.isEmpty()) {
                  throw new SAXException(localName + " must be the root element!");
               }

               Rectangle targetRect = XMLUtil.getAttributeAsRectangle(attributes, "rect");
               this.structureTreeElement = (StructureTreeElement)this.structureTreeElements.get(attributes.getValue("http://xmlgraphics.apache.org/fop/internal", "struct-ref"));
               Link link = new Link((AbstractAction)null, targetRect);
               this.objectStack.push(link);
            } else {
               String id;
               if (GOTO_XY.getLocalName().equals(localName)) {
                  id = attributes.getValue("idref");
                  GoToXYAction action;
                  if (id != null) {
                     action = new GoToXYAction(id);
                  } else {
                     id = attributes.getValue("id");
                     int pageIndex = XMLUtil.getAttributeAsInt(attributes, "page-index");
                     int pageIndexRelative = XMLUtil.getAttributeAsInt(attributes, "page-index-relative", 0);
                     Point location;
                     if (pageIndex < 0) {
                        location = null;
                     } else {
                        int currentPageIndex;
                        if (this.hasNavigation() && !this.inBookmark() && pageIndexRelative >= 0) {
                           currentPageIndex = this.navHandler.getPageIndex();
                           if (currentPageIndex >= 0) {
                              pageIndex = currentPageIndex;
                           }
                        }

                        currentPageIndex = XMLUtil.getAttributeAsInt(attributes, "x");
                        int y = XMLUtil.getAttributeAsInt(attributes, "y");
                        location = new Point(currentPageIndex, y);
                     }

                     action = new GoToXYAction(id, pageIndex, location, new PageIndexRelative(pageIndex, pageIndexRelative));
                  }

                  if (this.structureTreeElement != null) {
                     action.setStructureTreeElement(this.structureTreeElement);
                  }

                  this.objectStack.push(action);
               } else {
                  if (!GOTO_URI.getLocalName().equals(localName)) {
                     throw new SAXException("Invalid element '" + localName + "' in namespace: " + uri);
                  }

                  id = attributes.getValue("id");
                  gotoURI = attributes.getValue("uri");
                  id = attributes.getValue("show-destination");
                  boolean newWindow = "new".equals(id);
                  URIAction action = new URIAction(gotoURI, newWindow);
                  if (id != null) {
                     action.setID(id);
                  }

                  if (this.structureTreeElement != null) {
                     action.setStructureTreeElement(this.structureTreeElement);
                  }

                  this.objectStack.push(action);
               }
            }
         }

         handled = true;
      }

      if (!handled) {
         if ("http://xmlgraphics.apache.org/fop/intermediate/document-navigation".equals(uri)) {
            throw new SAXException("Unhandled element '" + localName + "' in namespace: " + uri);
         }

         log.warn("Unhandled element '" + localName + "' in namespace: " + uri);
      }

   }

   private boolean inBookmark() {
      return !this.objectStack.empty() && this.objectStack.peek() instanceof Bookmark;
   }

   public void endElement(String uri, String localName, String qName) throws SAXException {
      if ("http://xmlgraphics.apache.org/fop/intermediate/document-navigation".equals(uri)) {
         try {
            if (BOOKMARK_TREE.getLocalName().equals(localName)) {
               BookmarkTree tree = (BookmarkTree)this.objectStack.pop();
               if (this.hasNavigation()) {
                  this.navHandler.renderBookmarkTree(tree);
               }
            } else {
               AbstractAction action;
               if (BOOKMARK.getLocalName().equals(localName)) {
                  if (this.objectStack.peek() instanceof AbstractAction) {
                     action = (AbstractAction)this.objectStack.pop();
                     Bookmark b = (Bookmark)this.objectStack.pop();
                     b.setAction(action);
                  } else {
                     this.objectStack.pop();
                  }
               } else if (NAMED_DESTINATION.getLocalName().equals(localName)) {
                  action = (AbstractAction)this.objectStack.pop();
                  NamedDestination dest = (NamedDestination)this.objectStack.pop();
                  dest.setAction(action);
                  if (this.hasNavigation()) {
                     this.navHandler.renderNamedDestination(dest);
                  }
               } else if (LINK.getLocalName().equals(localName)) {
                  action = (AbstractAction)this.objectStack.pop();
                  Link link = (Link)this.objectStack.pop();
                  link.setAction(action);
                  if (this.hasNavigation()) {
                     this.navHandler.renderLink(link);
                  }
               } else if (localName.startsWith("goto-") && this.objectStack.size() == 1) {
                  action = (AbstractAction)this.objectStack.pop();
                  if (this.hasNavigation()) {
                     this.navHandler.addResolvedAction(action);
                  }
               }
            }
         } catch (IFException var6) {
            throw new SAXException(var6);
         }
      }

      this.content.setLength(0);
   }

   private boolean hasNavigation() {
      return this.navHandler != null;
   }

   public void characters(char[] ch, int start, int length) throws SAXException {
      this.content.append(ch, start, length);
   }

   public void endDocument() throws SAXException {
      assert this.objectStack.isEmpty();

   }

   static class PageIndexRelative implements PageIndexContext {
      private int pageIndex;

      PageIndexRelative(int pageIndex, int pageIndexRelative) {
         this.pageIndex = pageIndexRelative * -1 + pageIndex;
      }

      public int getPageIndex() {
         return this.pageIndex;
      }
   }
}
