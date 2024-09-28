package it.cnit.rest.server.controller;

import it.cnit.rest.server.model.Forum;
import it.cnit.rest.server.model.Topic;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/topic")
public class TopicController {
	private Forum forum = Forum.getForum();

	@GET
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getAllTopics() {
		Collection<Topic> topics = forum.getallTopics();
		//Wrapping into GenericEntity is needed by JAXB to correctly serialize high level types
		//If you want to use JSON (jackson) only you could have written return Response.ok(subList).build();
		return Response.ok(new GenericEntity<Collection<Topic>>(topics){}).build();
	}

	@GET
	@Path("{topic}")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getById(@PathParam("topic") Long topic_id) {
		Topic topic = forum.getTopic(topic_id);
		if(topic==null)
			return Response.status(Response.Status.NOT_FOUND).build();
		return Response.ok(topic).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response createTopic(Topic newTopic){
		Topic topic = forum.newTopic(newTopic);
		return Response.status(Response.Status.CREATED).entity(topic).build();
	}

	@DELETE
	@Path("{topic}")
	public Response deleteTopic(@PathParam("topic") Long topic){
		if(forum.removeTopic(topic)==null)
			Response.status(Response.Status.NOT_FOUND).build();
		return Response.status(Response.Status.NO_CONTENT).build();
	}
}
