package androidx.io.text;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.io.core.UriProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 异常记录文件
 */
public class CrashText extends Text implements Thread.UncaughtExceptionHandler {

    public final String TAG = CrashText.class.getSimpleName();
    private Context context;
    /**
     * 异常日志
     */
    private static volatile CrashText logging;
    /**
     * 时间格式
     */
    private SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 初始化异常日志
     *
     * @param context 上下文
     * @return
     */
    public static CrashText initialize(Context context) {
        if (logging == null) {
            synchronized (CrashText.class) {
                if (logging == null) {
                    logging = new CrashText(context);
                }
            }
        }
        return logging;
    }

    /**
     * 初始化异常日志
     *
     * @param context 上下文
     * @param project 项目名称
     * @param dir     文件夹
     * @param prefix  文件前缀名称
     * @return
     */
    public static CrashText initialize(Context context, String project, String dir, String prefix) {
        if (logging == null) {
            synchronized (CrashText.class) {
                if (logging == null) {
                    logging = new CrashText(context, project, dir, prefix);
                }
            }
        }
        return logging;
    }

    public static CrashText text() {
        return logging;
    }

    /**
     * 构造函数
     *
     * @param context
     */
    private CrashText(Context context) {
        this.context = context;
        setFolder("IO", "Exception ");
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param project 项目名称
     * @param dir     文件夹
     * @param prefix  文件前缀名称
     * @return
     */
    private CrashText(Context context, String project, String dir, String prefix) {
        super(project, dir, prefix);
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    /**
     * 获取奔溃异常
     *
     * @param thread
     * @param throwable
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        throwable.printStackTrace();
        String content = buildApplicationDevice(false) + buildRuntimeException(false, throwable);
        if (UriProvider.isMounted()) {
            write(content, true);
        } else {
            new RuntimeException("sdcard is not mounted").printStackTrace();
        }
    }

    /**
     * 营运设备信息
     *
     * @return
     */
    public String buildApplicationDevice(boolean isEnd) {
        StringBuilder sb = new StringBuilder();
        //项目信息
        PackageManager packageManager = getContext().getPackageManager();
        ApplicationInfo applicationInfo = getContext().getApplicationInfo();
        String appName = (String) packageManager.getApplicationLabel(applicationInfo);
        //项目名字
        sb.append(" \n");
        sb.append("┌───────────────────────────────────────────────────────").append("\n");
        sb.append("│").append(timeFormat.format(new Date())).append("\n");
        sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄").append("\n");
        sb.append("│").append(appName).append('\n');
        sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄").append("\n");
        try {
            PackageInfo pi = packageManager.getPackageInfo(applicationInfo.packageName, 0);
            //项目版本号
            sb.append("│Version Code:").append(pi.versionCode).append('\n');
            //项目版本名
            sb.append("│Version Name:").append(pi.versionName).append('\n');
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄").append("\n");
        //设备信息
        sb.append("│").append("Equipment").append('\n');
        sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄").append("\n");
        //手机品牌
        sb.append("│Brand:").append(Build.BRAND).append('\n');
        //SDK版本
        sb.append("│Release:Android ").append(Build.VERSION.RELEASE).append('\n');
        //设备名
        sb.append("│Device:").append(Build.DEVICE).append('\n');
        //产品名
        sb.append("│Product:").append(Build.PRODUCT).append('\n');
        //制造商
        sb.append("│Manufacturer:").append(Build.MANUFACTURER).append('\n');
        //手机版本
        sb.append("│Version Code:").append(Build.DISPLAY).append('\n');
        //指纹
        sb.append("│Fingerprint:").append(Build.FINGERPRINT).append('\n');
        if (isEnd) {
            sb.append("└───────────────────────────────────────────────────────").append("\n");
        } else {
            sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄").append("\n");
        }
        return sb.toString();
    }

    /**
     * 构建运行时间
     *
     * @param throwable 异常
     * @return
     */
    public String buildRuntimeException(boolean topDivider, Throwable throwable) {
        StringBuffer sb = new StringBuffer();
        if (topDivider) {
            sb.append("┌───────────────────────────────────────────────────────\n");
        }
        sb.append("│RuntimeException").append("\n");
        sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄\n");
        sb.append("│").append(throwable.getCause().toString()).append('\n');
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("│").append(element.toString()).append('\n');
        }
        sb.append("└────────────────────────────────────────────────────────").append("\n");
        return sb.toString();
    }

}
