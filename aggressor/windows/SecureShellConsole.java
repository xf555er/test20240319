package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconCommands;
import beacon.SecureShellTabCompletion;
import common.BeaconOutput;
import common.Callback;
import common.CommandParser;
import common.CommonUtils;
import console.Colors;
import console.GenericTabCompletion;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Stack;
import javax.swing.JTextField;
import sleep.runtime.SleepUtils;

public class SecureShellConsole extends BeaconConsole {
   public SecureShellConsole(String var1, AggressorClient var2) {
      super(var1, var2);
   }

   public String getPrompt() {
      return Colors.underline("ssh") + "> ";
   }

   public String Script(String var1) {
      return "SSH_" + var1;
   }

   public GenericTabCompletion getTabCompletion() {
      return new SecureShellTabCompletion(this.bid, this.client, this.console);
   }

   public void showPopup(String var1, MouseEvent var2) {
      Stack var3 = new Stack();
      LinkedList var4 = new LinkedList();
      var4.add(this.bid);
      var3.push(SleepUtils.getArrayWrapper(var4));
      this.engine.getMenuBuilder().installMenu(var2, "ssh", var3);
   }

   public void actionPerformed(ActionEvent var1) {
      String var2 = var1.getActionCommand().trim();
      ((JTextField)var1.getSource()).setText("");

      try {
         String var3 = this.console.processHistoryBang(var2);
         if (!var3.isEmpty()) {
            this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
            var2 = var3;
         }
      } catch (Exception var7) {
         this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
         this.console.append(var7.getMessage() + "\n");
         return;
      }

      this.console.addCommandToHistory(var2);
      CommandParser var8 = new CommandParser(var2);
      if (this.client.getSSHAliases().isAlias(var8.getCommand())) {
         this.master.input(var2);
         this.client.getSSHAliases().fireCommand(this.bid, var8.getCommand(), var8.getArguments());
      } else {
         String var4;
         if (!var8.is("help") && !var8.is("?")) {
            int var9;
            if (var8.is("history")) {
               this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
               var9 = var8.popHistoryArgument();
               if (var8.hasError()) {
                  this.console.append(this.formatLocal(BeaconOutput.Error(this.bid, var8.error())) + "\n");
               } else {
                  this.getConsole().showHistory(var9);
               }

            } else if (var8.is("downloads")) {
               this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
               this.conn.call("beacons.downloads", CommonUtils.args(this.bid), new Callback() {
                  public void result(String var1, Object var2) {
                     Stack var3 = new Stack();
                     var3.push(CommonUtils.convertAll(var2));
                     var3.push(SleepUtils.getScalar(SecureShellConsole.this.bid));
                     SecureShellConsole.this.console.append(SecureShellConsole.this.engine.format("BEACON_OUTPUT_DOWNLOADS", var3) + "\n");
                  }
               });
            } else {
               this.master.input(var2);
               if (var8.is("cancel")) {
                  if (var8.verify("Z")) {
                     this.master.Cancel(var8.popString());
                  }
               } else if (var8.is("cd")) {
                  if (var8.verify("Z")) {
                     this.master.Cd(var8.popString());
                  }
               } else if (var8.is("clear")) {
                  this.master.Clear();
               } else {
                  String var10;
                  if (var8.is("connect")) {
                     if (!var8.verify("AI") && !var8.reset()) {
                        if (var8.verify("Z")) {
                           var4 = var8.popString();
                           this.master.Connect(var4);
                        }
                     } else {
                        var9 = var8.popInt();
                        var10 = var8.popString();
                        this.master.Connect(var10, var9);
                     }
                  } else if (var8.is("download")) {
                     if (var8.verify("Z")) {
                        this.master.Download(var8.popString());
                     }
                  } else if (var8.is("exit")) {
                     this.master.Die();
                  } else if (var8.is("getuid")) {
                     this.master.GetUID();
                  } else if (var8.is("note")) {
                     if (var8.verify("Z")) {
                        var4 = var8.popString();
                        this.master.Note(var4);
                     } else if (var8.isMissingArguments()) {
                        this.master.Note("");
                     }
                  } else if (var8.is("pwd")) {
                     this.master.Pwd();
                  } else {
                     int var11;
                     if (var8.is("rportfwd")) {
                        if (!var8.verify("IAI") && !var8.reset()) {
                           if (var8.verify("AI")) {
                              var9 = var8.popInt();
                              var10 = var8.popString();
                              if (!"stop".equals(var10)) {
                                 var8.error("only acceptable argument is stop");
                              } else {
                                 this.master.PortForwardStop(var9);
                              }
                           }
                        } else {
                           var9 = var8.popInt();
                           var10 = var8.popString();
                           var11 = var8.popInt();
                           this.master.PortForward(var11, var10, var9);
                        }
                     } else if (var8.is("rportfwd_local")) {
                        if (!var8.verify("IAI") && !var8.reset()) {
                           if (var8.verify("AI")) {
                              var9 = var8.popInt();
                              var10 = var8.popString();
                              if (!"stop".equals(var10)) {
                                 var8.error("only acceptable argument is stop");
                              } else {
                                 this.master.PortForwardStop(var9);
                              }
                           }
                        } else {
                           var9 = var8.popInt();
                           var10 = var8.popString();
                           var11 = var8.popInt();
                           this.master.PortForwardLocal(var11, var10, var9);
                        }
                     } else if (var8.is("shell")) {
                        if (var8.verify("Z")) {
                           this.master.Shell(var8.popString());
                        }
                     } else if (var8.is("sleep")) {
                        if (!var8.verify("I%") && !var8.reset()) {
                           if (var8.verify("I")) {
                              this.master.Sleep(var8.popInt(), 0);
                           }
                        } else {
                           var9 = var8.popInt();
                           int var12 = var8.popInt();
                           this.master.Sleep(var12, var9);
                        }
                     } else if (var8.is("socks")) {
                        if (!var8.verify("I") && !var8.reset()) {
                           if (var8.verify("Z")) {
                              if (!var8.popString().equals("stop")) {
                                 var8.error("only acceptable argument is stop or port");
                              } else {
                                 this.master.SocksStop();
                              }
                           }
                        } else {
                           this.master.SocksStart(var8.popInt());
                        }
                     } else if (var8.is("sudo")) {
                        if (var8.verify("AZ")) {
                           var4 = var8.popString();
                           var10 = var8.popString();
                           this.master.ShellSudo(var10, var4);
                        }
                     } else if (var8.is("unlink")) {
                        if (!var8.verify("AI") && !var8.reset()) {
                           if (var8.verify("Z")) {
                              this.master.Unlink(var8.popString());
                           }
                        } else {
                           var4 = var8.popString();
                           var10 = var8.popString();
                           this.master.Unlink(var10, var4);
                        }
                     } else if (var8.is("upload") && var8.empty()) {
                        SafeDialogs.openFile("Select file to upload", (String)null, (String)null, false, false, new SafeDialogCallback() {
                           public void dialogResult(String var1) {
                              if (CommonUtils.lof(var1) > 786432L) {
                                 SecureShellConsole.this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(SecureShellConsole.this.bid, "File " + var1 + " is larger than 768KB")));
                              } else {
                                 SecureShellConsole.this.master.Upload(var1);
                              }

                           }
                        });
                     } else if (var8.is("upload")) {
                        if (var8.verify("F")) {
                           var4 = var8.popString();
                           if (CommonUtils.lof(var4) > 786432L) {
                              this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, "File " + var4 + " is larger than 768KB")));
                           } else {
                              this.master.Upload(var4);
                           }
                        }
                     } else {
                        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, "Unknown command: " + var2)));
                     }
                  }
               }

               if (var8.hasError()) {
                  this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, var8.error())));
               }

            }
         } else {
            this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
            if (!var8.verify("Z") && !var8.reset()) {
               this.console.append(this.engine.format("SSH_OUTPUT_HELP", new Stack()) + "\n");
            } else {
               var4 = var8.popString();
               BeaconCommands var5 = DataUtils.getSSHCommands(this.data);
               if (var5.isHelpAvailable(var4)) {
                  Stack var6 = new Stack();
                  var6.push(SleepUtils.getScalar(var4));
                  this.console.append(this.engine.format("SSH_OUTPUT_HELP_COMMAND", var6) + "\n");
               } else {
                  var8.error("no help is available for '" + var4 + "'");
               }
            }

            if (var8.hasError()) {
               this.console.append(this.formatLocal(BeaconOutput.Error(this.bid, var8.error())) + "\n");
            }

         }
      }
   }
}
