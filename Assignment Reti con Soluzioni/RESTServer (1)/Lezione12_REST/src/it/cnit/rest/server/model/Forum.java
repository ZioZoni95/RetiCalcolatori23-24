package it.cnit.rest.server.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Forum {
	private static Forum forum = new Forum();
	private Map<Long, Topic> topics = new HashMap<>();
	private long counter = 0;

	private Forum() {
	}

	public static Forum getForum() {
		return forum;
	}

	public Topic newTopic() {
		Topic t = new Topic().setId(counter++);
		topics.put(t.getId(),t);
		return t;
	}

	public Topic newTopic(Topic topic){
		topic.setId(counter++);
		topics.put(topic.getId(), topic);
		return topic;
	}

	public Topic removeTopic(Long id){
		return topics.remove(id);
	}

	public Collection<Topic> getallTopics(){
		return topics.values();
	}


	public Topic getTopic(Long topic) {
		return topics.get(topic);
	}
}
