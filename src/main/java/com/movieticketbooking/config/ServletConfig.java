package com.movieticketbooking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticketbooking.service.AuthService;
import com.movieticketbooking.servlet.LoginServlet;
import com.movieticketbooking.servlet.LogoutServlet;
import com.movieticketbooking.servlet.RegisterServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers the explicit HttpServlet endpoints required by the Java practical syllabus. */
@Configuration
public class ServletConfig {

    @Bean
    public ServletRegistrationBean<LoginServlet> loginServlet(AuthService authService, ObjectMapper objectMapper) {
        ServletRegistrationBean<LoginServlet> bean = new ServletRegistrationBean<>(
                new LoginServlet(authService, objectMapper), "/servlet/auth/login");
        bean.setName("loginServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<RegisterServlet> registerServlet(AuthService authService, ObjectMapper objectMapper) {
        ServletRegistrationBean<RegisterServlet> bean = new ServletRegistrationBean<>(
                new RegisterServlet(authService, objectMapper), "/servlet/auth/register");
        bean.setName("registerServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<LogoutServlet> logoutServlet(ObjectMapper objectMapper) {
        ServletRegistrationBean<LogoutServlet> bean = new ServletRegistrationBean<>(
                new LogoutServlet(objectMapper), "/servlet/auth/logout");
        bean.setName("logoutServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
