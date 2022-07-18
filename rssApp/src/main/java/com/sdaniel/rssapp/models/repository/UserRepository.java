package com.sdaniel.rssapp.models.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.sdaniel.rssapp.models.entity.User;

public interface UserRepository extends MongoRepository<User, String> {

	public User findByUsername(String username);
	
	public Page<User> findAll(Pageable pageable);
	
	public void deleteUserById(String id);
	
}
