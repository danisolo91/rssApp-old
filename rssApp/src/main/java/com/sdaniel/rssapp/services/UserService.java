package com.sdaniel.rssapp.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sdaniel.rssapp.models.entity.User;

public interface UserService {

	public User findUserById(String id);
	public User findUserByUsername(String username);
	public Page<User> findAll(Pageable pageable);
	public User save(User user);
	public void deleteById(String id);
	
}
