package com.sdaniel.rssapp.models.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sdaniel.rssapp.models.entity.Feed;

public interface FeedRepository extends MongoRepository<Feed, String> {

	//@Query("{'url': ?0}")
	public Feed findFeedByUrl(String url);
	
	public Boolean existsFeedByUrl(String url);
	
	public Boolean existsFeedById(String id);
	
}
