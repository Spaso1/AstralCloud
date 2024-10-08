package com.example.astralcloud;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.http.SslError;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;



@SuppressLint({"MissingInflatedId", "LocalSuppress"})
public class MainActivity extends AppCompatActivity {
    public final Context context = this;
    public static WebView webView;
    private static final int PERMISSION_REQUEST_CODE = 1;
    public static boolean flag = true;
    private boolean outT = true;
    private List<String> downloadNames = Arrays.asList();
    private int position = 0;
    private String cloudreveip;
    private Runnable hideButtonRunnable;
    private ValueCallback<Uri> valueCallback;
    private Button setbutton;
    private ValueCallback<Uri[]> valueCallbackArray;
    private boolean temp=true;
    private Button button;
    private final int REQUEST_CODE = 0x1010;
    private Handler handler = new Handler();
    private final static int RESULT_CODE = 0x1011;
    private Stack<String> history = new Stack<>();
    private View backButton;
    private boolean ifyes = true;
    private CircularProgressBar circularProgressBar;
    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 绑定按钮
         */
        setContentView(R.layout.activity_main);
        setbutton = findViewById(R.id.button_setting);
        button = findViewById(R.id.button2);
        backButton = findViewById(R.id.backbutton);
        hideButtonDelayed(3000);  // 3秒后隐藏按钮
        button.setVisibility(View.VISIBLE);

        setbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Intent intent = new Intent(MainActivity.this, Sec.class);
                startActivity(intent);
            }
        });
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
            if (ifyes) {
                history.pop();
                webView.goBack();
                TextView error = findViewById(R.id.textViewError);
                error.setVisibility(View.GONE);
            } else {
                Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("Download", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
            Log.d("TAG", "Preference changed: " + key);
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
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
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);//设置webView自适应屏幕大小
        settings.setBuiltInZoomControls(true);//关闭zoom
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);//关闭zoom按钮
        settings.setRenderPriority(WebSettings.RenderPriority.LOW);
        settings.setBlockNetworkImage(true);
        //反广告
        String userAgent = settings.getUserAgentString();
        userAgent += " NoAds";
        settings.setUserAgentString(userAgent);
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

        /**
         * 基础浏览器功能实现
         */
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
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
                handler.proceed();
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (error.getErrorCode() == -6){
                    Toast.makeText(MainActivity.this,"请检查网络权限,可能影响内容显示",Toast.LENGTH_SHORT).show();
                    outT = false;
                    circularProgressBar.setVisibility(View.GONE);
                    return;
                }
                if (error.getErrorCode() == -10){
                    Toast.makeText(MainActivity.this,"请检查网络权限,可能影响内容显示",Toast.LENGTH_SHORT).show();
                    outT = false;
                    circularProgressBar.setVisibility(View.GONE);
                    return;
                }
                if (!(error.getErrorCode() == -1)) {
                    Log.d("error", String.valueOf(error.getErrorCode()));
                    webView.setVisibility(View.GONE);
                    ifyes = false;
                    backButton.setVisibility(View.VISIBLE);
                    backButton.setEnabled(true);
                    try {
                        TextView textViewerror = findViewById(R.id.textViewError);
                        textViewerror.setVisibility(View.VISIBLE);
                        textViewerror.setText("页面错误! \n 访问的URL是 \n " + request.getUrl() + " \n如果网址存在则请检查网络或权限");
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this,"页面错误! \n 访问的URL是 \n " + request.getUrl() + " \n如果网址存在则请检查网络或权限",Toast.LENGTH_SHORT);
                    }


                    // 页面加载错误
                }
                outT = true;
            }
        });
        /**
         * 这里是为了图像显示正确
         */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.getSettings().setBlockNetworkImage(false);
        /**
         * 浏览器进度条
         */
        circularProgressBar = findViewById(R.id.circularProgressBar);

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                circularProgressBar.setProgress(newProgress);
                if (outT ) {
                    if (newProgress == 100) {
                        circularProgressBar.setVisibility(View.GONE);
                    } else {

                        circularProgressBar.setVisibility(View.VISIBLE);
                    }
                }else {
                    circularProgressBar.setVisibility(View.GONE);
                }
                // 当进度达到100时，隐藏加载界面

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
                // 自动获取文件名并下载
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("下载确认")
                        .setMessage("你即将下载: " + contentDisposition)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 用户点击了取消按钮后的操作
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                //new DownloadTask().execute(url, contentDisposition);
            }
        });

        if (readFile2().equals("beta=true")) {
            Toast.makeText(MainActivity.this,"启用测试",Toast.LENGTH_SHORT);
        }



        // 开启调试模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        String ip = readFile();
        cloudreveip = ip;
        Log.d("地址",ip);
        if (ip.equals("null")) {
            writeToFile("https://bilibili.com/");
            flag = false;
            // 加载新的布局文件
            LayoutInflater inflater = getLayoutInflater();
            View newView = inflater.inflate(R.layout.content_main, null);
            // 替换当前的布局
            ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
            rootView.removeAllViews();
            rootView.addView(newView);

            setContentView(R.layout.content_main);
            TextView textView = findViewById(R.id.textView2);
            textView.setText(get());
            Toast.makeText(MainActivity.this,"change CloudReveIP",Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            startActivity(intent);
        }else{
            Intent intent = getIntent();
            try {
                if(!(intent.getStringExtra("cloudreveip").equals("no"))) {
                    webView.loadUrl(intent.getStringExtra("cloudreveip"));
                }else {
                }
            }catch (Exception e) {
                webView.loadUrl(ip);


            }

        }

        // 检查是否有权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // 显示提示信息给用户
                Toast.makeText(this, "需要权限来显示画中画 ", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // 引导用户到权限管理页面
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                Toast.makeText(this, "需要权限来显示画中画 ", Toast.LENGTH_SHORT).show();
                startActivityForResult(intent, 1);
            }
        }
        Button button = findViewById(R.id.a1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Sec.class);
                SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("cloudreveip", webView.getUrl());
                editor.commit();
                Log.d("cloudreveip", cloudreveip);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // 隐藏状态栏和导航栏
        hideSystemUI();
        hideBottomUIMenu();
        // 获取Window对象
        Window window = getWindow();
