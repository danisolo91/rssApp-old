package com.sdaniel.rssapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sdaniel.rssapp.services.ItemService;

@RestController
public class ItemController {

	@Autowired
	private ItemService itemService;
	
	@GetMapping("/items/feed/{id}")
	public ResponseEntity<?> showFeedItems(@PathVariable String id) {
		if(id.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		return ResponseEntity.ok(itemService.findAllByFeedId(id));
	}
	
	@GetMapping("/items")
	public ResponseEntity<?> showAllItems(Pageable pageable) {
		return ResponseEntity.ok(itemService.findAllOrderByPublishDateDesc(pageable));
	}
}
