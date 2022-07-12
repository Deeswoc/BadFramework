package utils.VeryBadFramework.Routing;

import utils.VeryBadFramework.Middleware;
import utils.VeryBadFramework.Next;
import utils.VeryBadHTTP.METHOD;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.io.IOException;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Router implements Middleware {
    Map<METHOD, Middleware> handler;
    Request req;
    Response res;
    Pattern pattern;
    String parentUrl;
    Boolean slashAdded = false;
    private Next routeNext;

    public Next getRouteNext() {
        return routeNext;
    }

    public void setRouteNext(Next routeNext) {
        this.routeNext = routeNext;
    }

    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    List<Layer> stack;


    String path;

    public Router() {
        this.handler = new HashMap<>();
        stack = new ArrayList<>();
    }

    public Router(String path) {

    }

    public void use(String path, Middleware... middlewares) {
        for (Middleware m :
                middlewares) {
            stack.add(new Layer(path, m));
        }
    }

    public void use(Middleware... middlewares) {
        for (Middleware m :
                middlewares) {
            stack.add(new Layer("/", null, m));
        }
    }

    public void get(Middleware... middlewares) {
        Route route = route("/");
        route.get(middlewares);
    }


    public void post(Middleware... middlewares) {
        Route route = route("/");
        route.post(middlewares);
    }

    public void delete(Middleware... middlewares) {

    }

    public void put(Middleware... middlewares) {

    }

    public void run(Request req, Response res, Next next) {
        Next restore = (error) -> {
            try {
                if (error != null) {
                    res.status(500).message("Something went wrong");
                    error.printStackTrace();
                } else {
                    res.status(404).message("Invalid Route");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        };
        dispatch(req, res, restore);

    }


    public void get(String path, Middleware... middlewares) {
        Route route = route(path);
        route.get(middlewares);
    }

    public void post(String path, Middleware... middlewares) {
        Route route = route(path);
        route.post(middlewares);
    }

    public void put(String path, Middleware... middlewares) {

    }

    public void delete(String path, Middleware... middlewares) {

    }


    protected void dispatch(Request req, Response res, Next done) {
        IntHolder idx = new IntHolder(0);
        StringBuilder removed = new StringBuilder();
        StringBuilder parentUrl;

        if (req.getBaseUrl() != null)
            parentUrl = new StringBuilder(req.getBaseUrl().toString());
        else
            parentUrl = new StringBuilder();
        int sync = 0;
        if (stack.size() == 0) {
            done.apply(null);
            return;
        }
        class SomeNext implements Next {
            Next n;

            @Override
            public Void apply(Exception e) {
                n.apply(e);
                return null;
            }

            public void setN(Next n) {
                this.n = n;
            }
        }

        SomeNext l = new SomeNext();
        this.routeNext = l;

        class Trim_Prefix {
            void apply(Layer layer, Exception e, String layerPath, String path, Next next) {
                if (layerPath != null && !layerPath.isEmpty()) {
                    if (!layerPath.equals(path.substring(0, layerPath.length()))) {
                        next.apply(e);
                        return;
                    }

                    char c = -0;
                    if (path.length() > layerPath.length())
                        c = path.charAt(layerPath.length());

                    if (c != 0 && c != '/' && c != '.') {
                        next.apply(e);
                        return;
                    }

                    removed.append(layerPath);
                    req.setUrl(req.getUrl().substring(removed.length()));

                    if (!req.getUrl().isEmpty() && req.getUrl().charAt(0) != '/') {
                        req.setUrl("/" + req.getUrl());
                        slashAdded = true;
                    }


                    req.setBaseUrl(new StringBuilder(parentUrl.toString() + (removed.charAt(removed.length() - 1) == '/'
                            ? removed.substring(0, removed.length() - 1)
                            : removed)));
                }
                if (e != null) {
                    l.apply(e);
                } else {
                    layer.handler.run(req, res, next);
                }
            }
        }

        class NextHandler implements Next {

            @Override
            public Void apply(Exception e) {
                class LocalStack {
                    boolean match = false;
                    Layer layer;
                    Route route;
                }

                if (e != null) {
                    done.apply(e);
                }

                if (removed.length() > 0) {
                    req.setBaseUrl(parentUrl);
                    req.setUrl(removed + req.getUrl().substring(removed.length()));
                }

                if (idx.idx >= stack.size()) {
                    done.apply(null);
                    return null;
                }

                final LocalStack ls = new LocalStack();

                if (removed.length() != 0) {
                    req.setBaseUrl(parentUrl);
                    req.setUrl(removed + req.getUrl().substring(1));
                }
                while (!ls.match && idx.idx < stack.size()) {

                    if (e != null) {
                        done.apply(e);
                    }


                    ls.layer = stack.get(idx.increment());
                    ls.match = ls.layer.match(req.getUrl());
                    ls.route = ls.layer.getRoute();

                    if (!ls.match) {
                        continue;
                    }

                    if (ls.route != null) {
                        continue;
                    }
                }


                if (ls.layer.getMethod() != null && ls.layer.getMethod() != req.getMethod()) {
                    l.apply(e);
                    return null;
                }

                if (!ls.match) {
                    l.apply(null);
                    return null;
                }

                if (ls.route != null) {
                    req.setRoute(ls.route);
                }

                String layerPath = ls.layer.path;
                processParams(ls.layer, null, req, res, (Exception ex) -> {
                    if (ex != null) {
                        l.apply(ex);
                    } else if (ls.layer.route != null) {
                        ls.layer.handler.run(req, res, l);
                    } else {
                        Trim_Prefix trim_prefix = new Trim_Prefix();
                        String url = req.getUrl();
                        trim_prefix.apply(ls.layer, null, layerPath, url, l);
                    }
                    return null;
                });
                return null;
            }
        }


        NextHandler next = new NextHandler();
        l.setN(next);
        req.setBaseUrl(parentUrl);
        if (req.getOriginalUrl() == null || req.getOriginalUrl().isEmpty()) {
            req.setOriginalUrl(new StringBuilder(req.getUrl()));
        }
        try {
            l.apply(null);
        } catch (Exception e) {
            l.apply(e);
        }
    }

    private void processParams(Layer layer, Map<String, String> called, Request req, Response res, Next done) {
        done.apply(null);
    }


    private static class IntHolder {
        int idx;

        IntHolder(int initial) {
            idx = initial;
        }

        public int increment() {
            return idx++;
        }
    }

    private static class Route implements Middleware {
        Map<METHOD, Boolean> methods;
        List<Layer> stack;
        String path;

        Route(String path) {
            this.path = path;
            this.stack = new ArrayList<>();
            this.methods = new HashMap<>();
        }

        private boolean handles_method(METHOD method) {
            Boolean handles = this.methods.get(method);
            return handles != null && handles;
        }

        @Override
        public void run(Request req, Response res, Next next) {
            dispatch(req, res, next);
        }

        private void dispatch(Request req, Response res, Next done) {
            IntHolder idx = new IntHolder(0);

            int sync = 0;
            if (stack.size() == 0) {
                done.apply(null);
                return;
            }
            METHOD method = req.getMethod();

            class SomeNext implements Next {
                Next n;

                @Override
                public Void apply(Exception e) {
                    n.apply(e);
                    return null;
                }

                public void setN(Next n) {
                    this.n = n;
                }
            }

            SomeNext l = new SomeNext();


            class NextHandler implements Next {

                @Override
                public Void apply(Exception e) {
                    class LocalStack {
                        boolean match = false;
                        Layer layer;
                        Route route;
                    }

                    if (e != null) {
                        done.apply(e);
                    }


                    if (idx.idx >= stack.size()) {
                        done.apply(null);
                        return null;
                    }

                    final LocalStack ls = new LocalStack();


                    while (!ls.match && idx.idx < stack.size()) {

                        if (e != null) {
                            done.apply(e);
                        }


                        ls.layer = stack.get(idx.increment());
                        ls.match = ls.layer.match(req.getUrl());
                        ls.route = ls.layer.getRoute();

                        if (!ls.match) {
                            continue;
                        }

                        if (ls.route != null) {
                            continue;
                        }
                    }


                    if (ls.layer.getMethod() != null && ls.layer.getMethod() != req.getMethod()) {
                        l.apply(e);
                        return null;
                    }

                    if (!ls.match) {
                        l.apply(null);
                        return null;
                    }

                    if (ls.route != null) {
                        req.setRoute(ls.route);
                    }

                    if (ls.layer.method != null && ls.layer.method != method) {
                        l.apply(null);
                    }

                    ls.layer.handler.run(req, res, l);
                    return null;
                }
            }


            NextHandler next = new NextHandler();
            l.setN(next);
            try {
                l.apply(null);
            } catch (Exception e) {
                l.apply(e);
            }
        }

        public Route get(Middleware... handles) {
            for (Middleware handle : handles) {
                Layer layer = new Layer("/", handle);
                layer.method = METHOD.GET;
                this.methods.put(METHOD.GET, true);
                this.stack.add(layer);
            }
            return this;
        }

        public Route post(Middleware... handles) {
            for (Middleware handle : handles) {
                Layer layer = new Layer("/", handle);
                layer.method = METHOD.POST;
                this.methods.put(METHOD.POST, true);
                this.stack.add(layer);
            }
            return this;
        }
    }

    private Route route(String path) {
        Route route = new Route(path);

        Layer layer = new Layer(path, route);

        layer.route = route;
        stack.add(layer);
        return route;
    }

    private static class Layer implements Middleware {
        Router.Route route;
        Pattern pattern;
        String path;
        //        Router router;
        boolean fast_star = false;
        boolean fast_slash = false;

        public Router.Route getRoute() {
            return route;
        }

        public void setRoute(Router.Route route) {
            this.route = route;
        }

        //        public Router getRouter() {
//            return router;
//        }
//
//        public void setRouter(Router router) {
//            this.router = router;
//        }

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

        static class Route {
            String path;
            List<Layer> stack;
            List<METHOD> methods;
        }

        @Override
        public void run(Request req, Response res, Next next) {
            if (match(req.getUrl())) {
                handler.run(req, res, next);
            }
            next.apply(null);
        }
    }
}
