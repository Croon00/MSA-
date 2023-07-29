package com.example.userservice.security;

import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


// Configuration은 다른 클래스 보다 먼저 적용
@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {
    private UserService userService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Environment env;

    public WebSecurity(Environment env, UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.env = env;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.authorizeRequests().antMatchers("/actuator/**").permitAll();
        http.authorizeRequests()
                .antMatchers("/users")
                .access("hasIpAddress('172.30.1.30')")
                .and()
                .addFilter(getAuthentionFilter());

        http.headers().frameOptions().disable();
    }


    //
    private AuthenticationFilter getAuthentionFilter() throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager(), userService, env);
//        authenticationFilter.setAuthenticationManager(authenticationManager());

        return authenticationFilter;
    }

    // configure를 오버로딩해서 사용하고 있는데 이는 인증에 관련된 작업 인증 후 권한
    // select pwd from users where email=?
    // db_pwd(encrypted) == input_pwd(encrypted)
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        // SpringSecurity가 제공해주는 passwordEncoder를 통해서 변환처리를 하여서
        // userDetailservice가 사용자의 유저 네임과 password를 가지고 로그인 처리를 해준다.
        // 그럼으로 userService에서 사용자를 검색하는 작업을 하새 가져온다.
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
    }
}
