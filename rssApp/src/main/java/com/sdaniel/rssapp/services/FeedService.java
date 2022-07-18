package com.sdaniel.rssapp.services;

import java.util.List;

import com.sdaniel.rssapp.models.entity.Feed;
import com.sdaniel.rssapp.models.entity.Item;

public interface FeedService {
	
	public Feed findById(String id);
	
	public Feed findByUrl(String url);
	
	public List<Feed> findAll();
	
	public Boolean existsByUrl(String url);
	
	public Boolean existsById(String id);
	
	public Feed save(Feed feed);
	
	public Feed saveByUrl(String url) throws Exception;
	
	public void deleteById(String id);
	
	public void deleteFeedItems(String id);
	
	public List<Item> refreshFeedItems(Feed feed)  throws Exception;
	
}
