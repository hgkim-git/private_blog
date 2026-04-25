/*
	plugins block:
		- Gradle에 어떤 기능을 추가할지 선언하는 블록.
 */
plugins {
    /*
        java plugin 선언
            - compileJava, test, jar 같은 task가 활성화
     */
    java
    /*
        Spring Boot plugin 선언
            - bootRun, bootJar 등 task 활성화
            - 실행 가능한 jar 생성(bootJar task)
            - Spring Boot BOM 연동
                - Spring Boot가 관리하는 호환 검증된 의존성 버전 표준표
                - 다만 단순히 spring boot starter를 통해 들어온 의존 라이브러리들의 버전만
     */
    id("org.springframework.boot") version "3.5.9"
    /*
        Spring dependency management plugin 선언
            - Gradle 전체 의존성 해석 단계에 강제 적용
            - 멀티 모듈 구조에서 모든 하위 모듈에 버전을 일관성있게 강제할 수 있음
                - 멀티 모듈 구조에서 사실상 필수
            - 때문에 start.spring.io 에서 두 plugin을 동시 제공하는 것으로 보임
     */
    id("io.spring.dependency-management") version "1.1.7"
}
/*
	프로젝트 메타 데이터
 */
group = "io.github.hgkimer"
version = "0.0.1-SNAPSHOT"
description = "Private blog project"

/*
	확장 설정 블록 (Extension configuration block)
		- 혹은 Gradle extension 블록
		- 위에 plugins에 선언하면 Extension 객체를 사용할 수 있음.
		- 위에 java plugin 선언으로 java {...} 확장 설정 블록 사용 가능
			- "java extension 설정" 으로 호칭
 */
java {
    /*
        toolchain: 빌드에 사용할 JDK를 선언적으로 지정하는 기능
            - 로컬 JDK가 8이든 21이든 Gradle은 JDK 17을 찾아서 사용
            - 없으면 자동 다운로드 가능
     */
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

/*
    configurations
        - Gradle core에서 제공하는 기본 블럭(ConfigurationContainer)
        - 멀티 모듈의 경우 모듈마다 Lombok 선언 중복을 피하기 위해 사용
        - 사실상 필요 없으모로 주석처리
 */
//configurations {
//    // compileOnly가 annotationProcessor 스코프 의존성을 상속(extendsFrom)한다는 의미
//    compileOnly {
//        extendsFrom(configurations.annotationProcessor.get())
//    }
//}

/*
    repositories
         - 라이브러리를 어디서 받을지 정의
         Maven
        <repositories>
          <repository>
            <id>central</id>
            <url>https://repo.maven.apache.org/maven2</url>
          </repository>
        </repositories>
 */
repositories {
    mavenCentral()
}

/*
    dependencies
        - 의존성 선언
        - 각 스코프는 따로 정리함
 */
dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.4.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.apache.commons:commons-pool2")
    "developmentOnly"("org.springframework.boot:spring-boot-devtools")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Databases
    // MySQL
    runtimeOnly("com.mysql:mysql-connector-j")
    // H2
    runtimeOnly("com.h2database:h2")

    // Logging (SQL 바인딩 파라미터 출력)
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.2")

    // flexmark
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")

    // jsoup HTML parser library @ https://jsoup.org/
    implementation("org.jsoup:jsoup:1.22.1")

    // jjwt
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // springdoc-openapi (Swagger UI)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")

    // flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/*
    JUnit5를 사용하도록 선언. 선언하지 않으면 Gradle은 기본적으로 JUnit4를 사용한다.
    사실상 Spring Boot 3.x 버전은 JUnit5만 지원하므로 강제된다.
        - Gradle이 @Test와 같은 어노테이션이 붙은 메서드들을 탐색하는데 JUnit5는 어노테이션의 패키지명이 다 변경됨.
        - 따라서 아래를 주석처리하면 테스트 어노테이션을 찾지 못해 테스트가 할게 없다는 테스트 실패가 나온다.
        - "No matching tests found in any candidate test task."
 */
tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("app.jar")
}
