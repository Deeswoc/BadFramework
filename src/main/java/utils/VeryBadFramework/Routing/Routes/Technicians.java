package utils.VeryBadFramework.Routing.Routes;

import Data.Database;
import Model.Complaint;
import Model.Technician;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.VeryBadFramework.Next;
import utils.VeryBadFramework.Routing.Middleware.Utils;
import utils.VeryBadFramework.Routing.Router;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.io.IOException;

public class Technicians extends Router {
    public Technicians() {
        use(Utils.getSession);

        post("/complaints", (Request req, Response res, Next next) -> {
            try {
                JsonNode bodyNode = req.getBody().toJson();
                int technicianID = req.getSession().getUser().getUserID();
                Database db = Database.getDatabase();
                Session session = db.getSession();
                Transaction transaction = session.beginTransaction();
                Technician technician = session.get(Technician.class, technicianID);
                Complaint complaint = session.get(Complaint.class, bodyNode.get("id").asInt());
                technician.getAssigned().add(complaint);
                transaction.commit();

                res.status(201).end();
            } catch (IOException e) {
                next.apply(e);
            }
        });
    }
}
