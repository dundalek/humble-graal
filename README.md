# HumbleUI + GraalVM

This repo shows how to compile [HumbleUI](https://github.com/HumbleUI/HumbleUI) apps with [GraalVM](https://www.graalvm.org) as a fast starting native executable.

The build uses only [Clojure CLI](https://clojure.org/guides/deps_and_cli), deps and tools.build.
No Leiningen or shell scripts needed.

Tested on Linux x86_64 (PopOS 22.04 LTS, Ubuntu-compatible).
I expect it should work on Mac and Windows too (perhaps with minor tweaks, PRs welcome).

## Build

There are 3 levels of compilation from the easiest to the hardest.
If are feeling lucky or impatient start with the last level 3.
If it does not work then go back to level 1 and climb up verifying each step works before trying the next one.

1.  Running from source with Clojure:
    ```sh
    clj -M -m example.humble
    ```
2.  Compiling ahead of time into an uber jar.  
    Build a jar file with:
    ```sh
    clj -T:build uber
    ```
    Then run it with java:
    ```sh
    java -jar target/humble.jar
    ```
3.  Compile the jar file into native executable.  
    Make sure to have `GRAALVM_HOME` env variable pointing to your GraalVM installation, e.g.:
    ```sh
    export GRAALVM_HOME="$HOME/Downloads/graalvm-jdk-21.0.1+12.1"
    ```
    Compile with:
    ```sh
    clj -T:build native
    ```
    Run the executable:
    ```sh
    target/humble
    ```

## Debuging issues

#### JNI Config

In case compilation or running the executable fails due to problems native libraries it might be worth to re-run the jar to let tracer agent collect JNI metadata (see [auto collection docs](https://www.graalvm.org/latest/reference-manual/native-image/metadata/AutomaticMetadataCollection/)):

```sh
clj -T:build uber
$GRAALVM_HOME/bin/java -agentlib:native-image-agent=config-output-dir=traced-config -jar target/humble.jar
```

#### Divide and conquer

It can be hard to diagnose what went wrong from the full example.
It helps to break the problem into small pieces that build on top of each other.
The main building blocks of HumbleUI are:
- [JWM](https://github.com/HumbleUI/JWM) which handles windows and input methods
- [Skija](https://github.com/HumbleUI/Skija) which does the actual graphics rendering

Levels:

1. JWM  
   Showing a blank window using JWM, can also climb up the compilation sub-levels 1 from 3.
   1. `clj -M -m example.jwm`
   2. `clj -T:build uber :main jwm` and `java -jar target/jwm.jar`
   3. `clj -T:build native :main jwm` and `target/jwm`
2. Skija  
   Once we can show a window, we can try to draw into it with Skija, using `example.skija` as main for 1-3 compilation levels.
3. HumbleUI  
   Once we can draw with Skija into JWM window, then we can try to compile HumbleUI using the original instructions to tie it all together.

## Resources

- [Clojure Graal Guide](https://github.com/clj-easy/graal-docs) introduces how to compile Clojure programs and explains concepts in more depth.
- [Humble Outliner](https://github.com/dundalek/humble-outliner) demo shows native build of a larger demo program.
