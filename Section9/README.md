# 암호화 처리 (Encryption, Decryption)

- Encryption에 사용했던 키와 Decryption할 때 같은 키를 사용하면 대칭키 —> Symmetric Encrpytion
- 다른 키를 사용하면 비대칭키 —> Asymmetric Encryption
- 비대칭 —> 사용되는 각각의 키 Private / Public Key가 사용된다
- Public으로 암호화 하면 Private으로 복호화 Private으로 암호화 하면 Public으로 복호화
- Java keytool을 사용하여서 할 수 있다.

![1](https://github.com/Croon00/MSA-/assets/73871364/b3ce34cb-4dec-423c-9e5b-6a2d13e50a57)

- 전혀 알 수 없는 데이터 타입을 저장 시킨 후 사용할 때 복호화 해서 사용하는 법
- cipher —> 암호화 되어있는 키 값이라고 알려줌
- 각각의 마이크로서비스가 사용할 수 있게 Plain 값으로 바꾸어서 전달해준다.(Spring Cloud Config Server에서)
- Spring starter bootstrap dependencies가 필요

## 대칭키를 이용한 암호화

```yaml
encrypt:
  key: abcdefghijklmnopqrstuvwxyz0123456789
```

- bootstrap.yml에 위와 같이 키값을 설정
- 암호화 하는 법 : Config Server 프로젝트의  port/encrypt 에 Post로 값 보내기
- 복호화 하는 법 : 위와 마찬가지로 port/decrypt 에 Post로 값 보내기
- config 프로젝트의 yml 파일을 아래와 같이 설정

```yaml
spring:
  application:
    name: config-service
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: file:///MSAgit/MSA-/Section9/native-repo
```

### user-service.yml 을 아래와 같이 만든다.

```yaml
datasource:
      driver-calss-name: org.h2.Driver
      url: jdbc:h2:mem:testdb
      username: sa
      password: '{cipher}f8d811e5343886cb30e16c5dabaa06e196ecbec85c1a4bf8a9d20dfefa929c8f'
```

- password를 configserver를 통해서 만든 암호화 된 것으로 database의 password를 넣어준다.
- 원래는 위에 정보가 user-service에서 yml로 쓰이고 있었지만 이를 설정 파일인 user-service.yml로 만든 후에 config가 대신 해서 이를 넣어주게 한다.

```yaml
spring:
  cloud:
    config:
      uri: http://127.0.0.1:8888
      name: user-service
```

- user-service의 bootstrap.yml 에서 다음과 같이 user-service.yml을 config 포트에서 사용하겠다고 설정을 해준다.

![2](https://github.com/Croon00/MSA-/assets/73871364/012e56d9-0981-4d4f-9663-ed7e326d6f00)

- http://127.0.0.1:8888/user-service로 했을 대
- 보는 바와 같이 password부분이 복호화 되어서 나올 수 있는 것을 확인할 수 있다.

## 비대칭 키 사용법

- public, private key —> JDK keytool 이용
- 일반적으로 암호화 할때 private key 복호화 할때 public key 사용된다.
- RSA 알고리즘 사용
- 키툴 만들기 : keytool -genkeypair -alias apiEncryptionKey -keyalg RSA -dname "CN=Kenneth Lee, OU=API Development, [O=joneconsulting.co.kr](http://o%3Djoneconsulting.co.kr/), L=Seoul, C=KR" -keypass "test1234" -keystore apiEncryptionKey.jks -storepass "test1234"
- 키툴 상세 확인 : keytool -list -keystore apiEncryptionKey.jks -v
- 인증서 파일 만들기 : keytool -export apiEncryptionKey -keystore apiEncryptionKey.jks -rfc -file trustServer.cer
- 공개키로 변경하기 : keytool -import -alias trustServer -file trustServer.cer -keystore publickey.jks 후에 yes(예)
- 위에 명령어를 통해서 key 값들을 가지는 파일들을 생성한다.

```yaml
encrypt:
#  key: abcdefghijklmnopqrstuvwxyz0123456789
  key-store:
    location: file:///MSAgit\MSA-\Section9\keystore\apiEncryptionKey.jks
    password: test1234
    alias: apiEncryptionKey
```

- config 에 있는 yml 파일에서 위와 같이 설정을 해준다.
- 이제 위에 대칭키를 썼을때와 마찬가지로 /encryption으로 post를 암호화하고 싶은 데이터를 보내어서 이를 datasource의 password로 사용하면 된다.
- api-gateway에서 사용하는 토큰은 ecommerce 이고 user-service에서 사용하는 토큰은 user-service토큰
- 각각의 yml 파일이 아닌 공통의 정보를 가지고 있으면 application.yml에서 정의를 해놓으면 된다.
