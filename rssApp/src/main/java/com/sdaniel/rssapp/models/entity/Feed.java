package com.sdaniel.rssapp.models.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(	collection = "feeds")
public class Feed {

	@Id
	private String id;
	private String url;
	private String name;
	private Date lastItemDate;
	
	public Feed() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastItemDate() {
		return lastItemDate;
	}

	public void setLastItemDate(Date lastItemDate) {
		this.lastItemDate = lastItemDate;
	}
}
