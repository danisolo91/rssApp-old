package com.sdaniel.rssapp.controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sdaniel.rssapp.models.entity.Role;
import com.sdaniel.rssapp.models.entity.User;
import com.sdaniel.rssapp.services.FeedService;
import com.sdaniel.rssapp.services.ItemService;
import com.sdaniel.rssapp.services.UserService;

@RestController
public class UserController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private FeedService feedService;
	
	@Autowired
	private ItemService itemService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("/users")
	public ResponseEntity<?> getUsers(Pageable pageable) {
		return ResponseEntity.ok(userService.findAll(pageable));
	}
	
	@GetMapping("/users/{id}")
	public ResponseEntity<?> getUser(@PathVariable String id) {
		User user = userService.findUserById(id);
		
		if(user == null) {
			return ResponseEntity.notFound().build(); // 404
		}
		
		return ResponseEntity.ok(user);
	}
	
	@PostMapping("/users")
	public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult result) {
		if(result.hasErrors()) {
			return this.validate(result);
		}
		
		user.setUsername(user.getEmail()); // asignamos al username el valor del email
		user.setAuthorities(Arrays.asList(new Role("ROLE_USER"))); // asignamos por defecto "ROLE_USER"
		user.setPassword(passwordEncoder.encode(user.getPassword())); // codificamos la contraseña
		
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user)); // 201
	}
	
	@PutMapping("/users/{id}")
	public ResponseEntity<?> updateUser(@Valid @RequestBody User user, BindingResult result, @PathVariable String id) {
		if(result.hasErrors()) {
			return this.validate(result);
		}
		
		User userDB = userService.findUserById(id);
		if(userDB == null) {
			return ResponseEntity.notFound().build(); // 404
		}
		
		userDB.setFirstname(user.getFirstname());
		userDB.setLastname(user.getLastname());
		userDB.setUsername(user.getEmail());
		userDB.setEmail(user.getEmail());
		userDB.setPassword(passwordEncoder.encode(user.getPassword()));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(userDB));
	}
	
	@DeleteMapping("/users/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable String id) {
		if(id.isEmpty()) {
			return ResponseEntity.notFound().build(); // 404
		}
		
		userService.deleteById(id);
		
		return ResponseEntity.noContent().build(); // 204
	}
	
	@GetMapping("/users/logged-user")
	public ResponseEntity<?> getLoggedUser() {
		
		User user = new User();
		try {
			user = getAuthenticatedUser();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		
		return ResponseEntity.ok(user);
	}
	
	@GetMapping("/users/feeds/{feedId}/subscribe") // or unsubscribe...
	public ResponseEntity<?> subscribeToFeed(@PathVariable String feedId) {
		Boolean existsFeed = feedService.existsById(feedId);
		if(!existsFeed) {
			return ResponseEntity.notFound().build();
		}
		
		User user = new User();
		try {
			user = getAuthenticatedUser();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		
		List<String> userFeeds = user.getFeeds();
		if(userFeeds.contains(feedId)) {
			userFeeds.remove(feedId);
		} else {
			userFeeds.add(feedId);
		}
		user.setFeeds(userFeeds);
		
		return ResponseEntity.ok(userService.save(user));
	}
	
	@GetMapping("/users/feeds/items") // todos los items de los feeds a los que está suscrito el usuario
	public ResponseEntity<?> getFeedsItems(Pageable pageable) {
		User user = new User();
		try {
			user = this.getAuthenticatedUser();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		
		return ResponseEntity.ok(itemService.findAllByFeedIds(user.getFeeds(), pageable));
	}

	private ResponseEntity<?> validate(BindingResult result) {
		Map<String, Object> errores = new HashMap<>();
		result.getFieldErrors().forEach(err -> {
			errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
		});
		return ResponseEntity.badRequest().body(errores); // badRequest = status 400
	}
	
	private User getAuthenticatedUser() throws Exception {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = userService.findUserByUsername(username);
		if(user == null) {
			throw new Exception("User does not exists.");
		}
		return user;
	}
}
