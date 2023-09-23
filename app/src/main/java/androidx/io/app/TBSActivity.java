package androidx.io.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.io.adapter.TBSAdapter;
import androidx.io.core.IOProvider;
import androidx.io.R;
import androidx.io.core.TBS;
import androidx.io.core.UriProvider;
import androidx.io.net.Downloader;
import androidx.io.net.OnDownloadListener;
import androidx.io.photo.PhotoView;
import androidx.io.widget.CircleProgress;
import androidx.viewpager.widget.ViewPager;

import com.tencent.smtt.sdk.TbsReaderView;
import com.tencent.smtt.sdk.TbsVideo;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯TBS文件预览<br/>
 * 官网SDK: https://x5.tencent.com/tbs/index.html<br/>
 * 支持格式：doc、docx、ppt、pptx、xls、xlsx、pdf、txt、epub<br/>
 */
public class TBSActivity extends AppCompatActivity implements TbsReaderView.ReaderCallback, OnDownloadListener, ViewPager.OnPageChangeListener {

    private String TAG = TBSActivity.class.getSimpleName();
    private RelativeLayout tbs_container;
    private PhotoView image_view;
    private TbsReaderView readerView;
    private ViewPager viewPager;
    private CircleProgress progressView;
    private TextView tvIndex;
    private String path;
    private String url;
    private boolean override;
    private TBSAdapter adapter;
    private List<String> urls;
    private int position;

    /**
     * 打开文件预览
     *
     * @param activity 页面
     * @param file     本地文件
     */
    public static void start(Activity activity, File file) {
        Intent intent = new Intent(activity, TBSActivity.class);
        intent.putExtra("filePath", file.getAbsolutePath());
        activity.startActivity(intent);
    }

    /**
     * 打开文件预览
     *
     * @param fragment 页面
     * @param file     本地文件
     */
    public static void start(Fragment fragment, File file) {
        Intent intent = new Intent(fragment.getContext(), TBSActivity.class);
        intent.putExtra("filePath", file.getAbsolutePath());
        fragment.startActivity(intent);
    }

    /**
     * 打开文件预览
     *
     * @param activity 页面
     * @param url      网络文件
     * @param override 覆盖文件
     */
    public static void start(Activity activity, String url, boolean override) {
        Intent intent = new Intent(activity, TBSActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("override", override);
        activity.startActivity(intent);
    }

    /**
     * 打开文件预览
     *
     * @param activity 页面
     * @param urls     文件集合
     * @param position 位置
     */
    public static void start(Activity activity, List<String> urls, int position) {
        Intent intent = new Intent(activity, TBSActivity.class);
        intent.putExtra("json", toJson(urls));
        intent.putExtra("position", position);
        activity.startActivity(intent);
    }

    /**
     * 打开文件预览
     *
     * @param fragment 页面
     * @param url      网络文件
     * @param override 覆盖文件
     */
    public static void start(Fragment fragment, String url, boolean override) {
        Intent intent = new Intent(fragment.getContext(), TBSActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("override", override);
        fragment.startActivity(intent);
    }

    /**
     * 打开文件预览
     *
     * @param fragment 页面
     * @param urls     文件集合
     * @param position 位置
     */
    public static void start(Fragment fragment, List<String> urls, int position) {
        Intent intent = new Intent(fragment.getContext(), TBSActivity.class);

        intent.putExtra("json", toJson(urls));
        intent.putExtra("position", position);
        fragment.startActivity(intent);
    }

    /**
     * List转JSON
     *
     * @param list
     * @return
     */
    public static String toJson(List<String> list) {
        int size = list == null ? 0 : list.size();
        String[] items = new String[size];
        list.toArray(items);
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(items);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonArray.toString();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.io_tbs);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        image_view = findViewById(R.id.image_view);
        tbs_container = findViewById(R.id.tbs_container);
        viewPager = findViewById(R.id.view_pager);
        tvIndex = findViewById(R.id.tv_index);
        progressView = findViewById(R.id.tbs_progress);
        readerView = new TbsReaderView(this, this);
        int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
        progressView.setVisibility(View.GONE);
        tbs_container.addView(readerView, new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        position = getIntent().getIntExtra("position", 0);
        String json = getIntent().getStringExtra("json");
        if (json != null) {
            urls = toList(json);
            viewPager.setVisibility(View.VISIBLE);
            tvIndex.setVisibility(View.VISIBLE);
            initImageAdapter();
        } else {
            viewPager.setVisibility(View.GONE);
            tvIndex.setVisibility(View.GONE);
        }
        override = getIntent().getBooleanExtra("override", true);
        path = getIntent().getStringExtra("filePath");
        openFile(this, path);
        url = getIntent().getStringExtra("url");
        download(url);
        Log.i(TAG, "onCreate path: " + path + ",url: " + url);
    }

    /**
     * JSON转List
     *
     * @param json
     * @return
     */
    public List<String> toList(String json) {
        List<String> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                Object obj = jsonArray.get(i);
                list.add(String.valueOf(obj));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * 初始化图片
     */
    private void initImageAdapter() {
        if (TBS.getInstance() == null) {
            return;
        }
        adapter = new TBSAdapter(this);
        adapter.setImageLoader(TBS.getInstance().getImageLoader());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        adapter.setItems(urls);
        viewPager.setCurrentItem(position);
        tvIndex.setText((position + 1) + "/" + adapter.getCount());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        tvIndex.setText((position + 1) + "/" + adapter.getCount());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 打开文件
     *
     * @param context
     * @param filePath
     */
    private void openFile(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        String mimeType = IOProvider.getMimeType(filePath);
        Log.i(TAG, "mimeType: " + mimeType);
        if (mimeType.startsWith("image")) {
            image_view.setImageBitmap(BitmapFactory.decodeFile(filePath));
            return;
        }
        if (mimeType.startsWith("video")) {
            TBS.openVideo(this, new File(filePath));
            finish();
            return;
        }
        String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);
        boolean support = readerView.preOpen(suffix, false);
        Log.i(TAG, "support: " + support + ",suffix: " + suffix);
        if (support) {
            Bundle bundle = new Bundle();
            bundle.putString("filePath", filePath);
            bundle.putString("tempPath", IOProvider.makeCacheDir(context, "TBS").getAbsolutePath());
            readerView.openFile(bundle);
            return;
        }
        finish();
        File file = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(UriProvider.fromFile(context, file), mimeType);
        startActivity(intent);
        finish();
    }


    /**
     * 下载文件
     *
     * @param url
     */
    private void download(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String mimeType = IOProvider.getMimeType(url);
        if (mimeType.startsWith("video")) {
            TbsVideo.openVideo(this, url);
            finish();
            return;
        }
        progressView.setVisibility(View.VISIBLE);
        Downloader downloader = new Downloader(this, url);
        downloader.setOverride(override);
        downloader.setOnDownloadListener(this);
        downloader.start();
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }

    @Override
    public void onDownloading(long total, long progress) {
        progressView.setMax((int) total);
        progressView.setProgress((int) progress);
    }

    @Override
    public void onDownloadCompleted(File file) {
        progressView.setVisibility(View.GONE);
        path = file.getAbsolutePath();
        openFile(this, path);
    }

    @Override
    public void onDownloadFailed(Exception e) {
        progressView.setVisibility(View.GONE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        readerView.onStop();
    }


}
