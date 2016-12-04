# akka-http-json #

[![Join the chat at https://gitter.im/hseeberger/akka-http-json](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/hseeberger/akka-http-json?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/hseeberger/akka-http-json.svg?branch=master)](https://travis-ci.org/hseeberger/akka-http-json)
[![Maven Central](https://img.shields.io/maven-central/v/de.heikoseeberger/akka-http-circe_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/de.heikoseeberger/akka-http-circe_2.12)

akka-http-json provides JSON (un)marshalling support for [Akka](http://akka.io) HTTP. It offers support for the following JSON libraries:
- [Argonaut](http://argonaut.io)
- [circe](https://github.com/travisbrown/circe)
- [Jackson](https://github.com/FasterXML/jackson) via [Scala Module](https://github.com/FasterXML/jackson-module-scala) by default
- [Json4s](https://github.com/json4s/json4s)
- [Play JSON](https://www.playframework.com/documentation/2.5.x/ScalaJson) (currently not available, because no support for Scala 2.12)
- [uPickle](https://github.com/lihaoyi/upickle-pprint)

## Installation

Grab it while it's hot:

``` scala
// All releases including intermediate ones are published here,
// final ones are also published to Maven Central.
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= List(
  "de.heikoseeberger" %% "akka-http-circe" % "1.11.0",
  ...
)
```

## Usage

Mix `ArgonautSupport`, `CirceSupport`, `JacksonSupport`, `Json4sSupport`, `PlayJsonSupport`, `UpickleSupport` or into your Akka HTTP code which is supposed to (un)marshal from/to JSON. Don't forget to provide the type class instances for the respective JSON libraries, if needed.

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License ##

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
