package utils.VeryBadFramework.Routing;

import utils.VeryBadFramework.Middleware;
import utils.VeryBadFramework.Next;
import utils.VeryBadHTTP.METHOD;
import utils.VeryBadHTTP.Request;
import utils.VeryBadHTTP.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Route implements Middleware {
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

    private static class IntHolder {
        int idx;

        IntHolder(int initial) {
            idx = initial;
        }

        public int increment() {
            return idx++;
        }
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
                    return null;
                }


                if (idx.idx >= stack.size()) {
                    done.apply(null);
                    return null;
                }

                final LocalStack ls = new LocalStack();


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

    public Route delete(Middleware... handles) {
        for (Middleware handle : handles) {
            Layer layer = new Layer("/", handle);
            layer.method = METHOD.DELETE;
            this.methods.put(METHOD.DELETE, true);
            this.stack.add(layer);
        }
        return this;
    }

    public Route put(Middleware... handles) {
        for (Middleware handle : handles) {
            Layer layer = new Layer("/", handle);
            layer.method = METHOD.PUT;
            this.methods.put(METHOD.PUT, true);
            this.stack.add(layer);
        }
        return this;
    }
}
