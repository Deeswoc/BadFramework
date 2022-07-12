package utils.VeryBadFramework.Routing.Routes;

import Data.Database;
import Model.Session;
import Model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.Headers;
import utils.VeryBadFramework.Next;
import utils.VeryBadFramework.Routing.Router;
import utils.VeryBadHTTP.BodyParser.Json;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class Me extends Router {
    @Override
    protected void dispatch(Request req, Response res, Next done) {
        super.dispatch(req, res, done);
        System.out.println("Me Router is being ran");
    }

    public Me() {
        use((Request req, Response res, Next next) -> {
            Map<String, String> cookies = req.cookies();
            String sid = cookies.get("sid");
            try {
                if (sid != null) {
                    Database db = Database.getDatabase();
                    Model.Session session = (Model.Session) db.getByID(Session.class, sid);
                    req.setSession(session);
                    next.apply(null);
                } else {
                    res.status(401).message("unauthorized");
                }
            } catch (Exception e) {
                next.apply(e);
            }
        });

        get((Request req, Response res, Next next) -> {
            try {
                User user = req.getSession().getUser();
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode root = mapper.createObjectNode();
                root.put("userID", user.getUserID());
                root.put("fname", user.getFname());
                root.put("lname", user.getLname());
                root.put("role", user.getRole());
                String body = root.toString();
                res.status(200).json(body);
            }catch (IOException ex){
                next.apply(ex);
            }
        });
    }
}
