plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "com.eureckah.banking"
version = "0.0.1-SNAPSHOT"

extra["springGrpcVersion"] = "0.11.0"

repositories {
    mavenCentral()
}

dependencies {
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // gRPC client
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")
    implementation(project(":modules:tenants"))

    // BDD / Cucumber
    testImplementation("io.cucumber:cucumber-java:7.18.1")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.18.1")
    testImplementation("io.cucumber:cucumber-spring:7.18.1")

    // gRPC test server
    testImplementation("io.grpc:grpc-netty-shaded")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-suite-api")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
    }
}

tasks.test {
    useJUnitPlatform()
}
