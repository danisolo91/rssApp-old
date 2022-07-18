package com.sdaniel.rssapp.models.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(	collection = "users")
public class User implements Serializable {

	private static final long serialVersionUID = -3323945009514952732L;

	@Id
	private String id;
	
	@NotEmpty
	@Size(max = 20)
	private String firstname;
	
	@NotEmpty
	@Size(max = 20)
	private String lastname;
	
	private String username; //el campo username se rellena en el controlador con el valor del email antes de guardar en BD
	
	@Email
	@NotEmpty
	private String email;
	
	@NotEmpty
	@Size(min = 5, max = 50)
	private String password;
	
	private Boolean enabled;
	
	@CreatedDate
	@Field("created_at")
	private Date createdAt;
	
	private List<Role> authorities; // se rellena en el controlador por defecto "ROLE_USER"
	
	private List<String> feeds = new ArrayList<>();
	
	public User() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public List<Role> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<Role> authorities) {
		this.authorities = authorities;
	}
	
	public List<String> getFeeds() {
		return feeds;
	}

	public void setFeeds(List<String> feeds) {
		this.feeds = feeds;
	}

}
