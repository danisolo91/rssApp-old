package com.sdaniel.rssapp.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sdaniel.rssapp.models.entity.Feed;
import com.sdaniel.rssapp.models.entity.Item;
import com.sdaniel.rssapp.services.FeedService;
@RestController
public class FeedController {

	@Autowired
	private FeedService feedService;
		
	@GetMapping("/feeds")
	public ResponseEntity<?> getFeeds() {
		return ResponseEntity.ok(feedService.findAll());
	}
	
	@PostMapping("/feeds")
	public ResponseEntity<?> createFeed(@RequestParam String url) {
		Feed feed = feedService.findByUrl(url);
		
		if(feed == null) {
			try {
				feed = feedService.saveByUrl(url);
				feedService.refreshFeedItems(feed); // crear un interceptor y llamar a esta función cuando haya acabado la ejecución de este método... aunque habría que comprobar si existe el id del feed...
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.badRequest().body("Invalid URL or XML"); // error 400 (URL o XML mal...)
			}
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).body(feed); // 201
	}
	
	@DeleteMapping("/feeds/{id}")
	public ResponseEntity<?> deleteFeed(@PathVariable String id) {
		if(id.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		feedService.deleteById(id);
		feedService.deleteFeedItems(id);
		// habria que eliminar el feed de la lista de feeds de los usuarios que están suscritos
		
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/feeds/{id}/refresh")
	public ResponseEntity<?> refreshFeedItems(@PathVariable String id) {
		if(id.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		Feed feed = feedService.findById(id);
		
		if(feed == null) {
			return ResponseEntity.notFound().build();
		}
		
		List<Item> items = new ArrayList<>();
		
		try {
			items = feedService.refreshFeedItems(feed);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Error while refreshing the feed...");
		}
		
		return ResponseEntity.ok(items);
	}
}
