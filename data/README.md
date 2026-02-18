# YANG data

This directory contains artifacts which deal with representing YANG-modeled data in ways similar
to how Java models XML data. These includes:

- an object model representation similar to W3C Document Object Model as found in **org.w3c.dom**
- corresponding stream similar Streaming APIs for XML as found in **java.xml.stream**
- an implementation of a YANG datastore, which has a number of convenient features:
  - transactions with multi-version concurrency control
  - optional enforcement of simple YANG structural constraints:
    - **mandatory**
    - **min-elements**/**max-elements**
