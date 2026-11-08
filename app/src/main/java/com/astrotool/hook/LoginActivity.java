package com.astrotool.hook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.astrotool.hook.LoginActivity;
import java.util.ArrayList;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    private Button button1;
    private CheckBox rememberKey;
    private EditText edittext1;
    private SharedPreferences keyPrefs;
    private LinearLayout linear1;
    private LinearLayout linear3;
    private LinearLayout linear4;
    private LinearLayout linear5;
    private TextView textview1;
    private TextView textview4;
    private WebView webView1;

    public void setTypingEffectB(final TextView textView, final String text, final long delay, final Runnable onComplete) {
        final Handler handler = new Handler(Looper.getMainLooper());
        textView.setText("");
        Runnable runnable = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (this.index <= text.length()) {
                    textView.setText(text.substring(0, this.index));
                    this.index++;
                    handler.postDelayed(this, delay);
                } else {
                    Runnable runnable2 = onComplete;
                    if (runnable2 != null) {
                        runnable2.run();
                    }
                }
            }
        };
        handler.post(runnable);
    }

    public void setTypingEffectB(TextView textView, String text, Runnable onComplete) {
        setTypingEffectB(textView, text, 20L, onComplete);
    }

    public void setTypingEffectB(TextView textView, String text, long delay) {
        setTypingEffectB(textView, text, delay, null);
    }

    public void setTypingEffectB(TextView textView, String text) {
        setTypingEffectB(textView, text, 20L, null);
    }



    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.login);
        initialize(_savedInstanceState);
        String decode = "android.permission.READ_EXTERNAL_STORAGE";
        int checkSelfPermission = ContextCompat.checkSelfPermission(this, decode);
        String decode2 = "android.permission.WRITE_EXTERNAL_STORAGE";
        if (checkSelfPermission == -1 || ContextCompat.checkSelfPermission(this, decode2) == -1) {
            ActivityCompat.requestPermissions(this, new String[]{decode, decode2}, 1000);
        } else {
            initializeLogic();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            initializeLogic();
        }
    }

    private void initialize(Bundle _savedInstanceState) {
        this.linear1 = (LinearLayout) findViewById(R.id.linear1);
        WebView webView = (WebView) findViewById(R.id.webView1);
        this.webView1 = webView;
        webView.getSettings().setJavaScriptEnabled(true);
        this.webView1.getSettings().setSupportZoom(true);
        this.linear4 = (LinearLayout) findViewById(R.id.linear4);
        this.textview1 = (TextView) findViewById(R.id.textview1);
        this.linear5 = (LinearLayout) findViewById(R.id.linear5);
        this.edittext1 = (EditText) findViewById(R.id.edittext1);
        this.rememberKey = (CheckBox) findViewById(R.id.rememberKey);
        this.keyPrefs = getSharedPreferences("login_preferences", MODE_PRIVATE);
        this.linear3 = (LinearLayout) findViewById(R.id.linear3);
        this.button1 = (Button) findViewById(R.id.button1);
        this.textview4 = (TextView) findViewById(R.id.textview4);
        this.webView1.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView _param1, String _param2, Bitmap _param3) {
                super.onPageStarted(_param1, _param2, _param3);
            }

            @Override
            public void onPageFinished(WebView _param1, String _param2) {
                super.onPageFinished(_param1, _param2);
            }
        });
        this.button1.setOnClickListener(new AnonymousClass3());
        ModernUi.applyLogin(this, this.linear1, this.linear4, this.linear5, this.linear3, this.textview1, this.textview4, this.edittext1, this.button1);
    }



    class AnonymousClass3 implements View.OnClickListener {
        AnonymousClass3() {
        }

        @Override
        public void onClick(View _view) {
            LoginActivity.this.textview4.setVisibility(0);
            LoginActivity loginActivity = LoginActivity.this;
            loginActivity.setTypingEffectB(loginActivity.textview4, "Verifying Your Key...", new AnonymousClass1());
        }



        class AnonymousClass1 implements Runnable {
            AnonymousClass1() {
            }



            class RunnableC00001 implements Runnable {
                RunnableC00001() {
                }

                @Override
                public void run() {
                    final String key = LoginActivity.this.edittext1.getText().toString();
                    final String result = NativeCheck.nativeCheck(LoginActivity.this.getApplicationContext(), key);
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("OK".equals(result)) {
                                if (LoginActivity.this.rememberKey.isChecked()) {
                                    LoginActivity.this.keyPrefs.edit().putString("saved_key", key).apply();
                                } else {
                                    LoginActivity.this.keyPrefs.edit().remove("saved_key").apply();
                                }
                                LoginActivity.this.setTypingEffectB(LoginActivity.this.textview4, "The Key Is Verified✅", new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText((Context) LoginActivity.this, (CharSequence) "Login Successfully", 1).show();
                                            Intent intent = new Intent((Context) LoginActivity.this, (Class<?>) MainActivity.class);
                                            intent.putExtra("zxcvbnm", "LoginActivity.class");
                                            LoginActivity.this.startActivity(intent);
                                        }
                                });
                                return;
                            }
                            if ("USER OR GAME NOT REGISTERED".equals(result)) {
                                LoginActivity.this.setTypingEffectB(LoginActivity.this.textview4, "The Key Is Invalid🚫");
                                Toast.makeText((Context) LoginActivity.this, (CharSequence) "Not Registered", 1).show();
                            } else {
                                Toast.makeText((Context) LoginActivity.this, (CharSequence) result, 1).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void run() {
                new Thread(new RunnableC00001()).start();
            }
        }
    }



    private void initializeLogic() {
        String html = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'/><style>body { margin:0; padding:0; }img { width:100%; height:200px; display:block; object-fit:cover; }</style></head><body><img src='file:///android_asset/mygif.gif' /></body></html>";
        this.webView1.getSettings().setLoadWithOverviewMode(true);
        this.webView1.getSettings().setUseWideViewPort(true);
        this.webView1.setVerticalScrollBarEnabled(false);
        this.webView1.setHorizontalScrollBarEnabled(false);
        this.webView1.setBackgroundColor(0);
        this.webView1.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        setTypingEffectB(this.textview1, "ASTRO HOOK\\nWelcome back");
        this.textview4.setVisibility(8);
        final String savedKey = this.keyPrefs.getString("saved_key", "");
        if (!savedKey.isEmpty()) {
            this.rememberKey.setChecked(true);
            this.textview4.setVisibility(0);
            setTypingEffectB(this.textview4, "Trying To Login Your Previous Key...", new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String result = NativeCheck.nativeCheck(LoginActivity.this.getApplicationContext(), savedKey);
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ("OK".equals(result)) {
                                        LoginActivity.this.setTypingEffectB(LoginActivity.this.textview4, "The Key Is Verified✅", new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(LoginActivity.this, "Auto Login Successfully", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.putExtra("zxcvbnm", "LoginActivity.class");
                                                LoginActivity.this.startActivity(intent);
                                                LoginActivity.this.finish();
                                            }
                                        });
                                    } else {
                                        LoginActivity.this.keyPrefs.edit().remove("saved_key").apply();
                                        LoginActivity.this.rememberKey.setChecked(false);
                                        LoginActivity.this.setTypingEffectB(LoginActivity.this.textview4, "Saved Key Invalid🚫");
                                    }
                                }
                            });
                        }
                    }).start();
                }
            });
        }
    }



    @Deprecated
    public void showMessage(String _s) {
        Toast.makeText(getApplicationContext(), _s, 0).show();
    }

    @Deprecated
    public int getLocationX(View _v) {
        int[] _location = new int[2];
        _v.getLocationInWindow(_location);
        return _location[0];
    }

    @Deprecated
    public int getLocationY(View _v) {
        int[] _location = new int[2];
        _v.getLocationInWindow(_location);
        return _location[1];
    }

    @Deprecated
    public int getRandom(int _min, int _max) {
        Random random = new Random();
        return random.nextInt((_max - _min) + 1) + _min;
    }

    @Deprecated
    public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
        ArrayList<Double> _result = new ArrayList<>();
        SparseBooleanArray _arr = _list.getCheckedItemPositions();
        for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
            if (_arr.valueAt(_iIdx)) {
                _result.add(Double.valueOf(_arr.keyAt(_iIdx)));
            }
        }
        return _result;
    }

    @Deprecated
    public float getDip(int _input) {
        return TypedValue.applyDimension(1, _input, getResources().getDisplayMetrics());
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
