package com.astrotool.hook;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.json.JSONObject;

public class NativeCheck {
    public static String nativeCheck(Context context, String userKey) {
        try {
            String hwid = userKey + getAndroidID(context);
            String str = (hwid + getDeviceModel()) + getDeviceBrand();
            String UUID = getDeviceUniqueIdentifier();
            String postData = "game=CODMPREM&user_key=" + userKey + "&serial=" + UUID;
            URL url = new URL("https://my-angge.x10.mx/connect");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes("UTF-8"));
            os.flush();
            os.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            while (true) {
                String inputLine = in.readLine();
                if (inputLine == null) {
                    break;
                }
                response.append(inputLine);
            }
            in.close();
            JSONObject result = new JSONObject(response.toString());
            boolean status = result.optBoolean("status", false);
            if (!status) {
                return result.optString("reason", "Unknown error");
            }
            JSONObject data = result.getJSONObject("data");
            String token = data.getString("token");
            long rng = data.getLong("rng");
            long currentTime = System.currentTimeMillis() / 1000;
            if (rng + 30 <= currentTime) {
                return "Expired rng.";
            }
            String auth = "CODMPREM-" + userKey + "-" + UUID + "-Vm8Lk7Uj2JmsjCPVPVjrLa7zgfx3uz9E";
            String outputAuth = calcMD5(auth);
            if (token.equals(outputAuth)) {
                return "OK";
            }
            return "Invalid token.";
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }

    private static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), "android_id");
    }

    private static String getDeviceModel() {
        return Build.MODEL != null ? Build.MODEL : "";
    }

    private static String getDeviceBrand() {
        return Build.BRAND != null ? Build.BRAND : "";
    }

    private static String getDeviceUniqueIdentifier() {
        File file = new File("/storage/emulated/0/DCIM/empty.png");
        try {
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                reader.close();
                if (line != null && !line.trim().isEmpty()) {
                    return line.trim();
                }
            }
            String newUUID = UUID.randomUUID().toString();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(newUUID.getBytes());
            fos.close();
            return newUUID;
        } catch (Exception e) {
            return "";
        }
    }

    public static String calcMD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", Integer.valueOf(b & 255)));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
