- Configuration을 추가할 때에 Config 클래스에서 2가지 방법으로 가져다 사용할 수 있다.
1. Environment를 이용하여서 env.getProperty를 통한 방법
2. @Value를 이용하여 하는 방법

```java
package com.example.userservice.controller;

import com.example.userservice.vo.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {

    private Environment env;

    private Greeting greeting;

    @Autowired
    public UserController(Environment env, Greeting greeting) {
        this.env = env;
        this.greeting = greeting;
    }

    @GetMapping("/health_check")
    public String status() {
        return "It's Working in User Service";
    }

    @GetMapping("/welcome")
    public String welcome() {
//  Environment 방법을 사용할 때에는 아래와 같이
//        return env.getProperty("greeting.message");
//  @Value 방법을 사용할 때에는 아래와 같이
        return greeting.getMessage();
    }
}

/// 밑에는 vo로 만든 @Value를 통한 yml에서 환경 변수 받는 법

package com.example.userservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
//@AllArgsConstructor
//@NoArgsConstructor
public class Greeting {
    @Value("${greeting.message}")
    private String message;

}
```

- @Value 를 사용한 방법과 env 를 사용한 방법

```yaml
greeting:
  message: Welcome to the Simple E-commerce.
```

- yml에서 위와 같이 환경변수를 설정 해주고 사용한다.

### H2 Database

- 자바로 작성된 오픈소스 RDBMS
- Embedded, Server-Client 가능
- JPA 연동 가능

### 회원가입

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/687c36f9-159a-4757-86f7-8b57bb315a60/Untitled.png)

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/c2f20aa0-2d21-48ed-9c5a-b6c9f03d5687/Untitled.png)

- JPA를 이용하여 위와 같은 아키텍처로 구성할 것.
- RequestUser클래스가 UserEntity로 변경되어야 함으로 이때 간단하게 사용할 수 있는 것이 modelapper 라이브러리

```java
ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd("encrypted_password");
        userRepository.save(userEntity);
```

mapper의 환경 설정(매칭할 수 있는 정보) —>getConfiguration의 setMatchingStrategy(MatchingStrategies.STRICT); 딱 맞아 떨어지지 않으면 매칭할 수 없게 한다.

- setEncryptedPwd는 userDto에서 아직 적용되지 않은 값이기 때문에 여기서 추가해서 save하기

### Security 설정

- 해당 강의에서는 옛날 버전으로 Security를 설정하고 있다.
- WebSecurityConfigurerAdapter가 depricated가 되어서 다른 방식을 사용해야 한다

[Deprecated된 WebSecurityConfigurerAdapter, 어떻게 대처하지?](https://velog.io/@pjh612/Deprecated된-WebSecurityConfigurerAdapter-어떻게-대처하지)

- 일단 강의대로 설명

```java
// Configuration은 다른 클래스 보다 먼저 적용
@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeHttpRequests().antMatchers("/users/**").permitAll();

				http.headers().frameOptions().disable();
    }
}
```

- 위와 같이 설정하고 실행 했을 때(Security) security password가 생성되어서 사용할 수 있게 된다.
- 해당하는 springboot프로젝트에서 인증작업을 하기위해서 암호화 패스워드를 제공하지 않고 사용하기 위해서 제공된다. 그렇지 않다면 내가 일일히 암호화 해서 생성해서 사용해야 한다.
- Security를 적용해서 사용하다보면 h2-console로 들어갔을 때 UI가 깨진다.
- html의 프레임별로 데이터가 나누어져 있어서 그렇다 —> `http.headers().frameOptions().disable();` 로 프레임나누어져 있는 것을 무시할 수 있게 해준다.

### BCryptPasswordEncoder

- Password를 해싱하기 위해 Bcrypt 알고리즘 사용
- 랜덤 Salt를 부여하여 여러번 Hash를 적용한 암호화 방식
- `필드단위에서 생성자 생성보다 생성자를 통한 생성자 주입이 더 안정적`
- `BCryptPasswordEncoder는 Bean으로 직접 주입한적이 없다 --> 가장 먼저 호출되는 Spring Application의 기본 클래스에다가 해당하는 것을 넣는다.`

```java
package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        // 랜덤 유효 아이디 생성
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
				// 패스워드를  encoder하여서 넣기
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

        userRepository.save(userEntity);

        UserDto returnUserDto = mapper.map(userEntity, UserDto.class);

        return returnUserDto;
    }
}
```

```java
package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```
