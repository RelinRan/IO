package androidx.io.text;

public interface OnWriteListener {

    /**
     * 文件写入
     * @param content 内容
     */
    void onWrite(String content);

}
