package utils.VeryBadHTTP;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import utils.VeryBadHTTP.BodyParser.Json;

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

    public void json(String jsonString) throws IOException {
        List<String> contentTypes = new ArrayList<>();
        contentTypes.add("application/json");
        headers.put("Content-type", contentTypes);
        exchange.sendResponseHeaders(status, jsonString.length());
        OutputStream os = exchange.getResponseBody();
        os.write(jsonString.getBytes());
        os.close();
    }

    public void json(JsonNode jsonTree) throws IOException {
        json(jsonTree.toString());
    }

    public void json(Object object) throws IOException {
        json(Json.toJson(object));
    }
}
