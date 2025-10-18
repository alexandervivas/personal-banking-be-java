import com.google.protobuf.gradle.*

plugins {
    id("java-library")
    id("com.google.protobuf")
}

group = "com.eureckah.banking"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Dependencies required to compile generated sources
    implementation("io.grpc:grpc-stub:1.66.0")
    implementation("io.grpc:grpc-protobuf:1.66.0")
    implementation("com.google.protobuf:protobuf-java:3.25.5")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.66.0"
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

tasks.test {
    useJUnitPlatform()
}
