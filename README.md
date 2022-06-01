# FeatureProbe Server Side SDK for Java
[![Coverage Status](https://coveralls.io/repos/github/FeatureProbe/server-sdk-java/badge.svg?branch=main)](https://coveralls.io/github/FeatureProbe/server-sdk-java?branch=main)
[![GitHub Star](https://img.shields.io/github/stars/FeatureProbe/server-sdk-java)](https://github.com/FeatureProbe/server-sdk-java/stargazers)
[![License](https://img.shields.io/github/license/FeatureProbe/server-sdk-java)](https://github.com/FeatureProbe/server-sdk-java/blob/main/LICENSE)


Feature Probe is an open source feature management service. This SDK is used to control features in java programs. This
SDK is designed primarily for use in multi-user systems such as web servers and applications.

## Getting started

In this guide we explain how to use feature toggles in a Java application using FeatureProbe.

### Step 1. Install the Java SDK

First, install the FeatureProbe SDK as a dependency in your application.

#### Apache Maven

```xml

<dependency>
    <groupId>com.featureprobe</groupId>
    <artifactId>server-sdk-java</artifactId>
    <version>xx.xx.release</version>
</dependency>
```

#### Gradle Groovy DSL

```text
implementation 'com.featureprobe:server-sdk-java:xx.xx.release'
```

### Step 2. Create a FeatureProbe instance

After you install and import the SDK, create a single, shared instance of the FeatureProbe sdk.

```java
public class Demo {
    private static final FPConfig config = FPConfig.builder()
            .remoteUri("http://127.0.0.1:4007")
            .pollingMode(Duration.ofSeconds(3))
            .useMemoryRepository()
            .build();

    private static final FeatureProbe fpClient = new FeatureProbe("server-61db54ecea79824cae3ac38d73f1961d698d0477",
            config);
}
```

### Step 3. Use the feature toggle

You can use sdk to check which variation a particular user will receive for a given feature flag.

```java
public class Demo {
    private static final FPConfig config = FPConfig.builder()
            .remoteUri("http://127.0.0.1:4007")
            .pollingMode(Duration.ofSeconds(3))
            .useMemoryRepository()
            .build();

    private static final FeatureProbe fpClient = new FeatureProbe("server-61db54ecea79824cae3ac38d73f1961d698d0477",
            config);

    public void test() {
        FPUser user = new FPUser("user_unique");
        user.with("userId", "9876");
        user.with("tel", "12345678998");
        boolean boolValue = fpClient.boolValue("bool_toggle_key", user, false);
        if (boolValue) {
             #application code to show the feature
        } else {
            #the code to run if the feature is off
        }
    }
}
```

## Testing

We have unified integration tests for all our SDKs. Integration test cases are added as submodules for each SDK repo. So
be sure to pull submodules first to get the latest integration tests before running tests.

```shell
git pull --recurse-submodules
mvn test
```

## Contributing
We are working on continue evolving FeatureProbe core, making it flexible and easier to use. 
Development of FeatureProbe happens in the open on GitHub, and we are grateful to the 
community for contributing bugfixes and improvements.

Please read [CONTRIBUTING](https://github.com/FeatureProbe/featureprobe/blob/master/CONTRIBUTING.md) 
for details on our code of conduct, and the process for taking part in improving FeatureProbe.


## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.


