package com.switchmate.common.config;

import java.util.List;
import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.switchmate.security.JwtAuthenticationFilter;
import com.switchmate.common.error.ApiErrorResponse;

@Configuration
public class SecurityConfiguration {

	private final String frontendOrigin;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ObjectMapper objectMapper;

	public SecurityConfiguration(
			@Value("${app.cors.allowed-origin}") String frontendOrigin,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			ObjectMapper objectMapper) {
		this.frontendOrigin = frontendOrigin;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.objectMapper = objectMapper;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.httpBasic(basic -> basic.disable())
				.formLogin(form -> form.disable())
				.logout(logout -> logout.disable())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
						.requestMatchers("/swagger-ui", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
						.permitAll()
						.requestMatchers("/api/auth/signup/resume", "/api/auth/signup/complete", "/api/auth/login")
						.permitAll()
						.requestMatchers("/api/admin/jobs/ingest").permitAll()
						.requestMatchers("/api/**").authenticated()
						.anyRequest().denyAll())
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint((request, response, authException) -> writeError(
								response, 401, "UNAUTHORIZED", "Authentication is required"))
						.accessDeniedHandler((request, response, accessDeniedException) -> writeError(
								response, 403, "FORBIDDEN", "Access is denied")))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	private void writeError(jakarta.servlet.http.HttpServletResponse response, int status, String code, String message)
			throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getOutputStream(),
				new ApiErrorResponse(Instant.now(), status, code, message, null));
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
			JwtAuthenticationFilter filter) {
		FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(frontendOrigin));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}