package com.tony.daemon;

/**
 * Created by tony on 9/19/16.
 */
public class Daemon {
//    private static final String PROCESS = "com.aigestudio.daemon.process";
//    private static boolean sPower = true;
//    private static final File FILE = new File(new File(Environment.getDataDirectory(), "data"), "com.aigestudio.daemon");
//
//    private Daemon() {
//    }
//
//    public static void main(String[] args) {
//        Looper.prepare();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (sPower) {
//                    String cmd = String.format("am startservice%s-n com.aigestudio.daemon/" +
//                                    "com.aigestudio.daemon.services.DaemonService",
//                            SysUtil.isAfter17() ? " --user 0 " : " ");
//                    LogUtils.i("CMD exec " + cmd);
//                    try {
//                        Runtime.getRuntime().exec(cmd);
//                    } catch (IOException e) {
//                        LogUtils.w("CMD exec failed:" + e.toString());
//                        Intent intent = new Intent();
//                        ComponentName component = new ComponentName("com.aigestudio.daemon", CoreService.class.getName());
//                        intent.setComponent(component);
//                        IActivityManager am = ActivityManagerNative.getDefault();
//                        Method method;
//                        try {
//                            method = am.getClass().getMethod("startService",
//                                    IApplicationThread.class, Intent.class, String.class,
//                                    int.class);
//                            Object cn = method.invoke(am, null, intent, intent.getType(), 0);
//                            LogUtils.i("start service return: " + cn);
//                        } catch (NoSuchMethodException ex) {
//                            try {
//                                method = am.getClass().getMethod("startService",
//                                        IApplicationThread.class, Intent.class, String.class);
//                                Object cn = method.invoke(am, null, intent, intent.getType());
//                                LogUtils.i("start service return: " + cn);
//                            } catch (NoSuchMethodException exc) {
//                                LogUtils.i("start service method not found: " + exc);
//                            } catch (Exception exc) {
//                                LogUtils.e("Start service failed:" + exc.toString());
//                            }
//                        } catch (Exception ex) {
//                            LogUtils.e("Start service failed:" + ex.toString());
//                        }
//                    }
//                    try {
//                        Thread.sleep(1500);
//                    } catch (InterruptedException e) {
//                        LogUtils.w("Thread sleep failed:" + e.toString());
//                    }
//                }
//            }
//        }).start();
//        Looper.loop();
//        LogUtils.i("====================Daemon exit with error====================");
//    }
//
//    public static void start(Context context) {
//        LogUtils.i("====================Daemon will be start====================");
//        File[] processes = new File("/proc").listFiles();
//        for (File file : processes) {
//            if (file.isDirectory()) {
//                File cmd = new File(file, "cmdline");
//                if (!cmd.exists())
//                    continue;
//                try {
//                    BufferedReader br = new BufferedReader(new FileReader(cmd));
//                    String line = br.readLine();
//                    if (null != line && line.startsWith(PROCESS)) {
//                        LogUtils.w("Daemon already running");
//                        return;
//                    }
//                    br.close();
//                } catch (IOException e) {
//                    LogUtils.e("Check daemon running with error:" + e.toString());
//                }
//            }
//        }
//        ProcessBuilder builder = new ProcessBuilder();
//        Map env = builder.environment();
//        String classpath = env.get("CLASSPATH");
//        if (null == classpath)
//            classpath = context.getPackageCodePath();
//        else
//            classpath = classpath + ":" + context.getPackageCodePath();
//        env.put("CLASSPATH", classpath);
//        builder.directory(new File("/"));
//        try {
//            Process process = builder.command("sh").redirectErrorStream(false).start();
//            OutputStream os = process.getOutputStream();
//            String cmd = "id\n";
//            os.write(cmd.getBytes("utf8"));
//            os.flush();
//            LogUtils.i("Exec cmd " + cmd);
//            cmd = "cd " + FILE.getAbsolutePath() + "\n";
//            os.write(cmd.getBytes("utf8"));
//            os.flush();
//            LogUtils.i("Exec cmd " + cmd);
//            cmd = "app_process / " + Daemon.class.getName() + " --nice-name=" + PROCESS + " &\n";
//            os.write(cmd.getBytes("utf8"));
//            os.flush();
//            LogUtils.i("Exec cmd " + cmd);
//            os.write("exit\n".getBytes("utf8"));
//            os.flush();
//            LogUtils.i("Exec cmd " + cmd);
//        } catch (IOException e) {
//            LogUtils.e("Exec cmd with error:" + e.toString());
//        }
//    }
}
