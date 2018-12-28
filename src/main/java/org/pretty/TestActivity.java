package org.pretty;

import org.pretty.callback.DefaultCallBack;
import org.pretty.callback.ErrorCode;
import org.pretty.callback.StringCallBack;
import org.pretty.http.HttpUtils;


public class TestActivity {
    public static void main(String[] args) {
        String url = "http://47.75.40.123:9001/icoactivity/v2/activityForegin/findAll";
        HttpUtils.get().url(url).build().execute(new StringCallBack() {
            @Override
            public void onError(ErrorCode errorCode) {

            }

            @Override
            public void onSucceed(String content) {
                System.out.println("-----------"+content+"-----------");
            }
        });


    }
}