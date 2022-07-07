package utils.VeryBadFramework.Routing.Routes;

import Data.Database;
import Model.Customer;
import com.fasterxml.jackson.databind.JsonNode;
import utils.EncryptedPassword;
import utils.VeryBadFramework.Middleware;
import utils.VeryBadFramework.Next;
import utils.VeryBadFramework.Routing.Router;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class Login extends Router {
    Database db;

    public Login() {
        db = Database.getDatabase();

        post((Request req, Response res, Next next) -> {
            try {

                JsonNode body = req.getBody().toJson();
                String password = body.get("password").asText();
                int userID = body.get("id").asInt();

                Customer customer = (Customer) db.getByID(Customer.class, userID);
                if (customer == null) {
                    res.status(403).message("Incorrect username or password");
                }

                if (EncryptedPassword.checkpw(password, customer.getPwHash())) {
                    UUID session = UUID.randomUUID();
                    res.cookie("sid", session.toString());
                    res.status(200).message(session.toString());
                } else {
                    res.status(403).message("Incorrect username or password");
                }
                next.apply(null);
            } catch (IOException e) {
                System.out.println("Something went bonkers, dude");
            }
        });
    }


}
