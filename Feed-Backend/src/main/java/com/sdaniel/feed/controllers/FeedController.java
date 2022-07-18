package com.sdaniel.feed.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sdaniel.feed.services.FeedService;

@Controller
public class FeedController {

	@Autowired
	private FeedService feedService;
	
	@GetMapping("/feed")
	public String read(@RequestParam String url, Model model) {
		
		model.addAttribute("title", "Feed Backend");
		model.addAttribute("entries", feedService.getEntries(url));
		
		return "read";
	}
	
}
