package utils.VeryBadFramework;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import utils.VeryBadFramework.Routing.Index;
import utils.VeryBadFramework.Routing.Routes.*;
import utils.VeryBadFramework.Routing.Router;
import utils.VeryBadHTTP.BadRequestException;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BadFramework {
    private HttpServer server;
    private Router rootRouter;

    public BadFramework() {
        try {
            String path = "/";
            server = HttpServer.create();
            System.out.println("Starting server");
            rootRouter = new Index();
            rootRouter.use("/me", new Me());
            rootRouter.use("/login", new Login());
            rootRouter.use("/users", new Users());
            rootRouter.use("/complaints", new Complaints());
            rootRouter.use("/register", new Register());
            server.createContext(path, (HttpExchange t) -> {
                Request req = new Request(t);
                Response res = new Response(t);
                rootRouter.run(req, res, null);
            });
            server.setExecutor(null);
        } catch (BadRequestException e) {
            e.printStackTrace();
            System.out.println("Bad Request");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen(int port) {
        try {
            server.bind(new InetSocketAddress(port), 0);
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        BadFramework bf = new BadFramework();
        bf.listen(3000);
    }
}
