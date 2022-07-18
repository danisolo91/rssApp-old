package com.sdaniel.feed.services;

import java.util.List;

import com.sdaniel.feed.entity.Entry;

public interface FeedService {

	public List<Entry> getEntries(String url);
	
}
