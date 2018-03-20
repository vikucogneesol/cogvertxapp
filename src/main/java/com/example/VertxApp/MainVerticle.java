package com.example.VertxApp;


import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


public class MainVerticle extends AbstractVerticle {


    HttpServer httpServer;

    Router router;

    HttpClient httpClient;

    HandlebarsTemplateEngine engine;

    SQLConnection connection;

    String encodedImage = "";


    @Override

    public void start() throws Exception {


        httpServer = vertx.createHttpServer();

        router = Router.router(vertx);

        httpClient = vertx.createHttpClient();

        engine = HandlebarsTemplateEngine.create();

        HttpServerRequest globalRequest;

        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));


        router.route("/gagan/garg").handler(routingContext -> {


            HttpServerResponse serverResponse = routingContext.response();


            final String url = "****************";

            httpClient.getAbs(url, response -> {


                if (response.statusCode() != 200) {

                    System.err.println("fail");

                } else {


                    response.bodyHandler(res1 -> {

                        serverResponse.putHeader("content-type", "application/json").end(res1);

                    });


                    engine.render(routingContext, "templates/index.hbs", res -> {

                        if (res.succeeded()) {

                            serverResponse.setChunked(true);

                            serverResponse.write(res.result());

                            routingContext.put("body", response.bodyHandler(res1 -> {

                                serverResponse.putHeader("content-type", "application/json").end(res1);

                            }));

                        } else {

                            routingContext.fail(res.cause());

                        }

                    });

                }


            }).end();


        });


        router.get("/template/handler").handler(ctx -> {


            engine.render(ctx, "templates/index.hbs", res -> {

                if (res.succeeded()) {

                    ctx.response().end(res.result());

                } else {

                    ctx.fail(res.cause());

                }

            });


        });


        router.get("/template/submit_qaform").handler(ctx -> {


            engine.render(ctx, "templates/submit_qa.hbs", res -> {

                if (res.succeeded()) {

                    ctx.response().end(res.result());

                } else {

                    ctx.fail(res.cause());

                }

            });


        });


        router.post("/abc").handler(ctx -> {


            HttpServerRequest hr = ctx.request();

            String name = hr.params().get("firstname");

            String phone = hr.params().get("phone");

            String email = hr.params().get("email");

            String question = hr.params().get("question");

            encodedImage = getEncoded(ctx);

            saveInDatabase(ctx,ctx.response(), name, phone, email,

                question, encodedImage);

        });


        router.route("/mongo").handler(requestHandler->{
            engine.render(requestHandler,"templates/mongoform.hbs",handler->{
                if(handler.succeeded()) {
                    requestHandler.response().end(handler.result());
                }
                else{
                    requestHandler.fail(handler.cause());
                }
            });
        });


        router.post("/mongoget").handler(routingContext->{
            JsonObject config=new JsonObject();
            config.put( "host","127.0.0.1").put("port", 27017).put("db_name","mongo");
            JsonObject data=new JsonObject();
            data.put("name",routingContext.request().getFormAttribute("name"));
            data.put("phone",routingContext.request().getFormAttribute("phone"));
            data.put("email",routingContext.request().getFormAttribute("email"));
            data.put("question",routingContext.request().getFormAttribute("question"));
            data.put("encodedImage",getEncoded(routingContext));

            MongoClient client = MongoClient.createShared(vertx, config);

            client.insert("users",data,handler->{
                if(handler.succeeded()){
                    client.find("users",new JsonObject(),event -> {
                        routingContext.put("data",event.result());
                        engine.render(routingContext,"templates/mongofetch",hand->{
                            if(hand.succeeded()){
                                routingContext.response().end(hand.result());
                            }else{
                                routingContext.fail(hand.cause());
                            }
                        });
                    });
                }
            });
        });

        router.post("/update").handler(rtx->{
            String id=rtx.request().params().get("_id");
            JsonObject config=new JsonObject();
            config.put( "host","127.0.0.1").put("port", 27017).put("db_name","mongo");
            System.out.println(id);
            JsonObject update=new JsonObject().put("$set",new JsonObject()
                .put("email",rtx.request().params().get("email"))
                .put("phone",rtx.request().params().get("phone"))
                .put("name",rtx.request().params().get("name"))
                .put("question",rtx.request().params().get("question")));

            MongoClient client = MongoClient.createShared(vertx, config);
            client.updateCollection("users",new JsonObject().put("_id",rtx.request().getParam("_id")),update,hnd->{
                if(hnd.succeeded()){
                    System.out.print("Success");
                }else{
                    System.out.print(hnd.cause());
                }

            });
            rtx.response().end();

        });
        router.get("/mongofetch").handler(routingContext->{
            JsonObject config=new JsonObject();
            config.put( "host","127.0.0.1").put("port", 27017).put("db_name","mongo");

            MongoClient client = MongoClient.createShared(vertx, config);

            client.find("users",new JsonObject(),event -> {
                routingContext.put("data",event.result());
                engine.render(routingContext,"templates/mongofetch",hand->{
                    if(hand.succeeded()){
                        routingContext.response().end(hand.result());
                    }else{
                        routingContext.fail(hand.cause());
                    }
                });
            });
        });

        router.post("/deleteMongo").handler(routingContext->{
            JsonObject config=new JsonObject();
            config.put( "host","127.0.0.1").put("port", 27017).put("db_name","mongo");

            System.out.print(routingContext.request().params().get("id"));
            MongoClient client = MongoClient.createShared(vertx, config);

            client.findOneAndDelete("users",new JsonObject().put("_id",routingContext.request().params().get("id")),handler->{
                routingContext.response().end();
            });
        });

        httpServer.requestHandler(router::accept).listen(5200);


    }

    public String getEncoded(RoutingContext rtx){
        for (FileUpload fu : rtx.fileUploads()) {


            try {

                File f = new File(fu.uploadedFileName());

                BufferedImage image = ImageIO.read(f);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                ImageIO.write(image, "png", baos);

                byte[] res = baos.toByteArray();

                encodedImage = Base64.encode(baos.toByteArray());

            } catch (IOException e) {

                e.printStackTrace();

            }

        }
        return encodedImage;
    }
    private void fileUpload(RoutingContext rc, HttpServerResponse response) {


        response.putHeader("Content-Type", "text/plain");

        response.setChunked(true);


        for (FileUpload f : rc.fileUploads()) {

            //  System.out.println("f");

            rc.response().write("Filename: " + f.fileName());

            rc.response().write("\n");

            rc.response().write("Size: " + f.size());

        }


        response.end();


    }


    private void saveInDatabase(RoutingContext ctx,HttpServerResponse response, String name, String phone, String email, String

        question, String imageFile) {

        JsonObject mySQLClientConfig = new JsonObject()

            .put("host", "*")

            .put("username", "*")

            .put("password", "*")

            .put("database", "*")

            .put("charset", "*");

        SQLClient mySQLClient = MySQLClient.createShared(vertx,mySQLClientConfig);


        mySQLClient.getConnection(res -> {

            if (res.succeeded()) {


                connection = res.result();


                connection.execute("insert into tbl_vertx_demo (name,email,phone,question,image) values ('" + name + "', '" + email + "','" + phone + "','" + question + "','" + imageFile + "')", r -> {

                    if (r.succeeded()) {

                        connection.query("select name,email,phone,question,image from tbl_vertx_demo", show -> {


                            ResultSet rs = new ResultSet();

                            rs = show.result();

                            ctx.put("data",rs.getResults());

                            engine.render(ctx, "templates/index.hbs", rad -> {

                                if (rad.succeeded()) {

                                    ctx.response().end(rad.result());

                                } else {

                                    ctx.fail(rad.cause());

                                }

                            });

                        });

                    } else {

                        response.end();

                    }

                });


            } else {


            }

        });


    }
}
