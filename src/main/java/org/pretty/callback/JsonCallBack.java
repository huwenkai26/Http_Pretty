package org.pretty.callback;


import com.google.gson.Gson;
import org.pretty.utils.Debug;
import org.pretty.utils.Parser;

import java.lang.reflect.Type;


/**
 * Created by hwk on 2017/2/15.
 */

public abstract class JsonCallBack<M> extends StringCallBack {



    protected JsonCallBack() {

    }

    @Override
    public  void  onSucceed(String content) {
    	Debug.core("json:"+content);
        if (content != null) {
            Type type = Parser.getInstance().getSuperclassTypeParameter(getClass());
            final Object object = Parser.getInstance().parser(content, type);
            if (object != null) {
                onResponse((M) object);
            } else {
            	onError(new ErrorCode("Json parse error"));
            }
        } else {
        	onError(new ErrorCode("Json parse exception"));
        }
    }


    public abstract void onResponse(M m);

}
