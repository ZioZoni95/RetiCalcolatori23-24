package it.cnit.rest.server.util;

import it.cnit.rest.server.model.Comment;
import it.cnit.rest.server.model.Post;
import it.cnit.rest.server.model.Forum;
import it.cnit.rest.server.model.Topic;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class DemoFiller {
	private static Logger LOGGER = Logger.getAnonymousLogger();
	private static List<String> authors = Arrays.asList("rosso","verde","bianco","nero","giallo","fucsia","viola","arancio");
	private static Random r = new Random();
	public static void fillWithDemoData() {
		LOGGER.info("### LOADING DEMO DATA ###");
		Forum forum = Forum.getForum();
		for (int i = 0; i < 5; i++) {
			Topic topic = forum.newTopic();
			topic.setName(String.format("topic-%d", topic.getId()));
			for (int j = 0; j < 10; j++) {
				Post post = topic.newPost();
				post.setMessage(String.format("post-%d",post.getId()));
				post.setAuthor(authors.get(r.nextInt(authors.size())));
				for (int k = 0; k < 6; k++) {
					Comment comment = post.newComment();
					comment.setMessage(String.format("comment-%d", comment.getId()));
					comment.setAuthor(authors.get(r.nextInt(authors.size())));
				}
			}
		}
		LOGGER.info("### DEMO DATA LOADED ###");

	}
}

