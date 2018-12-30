package org.pretty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.pretty.callback.*;
import org.pretty.http.HttpUtils;
import org.pretty.model.Activity;

import java.nio.charset.Charset;


public class TestActivity {
    public static void main(String[] args) {
        String url = "http://47.75.40.123:9001/icoactivity/v2/activityForegin/findAll";
        HttpUtils.get().url(url).build().execute(new JsonCallBack<Activity>() {
            @Override
            public void onError(ErrorCode errorCode) {

            }

            @Override
            public void onResponse(Activity activity) {
                System.out.println(activity.getData().toString());
            }
        });

//        String str1 = "11111111";

//        ByteBuf buf1 = Unpooled.buffer();
////        buf1.writeBytes(str1.getBytes());
//        System.out.println("buf1---"+buf1.toString(Charset.defaultCharset()));
//        String str2 = "2222";
//
//        ByteBuf buf2 = Unpooled.buffer();
//        buf2.writeBytes(str2.getBytes());
//        System.out.println("buf2---"+buf2.toString(Charset.defaultCharset()));
//        ByteBuf allBuf = Unpooled.wrappedBuffer(buf1, buf2);
////        System.out.println("buf3---"+new String(allBuf.nioBuffer().array()));
//
//        String str4 = "44444";
//
//        ByteBuf buf4 = Unpooled.buffer();
//        buf4.writeBytes(str4.getBytes());
//        System.out.println("buf4---"+buf4.toString(Charset.defaultCharset()));
//        allBuf = Unpooled.wrappedBuffer(allBuf, buf4);
//        System.out.println("buf5---"+new String(allBuf.nioBuffer().array()));

    }
}