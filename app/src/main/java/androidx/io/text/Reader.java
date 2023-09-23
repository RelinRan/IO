package androidx.io.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 文本读取
 */
public class Reader {

    private File file;
    private ExecutorService service;
    private Future future;
    private Channels channels;

    /**
     * 读取文件
     * @param path 文件路径
     */
    public Reader(String path) {
        this.file = new File(path);
        service = Executors.newCachedThreadPool();
    }

    /**
     * 读取文件
     * @param file 文件
     */
    public Reader(File file) {
        this.file = file;
        service = Executors.newCachedThreadPool();
    }

    /**
     * 异步读取数据
     *
     * @param onReadListener
     */
    public void async(OnReadListener onReadListener) {
        if (channels == null) {
            channels = new Channels();
        }
        future = service.submit(() -> {
            synchronized (file) {
                String content = sync();
                if (onReadListener != null) {
                    channels.read(onReadListener, content.toString());
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
     * 同步读取数据
     *
     * @return
     */
    public synchronized String sync() {
        if (!file.exists()) {
            System.err.println("file is not exist " + file.getAbsolutePath());
            return null;
        }
        StringBuffer content = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content.toString();
    }

}
