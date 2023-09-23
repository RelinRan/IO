package androidx.io.adapter;

import androidx.io.photo.PhotoView;

/**
 * TBS图片加载器
 */
public interface TBSlmageLoader {

    /**
     * 图片加载显示
     *
     * @param target 图片View
     * @param url    图片资源路径
     */
    void onItemImageShow(PhotoView target, String url);

}
