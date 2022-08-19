package com.sdaniel.rssapp.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Autowired
	private JwtTokenStore jwtTokenStore; // configurado en el AuthorizationServer y añadido al contenedor de Spring con @Bean
	
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.tokenStore(jwtTokenStore);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/oauth/**").permitAll() // para login
		.antMatchers(HttpMethod.POST, "/users").permitAll() // para registrar usuarios
		.antMatchers(HttpMethod.GET, "/users").hasRole("ADMIN") // para ver todos los usuarios
		.antMatchers("/users/**").hasAnyRole("ADMIN", "USER") // para ver detalles/actualizar/eliminar...acciones de los usuarios (suscribe,like,comment,desuscribe...)
		.antMatchers(HttpMethod.POST, "/feeds").permitAll() // para crear un feed
		.antMatchers(HttpMethod.GET, "/feeds/**").permitAll() // ver todos los feeds/actualizar manualmente
		.antMatchers(HttpMethod.DELETE, "/feeds/**").hasRole("ADMIN") // solo el ADMIN puede eliminar feeds de la BD, el usuario solo se puede desuscribir
		.antMatchers(HttpMethod.GET, "/items").permitAll() // ver todos los items (portada)
		.antMatchers(HttpMethod.GET, "/items/**").hasAnyRole("ADMIN", "USER") // ver los items de un feed en concreto
		.anyRequest().authenticated() // cualquier otra ruta requiere autenticacion
		.and().cors().configurationSource(configurationSource()) // configuramos CORS para Spring Security
		;
	}

	@Bean
	public CorsConfigurationSource configurationSource() {
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOrigins(Arrays.asList("*", "http://192.168.1.127:4200", "http://andoid:0000")); // angular...
		corsConfig.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "OPTIONS"));
		corsConfig.setAllowCredentials(true);
		corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig); // pasamos la configuracion CORS a los endpoints (se aplica a todas las rutas)
		
		return source;
	}
	
	@Bean // filtro para que la configuración CORS se aplique a toda la aplicación y no solamente a SringSecurity
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(configurationSource()));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}
}
