package com.sdaniel.rssapp.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sdaniel.rssapp.models.entity.Item;
import com.sdaniel.rssapp.models.repository.ItemRepository;

@Service
public class ItemServiceImplOG implements ItemService {

	@Autowired
	private ItemRepository itemRepository;
	
	@Override
	public List<Item> findAll() {
		return itemRepository.findAll();
	}

	@Override
	public List<Item> findAllByFeedId(String feedId) {
		return itemRepository.findAllByFeedId(feedId);
	}
	
	@Override
	public Page<Item> findAllByFeedIds(List<String> feedIds, Pageable pageable) {
		return itemRepository.findAllItemsByFeedIds(feedIds, pageable);
	}
	
	@Override
	public Page<Item> findAllOrderByPublishDateDesc(Pageable pageable) {
		return itemRepository.findAllByOrderByPublishDateDesc(pageable);
	}
	
	@Override
	public List<Item> saveAll(List<Item> items) {
		return itemRepository.saveAll(items);
	}
	
	@Override
	public void deleteItemsByFeedId(String feedId) {
		itemRepository.deleteItemByFeedId(feedId);
	}
}
