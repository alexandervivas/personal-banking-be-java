plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "com.eureckah.banking"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
