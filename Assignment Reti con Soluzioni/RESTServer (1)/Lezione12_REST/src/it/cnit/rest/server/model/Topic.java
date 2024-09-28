package it.cnit.rest.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Topic {
	@XmlAttribute
	private Long id;
	private Date timestamp = new Date();
	private String name;
	@XmlTransient @JsonIgnore
	private Map<Long, Post> posts = new HashMap<>();
	@XmlTransient @JsonIgnore
	private long counter = 0;

	public Topic() {
	}

	public Topic(String name) {
		this.name = name;
		this.timestamp = new Date();
	}

	public Post newPost() {
		Post p = new Post().setId(counter++).setTopic(this);
		posts.put(p.getId(), p);
		return p;
	}

	public Post newPost(Post post) {
		post.setId(counter++).setTopic(this);
		posts.put(post.getId(), post);
		return post;
	}

	public Long getId() {
		return id;
	}

	public Topic setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Topic setName(String name) {
		this.name = name;
		return this;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Topic setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public Collection<Post> getPosts() {
		return posts.values();
	}

	public Post getPost(Long id) {
		return posts.get(id);
	}

	public void removeAllPosts(){
		posts = new HashMap<>();
	}

	public Post editPost(Long post_id, Post post) {
		post.setId(post_id);
		return posts.replace(post_id, post);
	}

	public Post removePost(Long post_id) {
		return posts.remove(post_id);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("id=").append(id);
		sb.append(", timestamp=").append(timestamp);
		sb.append(", name='").append(name).append('\'');
		sb.append(", posts=").append(posts);
		sb.append('\n');
		return sb.toString();
	}
}
