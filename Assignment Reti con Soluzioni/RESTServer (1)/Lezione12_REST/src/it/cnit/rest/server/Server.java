package it.cnit.rest.server;

import it.cnit.rest.server.controller.exception.NotFoundExceptionMapper;
import it.cnit.rest.server.util.DemoFiller;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jaxb.internal.JaxbMessagingBinder;
import org.glassfish.jersey.jaxb.internal.JaxbParamConverterBinder;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
public static void main(String[] args){
	//Set up package to be explored looking for controllers / API
	ResourceConfig config = new ResourceConfig().packages("it.cnit.rest.server.controller");

	//Set up the logging feature to print out requests/responses
	config.register(new LoggingFeature(Logger.getLogger("Server"), Level.INFO, null, null));
	config.register(new JacksonFeature());
	config.register(new NotFoundExceptionMapper());
	config.register(new JaxbMessagingBinder());
	config.register(new JaxbParamConverterBinder());
	HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:9999"), config);
	if(args.length>0 && args[0].equals("demo"))
		DemoFiller.fillWithDemoData();
	try {
		httpServer.start();
	} catch (IOException e) {
		e.printStackTrace();
	}
}
}


