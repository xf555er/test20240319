package org.apache.batik.dom;

import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.dom.stylesheets.StyleSheet;

public interface StyleSheetFactory {
   StyleSheet createStyleSheet(Node var1, HashMap var2);
}
