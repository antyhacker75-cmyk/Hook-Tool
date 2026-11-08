package com.astrotool.hook;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.FileFilter;

public class MainActivity extends AppCompatActivity {
    private static final String BOT_TOKEN = "9255183987:AAGzDXJqJ5qZHvM6JjCkugweG1pcOLfz8zc";
    private static final String CHAT_ID = "-1003798292431";
    private static final String DIR1 = "/sdcard/Qwerty/1/";
    private static final String DIR2 = "/sdcard/Qwerty/2/";
    private static final String RESULT_FILE = "/sdcard/result.txt";
    private static final String TARGET_FILE = "/sdcard/targetLib";

    // ── Dump state ──────────────────────────────────────────────────
    private final AtomicInteger runId = new AtomicInteger();
    private volatile boolean busy = false;

    // ── UI ──────────────────────────────────────────────────────────
    private Button button1;
    private Button button2;
    private Button button5;
    private EditText edittext1;
    private LinearLayout linear1;
    private LinearLayout linear2;
    private TextView textview1;
    private TextView textview3;
    private TextView textview4;
    private TextView textview5;
    private TextView textview7;
    private TextView textview8;
    private TextView textview9;
    private ScrollView vscroll1;

    // ── Misc state ──────────────────────────────────────────────────
    private String bool = "";
    private String bool2 = "";
    private String b = "";
    private ArrayList<String> librariesList;
    private ArrayList<String> processList;

    public MainActivity() {
        this.processList = new ArrayList<>();
        this.librariesList = new ArrayList<>();
    }

    // ════════════════════════════════════════════════════════════════
    //  TELEGRAM SENDER — chunked, retrying, self-reporting
    // ════════════════════════════════════════════════════════════════
    private static final int TG_CHUNK = 3800;
    private static final String TG_API = "https://api.telegram.org/bot" + BOT_TOKEN;
    private static WeakReference<MainActivity> tgHost = new WeakReference<>(null);

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s == null ? "" : s, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private static String shortResp(String s) {
        if (s == null) return "";
        s = s.replace('\n', ' ');
        return s.length() > 300 ? s.substring(0, 300) : s;
    }

