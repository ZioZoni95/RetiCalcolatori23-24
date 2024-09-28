package it.cnit.rest.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.cnit.rest.server.model.Comment;
import it.cnit.rest.server.model.Post;
import it.cnit.rest.server.model.Forum;
import it.cnit.rest.server.model.Topic;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/summary")
public class SummaryController {
	/**
	 * Manual creation of the JSON object
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getSummary(){
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = mapper.createObjectNode();
		Collection<Topic> topics = Forum.getForum().getallTopics();

		//Java stream
		int topic_count = topics.size();
		int post_count = topics.stream().mapToInt(t -> t.getPosts().size()).sum();
		int comment_count = topics.stream().map(Topic::getPosts).mapToInt(posts -> posts.stream().mapToInt(p->p.getComments().size()).sum()).sum();
		root.put("topic_count", topic_count);
		root.put("post_count", post_count);
		root.put("comment_count", comment_count);


		// Example of manual json creation (Booooring)
		ArrayNode jsonTopics = mapper.createArrayNode();
		for(Topic topic : topics){
			ObjectNode jsonTopic = mapper.createObjectNode();
			ArrayNode jsonPosts = mapper.createArrayNode();
			jsonTopic.put("name",topic.getName());
			jsonTopic.put("id",topic.getId());
			for(Post post : topic.getPosts()){
				jsonTopic.set("posts",jsonPosts);
				ObjectNode jsonPost = mapper.createObjectNode();
				jsonPosts.add(jsonPost);
				jsonPost.put("id",post.getId());
				ArrayNode jsonComments = mapper.createArrayNode();
				for(Comment comment : post.getComments()){
					ObjectNode jsonComment = mapper.createObjectNode();
					jsonComment.put("id",comment.getId());
					jsonComments.add(jsonComment);
				}
				jsonPost.set("comments", jsonComments);
			}
			jsonTopics.add(jsonTopic);
		}
		root.set("topics",jsonTopics);
		return root;
	}
}
