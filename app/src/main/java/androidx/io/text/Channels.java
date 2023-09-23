package androidx.io.text;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

/**
 * 通道
 */
public class Channels extends Handler {

    /**
     * 读取
     *
     * @param listener 监听
     * @param content  内容
     */
    public void read(OnReadListener listener, String content) {
        Message message = obtainMessage();
        message.what = 1;
        ChannelsBody body = new ChannelsBody();
        body.setContent(content);
        body.setOnReadListener(listener);
        message.obj = body;
        sendMessage(message);
    }

    /**
     * 写入
     *
     * @param listener 监听
     * @param content  内容
     */
    public void write(OnWriteListener listener, String content) {
        Message message = obtainMessage();
        message.what = 2;
        ChannelsBody body = new ChannelsBody();
        body.setContent(content);
        body.setOnWriteListener(listener);
        message.obj = body;
        sendMessage(message);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        ChannelsBody body = (ChannelsBody) msg.obj;
        switch (msg.what) {
            case 1:
                body.getOnReadListener().onRead(body.getContent());
                break;
            case 2:
                body.getOnWriteListener().onWrite(body.getContent());
                break;
        }
    }

}
