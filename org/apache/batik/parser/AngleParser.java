package org.apache.batik.parser;

import java.io.IOException;

public class AngleParser extends NumberParser {
   protected AngleHandler angleHandler;

   public AngleParser() {
      this.angleHandler = DefaultAngleHandler.INSTANCE;
   }

   public void setAngleHandler(AngleHandler handler) {
      this.angleHandler = handler;
   }

   public AngleHandler getAngleHandler() {
      return this.angleHandler;
   }

   protected void doParse() throws ParseException, IOException {
      this.angleHandler.startAngle();
      this.current = this.reader.read();
      this.skipSpaces();

      try {
         float f = this.parseFloat();
         this.angleHandler.angleValue(f);
         if (this.current != -1) {
            switch (this.current) {
               case 9:
               case 10:
               case 13:
               case 32:
                  break;
               default:
                  switch (this.current) {
                     case 100:
                        this.current = this.reader.read();
                        if (this.current != 101) {
                           this.reportCharacterExpectedError('e', this.current);
                        } else {
                           this.current = this.reader.read();
                           if (this.current != 103) {
                              this.reportCharacterExpectedError('g', this.current);
                           } else {
                              this.angleHandler.deg();
                              this.current = this.reader.read();
                           }
                        }
                        break;
                     case 103:
                        this.current = this.reader.read();
                        if (this.current != 114) {
                           this.reportCharacterExpectedError('r', this.current);
                        } else {
                           this.current = this.reader.read();
                           if (this.current != 97) {
                              this.reportCharacterExpectedError('a', this.current);
                           } else {
                              this.current = this.reader.read();
                              if (this.current != 100) {
                                 this.reportCharacterExpectedError('d', this.current);
                              } else {
                                 this.angleHandler.grad();
                                 this.current = this.reader.read();
                              }
                           }
                        }
                        break;
                     case 114:
                        this.current = this.reader.read();
                        if (this.current != 97) {
                           this.reportCharacterExpectedError('a', this.current);
                        } else {
                           this.current = this.reader.read();
                           if (this.current != 100) {
                              this.reportCharacterExpectedError('d', this.current);
                           } else {
                              this.angleHandler.rad();
                              this.current = this.reader.read();
                           }
                        }
                        break;
                     default:
                        this.reportUnexpectedCharacterError(this.current);
                  }
            }
         }

         this.skipSpaces();
         if (this.current != -1) {
            this.reportError("end.of.stream.expected", new Object[]{this.current});
         }
      } catch (NumberFormatException var2) {
         this.reportUnexpectedCharacterError(this.current);
      }

      this.angleHandler.endAngle();
   }
}
