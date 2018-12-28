package org.pretty.http.handler;

import io.netty.handler.timeout.ReadTimeoutHandler;

import java.lang.reflect.Field;

public class HttpTimeoutHandler extends ReadTimeoutHandler {

  private static Field READ_TIME;

  public HttpTimeoutHandler(int timeoutSeconds) {
    super(timeoutSeconds);
    try {
      READ_TIME = this.getClass().getSuperclass().getSuperclass().getDeclaredField("lastReadTime");
      READ_TIME.setAccessible(true);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
  }

  public void updateLastReadTime(long sleepTime) throws IllegalAccessException {
    long readTime = READ_TIME.getLong(this);
    READ_TIME.set(this, readTime == -1 ? sleepTime : readTime + sleepTime);
  }
}
