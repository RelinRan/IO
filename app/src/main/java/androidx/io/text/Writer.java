package androidx.io.text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 文本写入
 */
public class Writer {

    private File file;
    private ExecutorService service;
    private Future future;
    private Channels channels;

    /**
     * 写入文件
     *
     * @param path 路径
     */
    public Writer(String path) {
        this.file = new File(path);
        service = Executors.newCachedThreadPool();
    }

    /**
     * 写入文件
     *
     * @param file 文件
     */
    public Writer(File file) {
        this.file = file;
        service = Executors.newCachedThreadPool();
    }

    /**
     * 异步写入数据
     *
     * @param onReadListener
     */
    public void async(String content, boolean append, OnWriteListener onReadListener) {
        if (channels == null) {
            channels = new Channels();
        }
        future = service.submit(() -> {
            synchronized (file) {
                String value = sync(content, append);
                if (onReadListener != null) {
                    channels.write(onReadListener, value);
                }
            }
        });
    }

    /**
     * 取消操作
     */
    public void cancel() {
        if (future != null) {
            future.cancel(true);
        }
        if (channels != null) {
            channels.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 同步写入数据
     *
     * @param content 内容
     * @param append  是否追加
     * @return
     */
    public synchronized String sync(String content, boolean append) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

}
