import com.google.protobuf.gradle.*
import org.gradle.kotlin.dsl.all
import org.gradle.kotlin.dsl.plugins

plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.google.protobuf")
}

group = "com.eureckah.banking"
version = "0.0.1-SNAPSHOT"

extra["springGrpcVersion"] = "0.11.0"

dependencies {
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // gRPC server
    implementation("io.grpc:grpc-services")
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    // database
    runtimeOnly("com.h2database:h2")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.grpc:spring-grpc-test")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDir("build/generated/source/proto/main/grpc")
            srcDir("build/generated/source/proto/main/java")
        }
    }
}
