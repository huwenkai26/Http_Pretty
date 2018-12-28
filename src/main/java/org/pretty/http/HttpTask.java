package org.pretty.http;



import java.util.Map;

/**
 * Created by hwk on 2017/2/15.
 */

public class HttpTask implements Runnable {
    private HttpRequestInfo httpRequestInfo;

    HttpTask( int requestMode, Map<String, String> requestHeaders, String bodyContent,
    		Map<String, String> requestParams, String url, HttpCallBack httpListener) {
        httpRequestInfo = new HttpRequestInfo();
        httpRequestInfo.setUrl(url);
        httpRequestInfo.setHttpCallBack(httpListener);
        httpRequestInfo.setRequestParams(requestParams);
        httpRequestInfo.setRequestHeader(requestHeaders);
        httpRequestInfo.setRequestMode(requestMode);
        httpRequestInfo.setBoby(bodyContent);
    }

    @Override
    public void run() {
        httpRequestInfo.execute();
    }
}
