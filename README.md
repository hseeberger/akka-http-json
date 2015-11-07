# akka-http-json #

[![Join the chat at https://gitter.im/hseeberger/akka-http-json](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/hseeberger/akka-http-json?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/hseeberger/akka-http-json.svg?branch=master)](https://travis-ci.org/hseeberger/akka-http-json)

akka-http-json provides JSON (un)marshalling support for [Akka](http://akka.io) HTTP. It offers support for the following JSON libraries:
- [Json4s](https://github.com/json4s/json4s)
- [Play JSON](https://www.playframework.com/documentation/2.4.x/ScalaJson)
- [uPickle](https://github.com/lihaoyi/upickle-pprint)

## Installation

akka-http-json 1.1.0 depends on akka-http 1.0 and 1.2.0 depends on akka-http 2.0-M1.

Grab it while it's hot:

``` scala
// All releases including intermediate ones are published here,
// final ones are also published to Maven Central.
resolvers += "hseeberger at bintray" at "http://dl.bintray.com/hseeberger/maven"

libraryDependencies ++= List(
  "de.heikoseeberger" %% "akka-http-json4s" % "1.2.0", // or 1.1.0 for akka-http 1.0
  ...
)
```

## Usage

Mix `PlayJsonSupport`, `Json4sSupport` or `UpickleSupport` into your Akka HTTP code which is supposed to (un)marshal from/to JSON. Don't forget to provide the necessary format type class instances.

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