// 隐藏底部导航栏
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    protected void hideBottomUIMenu() {
        int flags;
        int curApiVersion = android.os.Build.VERSION.SDK_INT;
        // This work only for android 4.4+
        if(curApiVersion >= Build.VERSION_CODES.KITKAT){

            // This work only for android 4.4+
            // hide navigation bar permanently in android activity
            // touch the screen, the navigation bar will not show

            flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }else{
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        // must be executed in main thread :)
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    private void hideSystemUI() {
        // 隐藏状态栏和导航栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // 用户离开时恢复系统UI
        showSystemUI();
    }

    private void showSystemUI() {
        // 恢复状态栏和导航栏
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
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
            return "null";

        }
        return content.toString();

    }
    private void writeToFile(String data) {
        try {
            FileOutputStream fos = openFileOutput("serveripcloudreve.prop", MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
            Toast.makeText(this, "数据已写入文件" , Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "写入文件失败", Toast.LENGTH_SHORT).show();
        }
    }


    private void hideButtonDelayed(long delayMillis) {
        hideButtonRunnable = new Runnable() {
            @Override
            public void run() {
                if(flag) {
                    button.animate().alpha(0).setDuration(3000).start();
                    setbutton.animate().alpha(0).setDuration(3000).start();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    button.setEnabled(false);
                    setbutton.setEnabled(false);
                }else {
                    button.animate().alpha(0).setDuration(12000).start();
                    setbutton.animate().alpha(0).setDuration(12000).start();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    button.setEnabled(false);
                    setbutton.setEnabled(false);
                }

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
    public String readFile2() {
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream fis = openFileInput("beta.prop");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            content.append(new String(buffer, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return "null";

        }
        return content.toString();

    }
    private String parseFileName(String contentDisposition) {
        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            String[] parts = contentDisposition.split(";");
            for (String part : parts) {
                if (part.trim().startsWith("filename")) {
                    String filename = part.substring(part.indexOf('=') + 1).trim();
                    if (filename.startsWith("\"") && filename.endsWith("\"")) {
                        filename = filename.substring(1, filename.length() - 1);
                    }
                    return filename;
                }
            }
        }
        return null;
    }
    private class DownloadTask extends AsyncTask<String, Void, String> {
        private String filename;
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String contentDisposition = params[1];
            this.filename = filename;
            // 解析文件名
            String fileName = parseFileName(contentDisposition);
            if (fileName == null || fileName.isEmpty()) {
                fileName = "downloadedfile.zip"; // 默认文件名
            }

            // 保存路径
            String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download";
            File rootDir = new File(rootPath);
            if (!rootDir.exists()) {
                rootDir.mkdirs();
            }

            String outputPath = rootPath + "/" + fileName;

            // 下载文件
            download(url, outputPath);
            return outputPath;
        }

        @Override
        protected void onPostExecute(String outputPath) {
            Toast.makeText(MainActivity.this, "文件已下载到 " + outputPath, Toast.LENGTH_SHORT).show();
            // 通知媒体扫描服务


        }

        private void download(String url, String outputPath) {
            HttpURLConnection connection = null;
            try {
                URL u = new URL(url);
                connection = (HttpURLConnection) u.openConnection();
                connection.setRequestMethod("GET");

                File outputFile = new File(outputPath);
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = connection.getInputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }

                fos.close();
                is.close();
                String mimeType = "text/plain";
                MediaScannerConnection.scanFile(context,
                        new String[]{outputFile.getAbsolutePath()}, new String[]{mimeType}, null);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }

    }
    private class MuDownloadTask extends AsyncTask<String, Integer, String> {
        private static final int THREAD_COUNT = 4; // 可以调整线程数量
        private String filename;
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String contentDisposition = params[1];

            // 解析文件名
            String fileName = parseFileName(contentDisposition);
            if (fileName == null || fileName.isEmpty()) {
                fileName = "downloadedfile.zip"; // 默认文件名
            }
            this.filename = fileName;
            // 保存路径
            String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download";
            File rootDir = new File(rootPath);
            if (!rootDir.exists()) {
                rootDir.mkdirs();
            }

            String outputPath = rootPath + "/" + fileName;

            // 获取文件大小
            long fileSize = 0;
            try {
                fileSize = getFileSize(url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 分割文件
            long chunkSize = fileSize / THREAD_COUNT;
            Thread[] threads = new Thread[THREAD_COUNT];
            RandomAccessFile[] parts = new RandomAccessFile[THREAD_COUNT];

            try {
                for (int i = 0; i < THREAD_COUNT; i++) {
                    long start = i * chunkSize;
                    long end = (i == THREAD_COUNT - 1) ? fileSize : start + chunkSize;

                    File partFile = new File(rootPath, fileName + ".part" + i);
                    parts[i] = new RandomAccessFile(partFile, "rw");

                    int finalI = i;
                    threads[i] = new Thread(() -> {
                        downloadPart(url, parts[finalI], start, end);
                    });
                    threads[i].start();
                }

                for (Thread thread : threads) {
                    thread.join();
                }

                // 合并文件
                mergeParts(rootPath, fileName, parts);

                // 删除临时文件
                for (int i = 0; i < THREAD_COUNT; i++) {
                    parts[i].close();
                    File partFile = new File(rootPath, fileName + ".part" + i);
                    partFile.delete();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return null;
            }

            return outputPath;
        }

        @Override
        protected void onPostExecute(String outputPath) {
            Toast.makeText(MainActivity.this, "文件已下载到 " + outputPath + "已自动加入到文件管理器界面", Toast.LENGTH_SHORT ).show();
        }

        private long getFileSize(String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            long size = connection.getContentLengthLong();
            connection.disconnect();
            return size;
        }

        private void downloadPart(String urlString, RandomAccessFile part, long start, long end) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
                connection.setRequestMethod("GET");

                InputStream is = connection.getInputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    part.write(buffer, 0, bytesRead);
                }

                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        private void mergeParts(String rootPath, String fileName, RandomAccessFile[] parts) throws IOException {
            File outputFile = new File(rootPath, fileName);
            FileOutputStream fos = new FileOutputStream(outputFile);

            for (RandomAccessFile part : parts) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = part.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            String mimeType = "text/plain";
            MediaScannerConnection.scanFile(context,
                    new String[]{outputFile.getAbsolutePath()}, new String[]{mimeType}, null);
            fos.close();
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            SharedPreferences sharedPreferences = getSharedPreferences("Download", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("DOWN:" + this.filename ,progress[0]);
            editor.apply();
        }
    }
}