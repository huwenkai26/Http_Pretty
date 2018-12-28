package org.pretty.image;

/**
 * Created by hwk on 2017/5/15.
 */

public interface ImageLoadListener {
    void start();
    void error(String errorMsg);
    void complete();
}
