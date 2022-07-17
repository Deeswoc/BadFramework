package utils.VeryBadFramework.Routing;

import utils.VeryBadFramework.Middleware;
import utils.VeryBadFramework.Next;
import utils.VeryBadHTTP.METHOD;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Layer implements Middleware {
    Route route;
    Pattern pattern;
    String path;
    boolean fast_star = false;
    boolean fast_slash = false;

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    Middleware handler;
    METHOD method;

    public String getPath() {
        return path;
    }

    public METHOD getMethod() {
        return method;
    }

    boolean match(String path) {
        Matcher m;
        String matchedPath = null;
        if (pattern != null) {
            m = pattern.matcher(path);
            if (fast_slash) {
                this.path = "";
                return true;
            }
            if (fast_star) {
                this.path = path;
                return true;
            }
            if (m.find()) {
                matchedPath = m.group(0);
            }
        }
        if (matchedPath == null) {
            this.path = null;
            return false;
        }

        this.path = matchedPath;
        return true;
    }

    Layer() {
    }

    Layer(String path, Middleware handler) {
        this(path, null, handler);
    }

    Layer(String path, METHOD method, Middleware handler) {
        if (path.equals("/")) {
            fast_slash = true;
        }
        if (path.equals("*")) {
            fast_star = true;
        }

        StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '/') {
                sb.append("\\/");
            } else {
                sb.append(c);
            }
        }
        pattern = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
        String[] paths = path.split("/");
        this.handler = handler;
        this.method = method;
    }

    Layer(Middleware handler) {
        this.path = "/";
        this.fast_slash = true;
        this.handler = handler;
    }

    Layer(Middleware handler, METHOD method) {
        this.path = "/";
        this.fast_slash = true;
        this.handler = handler;
        this.method = method;
    }


    @Override
    public void run(Request req, Response res, Next next) {
        if (match(req.getUrl())) {
            handler.run(req, res, next);
        }
        next.apply(null);
    }
}
