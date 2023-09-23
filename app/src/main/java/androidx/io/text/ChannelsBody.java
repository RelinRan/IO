package androidx.io.text;

/**
 * 通道内容
 */
public class ChannelsBody {

    /**
     * 内容
     */
    private String content;
    /**
     * 读取监听
     */
    private OnReadListener onReadListener;
    /**
     * 写入监听
     */
    private OnWriteListener onWriteListener;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OnReadListener getOnReadListener() {
        return onReadListener;
    }

    public void setOnReadListener(OnReadListener onReadListener) {
        this.onReadListener = onReadListener;
    }

    public OnWriteListener getOnWriteListener() {
        return onWriteListener;
    }

    public void setOnWriteListener(OnWriteListener onWriteListener) {
        this.onWriteListener = onWriteListener;
    }
}
