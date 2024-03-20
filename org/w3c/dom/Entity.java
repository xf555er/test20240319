package org.w3c.dom;

public interface Entity extends Node {
   String getPublicId();

   String getSystemId();

   String getNotationName();
}
