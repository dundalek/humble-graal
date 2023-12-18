
```
export GRAALVM_HOME="$HOME/Downloads/graalvm-jdk-21.0.1+12.1"
```

```
clj -T:build uber

java -jar target/example.jar
```

```
clj -T:build native

target/example
```
