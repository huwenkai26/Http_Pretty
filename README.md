# Http_Pretty
基于netty写的一款http网络访问框架,可以配置超时时间，重试次数，Http Socket4  Socket5代理
### 基础使用
```
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
```