[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.opendaylight.yangtools/yangtools-artifacts/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.opendaylight.yangtools/yangtools-artifacts)
[![License](https://img.shields.io/badge/License-EPL%201.0-blue.svg)](https://opensource.org/licenses/EPL-1.0)

# YANG Tools

YANG Tools is to develop necessary tooling and libraries to provide Java runtime
and support for [YANG modeling language][RFC6020], data structures modeled by YANG and their
serialization and deserialization as defined in IETF drafts and standards.

## Current Features

* parsing of [YANG sources][RFC6020] and semantic inference of relationship across YANG models as defined in [RFC6020]
* representation of YANG-modeled data in Java
  * **DOM-like APIs** - DOM-like tree model, which uses conceptual meta-model
  * **Java Bindings** - Concrete data model generated from YANG models
* serialization / deserialization of YANG-modeled data driven by YANG models
  * XML - as defined in [RFC6020]
  * JSON - as defined in [draft-lhotka-netmod-yang-json-01]
* Integration of YANG model parsing into Maven build lifecycle and
  support for third-party generators processing  YANG models.


[RFC6020]:https://tools.ietf.org/html/rfc6020
[draft-lhotka-netmod-yang-json-01]:https://tools.ietf.org/html/draft-lhotka-netmod-yang-json-01
