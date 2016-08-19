Vert.X for Ninja Framework
=======================================

## Overview

Standalone implementation for the [Ninja Framework](https://github.com/ninjaframework/ninja)
using Vert.X.  ninja-vertx-standalone does *not use servlets* under-the-hood -- therefore
it bypasses a significant amount of code that Ninja's default Jetty-based standalone
uses.  ninja-vertx-standalone is a drop-in replacement for Ninja's Jetty-based standalone.


## Differences with ninja-standalone (jetty)?

ninja-vertx-standalone does not implement any of Ninja's async-machine-beta features (which Ninja will 
probably deprecate in a future release).

ninja-vertx-standalone is compiled with Java 8, whereas Ninja supports Java 7+.


## Usage

ninja-vertx-standalone is on maven central.  The version will always be the Ninja
version it was compiled against + `Vert.X3` which represents the Vertx.X
build increment.  As of Ninja v5.4.0, as long as you don't have `ninja.standalone.NinjaJetty`
on your classpath, Ninja will automatically find ninja-vertx-standalone and use it
for everything (maven-plugin, testing, standalone). So if you previously had
a dependency on `ninja-standalone`, you'll want to make sure you are only
pulling `ninja-core` and `ninja-vertx-standalone`.

```xml
<dependency>
    <groupId>org.ninjaframework</groupId>
    <artifactId>ninja-core</artifactId>
    <version>5.7.0</version>
</dependency>
<dependency>
	<groupId>com.jiabangou</groupId>
    <artifactId>ninja-vertx-standalone</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```


## License

Copyright (C) 2016 Jiabangou, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.