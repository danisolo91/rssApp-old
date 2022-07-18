package com.sdaniel.rssapp.models.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sdaniel.rssapp.models.entity.Item;

public interface ItemRepository extends MongoRepository<Item, String> {

	@Query("{'feedId': ?0}")
	public List<Item> findAllByFeedId(String feedId);
	
	@Query(value = "{'feedId' : {$in : ?0}}", sort = "{'publishDate' : -1}")
	public Page<Item> findAllItemsByFeedIds(List<String> feedIds, Pageable pageable);
	
	public void deleteItemByFeedId(String feedId);
	
	public Page<Item> findAllByOrderByPublishDateDesc(Pageable pageable);

}