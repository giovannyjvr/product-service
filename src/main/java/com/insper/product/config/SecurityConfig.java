package com.insper.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
// Habilita @PreAuthorize no Controller, se quiser usar (por ex. @PreAuthorize("hasRole('ADMIN')"))
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * Configura um authentication provider mínimo (In‐Memory) para quebrar o ciclo
     * no AuthenticationManagerBean. Como os testes usam @WithMockUser, não precisamos
     * inserir usuários reais aqui; basta chamar inMemoryAuthentication() para registrar
     * um provedor vazio (que não adiciona users), mas que obriga o Spring a criar
     * o AuthenticationManager corretamente.
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
               .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
               .antMatchers("/products", "/products/*").permitAll()
               // Exige ROLE_ADMIN para POST/PUT/DELETE:
               .antMatchers("/products/**").hasRole("ADMIN")
               .anyRequest().authenticated()
            .and()
            // Se não estiver autenticado, devolve 403 (em vez do padrão 401)
            .exceptionHandling()
             .authenticationEntryPoint((request, response, authException) ->
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            )
            .and()
            // Insere o filtro JWT antes do filtro padrão de autenticação
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
