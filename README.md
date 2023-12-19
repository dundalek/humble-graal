
```
export GRAALVM_HOME="$HOME/Downloads/graalvm-jdk-21.0.1+12.1"
```

https://www.graalvm.org/latest/reference-manual/native-image/metadata/AutomaticMetadataCollection/

maybe put in in META-INF/native-image
https://www.graalvm.org/22.2/reference-manual/native-image/guides/build-with-reflection/


$GRAALVM_HOME/bin/java -agentlib:native-image-agent=config-output-dir=traced-config -jar target/example.jar


```
clj -M -m example.main
```


```
clj -T:build uber

java -jar target/example.jar
```

```
clj -T:build native

target/example
```
