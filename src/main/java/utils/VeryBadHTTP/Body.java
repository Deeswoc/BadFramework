package utils.VeryBadHTTP;

import com.fasterxml.jackson.databind.JsonNode;
import utils.VeryBadHTTP.BodyParser.Json;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Body {
    byte[] raw;
    public Body(InputStream is, int contentLength) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        raw = new byte[contentLength];
        if(dis.read(raw)!=contentLength){
            throw new BadRequestException("Bad Request", 400);
        };
    }

    public String json(){
        return raw.toString();
    }

    public JsonNode toJson() throws IOException {
        String bodyString = new String(raw);
        return Json.parse(bodyString);
    }
}
