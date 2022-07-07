package utils.VeryBadHTTP;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response extends Message {
    private int status;

    public Response(HttpExchange exchange) {
        super(exchange);
        this.headers = exchange.getResponseHeaders();
    }

    public void message(String message) throws IOException {
        exchange.sendResponseHeaders(status, message.length());
        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }

    public Response cookie(String name, String value){
        List<String> options = new ArrayList<>();
        options.add(name+"="+value);
        headers.put("Set-Cookie", options);
        return this;
    }

    public Response status(int httpStatusCode) {
        status = httpStatusCode;
        return this;
    }
}
