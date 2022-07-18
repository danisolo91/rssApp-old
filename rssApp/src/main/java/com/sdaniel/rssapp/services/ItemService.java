package com.sdaniel.rssapp.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sdaniel.rssapp.models.entity.Item;

public interface ItemService {

	public List<Item> findAll();
	
	public List<Item> findAllByFeedId(String feedId);
	
	public Page<Item> findAllByFeedIds(List<String> feedIds, Pageable pageable);
	
	public Page<Item> findAllOrderByPublishDateDesc(Pageable pageable);
	
	public List<Item> saveAll(List<Item> items);
	
	public void deleteItemsByFeedId(String feedId);
	
}
