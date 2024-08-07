# akka-http-json #

Attention: This project is no longer actively maintained. Feel free to fork it or use [Pekko](https://pekko.apache.org/docs/pekko-http/current/common/json-support.html).

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/de.heikoseeberger/akka-http-circe_2.13)](https://search.maven.org/artifact/de.heikoseeberger/akka-http-circe_2.13)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

akka-http-json provides JSON (un)marshalling support for [Akka HTTP](https://github.com/akka/akka-http) via the following JSON libraries:
- [Argonaut](http://argonaut.io)
- [avro4s](https://github.com/sksamuel/avro4s)
- [AVSystem GenCodec](https://github.com/AVSystem/scala-commons/blob/master/docs/GenCodec.md)
- [circe](https://circe.github.io/circe/)
- [Jackson](https://github.com/FasterXML/jackson) via [Scala Module](https://github.com/FasterXML/jackson-module-scala) by default
- [Json4s](https://github.com/json4s/json4s)
- [jsoniter-scala](https://github.com/plokhotnyuk/jsoniter-scala)
- [ninny](https://nrktkt.github.io/ninny-json/USERGUIDE)
- [Play JSON](https://www.playframework.com/documentation/2.6.x/ScalaJson)
- [uPickle](https://github.com/lihaoyi/upickle-pprint)
- [zio-json](https://github.com/zio/zio-json)

## Installation

The artifacts are published to Maven Central.

``` scala
libraryDependencies ++= Seq(
  "de.heikoseeberger" %% "akka-http-circe" % AkkaHttpJsonVersion,
  ...
)
```

## Usage

Use respective support trait or object, e.g. `ArgonautSupport`, `FailFastCirceSupport`, etc. into your Akka HTTP code which is supposed to (un)marshal from/to JSON. Don't forget to provide the type class instances for the respective JSON libraries, if needed.

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License ##

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).

[![YourKit](https://www.yourkit.com/images/yklogo.png)](https://www.yourkit.com)

YourKit supports open source projects with its full-featured Java Profiler. YourKit, LLC is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler).
