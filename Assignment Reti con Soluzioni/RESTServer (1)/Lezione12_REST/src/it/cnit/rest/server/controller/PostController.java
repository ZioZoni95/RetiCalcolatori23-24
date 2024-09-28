package it.cnit.rest.server.controller;

import it.cnit.rest.server.model.Post;
import it.cnit.rest.server.model.Forum;
import it.cnit.rest.server.model.Topic;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Path("/topic/{topic}/post")
public class PostController {
	Forum forum = Forum.getForum();


	@GET
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getFilteredPost(@PathParam("topic") Long topic_id, @QueryParam("author") String author) {
		Collection<Post> posts = forum.getTopic(topic_id).getPosts();
		if(author==null)
			return Response.ok(new GenericEntity<Collection<Post>>(posts){}).build();
		List<Post> filteredPosts = posts.stream().filter(c -> c.getAuthor().equals(author)).collect(Collectors.toList());
		return Response.ok(new GenericEntity<Collection<Post>>(filteredPosts){}).build();
	}

	@GET
	@Path("{post}")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getById(@PathParam("topic") Long topic_id, @PathParam("post") Long post_id) {
		Post post = forum.getTopic(topic_id).getPost(post_id);
		if(post==null)
			return Response.status(Response.Status.NOT_FOUND).build();
		return Response.ok(post).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response createPost(@PathParam("topic") Long topic_id, Post newPost){
		Topic topic = forum.getTopic(topic_id);
		Post post = topic.newPost(newPost);
		return Response.status(Response.Status.CREATED).entity(post).build();

	}

	@PUT
	@Path("{post}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response editPost(@PathParam("topic") Long topic_id, @PathParam("post") Long post_id, Post newPost){
		Topic topic = forum.getTopic(topic_id);
		if(topic.editPost(post_id, newPost) == null)
			return Response.status(Response.Status.NOT_FOUND).build();
		return Response.ok(newPost).build();
	}

	@DELETE
	@Path("{post}")
	public Response deletePost(@PathParam("topic") Long topic_id, @PathParam("post") Long post_id){
		Topic topic = forum.getTopic(topic_id);
		if(topic.removePost(post_id)==null)
			return Response.status(Response.Status.NOT_FOUND).build();
		return Response.noContent().build();
	}

	@DELETE
	public Response deleteAllPosts(@PathParam("topic") Long topic_id){
		Topic topic = forum.getTopic(topic_id);
		topic.removeAllPosts();
		return Response.noContent().build();
	}

}
