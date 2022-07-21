package utils.VeryBadFramework.Routing.Routes;

import Data.Database;
import Model.Customer;
import Model.Technician;
import com.fasterxml.jackson.databind.JsonNode;
import utils.VeryBadFramework.Next;
import utils.VeryBadFramework.Routing.Middleware.Utils;
import utils.VeryBadFramework.Routing.Router;
import utils.VeryBadHTTP.BodyParser.Json;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.io.IOException;

public class Register extends Router {
    public Register() {
        post((Request req, Response res, Next next) -> {
            try {
                Database db = Database.getDatabase();
                JsonNode body = req.getBody().toJson();
                String user = body.get("role").asText();
                switch (user.toUpperCase()) {
                    case "CUSTOMER" -> {
                        Customer customer = Json.fromJson(body, Customer.class);
                        customer.setPwHash(body.get("password").asText());
                        db.save(customer);
                        res.status(201).json(customer);
                    }
                    case "TECHNICIAN" ->{
                        Technician technician = Json.fromJson(body, Technician.class);
                        technician.setPwHash(body.get("password").asText());
                        db.save(technician);
                        res.status(201).json(technician);
                    }
                }
            } catch (IOException e) {
                next.apply(e);
            }

        });
    }
}
