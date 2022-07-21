package utils.VeryBadFramework.Routing.Middleware;

import Data.Database;
import Model.Session;
import utils.VeryBadFramework.Middleware;
import utils.VeryBadFramework.Next;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.util.Map;

public class Utils {
    public static Middleware getSession = (Request req, Response res, Next next) -> {
        System.out.println("Getting Session");
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
    };
}
