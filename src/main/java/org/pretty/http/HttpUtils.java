package org.pretty.http;


import org.pretty.proxy.ProxyConfig;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by hwk on 2017/2/15.
 */

public class HttpUtils {

    private int requestMode;
    private String url;
    private String body;
    private Map<String,String> requestParams;
    private Map<String,String> requestHeaders;
    private ProxyConfig proxyConfig;

    private HttpUtils(Builder builder ) {
        this.requestMode = builder.requestMode;
        this.url = builder.url;
        this.requestParams = builder.requestParams;
        this.requestHeaders = builder.requestHeaders;
        this.body = builder.body;
        this.proxyConfig = builder.proxyConfig;
    }

    public static Builder get() {
        return new Builder(IHttpRequest.GET);
    }

    public static Builder post() {
        return new Builder(IHttpRequest.POST);
    }


    public void execute(HttpCallBack listener) {
        HttpTask httpTask = new HttpTask( requestMode,requestHeaders, body,
        		requestParams, url,proxyConfig, listener);
        ThreadPoolManger.getInstance().execute(httpTask);
    }


    public static class Builder {
        private int requestMode;
        private String url;
        private Map<String,String> requestParams;
        private Map<String,String> requestHeaders;
        private String body;
        private ProxyConfig proxyConfig;
        
        private Builder(int requestMode) {
            this.requestMode = requestMode;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder addParam(String key, String value) {
            if (requestParams==null){
                requestParams=new HashMap<String, String>();
            }
            requestParams.put(key,value);
            return this;
        }
        public Builder addParams(Map<String,String> requestParams) {
            this.requestParams=requestParams;
            return this;
        }
        public Builder addHeader(String key, String value) {
            if (requestHeaders==null){
                requestHeaders=new HashMap<String, String>();
            }
            requestHeaders.put(key,value);
            return this;
        }
        public Builder addHeaders(Map<String,String> requestParams) {
            this.requestHeaders=requestParams;
            return this;
        }
        
        public Builder addBody(String body) {
            this.body=body;
            return this;
        }

        public Builder addproxyConfig(ProxyConfig proxyConfig) {
            this.proxyConfig=proxyConfig;
            return this;
        }
        public HttpUtils build() {
            return new HttpUtils(this);
        }
    }

}
