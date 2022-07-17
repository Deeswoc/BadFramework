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
        Route route = route("/");
        route.delete(middlewares);
    }

    public void put(Middleware... middlewares) {
        Route route = route("/");
        route.put(middlewares);
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
                    return null;
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


    private Route route(String path) {
        Route route = new Route(path);

        Layer layer = new Layer(path, route);

        layer.route = route;
        stack.add(layer);
        return route;
    }
}
