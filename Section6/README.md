## User MicroService 로그인 추가

![user로그인](https://github.com/Croon00/MSA-/assets/73871364/b4c83579-6e82-46bb-927d-a457d18acbdf)

![login api](https://github.com/Croon00/MSA-/assets/73871364/3fde844e-8887-49ad-8af3-ea5952158dcf)

- login API에서 클라이언트에서 서버에 요청을 하면 userId(랜덤하게 발생하는 UUID 값), 정상적인 로그인이 되었다는 JWT토큰을 반환시켜준다.

![AuthenticationFilter](https://github.com/Croon00/MSA-/assets/73871364/96467700-653f-49bd-8cdc-bafd06c4820a)

- 로그인 요청 발생시 작업을 처리해주는 Custom Filter 클래스
- AuthenticationFilter와 SuccessfulAuthentication 필요

```java
RequestLogin creds = new ObjectMapper(request.getInputStream(), RequestLogin.class);
```

```java
@Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {

            RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getEmail(),
                            creds.getPassword(),
                            new ArrayList<>())
                    );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
```

- 전달되는 request값은 post 형태로 전달 됨으로 이것을 reqeust파라미터로 받을 수 없어서 InputStream으로 받으면 수작업으로 데이터가 어떤 게 들어왔는지 처리할 수 있다.
- 사용자가 입력한 이메일과 아이디 값을 SpringSecurity에서 사용할 수 있는 값으로 변환 하기 위해 UsernamePasswordAuthenticationToken() 값으로 변환 시켜주어야 한다.

## WebSecurity

```java
package com.example.userservice.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

// Configuration은 다른 클래스 보다 먼저 적용
@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
	
        http.authorizeRequests()
                .antMatchers("/**")
                .access("hasIpAddress('14.55.39.14')")
                .and()
                .addFilter(getAuthentionFilter());

        http.headers().frameOptions().disable();
    }

    // 
    private AuthenticationFilter getAuthentionFilter() throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter();
        authenticationFilter.setAuthenticationManager(authenticationManager());

        return authenticationFilter;
    }
}
```

- webSecurity에서 요청들에 대해서 Ip를 내 ip로 요청한 것들만 한다.
- 마지막으로 filter를 통과시킨 것에 대해서만 권한을 부여하고 작업을 진행하게 한다.
- 위에 configure는 권한에 관련된 부분

```java
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
```

```java
public interface UserService extends UserDetailsService {
```

```java
@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);

        if(userEntity == null)
            throw new UsernameNotFoundException(username);

        // 마지막으로 추가적인 권한을 넣어주면되는데 아직 추가되는 권한이 없기 때문에 ArrayList로 넣기
        // enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities 등을 정의
        // 기한들을 설정 혹은 권한을 어떻게 설정할지
        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true, true, true, true,
                new ArrayList<>());
    }
```

- 권한을 줘야하는 것은 AuthenticationManageBuild를 파라미터로 가진 cofigure메서드 이다.
- auth.userDetailsService로 권한을 줄 수 있는데 여기서 넘겨야 하는 값은 userService 값과 이를 passwordEncoder를 한 값
- userService를 가져와 사용하는데 이는 실제 userService처럼 처리하기 위해서 userService를 UserDetailsService를 상속 받게 해야한다.
- 이를 상속 받으면 구현해야하는 메서드인 loadUserByUsername을 구현하여서 이를 userEntity를 가져와서 username으로 찾게하고 이것이 있을 경우 UserDetails를 상속한 User 구현체로 만들어서 return 하는데 여기서 권한들을 지정하여서 Email과 encrypted된 비밀번호를 넣어서 리턴한다.

### API-Gate

```java
- id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/user-service/login
            - Method=POST
          filters:
            - RemoveRequestHeader=Cookie
            - RewritePath=/user-service/(?<segment>.*), /$\{segment}
```

- 로그인 정보가 요청되었을 때에는 변환작업을 준다.
- Header값을 삭제하게 해준다(새로운 데이터로 입력하기 위해서)
- 사용자가 정보 요청을 했을 때 앞에 데이터를 빼고 → ?<segment>.*  /$\{segment}요 데이터 형태로 바꾸겠다.

```java
- id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/user-service/users
            - Method=POST
          filters:
            - RemoveRequestHeader=Cookie
            - RewritePath=/user-service/(?<segment>.*), /$\{segment}
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/user-service
            - Method=GET
          filters:
            - RemoveRequestHeader=Cookie
            - RewritePath=/user-service/(?<segment>.*), /$\{segment}
```

- 나머지 API의 형태도 (회원가입, 나머지) 만들어준다.
- http:L//127.0.0.1:60000/user-service/welcome —> 이렇게 썼어야 했다api-gate를 이용해서 user-service를 빼서 사용할 수 있다.
- 한마디로 원래 RequestMapping에서 user-service를 추가해서 이를 Path로 찾아가게 하였지만 이제는 이를 user-service의 인스턴스 네임으로 처리하여서 찾아가게 하고 그 뒤에 붙은 값인  login나 users 혹은 아무것도 없는 filters를 통해서 요청을 받는다.
- login은 springSecurity를 통해서 정상적으로 자동으로 url이 만들어짐으로 컨트롤러 단에서 만들 필요가 없다.

- 디버거 모드를 통해서 하나씩 값들과 코드가 진행하는 depth를 확인 가능

```java
logging:
  level:
    com.example.userservice: DEBUG
```

- 디버거 level 설정을 해주어야 console창에서 debug할때 log 확인 가능
- 로그인을 하게되면 가장 먼저 걸리는 것이 attemptAuthentication
- loadUserByUsername을 통해서 username을 받아서 findByEmail을 통해서 해당 username이 존재하는지 확인

```java
@Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // 유저타입으로 캐스팅된 getPrincepal에서 얻은 Username 값을 debug해본다. beakPoint를 이용하여서 debug를 확인할 수 있다.
        log.debug( ((User)authResult.getPrincipal()).getUsername());
    }
```

- 최종적으로 successfultAuthentication에서 authResult의 Principal에 username을 확인해 보면 로그인 할 때 사용한 username이 나오는 것을 확인 가능

![jwtBuilder 추가](https://github.com/Croon00/MSA-/assets/73871364/6ae46593-90cc-40d5-84ee-d5fa91f4e852)

- 회원가입을 하게 되면 랜덤하게 UUID로 만들어지는 userId를 가지고 토큰을 만드려한다.
- 이를 위해서 successfulAuthentication에서 userEmail을 가지고 다시 한번 database에서 userDto를 가져와서 여기에 있는 userId를 가지고 토큰을 생성 후 이 토큰을 클라이언트한테 반환 해준다.
- @Configuration은 실행되기전에 모두 생성자 주입이 되어서 @Autowired를 사용할 필요 x

```java
@Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // 유저타입으로 캐스팅된 getPrincepal에서 얻은 Username 값을 debug해본다. beakPoint를 이용하여서 debug를 확인할 수 있다.
        log.debug( ((User)authResult.getPrincipal()).getUsername());

        String userName = ((User) authResult.getPrincipal()).getUsername();
        UserDto userDto = userService.getUserDetailsByEmail(userName);
    }
```

WebSecurity에서 이제 생성할 때 파라미터 넘겨주어야 한다.

```java
private AuthenticationFilter getAuthentionFilter() throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager(), userService, env);
//        authenticationFilter.setAuthenticationManager(authenticationManager());

        return authenticationFilter;
    }
```

- userService에서도 getUserDetailsByEmail 필요

```java
UserDto getUserDetailsByEmail(String userName);

@Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null)
            throw new UsernameNotFoundException(email);

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
        return null;
    }
```

```java
token:
  expiration_time: 86400000
  secret: user_token
```

- yml에서 token 설정 60 * 60 * 24 * 1000 → 하루

```java
@Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // 유저타입으로 캐스팅된 getPrincepal에서 얻은 Username 값을 debug해본다. beakPoint를 이용하여서 debug를 확인할 수 있다.
        log.debug( ((User)authResult.getPrincipal()).getUsername());

        String userName = ((User) authResult.getPrincipal()).getUsername();
        UserDto userDetails = userService.getUserDetailsByEmail(userName);

        String token = Jwts.builder()
                .setSubject(userDetails.getUserId())
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(env.getProperty("token.expiration_time"))))
                .signWith(SignatureAlgorithm.HS256, env.getProperty("token.secret"))
                .compact();
        response.addHeader("token", token);
        response.addHeader("userId", userDetails.getUserId());
    }
```

- token에서 Jwt의 builder를 통해서 setSubject에서 userId 값을 넣어주고, 기한 설정, 알고리즘, 키값을 설정해주고 response에서 header에 추가 해서 반환해준다.

![전통적인인증시스템](https://github.com/Croon00/MSA-/assets/73871364/be9279c8-51c4-4ebe-ac76-d68471153b41)

- 세션과 쿠키를 사용하면 client(특히 요즘 쓰는 react 등의 프론트 라이브러리) 와 server(java)에서 유요하게 이동 불가, 또한 모바일에서도 별도의 언어일 수도 있어서 불가능

![Token인증시스템](https://github.com/Croon00/MSA-/assets/73871364/3f629fd1-73b6-471f-a865-8bebd7618e7c)

- 토큰은 서버에서 발급을 해서 클라이언트가 토큰을 가지고 요청을 다시 하게 함으로 서버에서는 자신에서 발급시킨것이기 때문에 이를 일치하는지 다시 체크할 수 있게 해준다.

![JWT](https://github.com/Croon00/MSA-/assets/73871364/6052a988-fe57-4854-b916-5a8d79fa71e5)

- jwt토큰의 구성
- 인증 헤더 내에서 사용되는 토큰 포맷
- 두 개의 시스템끼리 안전한 방법으로 통신 가능
- 클라이언트 독립적인 서비스(stateless)
- CDN
- No Cookie-Session(No CSRF)
- 지속적인 토큰 저장

## API-gate에서 token을 통한 AuthorizationHeaderFilter 설정

```java
package com.example.apigatewayservice.filter;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    Environment env;

    public AuthorizationHeaderFilter(Environment env) {
        this.env = env;
    }

    public static class Config {

    }

    // login -> token -> user (with token) -> header(include token)
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "no authorization header", HttpStatus.UNAUTHORIZED);
            }

            // Bearer 토큰값이 존재할 것이다.
            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            // Bearer 부분을 빼서 String 형태로 만들기
            String jwt = authorizationHeader.replace("Bearer", "");

            if (!isJwtValid(jwt)){
                return onError(exchange,"JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }
            return chain.filter(exchange);
        });
    }

    // jwt가 유효한지
    private boolean isJwtValid(String jwt) {
        // mvc 형태로 구성하는게 아니라 Spring webflux를 이용한 function api를 가지고 비동기 방식으로 만든다.
        boolean returnValue = true;

        // 서브젝트를 토큰으로 부터 뽑아서 확인
        String subject = null;

        // 복호화를 통해서 가져오기
        try{
            subject = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                    .parseClaimsJws(jwt).getBody()
                    .getSubject();
        }catch (Exception ex){
            returnValue = false;
        }

        if(subject == null || subject.isEmpty()){
            returnValue = false;
        }

        return returnValue;

    }

    // 에러 발생
    // Mono, Flux -> Spring WebFlux
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatust) {
        // servlet을 사용 x
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatust);
        log.error(err);

        return response.setComplete();
    }

}
```

- AuthorizationHeaderFilter  클래스를 생성하여서 AbstractGatewayFilterFactory를 통해서 apply에서 httpRequest에서 받은 request에서 받은 header에서 *`AUTHORIZATION` 을 받아서 이 값을 Bearer 값을 빼서 이것이 env에서 설정한 token 키값을 통해 서브젝트가 존재하는지 복호화를 통해서 확인한 후 이를 return 한다.*

```java
- id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/user-service
            - Method=GET
          filters:
            - RemoveRequestHeader=Cookie
            - RewritePath=/user-service/(?<segment>.*), /$\{segment}
            - AuthorizationHeaderFilter
```

- user-service에서 이제 이 토큰을 들고다니는지 확인 하기 위해서 회원가입과 로그인 서비스 이외에 부분에서 filter로 추가를 해주어야 한다.
