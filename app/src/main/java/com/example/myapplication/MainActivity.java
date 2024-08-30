package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@SuppressLint({"MissingInflatedId", "LocalSuppress"})
public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private String cloudreveip;
    private Runnable hideButtonRunnable;
    private ValueCallback<Uri> valueCallback;
    private ValueCallback<Uri[]> valueCallbackArray;
    private boolean temp=true;
    private Button button;
    private final int REQUEST_CODE = 0x1010;
    private Handler handler = new Handler();
    private final static int RESULT_CODE = 0x1011;
    private Stack<String> history = new Stack<>();
    private View backButton;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 绑定按钮
         */
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button2);
        backButton = findViewById(R.id.backbutton);
        hideButtonDelayed(3000);  // 3秒后隐藏按钮
        button.setVisibility(View.VISIBLE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                // 加载新的布局文件
                LayoutInflater inflater = getLayoutInflater();
                View newView = inflater.inflate(R.layout.content_main, null);
                // 替换当前的布局
                ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
                rootView.removeAllViews();
                rootView.addView(newView);

                setContentView(R.layout.content_main);
                Button button4 = findViewById(R.id.button5);
                button4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View v) {
                        EditText serverip = findViewById(R.id.edit_text);
                        writeToFile(serverip.getText().toString());
                        Toast.makeText(getApplicationContext(),"change CloudReveIP 重启即可生效!",Toast.LENGTH_SHORT).show();
                    }
                });
                Button button7 = findViewById(R.id.button6);
                button7.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View v) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }
                });
                TextView textView = findViewById(R.id.textView2);
                textView.setText(get());
                Toast.makeText(MainActivity.this,"change CloudReveIP",Toast.LENGTH_SHORT).show();
            }
        });
        // 设置按钮点击事件
        backButton.setOnClickListener(v -> {
            history.pop();
            webView.goBack();

        });

        /**
         * 以上是按钮和页面部分
         * 下方是 webview 部分
         */
        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setDomStorageEnabled(true);//设置可以使用localStorage
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);//设置webView自适应屏幕大小
        settings.setBuiltInZoomControls(true);//关闭zoom
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);//关闭zoom按钮
        settings.setRenderPriority(WebSettings.RenderPriority.LOW);
        settings.setBlockNetworkImage(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.LOAD_NORMAL);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            settings.setUseWideViewPort(true);
        } else {
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // 重定向到新的URL
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // 页面加载完成
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // 页面开始加载
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress == 100) {
                    // 页面加载完成时更新按钮状态
                    backButton.setEnabled(webView.canGoBack());
                    history.push(webView.getUrl());

                    String r = webView.getUrl().replace(cloudreveip,"");
                    r = r.replace("home?path=%2F","").replace("/","");
                    if (r.equals("")) {
                        webView.clearHistory();
                    }
                    if (webView.canGoBack()) {
                        backButton.setVisibility(View.VISIBLE);
                    } else {
                        backButton.setVisibility(View.GONE);
                    }

                }
            }
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (valueCallbackArray != null) {

                    valueCallbackArray.onReceiveValue(null);
                    valueCallbackArray = null;
                }
                valueCallbackArray = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (Exception e) {
                    valueCallbackArray = null;
                    return false;
                }
                return true;
            }
        });

        // 设置DownloadListener
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });


        // 开启调试模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        String ip = readFile();
        cloudreveip = ip;
        Log.d("地址",ip);
        if (ip.isEmpty()) {
            Toast.makeText(MainActivity.this,"未设置CloudReveIP或设置失败",Toast.LENGTH_SHORT).show();
            webView.loadUrl("http://4x.ink:5212/"); // 替换为你的URL
            writeToFile("http://4x.ink:5212/");
        }else{
            webView.loadUrl(ip);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_CODE) {
                if (valueCallbackArray == null)
                    return;
                valueCallbackArray.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                valueCallbackArray = null;
            }
        } else if (requestCode == RESULT_CODE) {
            if (null == valueCallback)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            valueCallback.onReceiveValue(result);
            valueCallback = null;
        }
    }

    private String readFile() {
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream fis = openFileInput("serveripcloudreve.prop");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            content.append(new String(buffer, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "读取文件失败", Toast.LENGTH_SHORT).show();
            writeToFile("http://4x.ink:5212/");
            return "null";

        }
        return content.toString();
    }
    private void writeToFile(String data) {
        try {
            FileOutputStream fos = openFileOutput("serveripcloudreve.prop", MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
            Toast.makeText(this, "数据已写入文件", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "写入文件失败", Toast.LENGTH_SHORT).show();
        }
    }
    private void hideButtonDelayed(long delayMillis) {
        hideButtonRunnable = new Runnable() {
            @Override
            public void run() {
                button.animate().alpha(0).setDuration(2000).start();
                button.setEnabled(false);

            }
        };
        handler.postDelayed(hideButtonRunnable, delayMillis);
    }
    private void cancelHideButtonTask() {
        if (hideButtonRunnable != null) {
            handler.removeCallbacks(hideButtonRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelHideButtonTask();  // 在Activity销毁时取消定时任务
    }

    private String get(){
        String content = "Privacy policy\n" +
                "Last modified on Jun 4, 2022\n" +
                "\n" +
                "Privacy Policy\n" +
                "By using our website or apps, you consent to our privacy policy by default.\n" +
                "\n" +
                "We do not collect any personal information, especially private information, from any files/binary released from official sources, including but not limited to Cloudreve Community Edition, Cloudreve Pro Edition and Cloudreve iOS Client, all of them will not collect anything from user.\n" +
                "\n" +
                "We can guarantee that no personal information is collected on any files released from official sources, but websites built by Cloudreve Community/Pro Edition and any secondary development sites are at the discretion of the administrator and are not subject to this provision.\n" +
                "\n" +
                "Policy Change Timeline\n" +
                "The privacy policy will follow the Cloudreve version published, we reserve the right to modify the privacy policy document any time.";
        return content;
    }
    public int getHistoryCount() {
        if (webView != null) {
            WebBackForwardList history2 = webView.copyBackForwardList();
            return history2.getSize();
        }
        return 0;
    }
}