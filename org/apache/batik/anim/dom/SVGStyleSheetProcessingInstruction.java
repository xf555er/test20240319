package org.apache.batik.anim.dom;

import java.util.HashMap;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStyleSheetNode;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.StyleSheetFactory;
import org.apache.batik.dom.StyleSheetProcessingInstruction;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class SVGStyleSheetProcessingInstruction extends StyleSheetProcessingInstruction implements CSSStyleSheetNode {
   protected StyleSheet styleSheet;

   protected SVGStyleSheetProcessingInstruction() {
   }

   public SVGStyleSheetProcessingInstruction(String data, AbstractDocument owner, StyleSheetFactory f) {
      super(data, owner, f);
   }

   public String getStyleSheetURI() {
      SVGOMDocument svgDoc = (SVGOMDocument)this.getOwnerDocument();
      ParsedURL url = svgDoc.getParsedURL();
      String href = (String)this.getPseudoAttributes().get("href");
      return url != null ? (new ParsedURL(url, href)).toString() : href;
   }

   public StyleSheet getCSSStyleSheet() {
      if (this.styleSheet == null) {
         HashMap attrs = this.getPseudoAttributes();
         String type = (String)attrs.get("type");
         if ("text/css".equals(type)) {
            String title = (String)attrs.get("title");
            String media = (String)attrs.get("media");
            String href = (String)attrs.get("href");
            String alternate = (String)attrs.get("alternate");
            SVGOMDocument doc = (SVGOMDocument)this.getOwnerDocument();
            ParsedURL durl = doc.getParsedURL();
            ParsedURL burl = new ParsedURL(durl, href);
            CSSEngine e = doc.getCSSEngine();
            this.styleSheet = e.parseStyleSheet(burl, media);
            this.styleSheet.setAlternate("yes".equals(alternate));
            this.styleSheet.setTitle(title);
         }
      }

      return this.styleSheet;
   }

   public void setData(String data) throws DOMException {
      super.setData(data);
      this.styleSheet = null;
   }

   protected Node newNode() {
      return new SVGStyleSheetProcessingInstruction();
   }
}
