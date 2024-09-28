package it.cnit.rest.server.controller;

import it.cnit.rest.server.model.Comment;
import it.cnit.rest.server.model.Post;
import it.cnit.rest.server.model.Forum;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Path("/topic/{topic}/post/{post}/comment")
public class CommentController {
	private Forum forum = Forum.getForum();

	@GET
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getAllComments(@PathParam("topic") Long topic_id, @PathParam("post") Long post_id, @QueryParam("author") String author) {
		Collection<Comment> comments = forum.getTopic(topic_id).getPost(post_id).getComments();
		if(author==null)
			return Response.ok(new GenericEntity<Collection<Comment>>(comments){}).build();
		List<Comment> filteredComments = comments.stream().filter(c -> c.getAuthor().equals(author))
										.collect(Collectors.toList());
		return Response.ok(new GenericEntity<Collection<Comment>>(filteredComments){}).build();
	}

	@GET
	@Path("{comment}")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getById(@PathParam("topic") Long topic_id, @PathParam("post") Long post_id, @PathParam("comment") Long comment_id) {
		Comment comment = forum.getTopic(topic_id).getPost(post_id).getComment(comment_id);
		if(comment==null)
			return Response.status(Response.Status.NOT_FOUND).build();
		return Response.ok(comment).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response createComment(@PathParam("topic") Long topic_id, @PathParam("post") Long post_id, Comment newComment){
		Post post = forum.getTopic(topic_id).getPost(post_id);
		post.addComment(newComment);
		return Response.status(Response.Status.CREATED).entity(newComment).build();

	}

	@PUT
	@Path("{comment}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response editComment(@PathParam("topic") Long topic_id, @PathParam("post") Long post_id,@PathParam("comment") Long comment_id, Comment newComment){
		Post post = forum.getTopic(topic_id).getPost(post_id);
		if(post.editComment(comment_id, newComment) == null)
			return Response.status(Response.Status.NOT_FOUND).build();
		return Response.ok(newComment).build();
	}

	@DELETE
	@Path("{comment}")
	public Response deleteComment(@PathParam("topic") Long topic_id, @PathParam("post") Long post_id,@PathParam("comment") Long comment_id){
		Post post = forum.getTopic(topic_id).getPost(post_id);
		if(post.removeComment(comment_id)==null)
			return Response.status(Response.Status.NOT_FOUND).build();
		return Response.noContent().build();
	}

	@DELETE
	public Response deleteAllComments(@PathParam("topic") Long topic_id, @PathParam("post") Long post_id){
		forum.getTopic(topic_id).getPost(post_id).removeAllComments();
		return Response.noContent().build();
	}

}
