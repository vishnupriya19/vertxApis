package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.core.Future;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.json.JsonArray;

import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Executable;


public class MainVerticle extends AbstractVerticle {

  MongoClient mongoClient;

  @Override
  public void start() throws Exception {
    /*vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });*/

     /*
     Router router = Router.router(vertx);

     //At every path and HTTP method for all incoming requests mount the handler
     router.route().handler(context -> {
       // Find the address of the request by using request(), connection() and remoteAddress() method
       String address = context.request().connection().remoteAddress().toString();

       // Find the "name" query parameter from the queryParams() by using get() method
       MultiMap queryParams = context.queryParams();
       String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
       // write information in JSON format as a response

       final JsonObject mongoConfig = new JsonObject()
         .put("connection_string", DB_URI)
         .put("db_name", DB_NAME);



      JsonObject query = new JsonObject()
        .put("author", "J. R. R. Tolkien");
      mongoClient.find("books", query, res -> {
        if (res.succeeded()) {
          for (JsonObject json : res.result()) {
            System.out.println(json.encodePrettily());
          }
        } else {
          res.cause().printStackTrace();
        }
      });
      */
      /*
      context.json(
        new JsonObject()
          .put("name", name)
          .put("address", address)
          .put("message", "Hello " + name + " connected from " + address)
      );
     });


       // use createHttpServer() method to create Http server
       vertx.createHttpServer()
         // use router to handle each upcoming request
         .requestHandler(router)
         // use listen() method to start listening
         .listen(8888)
         // using actualPort() method of server to print port
         .onSuccess(server ->
           System.out.println(
             "HTTP server started on port " + server.actualPort()
           )
         );

     });
     */

    String uri = "mongodb://localhost:27017";

    String db = "admin";

    JsonObject mongoconfig = new JsonObject()
      .put("connection_string", uri)
      .put("db_name", db);

    mongoClient = MongoClient.createShared(vertx, mongoconfig);
    Router router = Router.router(vertx);

    router.route().handler(io.vertx.ext.web.handler.CorsHandler.create("*")
      .allowedMethod(io.vertx.core.http.HttpMethod.GET)
      .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
      .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
      .allowedHeader("Access-Control-Request-Method")
      .allowedHeader("Access-Control-Allow-Credentials")
      .allowedHeader("Access-Control-Allow-Origin")
      .allowedHeader("Access-Control-Allow-Headers")
      .allowedHeader("Content-Type"));

    router.route().handler(BodyHandler.create());
    router.get("/details").handler(this::handleDetails); //Fetch all products (to read)
    router.put("/details").handler(this::handleUpdateDetails);
    // vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    vertx.createHttpServer()
      // use router to handle each upcoming request
      .requestHandler(router)
      // use listen() method to start listening
      .listen(8888)
      // using actualPort() method of server to print port
      .onSuccess(server ->
        System.out.println(
          "HTTP server started on port " + server.actualPort()
        )
      );
   }

  private void handleDetails(RoutingContext routingContext) {
    JsonArray arr = new JsonArray();
    JsonObject emp = new JsonObject();
    mongoClient.find("cv", emp, res -> {
      if (res.succeeded()) {
        for (JsonObject json : res.result()) {
          arr.add(json);
        }
        routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());
      } else {
        res.cause().printStackTrace();
      }
    });
  }

  private void handleUpdateDetails(RoutingContext routingContext) {
      //String productID = routingContext.request().getParam("detail");
      HttpServerResponse response = routingContext.response();
      JsonObject update = new JsonObject().put("$set", routingContext.getBodyAsJson());
      System.out.println(update);
      if (update == null) {
        sendError(400, response);
      } else {
        JsonObject query = new JsonObject().put("name", "nodeDetails");
        //JsonObject update1 = new JsonObject().put("detail", update);
          //routingContext.getBodyAsJson());
        JsonObject update1 = new JsonObject().put("$set", routingContext.getBodyAsJson());
        mongoClient.updateCollection("cv", query, update1, res -> {
          if (res.succeeded()) {
            System.out.println("Updated Details");
          } else {
            res.cause().printStackTrace();
          }
        });
        response.end("Details Updated Successfully");
      }
  }

  private void sendError(int statusCode, HttpServerResponse response) {
    response.setStatusCode(statusCode).end();
  }

}
