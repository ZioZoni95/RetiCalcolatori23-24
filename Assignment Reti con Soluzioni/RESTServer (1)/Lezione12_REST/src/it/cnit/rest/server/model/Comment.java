package it.cnit.rest.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.*;
import java.util.Date;
import java.util.Objects;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Comment {
	@JsonIgnore @XmlTransient
	private Post post;
	private Date timestamp = new Date();
	private String author;
	private String message;
	@XmlAttribute
	private long id;

	public Comment(Post post, String message) {
		this.post = post;
		this.message = message;
	}

	public Comment() {
	}

	public String getAuthor() {
		return author;
	}

	public Comment setAuthor(String author) {
		this.author = author;
		return this;
	}

	public Post getPost() {
		return post;
	}

	public Comment setPost(Post post) {
		this.post = post;
		return this;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Comment setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public Comment setMessage(String message) {
		this.message = message;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Comment comment = (Comment) o;
		return Objects.equals(post, comment.post) &&
				Objects.equals(timestamp, comment.timestamp) &&
				Objects.equals(message, comment.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(post, timestamp, message);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("timestamp=").append(timestamp);
		sb.append(", message='").append(message).append('\'');
		sb.append(", id=").append(id);
		sb.append('\n');
		return sb.toString();
	}
}
