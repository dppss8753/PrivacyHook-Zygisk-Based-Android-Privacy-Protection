package com.example.privacyhook; // ⚠️请务必确认包名

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Random;

public class HookMain implements IXposedHookLoadPackage {

    private boolean idOn = true, contactOn = true, locationOn = true;
    private double fLat = 22.3193, fLng = 114.1694;

    private static final String[] SURNAMES = {"陈", "李", "张", "王", "何", "黄"};
    private static final String[] NAMES = {"家明", "伟强", "嘉欣", "俊杰", "浩然"};
    private static final String[] EN_NAMES = {"James", "Mary", "John", "Linda", "Andy"};

    // 【核心修复】：放弃 su，直接使用 Java 文件流追加，无视 Root 权限弹窗
    private void writeInterceptLog(String pkg, String msg) {
        try {
            String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            String logLine = "[" + time + "] [" + pkg + "] " + msg + "\n";

            File logFile = new File("/data/local/tmp/intercept.log");
            // 第二个参数 true 代表追加模式
            FileOutputStream fos = new FileOutputStream(logFile, true);
            fos.write(logLine.getBytes());
            fos.close();
        } catch (Exception e) {
            // 仅在 LSPosed 管理器日志中打印错误，不骚扰用户
            de.robv.android.xposed.XposedBridge.log("PrivacyHook Log Error: " + e.getMessage());
        }
    }

    private void loadConfig() {
        try {
            File f = new File("/data/local/tmp/privacy.txt");
            if (f.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("ID_ON:")) idOn = line.endsWith("true");
                    if (line.startsWith("CONTACT_ON:")) contactOn = line.endsWith("true");
                    if (line.startsWith("LOCATION_ON:")) locationOn = line.endsWith("true");
                    if (line.startsWith("LAT:")) fLat = Double.parseDouble(line.split(":")[1]);
                    if (line.startsWith("LNG:")) fLng = Double.parseDouble(line.split(":")[1]);
                }
                br.close();
            }
        } catch (Exception e) {}
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // 不拦截自己
        if (lpparam.packageName.equals("com.example.privacyhook")) return;

        // 1. Android ID Hook
        XposedHelpers.findAndHookMethod("android.provider.Settings.Secure", lpparam.classLoader, "getString",
                "android.content.ContentResolver", String.class.getName(), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        loadConfig();
                        if (idOn && "android_id".equals(param.args[1])) {
                            param.setResult("f" + Long.toHexString(System.currentTimeMillis()).substring(0, 10));
                            writeInterceptLog(lpparam.packageName, "读取 ID -> 随机化处理");
                        }
                    }
                });

        // 2. Location Hook
        XC_MethodHook locHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                loadConfig();
                if (locationOn) {
                    if (param.method.getName().equals("getLatitude")) param.setResult(fLat);
                    else param.setResult(fLng);
                    writeInterceptLog(lpparam.packageName, "获取位置 -> 偏移至指定坐标");
                }
            }
        };
        XposedHelpers.findAndHookMethod("android.location.Location", lpparam.classLoader, "getLatitude", locHook);
        XposedHelpers.findAndHookMethod("android.location.Location", lpparam.classLoader, "getLongitude", locHook);

        // 3. Contact Hook (双语识别 + 哈希种子版)
        XC_MethodHook contactHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                loadConfig();
                if (!contactOn) return;
                Uri uri = (Uri) param.args[0];
                if (uri != null && uri.toString().contains("com.android.contacts")) {
                    Cursor res = (Cursor) param.getResult();
                    if (res == null) return;
                    writeInterceptLog(lpparam.packageName, "查询通讯录 -> 引擎注入成功");

                    param.setResult(new CursorWrapper(res) {
                        @Override
                        public String getString(int index) {
                            String val = super.getString(index);
                            if (val == null || val.isEmpty()) return val;
                            int seed = val.hashCode();
                            Random r = new Random(seed);

                            if (val.replaceAll("[^0-9]", "").matches("\\d{7,15}")) {
                                return "6" + (1000000 + r.nextInt(8999999));
                            }

                            if (val.length() >= 2 && val.length() <= 20 && !val.matches(".*\\d.*") && !val.contains("/")) {
                                if (val.matches(".*[\\u4e00-\\u9fa5].*")) {
                                    return SURNAMES[r.nextInt(SURNAMES.length)] + NAMES[r.nextInt(NAMES.length)];
                                } else {
                                    return EN_NAMES[r.nextInt(EN_NAMES.length)];
                                }
                            }
                            return val;
                        }
                    });
                }
            }
        };
        XposedHelpers.findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "query", Uri.class, String[].class, String.class, String[].class, String.class, contactHook);
    }
}