package utils.VeryBadFramework.Routing.Routes;

import Data.Database;
import Model.Complaint;
import Model.Customer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.VeryBadFramework.Next;
import utils.VeryBadFramework.Routing.Middleware.Utils;
import utils.VeryBadFramework.Routing.Router;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.List;

public class Complaints extends Router {
    public Complaints() {
        use(Utils.getSession);

        get((Request req, Response res, Next next) -> {
            Database db = Database.getDatabase();
            int customerID = req.getSession().getUser().getUserID();
            Session session = db.getSession();
            Transaction ts = session.beginTransaction();
            Customer customer = session.get(Customer.class, customerID);
            List<Complaint> complaintList = customer.getComplaints();
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode complaints = mapper.createArrayNode();
            for (Complaint complaint : complaintList) {
                ObjectNode complaintNode = mapper.createObjectNode();
                complaintNode.put("id", complaint.getId());
                complaintNode.put("category", complaint.getCategory());
                complaintNode.put("issue", complaint.getIssue());
                complaintNode.put("details", complaint.getDetails());
                complaints.add(complaintNode);
            }
            ts.commit();
            try {
                res.status(200).json(complaints);
            } catch (IOException e) {
                next.apply(e);
            }
        });

        post("/:id/:assign", (Request req, Response res, Next next) ->{
            try {
                res.status(200).end();
            } catch (IOException e) {
                next.apply(e);
            }
        });


        post((Request req, Response res, Next next) -> {
            try {
                Database db = Database.getDatabase();
                int customerID = req.getSession().getUser().getUserID();
                Customer customer = (Customer) db.getByID(Customer.class, customerID);
                JsonNode body = req.getBody().toJson();
                String category = body.get("category").asText();
                String issue = body.get("issue").asText();
                String details = body.get("details").asText();

                Complaint complaint = new Complaint();
                complaint.setCategory(category);
                complaint.setIssue(issue);
                complaint.setDetails(details);
                complaint.setCustomer(customer);
                Database.getDatabase().save(complaint);
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode complaintNode = mapper.createObjectNode();
                complaintNode.put("id", complaint.getId());
                complaintNode.put("issue", issue);
                complaintNode.put("details", details);
                complaintNode.put("category", category);
                res.status(201).json(complaintNode);
            } catch (IOException ex) {
                next.apply(ex);
            }
        });


        delete("/:id", (Request req, Response res, Next next) -> {
            String id = req.getParams().get("id");
            Database db = Database.getDatabase();
            Session session = db.getSession();

            Transaction transaction = session.beginTransaction();
            try {
                Complaint complaint = session.get(Complaint.class, Integer.parseInt(id));
                if(complaint == null){
                    res.status(404).message("Resource not found");
                    return;
                }
                int complaintAuthor = complaint.getCustomer().getUserID();
                int userId = req.getSession().getUser().getUserID();
                if (complaintAuthor == userId) {
                    session.delete(complaint);
                    res.status(200).end();
                } else {
                    res.status(401).end();
                }
            } catch (IOException e) {
                next.apply(e);
            } finally {
                transaction.commit();
            }
        });
    }
}
