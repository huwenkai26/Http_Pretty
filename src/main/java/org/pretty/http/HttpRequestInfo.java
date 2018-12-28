package org.pretty.http;

import com.sun.tools.javac.util.ArrayUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.ReferenceCountUtil;
import org.pretty.proxy.ProxyConfig;
import org.pretty.proxy.ProxyHandleFactory;
import org.pretty.utils.Debug;
import org.pretty.utils.ProtoUtil;
import org.pretty.utils.ProtoUtil.RequestProto;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by hwk on 2017/2/15.
 */

public class HttpRequestInfo implements IHttpRequest {


    public int mReadTimeout;
    public int mConnectionTimeout;
    private int mRetryCount;//请求网络失败 重试次数
    private int mRetryTime;//请求网络失败后 重试加载时间间隔
    private HttpCallBack mHttpCallBack;
    private String mRequestUrl;
    private Map<String, String> mRequestHeaders;
    private Map<String, String> mRequestParams;
    private int mRequestMode;
    private String mBodyContent;
    private ProxyConfig mProxyConfig;
    private RequestProto mRequestProto;
    private ChannelFuture cf;
    private ByteBuffer mByteBuffer;
    private byte[] mByteBuf;

    public HttpRequestInfo() {
        //设置默认参数
        mRetryCount = 1;
        mRetryTime = 1000;
        mConnectionTimeout = 8000;
        mReadTimeout = 8000;
    }

    public HttpRequestInfo(HttpRequestConfig config) {
        mRetryCount = config.retryCount;
        mRetryTime = config.retryTime;
        mConnectionTimeout = config.connectionTimeout;
        mReadTimeout = config.readTimeout;
        mProxyConfig = config.proxyConfig;
    }


    @Override
    public void execute() {
        Debug.core("http execute----" + "method:" + (mRequestMode == 0 ? "get" : "post"));
        Debug.core("http execute----" + "url:" + mRequestUrl);
        if (mRequestParams != null) {
            String requestParams = mapToString(mRequestParams);
            if (!mRequestUrl.contains("?")) {
                mRequestUrl = mRequestUrl + "?" + requestParams;
            } else if (mRequestUrl.substring(mRequestUrl.length() - 1).equals("?")) {
                mRequestUrl = mRequestUrl + requestParams;
            }
        }
        try {
            DefaultFullHttpRequest request = buildRequest(mRequestMode, mRequestUrl, mRequestHeaders, mBodyContent);
            mRequestProto = ProtoUtil.getRequestProto(request);
            requestNetwork(request);
        } catch (MalformedURLException e) {
            retryRequest(new HttpErrorCode("Request error url:"));
            e.printStackTrace();
        } catch (InterruptedException e) {
            retryRequest(new HttpErrorCode("Request error"));
            e.printStackTrace();
        } catch (TimeoutException e) {
            retryRequest(new HttpErrorCode("Time out error"));

            e.printStackTrace();
        }


    }


