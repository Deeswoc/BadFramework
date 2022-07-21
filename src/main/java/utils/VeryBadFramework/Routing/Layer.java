package utils.VeryBadFramework.Routing;

import utils.PathToRegex.Key;
import utils.PathToRegex.PathToRegex;
import utils.VeryBadFramework.Middleware;
import utils.VeryBadFramework.Next;
import utils.VeryBadHTTP.METHOD;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Layer implements Middleware {
    Route route;
    Pattern pattern;
    String path;
    boolean fast_star = false;
    List<Key> keys;
    boolean fast_slash = false;
    Map<String, String> params;
    Middleware handler;

    METHOD method;

    Layer() {
    }

    boolean match(String path) {
        Matcher m = null;
        String matchedPath = null;

        if (path != null) {
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
            this.params = null;
            this.path = null;
            return false;
        }

        for (int i = 1; i < m.groupCount() + 1; i++) {
            Key key = keys.get(i - 1);
            String property = key.getName();
            String value = decode_param(m.group(i));
            if (value != null || params.containsKey(property))
                params.put(property, value);
        }

        this.path = matchedPath;
        return true;
    }

    String decode_param(String param) {
        return param;
    }

    Layer(String path, PathToRegex.Config config, Middleware fn) {
        if (path.equals("/")) {
            fast_slash = true;
        }
        if (path.equals("*")) {
            fast_star = true;
        }
        this.keys = new ArrayList<>();
        this.params = new HashMap<>();
        if (config == null)
            config = new PathToRegex.Config();
        pattern = PathToRegex.parse(path, keys, config);
        this.handler = fn;
    }


    @Override
    public void run(Request req, Response res, Next next) {
        if (match(req.getUrl())) {
            handler.run(req, res, next);
        }
        next.apply(null);
    }

    public String getPath() {
        return path;
    }

    public METHOD getMethod() {
        return method;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
