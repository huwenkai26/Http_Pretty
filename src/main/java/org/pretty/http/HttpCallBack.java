package org.pretty.http;



/**
 * Created by hwk on 2017/2/15.
 */

public interface HttpCallBack {
    void onResponse(byte[] bytes);
    void onFailure(HttpErrorCode errorCode);
}
