package org.apache.fop.pdf;

public class PDFParentTree extends PDFNumberTreeNode {
   private static final int MAX_NUMS_ARRAY_SIZE = 50;

   public PDFParentTree() {
      this.put("Kids", new PDFArray());
   }

   public void addToNums(int num, Object object) {
      int arrayIndex = num / 50;
      this.setNumOfKidsArrays(arrayIndex + 1);
      this.insertItemToNumsArray(arrayIndex, num, object);
   }

   private void setNumOfKidsArrays(int numKids) {
      for(int i = this.getKids().length(); i < numKids; ++i) {
         PDFNumberTreeNode newArray = new PDFNumberTreeNode();
         newArray.setNums(new PDFNumsArray(newArray));
         newArray.setLowerLimit(i * 50);
         newArray.setUpperLimit(i * 50);
         this.addKid(newArray);
      }

   }

   private void addKid(PDFObject kid) {
      assert this.getDocument() != null;

      this.getDocument().assignObjectNumber(kid);
      this.getDocument().addTrailerObject(kid);
      ((PDFArray)this.get("Kids")).add(kid);
   }

   private void insertItemToNumsArray(int array, int num, Object object) {
      assert this.getKids().get(array) instanceof PDFNumberTreeNode;

      PDFNumberTreeNode numsArray = (PDFNumberTreeNode)this.getKids().get(array);
      numsArray.addToNums(num, object);
   }
}
