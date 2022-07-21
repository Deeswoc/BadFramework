package utils.VeryBadFramework.Routing.Routes;

import Data.Database;
import Model.Session;
import Model.User;
import com.fasterxml.jackson.databind.JsonNode;
import utils.EncryptedPassword;
import utils.VeryBadFramework.Next;
import utils.VeryBadFramework.Routing.Router;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.io.IOException;

public class Login extends Router {
    Database db;
    @Override
    protected void dispatch(Request req, Response res, Next done) {
        super.dispatch(req, res, done);
        System.out.println("Login Router is being ran");
    }
    public Login() {
        db = Database.getDatabase();

        post((Request req, Response res, Next next) -> {
            try {

                JsonNode body = req.getBody().toJson();
                String password = body.get("password").asText();
                int userID = body.get("id").asInt();

                User user = (User) db.getByID(User.class, userID);
                if (user == null) {
                    res.status(403).message("Incorrect username or password");
                    return;
                }

                if (EncryptedPassword.checkpw(password, user.getPwHash())) {

                    Session userSession = new Session(user);
                    String sid = userSession.getSid();
                    res.cookie("sid", userSession.getSid());
                    db.save(userSession);
                    res.status(200).message(sid);
                } else {
                    res.status(401).message("Incorrect username or password");
                }
            } catch (IOException e) {
                System.out.println("Something went bonkers, dude");
                next.apply(e);
            }
        });
    }


}
