package com.example.VertxApp;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class WebClass {

    HttpServerResponse response;

    public void fun(RoutingContext routingContext) {
        response = routingContext.response();
        response.end("Hello Gagan");
    }

}
