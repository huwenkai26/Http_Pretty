package org.pretty.callback;


import org.pretty.http.HttpCallBack;
import org.pretty.http.HttpErrorCode;

/**
 * Created by hwk on 2017/2/15.
 */

public abstract class DefaultCallBack implements HttpCallBack {

    @Override
    public final void onResponse(final byte[] bytes) {
        onSucceed(bytes);
         
    }

    @Override
    public final void onFailure(final HttpErrorCode errorCode) {
            onError(errorCode);
    }

    public abstract void onSucceed(byte[] content);

    public abstract void onError(ErrorCode errorCode);

}
