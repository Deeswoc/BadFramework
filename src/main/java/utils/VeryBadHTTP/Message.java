package utils.VeryBadHTTP;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import java.util.Map;

public abstract class Message {
    Body body;
    Headers headers;
    HttpExchange exchange;

    Message(HttpExchange exchange) {
        this.exchange = exchange;
    }
}