    private void requestNetwork(HttpRequest httpRequest) throws InterruptedException, TimeoutException {
        Bootstrap bootstrap = new Bootstrap();
        //应该可以配置
        NioEventLoopGroup loopGroup = new NioEventLoopGroup(1);
        bootstrap.group(loopGroup) // 注册线程池
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_TIMEOUT, mConnectionTimeout)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {

                        if (HttpRequestInfo.this.mProxyConfig != null) {
                            ch.pipeline().addLast(ProxyHandleFactory.build(HttpRequestInfo.this.mProxyConfig));
                        }
                        if (mRequestProto.getSsl()) {
                            SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                            ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), mRequestProto.getHost(), mRequestProto.getPort()));
                        }
                        ch.pipeline().addLast("httpCodec", new HttpClientCodec());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            private boolean isSuccess;

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                try {
                                    if (msg instanceof HttpResponse) {
                                        HttpResponse httpResponse = (HttpResponse) msg;
                                        Integer responseCode = httpResponse.status().code();
                                        if (responseCode < 200 || responseCode >= 300) {
                                            cf.channel().closeFuture();
                                            retryRequest(new HttpErrorCode("Request error url code :" + responseCode));
                                        } else {
                                            isSuccess = true;
                                        }
                                    }
                                    if (msg instanceof HttpContent) {
                                        if (!isSuccess) {
                                            return;
                                        }
                                        HttpContent httpContent = (HttpContent) msg;
                                        if (mByteBuf != null) {
                                            byte[] bytes = new byte[httpContent.content().readableBytes()];
                                            httpContent.content().readBytes(bytes);
                                            byte[] concat = concat(mByteBuf, bytes);
                                            mByteBuf = concat;
                                        } else {
                                            mByteBuf = new byte[ httpContent.content().readableBytes()];
                                              httpContent.content().readBytes(mByteBuf);
                                        }

                                    }

                                    if (msg instanceof LastHttpContent) {
                                        LastHttpContent content = (LastHttpContent) msg;
                                        mHttpCallBack.onResponse(mByteBuf);
                                    }
                                } catch (Exception e) {
                                    throw e;
                                } finally {
                                    ReferenceCountUtil.release(msg);
                                }

                            }

                        });
                    }
                });
        if (HttpRequestInfo.this.mProxyConfig != null) {
            //代理服务器解析DNS和连接
            bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
        }
        cf = bootstrap.connect(mRequestProto.getHost(), mRequestProto.getPort());

        cf.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                cf.channel().writeAndFlush(httpRequest);

            } else {
                future.channel().close();
                cf.channel().closeFuture();
            }
        });


    }

    public static  byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private void retryRequest(HttpErrorCode error) {
        //网络加载失败后重试
        if (mRetryCount > 0) {
            mRetryCount--;
            Debug.core(error + " try reload after " + mRetryTime / 1000 + " seconds");
            try {
                Thread.sleep(mRetryTime);
                execute();
            } catch (Exception e) {
                // TODO: handle exception
            }
        } else {
            if (mHttpCallBack != null) {
                mHttpCallBack.onFailure(error);
            }
        }
    }

    private String mapToString(Map<String, String> map) {
        String[] params = new String[map.size()];
        int i = 0;
        for (String paramKey : map.keySet()) {
            String value = map.get(paramKey);
            paramKey = URLEncoder.encode(paramKey);
            value = ((value == null) ? "" : URLEncoder.encode(value));
            String param = paramKey + "=" + value;
            params[i] = param;
            i++;
        }
        String paramsTemp = "";
        if (params != null) {
            for (String param : params) {
                if (param != null && !"".equals(param.trim())) {
                    paramsTemp += "&" + param;
                }
            }
        }
        Debug.core("request param:" + paramsTemp);
        return paramsTemp;
    }


    @Override
    public void setUrl(String url) {
        this.mRequestUrl = url;
    }

    @Override
    public void setHttpCallBack(HttpCallBack httpCallBack) {
        this.mHttpCallBack = httpCallBack;
    }

    @Override
    public void setRequestHeader(Map<String, String> requestHeader) {
        this.mRequestHeaders = requestHeader;
    }

    @Override
    public void setRequestParams(Map<String, String> requestParams) {
        this.mRequestParams = requestParams;
    }

    @Override
    public void setRequestMode(int mode) {
        this.mRequestMode = mode;
    }

    @Override
    public void setRequestFailRetryCount(int retryCount) {
        this.mRetryCount = retryCount;
    }

    @Override
    public void setBoby(String body) {
        mBodyContent = body;
    }

    @Override
    public void setProxyConfig(ProxyConfig proxyConfig) {
        this.mProxyConfig = proxyConfig;
    }


    public static DefaultFullHttpRequest buildRequest(int method, String url)
            throws MalformedURLException {
        return buildRequest(method, url, null, null);
    }

    public static DefaultFullHttpRequest buildRequest(int method, String url, Map<String, String> heads)
            throws MalformedURLException {
        return buildRequest(method, url, heads, null);
    }

    public static DefaultFullHttpRequest buildRequest(int method, String url, Map<String, String> heads, String body)
            throws MalformedURLException {
        URL u = new URL(url);
        HttpHeadsInfo headsInfo = new HttpHeadsInfo();
        headsInfo.add("Host", u.getHost());
        headsInfo.add("Connection", "keep-alive");
        headsInfo.add("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36");
        headsInfo.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        headsInfo.add("Referer", u.getHost());
        if (heads != null) {
            for (Map.Entry<String, String> entry : heads.entrySet()) {
                headsInfo.set(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
            }
        }
        byte[] content = null;
        ByteBuf byteBuf = null;
        if (body != null && body.length() > 0) {
            content = body.getBytes();
            headsInfo.add("Content-Length", content.length);
            byteBuf = Unpooled.wrappedBuffer(content);
        }
        HttpMethod httpMethod = method == GET ? HttpMethod.GET : HttpMethod.POST;
        DefaultFullHttpRequest defaultFullHttpRequest = null;
        if (method == GET) {
            defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, url);
        } else {
            defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, url, byteBuf);
        }
        defaultFullHttpRequest.headers().set(headsInfo);
        return defaultFullHttpRequest;
    }

    public class HttpRequestConfig {
        private int retryCount;//请求网络失败 重试次数
        private int retryTime;//请求网络失败后 重试加载时间间隔
        private int readTimeout;
        private int connectionTimeout;
        private ProxyConfig proxyConfig;
    }
}
