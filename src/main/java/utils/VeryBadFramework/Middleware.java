package utils.VeryBadFramework;

import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.util.function.Consumer;

public interface Middleware{

    void run(Request req, Response res, Next next) ;
}
