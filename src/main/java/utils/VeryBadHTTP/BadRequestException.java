package utils.VeryBadHTTP;

import java.io.IOException;

public class BadRequestException extends IOException {
    private int code;

    BadRequestException() {
    }

    BadRequestException(String message, int code) {
        super(message);
        this.code = code;
    }

    BadRequestException(String message){
        super(message);
    }

    public int getCode() {
        return code;
    }
}
