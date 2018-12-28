package org.pretty.callback;

public class ErrorCode {
    private final String message;

    public ErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
