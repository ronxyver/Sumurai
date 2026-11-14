// OWNER: Ron Xyver Atienza
package com.sumurai.model;

public class InvalidAnswerException extends Exception {
    public InvalidAnswerException(String message) {
        super(message);
    }

    public InvalidAnswerException(String message, Throwable cause) {
        super(message, cause);
    }
}
