[![Maven Central](https://maven-badges.sml.io/maven-central/org.opendaylight.yangtools/yangtools-artifacts/badge.svg)](https://maven-badges.sml.io/maven-central/org.opendaylight.yangtools/yangtools-artifacts)
[![Javadocs](https://www.javadoc.io/badge/org.opendaylight.yangtools/yangtools-docs.svg)](https://www.javadoc.io/doc/org.opendaylight.yangtools/yangtools-docs)
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
  * XML - as defined in [RFC6020] and revised in [RFC7950]
  * JSON - as defined in [draft-lhotka-netmod-yang-json-01] and standardized in [RFC7951]
* Integration of YANG model parsing into Maven build lifecycle and
  support for third-party generators processing  YANG models.


## The gory details
There are sorts of things here:
* basic project infrastructure, including
  * [the BOM](artifacts)
  * [bnd-based parent](bnd-parent)
  * [bundle-based parent](bnd-parent)
  * [our documentation subproject](docs)
  * [Karaf features](features)
  * [Karaf distribution](karaf) meant for local testing
* a few [baseline libraries](common) useful without much other context
* a YANG-opinionated view of [XML components](xml)
* our take on a [YANG metamodel](model) supporting both as-declared and as-effective views on a set of YANG/YIN files
* corresponding [YANG parser](parser), which really is a compiler, capable of turning a set of YANG/YIN file sources
  into a YANG metamodel instance
* our take on a model of [YANG-normalized data](data) and its streaming format
* corresponding [serialization codecs](codec) to and from various serialization formats
* our take on type-safe [Java Bindings for YANG](binding) with split compile-time and run-time parts
* a [Maven plugin](plugin) for packaging YANG files with derived code and resources


[RFC6020]:https://tools.ietf.org/html/rfc6020
[RFC7950]:https://tools.ietf.org/html/rfc7950
[RFC7951]:https://tools.ietf.org/html/rfc7951
[draft-lhotka-netmod-yang-json-01]:https://tools.ietf.org/html/draft-lhotka-netmod-yang-json-01
