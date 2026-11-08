package com.astrotool.hook;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DeviceUtils {
    public static boolean checkIfDeviceIsRooted() {
        String decode = "su";
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", decode});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            reader.close();
            if (result != null) {
                return result.contains(decode);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isInVirtualApp(Context context) {
        String appDataDir = context.getFilesDir().getAbsolutePath();
        return appDataDir.split("/").length > 7;
    }
}
