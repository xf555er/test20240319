package org.apache.fop.afp.modca;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.fop.afp.AFPLineDataInfo;
import org.apache.fop.afp.AFPTextDataInfo;
import org.apache.fop.afp.ptoca.LineDataInfoProducer;
import org.apache.fop.afp.ptoca.PtocaBuilder;
import org.apache.fop.afp.ptoca.PtocaProducer;
import org.apache.fop.afp.ptoca.TextDataInfoProducer;

public class PresentationTextObject extends AbstractNamedAFPObject {
   private PresentationTextData currentPresentationTextData;
   private List presentationTextDataList;
   private DefaultBuilder builder = new DefaultBuilder();

   public PresentationTextObject(String name) {
      super(name);
   }

   public void createTextData(AFPTextDataInfo textDataInfo) throws UnsupportedEncodingException {
      this.createControlSequences(new TextDataInfoProducer(textDataInfo));
   }

   public boolean createControlSequences(PtocaProducer producer) throws UnsupportedEncodingException {
      if (this.currentPresentationTextData == null) {
         this.startPresentationTextData();
      }

      try {
         if (this.getBytesAvailable() != null && this.getBytesAvailable() < this.getSize(producer)) {
            return false;
         }

         producer.produce(this.builder);
      } catch (UnsupportedEncodingException var3) {
         this.endPresentationTextData();
         throw var3;
      } catch (IOException var4) {
         this.endPresentationTextData();
         this.handleUnexpectedIOError(var4);
      }

      return true;
   }

   private int getSize(PtocaProducer producer) throws IOException {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PtocaBuilder pb = new PtocaBuilder() {
         protected OutputStream getOutputStreamForControlSequence(int length) {
            return bos;
         }
      };
      producer.produce(pb);
      return bos.size();
   }

   public boolean createLineData(AFPLineDataInfo lineDataInfo) {
      try {
         return this.createControlSequences(new LineDataInfoProducer(lineDataInfo));
      } catch (UnsupportedEncodingException var3) {
         this.handleUnexpectedIOError(var3);
         return false;
      }
   }

   private void startPresentationTextData() {
      if (this.presentationTextDataList == null) {
         this.presentationTextDataList = new ArrayList();
      }

      if (this.presentationTextDataList.size() == 0) {
         this.currentPresentationTextData = new PresentationTextData(true);
      } else {
         this.currentPresentationTextData = new PresentationTextData();
      }

      this.presentationTextDataList.add(this.currentPresentationTextData);
   }

   private void endPresentationTextData() {
      this.currentPresentationTextData = null;
   }

   protected void writeStart(OutputStream os) throws IOException {
      byte[] data = new byte[17];
      this.copySF(data, (byte)-88, (byte)-101);
      os.write(data);
   }

   protected void writeContent(OutputStream os) throws IOException {
      this.writeObjects(this.presentationTextDataList, os);
   }

   protected void writeEnd(OutputStream os) throws IOException {
      byte[] data = new byte[17];
      this.copySF(data, (byte)-87, (byte)-101);
      os.write(data);
   }

   public void endControlSequence() {
      if (this.currentPresentationTextData == null) {
         this.startPresentationTextData();
      }

      try {
         this.builder.endChainedControlSequence();
      } catch (IOException var2) {
         this.endPresentationTextData();
         this.handleUnexpectedIOError(var2);
      }

   }

   private void handleUnexpectedIOError(IOException ioe) {
      throw new RuntimeException("Unexpected I/O error: " + ioe.getMessage(), ioe);
   }

   public String toString() {
      return this.presentationTextDataList != null ? this.presentationTextDataList.toString() : super.toString();
   }

   public Integer getBytesAvailable() {
      return this.builder.getBytesAvailable();
   }

   private class DefaultBuilder extends PtocaBuilder {
      private DefaultBuilder() {
      }

      public Integer getBytesAvailable() {
         return PresentationTextObject.this.currentPresentationTextData == null ? null : PresentationTextObject.this.currentPresentationTextData.getBytesAvailable();
      }

      protected OutputStream getOutputStreamForControlSequence(int length) {
         if (length > PresentationTextObject.this.currentPresentationTextData.getBytesAvailable()) {
            PresentationTextObject.this.endPresentationTextData();
            PresentationTextObject.this.startPresentationTextData();
         }

         return PresentationTextObject.this.currentPresentationTextData.getOutputStream();
      }

      // $FF: synthetic method
      DefaultBuilder(Object x1) {
         this();
      }
   }
}
