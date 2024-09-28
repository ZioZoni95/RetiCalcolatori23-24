package it.cnit.rest.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Post {

	private String message;
	@XmlTransient @JsonIgnore
	private Map<Long, Comment> comments = new HashMap<>();
	private Date timestamp = new Date();
	@XmlAttribute
	private long id;
	@XmlTransient @JsonIgnore
	private Topic topic;
	@XmlTransient @JsonIgnore
	private long counter = 0;
	private String author;

	public Post() {
	}

	public Post(Topic topic, String message) {
		this.topic = topic;
		this.message = message;
		this.timestamp = new Date();
	}

	public String getAuthor() {
		return author;
	}

	public Post setAuthor(String author) {
		this.author = author;
		return this;
	}

	public Comment newComment() {
		Comment comment = new Comment();
		comment.setPost(this);
		comment.setId(counter++);
		comments.put(comment.getId(), comment);
		return comment;
	}

	public Comment newComment(Comment comment) {
		comment.setPost(this);
		comment.setId(counter++);
		comments.put(comment.getId(), comment);
		return comment;
	}

	public String getMessage() {
		return message;
	}

	public Post setMessage(String message) {
		this.message = message;
		return this;
	}

	@XmlTransient
	public Topic getTopic() {
		return topic;
	}

	public Post setTopic(Topic topic) {
		this.topic = topic;
		return this;
	}


	public Date getTimestamp() {
		return timestamp;
	}

	public Post setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public Post addComment(Comment comment) {
		Comment newComment = newComment(comment);
		comments.put(comment.getId(), newComment);
		return this;
	}

	public Comment removeComment(Long comment_id){
		return comments.remove(comment_id);
	}

	public Comment editComment(Long comment_id, Comment newComment){
		newComment.setId(comment_id);
		return comments.replace(comment_id, newComment);
	}


	public Collection<Comment> getComments() {
		return comments.values();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Post post = (Post) o;
		return Objects.equals(topic, post.topic) &&
				Objects.equals(comments, post.comments) &&
				Objects.equals(timestamp, post.timestamp);
	}

	@Override
	public int hashCode() {
		return Objects.hash(topic, comments, timestamp);
	}

	public long getId() {
		return id;
	}

	public Post setId(long id) {
		this.id = id;
		return this;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("message='").append(message).append('\'');
		sb.append(", comments=").append(comments);
		sb.append(", timestamp=").append(timestamp);
		sb.append(", id=").append(id);
		sb.append('\n');
		return sb.toString();
	}

	public Comment getComment(Long comment_id) {
		return comments.get(comment_id);
	}

	public void removeAllComments() {
		comments = new HashMap<>();
	}
}
