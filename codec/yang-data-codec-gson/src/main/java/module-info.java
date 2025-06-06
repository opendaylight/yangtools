/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Utilities for parsing JSON as YANG-modeled data and writing the same.
 */
module org.opendaylight.yangtools.yang.data.codec.gson {
    exports org.opendaylight.yangtools.yang.data.codec.gson;

    requires transitive com.google.gson;
    requires transitive org.opendaylight.yangtools.yang.data.api;

    requires com.google.common;
    requires java.xml;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.data.impl;
    requires org.opendaylight.yangtools.yang.data.spi;
    requires org.opendaylight.yangtools.yang.data.util;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.slf4j;

    // Annotations
    requires static com.github.spotbugs.annotations;
    requires static org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
}
