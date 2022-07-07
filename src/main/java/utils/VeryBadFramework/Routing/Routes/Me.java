package utils.VeryBadFramework.Routing.Routes;

import com.sun.net.httpserver.Headers;
import utils.VeryBadFramework.Next;
import utils.VeryBadFramework.Routing.Router;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.io.IOException;
import java.util.Map;

public class Me extends Router {
    public Me() {
        use((Request req, Response res, Next next) -> {
            System.out.println("this is being hit");
            next.apply(null);
        });

        get("/test", (Request req, Response res, Next next)->{
            try{
                res.status(200).message("Test Message");
            } catch (IOException e) {
                next.apply(e);
            }
        });
    }
}
