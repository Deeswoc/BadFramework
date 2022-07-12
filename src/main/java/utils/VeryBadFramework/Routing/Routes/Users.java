package utils.VeryBadFramework.Routing.Routes;

import Data.Database;
import Model.User;
import com.fasterxml.jackson.databind.JsonNode;
import utils.VeryBadFramework.Next;
import utils.VeryBadFramework.Routing.Router;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.io.IOException;

public class Users extends Router {
    public Users(){
        post((Request req, Response res, Next next)->{
            try {
                JsonNode body = req.getBody().toJson();
                String fname = body.get("fname").asText();
                String lname = body.get("lname").asText();
                String role = body.get("role").asText();
                String password = body.get("password").asText();
                User user = new User();
                user.setFname(fname);
                user.setLname(lname);
                user.setRole(role);
                user.setPwHash(password);
                Database db = Database.getDatabase();
                db.save(user);
                res.status(200).message("User Created");
            } catch (IOException e) {
                next.apply(e);
            }
        });
    }

}