    private static void tgStatus(final String s) {
        Log.d("TelegramSend", s);
        final MainActivity host = tgHost.get();
        if (host == null) return;
        host.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (host.textview5 != null) host.textview5.setText("Telegram: " + s);
                if (host.textview7 != null) host.textview7.append("[tg] " + s + "\n");
            }
        });
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    private static boolean tgPost(String method, String body) {
        HttpURLConnection conn = null;
        try {
            byte[] data = body.getBytes("UTF-8");
            URL url = new URL(TG_API + "/" + method);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(data.length);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(30000);

            OutputStream os = conn.getOutputStream();
            os.write(data);
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
            String resp = "";
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                resp = sb.toString();
            }
            if (code == 200) return true;
            tgStatus(method + " HTTP " + code + " -> " + shortResp(resp));
            return false;
        } catch (Exception e) {
            tgStatus(method + " exception: " + e);
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static void sendToTelegram(final String message) {
        if (message == null || message.trim().isEmpty()) {
            tgStatus("skip: empty message");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String rest = message;
                int part = 0;
                while (rest.length() > 0) {
                    part++;
                    String chunk;
                    if (rest.length() > TG_CHUNK) {
                        int cut = rest.lastIndexOf('\n', TG_CHUNK);
                        if (cut < TG_CHUNK / 2) cut = TG_CHUNK;
                        chunk = rest.substring(0, cut);
                        rest = rest.substring(cut);
                    } else {
                        chunk = rest;
                        rest = "";
                    }
                    String text = (part == 1) ? chunk : "\u2026(" + part + ")\n" + chunk;
                    String body = "chat_id=" + enc(CHAT_ID)
                            + "&disable_web_page_preview=true&text=" + enc(text);

                    boolean ok = tgPost("sendMessage", body);
                    if (!ok) {
                        sleep(1200);
                        ok = tgPost("sendMessage", body);
                    }
                    if (!ok) {
                        tgStatus("gave up on part " + part);
                        return;
                    }
                    if (rest.length() > 0) sleep(400);
                }
                tgStatus("sent " + part + " part(s) OK");
            }
        }, "TelegramSendThread").start();
    }

    public static void sendTelegramDocument(final String filePath, final String caption) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File f = new File(filePath);
                if (!f.exists() || f.length() == 0) {
                    tgStatus("document missing: " + filePath);
                    return;
                }
                String boundary = "----Astro" + System.currentTimeMillis();
                String cap = caption == null ? ""
                        : (caption.length() > 1000 ? caption.substring(0, 1000) : caption);
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(TG_API + "/sendDocument");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setChunkedStreamingMode(0);
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data; boundary=" + boundary);
                    conn.setConnectTimeout(20000);
                    conn.setReadTimeout(60000);

                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    out.writeBytes("--" + boundary + "\r\nContent-Disposition: form-data; name=\"chat_id\"\r\n\r\n" + CHAT_ID + "\r\n");
                    out.writeBytes("--" + boundary + "\r\nContent-Disposition: form-data; name=\"caption\"\r\n\r\n" + cap + "\r\n");
                    out.writeBytes("--" + boundary + "\r\nContent-Disposition: form-data; name=\"document\"; filename=\"" + f.getName() + "\"\r\n");
                    out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");

                    FileInputStream in = new FileInputStream(f);
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                    in.close();

                    out.writeBytes("\r\n--" + boundary + "--\r\n");
                    out.flush();
                    out.close();

                    int code = conn.getResponseCode();
                    if (code == 200) {
                        tgStatus("document uploaded OK");
                        return;
                    }
                    InputStream is = conn.getErrorStream();
                    String resp = "";
                    if (is != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        br.close();
                        resp = sb.toString();
                    }
                    tgStatus("sendDocument HTTP " + code + " -> " + shortResp(resp));
                } catch (Exception e) {
                    tgStatus("sendDocument exception: " + e);
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }
        }, "TelegramDocThread").start();
    }

    /** Writes result.txt, updates the UI, then delivers via Telegram. */
    private void reportResult(final String text, final boolean bigReport) {
        try {
            FileOutputStream fos = new FileOutputStream(RESULT_FILE, false);
            fos.write(text.getBytes("UTF-8"));
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Log.e("Report", "write failed: " + e);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (textview4 != null) {
                    textview4.setText(text.length() > 120000
                            ? text.substring(0, 120000) + "\n...[display truncated]"
                            : text);
                }
                if (textview5 != null)
                    textview5.setText("Message: Long Press To Auto Copy!!!");
                if (textview9 != null)
                    textview9.setText("Comparison complete: 100%");
                AstroUtils.showMessage(getApplicationContext(), "Done");
            }
        });
        if (bigReport) {
            sendTelegramDocument(RESULT_FILE, "Diff report: " + text.length() + " chars");
        } else {
            sendToTelegram(text);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  DUMP ORCHESTRATION
    // ════════════════════════════════════════════════════════════════

    /**
     * Kicks off a background dump, then calls finishDump() exactly once.
     *
     * @param exeName   binary inside app files dir (e.g. "Mgubs1")
     * @param dir       output folder (DIR1 or DIR2)
     * @param binName   sentinel file created by the binary (e.g. "1.bin")
     * @param diffAfter true → run doDiffAndReport() on success
     */
    private void startDump(final String exeName,
                           final String dir,
                           final String binName,
                           final boolean diffAfter) {
        if (busy) {
            showToast("A dump is already running, please wait...");
            return;
        }
        busy = true;
        final int myRun = runId.incrementAndGet();
        final String target = FileUtil.readFile(TARGET_FILE);

        textview7.setText("Logs Here\u2193\n");
        textview7.append("Dumping " + target + ",...\n");
        textview5.setText("Message: Processing Please Wait...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ok = false;
                try {
                    String path = "/data/user/0/com.astrotool.hook/files/" + exeName;
                    boolean rooted = DeviceUtils.checkIfDeviceIsRooted();
                    String cmd = rooted
                            ? "su -c chmod 7777 " + path + " && su -c " + path
                            : "chmod 7777 " + path + " && " + path;
                    Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
                    p.waitFor();
                    if (FileUtil.isExistFile(dir + binName)) {
                        ok = true;
                    } else {
                        ok = dumpLibFromProcess(target, dir);
                    }
                } catch (Throwable t) {
                    Log.e("Dump", "crash in startDump", t);
                }
                finishDump(myRun, ok, target, diffAfter);
            }
        }, "DumpThread-" + myRun).start();
    }

    /** Always called exactly once per startDump(). Updates UI on the main thread. */
    private void finishDump(final int myRun,
                            final boolean ok,
                            final String target,
                            final boolean diffAfter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (myRun != runId.get()) return;   // stale — a newer run already took over
                busy = false;
                if (ok) {
                    AstroUtils.showMessage(getApplicationContext(), "\u2705");
                    textview7.append("Dumped " + target + "\u2705\n");
                    if (diffAfter) doDiffAndReport();
                } else {
                    textview7.append("Dumping " + target + " Failed\u274C\n");
                    textview5.setText("Message: Failed, Make Sure The Target Game Is Running!!!");
                }
            }
        });
    }

    // ════════════════════════════════════════════════════════════════
    //  LIBRARY DUMPER — returns true on success, never touches UI
    //  directly (uses thread-safe showToast).
    // ════════════════════════════════════════════════════════════════
    private boolean dumpLibFromProcess(String libraryName, String outputfilePath) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            showToast("No target library set.");
            return false;
        }
        try {
            File[] pidDirs = new File("/proc").listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });
            if (pidDirs == null || pidDirs.length == 0) {
                showToast("No processes found.");
                return false;
            }
            for (File pidDir : pidDirs) {
                String pid = pidDir.getName();
                if (!pid.matches("\\d+")) continue;

                File mapsFile = new File("/proc/" + pid + "/maps");
                if (!mapsFile.exists()) continue;

                boolean foundLib = false;
                long firstStartAddress = 0;
                long lastEndAddress = 0;

                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(
                            new InputStreamReader(new FileInputStream(mapsFile)));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains(libraryName) && line.contains("r-xp")) {
                            String[] parts = line.split(" ")[0].split("-");
                            long s = Long.parseLong(parts[0], 16);
                            long e = Long.parseLong(parts[1], 16);
                            if (firstStartAddress == 0) firstStartAddress = s;
                            lastEndAddress = e;
                            foundLib = true;
                        }
                    }
                } catch (Exception skip) {
                    // unreadable process (e.g. permission denied) — skip it
                    continue;
                } finally {
                    if (reader != null) {
                        try { reader.close(); } catch (IOException ignored) {}
                    }
                }
                if (!foundLib) continue;

                long total = lastEndAddress - firstStartAddress;
                FileInputStream memFile = new FileInputStream("/proc/" + pid + "/mem");
                memFile.getChannel().position(firstStartAddress);

                File outFile = new File(outputfilePath + "_.bin");
                FileOutputStream outputStream = new FileOutputStream(outFile);
                byte[] buf = new byte[4096];
                long done = 0;
                while (done < total) {
                    int want = (int) Math.min(buf.length, total - done);
                    int len = memFile.read(buf, 0, want);
                    if (len == -1) break;
                    outputStream.write(buf, 0, len);
                    done += len;
                }
                outputStream.close();
                memFile.close();

                showToast("Library found in PID: " + pid + " and dumped successfully.");
                return true;
            }
            showToast("Library " + libraryName + " not found in any process.");
            return false;

        } catch (Throwable t) {
            showToast("Error while dumping library: " + t);
            Log.e("Dump", "crash in dumpLibFromProcess", t);
            return false;
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  DIFF
    // ════════════════════════════════════════════════════════════════
    private void doDiffAndReport() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String libName = "Unknown library";
                try {
                    String t = FileUtil.readFile(TARGET_FILE);
                    if (t != null && t.trim().length() > 0)
                        libName = new File(t.trim()).getName();
                } catch (Throwable ignored) {}

                try {
                    String path1 = getLatestBinFile(DIR1);
                    String path2 = getLatestBinFile(DIR2);

                    if (path1 == null || path2 == null) {
                        reportResult("Library: " + libName + "\nERROR: dump missing ("
                                + (path1 == null ? "original .bin not found"
                                               : "patched .bin not found")
                                + "). Make sure the target app is running and the dump succeeded.",
                                false);
                        return;
                    }

                    File file1 = new File(path1);
                    File file2 = new File(path2);
                    long len1 = file1.length();
                    long len2 = file2.length();
                    long limit = Math.min(len1, len2);

                    if (limit == 0) {
                        reportResult("Library: " + libName + "\nERROR: one of the dumps is empty ("
                                + len1 + " B vs " + len2 + " B).", false);
                        return;
                    }

                    FileInputStream fis1 = new FileInputStream(file1);
                    FileInputStream fis2 = new FileInputStream(file2);
                    FileChannel ch1 = fis1.getChannel();
                    FileChannel ch2 = fis2.getChannel();

                    StringBuilder out = new StringBuilder();
                    StringBuilder hex = new StringBuilder();
                    long patchStart = -1;
                    int diffCount = 0;
                    long offset = 0;
                    long lastPct = -1;

                    ByteBuffer buf1 = ByteBuffer.allocateDirect(1 << 20);
                    ByteBuffer buf2 = ByteBuffer.allocateDirect(1 << 20);

                    while (offset < limit) {
                        buf1.clear();
                        buf2.clear();
                        int want = (int) Math.min(buf1.capacity(), limit - offset);
                        buf1.limit(want);
                        buf2.limit(want);

                        int r1 = ch1.read(buf1);
                        int r2 = ch2.read(buf2);
                        if (r1 <= 0 || r2 <= 0) break;
                        int n = Math.min(r1, r2);

                        buf1.flip();
                        buf2.flip();

                        for (int i = 0; i < n; i++) {
                            byte b1 = buf1.get();
                            byte b2 = buf2.get();
                            long abs = offset + i;
                            if (b1 != b2) {
                                if (patchStart < 0) {
                                    patchStart = abs;
                                    hex.setLength(0);
                                } else if (abs != patchStart + (hex.length() / 2)) {
                                    out.append(String.format(
                                            "Library: %s | Offset: 0x%X | Changed bytes: %s\n",
                                            libName, patchStart, hex.toString()));
                                    diffCount++;
                                    patchStart = abs;
                                    hex.setLength(0);
                                }
                                hex.append(String.format("%02X", b2 & 0xFF));
                            } else if (patchStart >= 0) {
                                out.append(String.format(
                                        "Library: %s | Offset: 0x%X | Changed bytes: %s\n",
                                        libName, patchStart, hex.toString()));
                                diffCount++;
                                patchStart = -1;
                                hex.setLength(0);
                            }
                        }

                        offset += n;
                        final long pct = (100 * offset) / limit;
                        if (pct != lastPct) {
                            final long p = pct;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (textview9 != null)
                                        textview9.setText("Progress: " + p + "%");
                                }
                            });
                            lastPct = pct;
                        }
                    }

                    if (patchStart >= 0 && hex.length() > 0) {
                        out.append(String.format(
                                "Library: %s | Offset: 0x%X | Changed bytes: %s\n",
                                libName, patchStart, hex.toString()));
                        diffCount++;
                    }

                    ch1.close();
                    ch2.close();
                    fis1.close();
                    fis2.close();

                    if (len1 != len2) {
                        out.append("NOTE: size mismatch \u2014 original ").append(len1)
                                .append(" B, patched ").append(len2)
                                .append(" B. Compared first ").append(limit).append(" B only.\n");
                    }
                    out.append("Comparison complete. Total differences: ").append(diffCount);

                    String result = out.toString();
                    reportResult(result, result.length() > 20000);

                } catch (Throwable t) {
                    Log.e("Diff", "crash", t);
                    reportResult("Library: " + libName + "\nDIFF CRASHED:\n"
                            + Log.getStackTraceString(t), false);
                }
            }
        }, "DiffThread").start();
    }

    // ════════════════════════════════════════════════════════════════
    //  ROOT / FILE HELPERS
    // ════════════════════════════════════════════════════════════════
    private boolean isDeviceRooted() {
        try {
            Process process = Runtime.getRuntime().exec("which su");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            reader.close();
            return result != null && result.contains("su");
        } catch (IOException e) {
            return false;
        }
    }

    public void requestRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            if (process != null) {
                showToast("Root access granted!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFilesToInternalStorage() {
        String decode = "AstroB";
        try {
            InputStream inputStreamss = getAssets().open("AstroA");
            FileOutputStream outputStreamss = openFileOutput("AstroA", 0);
            copyStream(inputStreamss, outputStreamss);
            InputStream inputStreams = getAssets().open(decode);
            FileOutputStream outputStreams = openFileOutput(decode, 0);
            copyStream(inputStreams, outputStreams);
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Error copying files");
        }
    }

    private void copyStream(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }

    public void copyAndRenameAssetToSDCard() {
        AssetManager assetManager = getAssets();
        try {
            InputStream in = assetManager.open("libmain.so");
            File outputDir = new File(Environment.getExternalStorageDirectory(), "Qwerty/1");
            if (!outputDir.exists()) outputDir.mkdirs();
            File outFile = new File(outputDir, "libmain.bin");
            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) out.write(buffer, 0, length);
            in.close();
            out.close();
            Log.d("MainActivity", "Asset copied to " + outFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        FileInputStream fis = new FileInputStream(sourceFile);
        FileOutputStream fos = new FileOutputStream(destFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
        fis.close();
        fos.close();
    }

    /** Thread-safe toast — safe to call from any thread. */
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void saveToFile(String text, String filePath) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(text.getBytes());
            fos.write("\n".getBytes());
            fos.flush();
            fos.close();
            showToast("Text saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Error saving text to file.");
        }
    }

    public String getLatestBinFile(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return null;
        File[] binFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String str) {
                return str.endsWith(".bin");
            }
        });
        if (binFiles == null || binFiles.length == 0) return null;
        Arrays.sort(binFiles, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return Long.compare(a.lastModified(), b.lastModified());
            }
        });
        File latest = binFiles[binFiles.length - 1];
        for (int i = 0; i < binFiles.length - 1; i++) binFiles[i].delete();
        return latest.getAbsolutePath();
    }

    // ════════════════════════════════════════════════════════════════
    //  ACTIVITY LIFECYCLE
    // ════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tgHost = new WeakReference<>(this);
        setContentView(R.layout.main);
        initialize(savedInstanceState);

        String read   = "android.permission.READ_EXTERNAL_STORAGE";
        String write  = "android.permission.WRITE_EXTERNAL_STORAGE";
        if (ContextCompat.checkSelfPermission(this, read)  == -1 ||
            ContextCompat.checkSelfPermission(this, write) == -1) {
            ActivityCompat.requestPermissions(this, new String[]{read, write}, 1000);
        } else {
            initializeLogic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) initializeLogic();
    }

    private void initialize(Bundle savedInstanceState) {
        linear1    = (LinearLayout) findViewById(R.id.linear1);
        textview1  = (TextView)     findViewById(R.id.textview1);
        vscroll1   = (ScrollView)   findViewById(R.id.vscroll1);
        textview5  = (TextView)     findViewById(R.id.textview5);
        textview9  = (TextView)     findViewById(R.id.textview9);
        edittext1  = (EditText)     findViewById(R.id.edittext1);
        textview3  = (TextView)     findViewById(R.id.textview3);
        linear2    = (LinearLayout) findViewById(R.id.linear2);
        button5    = (Button)       findViewById(R.id.button5);
        textview7  = (TextView)     findViewById(R.id.textview7);
        textview8  = (TextView)     findViewById(R.id.textview8);
        textview4  = (TextView)     findViewById(R.id.textview4);
        button1    = (Button)       findViewById(R.id.button1);
        button2    = (Button)       findViewById(R.id.button2);

        // Copy result to clipboard on click
        textview5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bool2.equals("true")) {
                    ClipboardManager cm = (ClipboardManager)
                            getSystemService("clipboard");
                    if (cm != null)
                        cm.setPrimaryClip(ClipData.newPlainText("clipboard", b));
                }
            }
        });

        // Save target lib name as user types
        edittext1.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                FileUtil.writeFile(TARGET_FILE, edittext1.getText().toString());
                textview3.setText("TARGET LIB: "
                        .concat(edittext1.getText().toString()));
            }
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        // button5: dump patched binary + diff
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bool.equals("true")) return;

                FileUtil.deleteFile(DIR1);  FileUtil.makeDir(DIR1);
                FileUtil.deleteFile(RESULT_FILE);
                FileUtil.deleteFile(DIR2);  FileUtil.makeDir(DIR2);
                copyAndRenameAssetToSDCard();

                String lib = edittext1.getText().toString().trim();
                if (lib.isEmpty()) lib = "libunity.so";
                FileUtil.writeFile(TARGET_FILE, lib);

                startDump("AstroB", DIR2, "2.bin", true);
            }
        });

        // Long-press result text → copy to clipboard
        textview4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cm = (ClipboardManager)
                        getSystemService("clipboard");
                if (cm != null)
                    cm.setPrimaryClip(ClipData.newPlainText("clipboard",
                            textview4.getText().toString()));
                return true;
            }
        });

        // Long-press progress label → Telegram self-test
        textview9.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sendToTelegram("Telegram test from AstroTool \u2014 config OK");
                return true;
            }
        });

        // button1: dump original (no diff)
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bool.equals("true")) return;
                FileUtil.deleteFile(RESULT_FILE);
                FileUtil.deleteFile(DIR1);  FileUtil.makeDir(DIR1);
                startDump("AstroA", DIR1, "1.bin", false);
            }
        });

        // button2: dump patched + diff (no asset copy, no DIR1 wipe)
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bool.equals("true")) return;
                FileUtil.deleteFile(RESULT_FILE);
                startDump("AstroB", DIR2, "2.bin", true);
            }
        });

        ModernUi.applyMain(this, linear1, linear2, vscroll1, textview1, textview3,
                textview4, textview5, textview7, textview8, textview9,
                edittext1, button5, button1, button2);
    }

    private void initializeLogic() {
        new CheckPastebinContent().execute("https://my-angge.x10.mx/uploads/updatertool.txt");
        Intent intent = getIntent();
        String value = intent.getStringExtra("zxcvbnm");
        if (!"LoginActivity.class".equals(value)) {
            bool = "";
            int i = 1 / 0;
            finish();
            finishAffinity();
            return;
        }
        bool = "true";
        copyFilesToInternalStorage();
        textview3.setText("TARGET LIB: ".concat(FileUtil.readFile(TARGET_FILE)));
        requestRootAccess();
        if (isDeviceRooted())
            AstroUtils.showMessage(getApplicationContext(), "Root Access Granted");
    }

    // ════════════════════════════════════════════════════════════════
    //  PASTEBIN / LICENSE CHECK
    // ════════════════════════════════════════════════════════════════
    private class CheckPastebinContent
            extends android.os.AsyncTask<String, Void, String> {
        @Override
        public String doInBackground(String... urls) {
            StringBuilder content = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null)
                    content.append(line).append("\n");
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return content.toString().trim();
        }

        @Override
        public void onPostExecute(String content) {
            if (content == null || content.trim().isEmpty()) {
                showToast("Cannot reach my-angge.x10.mx. "
                        + "Check your internet connection or try again later.");
                return;
            }
            String content2 = removespacesandcomments(content);
            String[] validUsers = {"active"};
            boolean isValidUser = false;
            outer:
            for (String line : content2.split("\n")) {
                for (String user : validUsers) {
                    if (line.equalsIgnoreCase(user)) {
                        isValidUser = true;
                        break outer;
                    }
                }
            }
            if (!isValidUser) {
                int i = 1 / 0;
                MainActivity.this.finish();
                MainActivity.this.finishAffinity();
            }
        }

        private String removespacesandcomments(String input) {
            StringBuilder result = new StringBuilder();
            for (String line : input.split("\n")) {
                String cleaned = line.split("#")[0].trim();
                if (!cleaned.isEmpty()) result.append(cleaned).append("\n");
            }
            return result.toString().trim();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  DEPRECATED HELPERS (kept for layout compatibility)
    // ════════════════════════════════════════════════════════════════
    @Deprecated
    public void showMessage(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Deprecated
    public int getLocationX(View v) {
        int[] l = new int[2];
        v.getLocationInWindow(l);
        return l[0];
    }

    @Deprecated
    public int getLocationY(View v) {
        int[] l = new int[2];
        v.getLocationInWindow(l);
        return l[1];
    }

    @Deprecated
    public int getRandom(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    @Deprecated
    public ArrayList<Double> getCheckedItemPositionsToArray(ListView list) {
        ArrayList<Double> r = new ArrayList<>();
        SparseBooleanArray a = list.getCheckedItemPositions();
        for (int i = 0; i < a.size(); i++)
            if (a.valueAt(i)) r.add((double) a.keyAt(i));
        return r;
    }

    @Deprecated
    public float getDip(int input) {
        return TypedValue.applyDimension(1, input, getResources().getDisplayMetrics());
    }

    @Deprecated
    public int getDisplayWidthPixels() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    @Deprecated
    public int getDisplayHeightPixels() {
        return getResources().getDisplayMetrics().heightPixels;
    }
}
