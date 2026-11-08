package com.astrotool.hook;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class DebugActivity extends Activity {
    private String[] exceptionTypes = {"StringIndexOutOfBoundsException", "IndexOutOfBoundsException", "ArithmeticException", "NumberFormatException", "ActivityNotFoundException"};
    private String[] exceptionMessages = {"Invalid string operation\n", "Invalid list operation\n", "Invalid arithmetical operation\n", "Invalid toNumber block operation\n", "Invalid intent operation"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String madeErrorMessage = "";
        if (intent != null) {
            String errorMessage = intent.getStringExtra("error");
            String[] split = errorMessage.split("\n");
            int j = 0;
            while (true) {
                try {
                    String[] strArr = this.exceptionTypes;
                    if (j >= strArr.length) {
                        break;
                    }
                    if (!split[0].contains(strArr[j])) {
                        j++;
                    } else {
                        String madeErrorMessage2 = this.exceptionMessages[j];
                        int addIndex = split[0].indexOf(this.exceptionTypes[j]) + this.exceptionTypes[j].length();
                        madeErrorMessage = (madeErrorMessage2 + split[0].substring(addIndex, split[0].length())) + "\n\nDetailed error message:\n" + errorMessage;
                        break;
                    }
                } catch (Exception e) {
                    madeErrorMessage = madeErrorMessage + "\n\nError while getting error: " + Log.getStackTraceString(e);
                }
            }
            if (madeErrorMessage.isEmpty()) {
                madeErrorMessage = errorMessage;
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("An error occurred").setMessage(madeErrorMessage).setPositiveButton("End Application", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog2, int which) {
                DebugActivity.this.finish();
            }
        }).create();
        dialog.show();
        ((TextView) dialog.findViewById(R.id.message)).setTextIsSelectable(true);
    }
}
