/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Object models of NETCONF/RESTCONF constructs and their YANG-assisted data binding operations.
 */
module org.opendaylight.netconf.databind {
    exports org.opendaylight.netconf.databind;
    exports org.opendaylight.netconf.databind.subtree;

    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.data.codec.gson;
    requires transitive org.opendaylight.yangtools.yang.data.codec.xml;
    requires transitive org.opendaylight.yangtools.yang.data.util;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires org.apache.commons.text;
    requires org.opendaylight.yangtools.yang.data.impl;
    requires org.opendaylight.yangtools.yang.data.spi;
    requires com.google.common;
    requires stax.utils;
    requires org.slf4j;

    // Annotation-only dependencies
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.osgi.annotation.bundle;
}
