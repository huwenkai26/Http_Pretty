package org.pretty.callback;

import java.io.UnsupportedEncodingException;


/**
 * Created by hwk on 2017/2/15.
 */

public abstract class StringCallBack extends DefaultCallBack {
    @Override
    public void onSucceed(byte[] content) {
            onSucceed(new String(content));
    }

    public abstract void onSucceed(String content);
}
