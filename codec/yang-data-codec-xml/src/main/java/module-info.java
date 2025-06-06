/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Utilities for parsing XML as YANG-modeled data and writing the same.
 */
module org.opendaylight.yangtools.yang.data.codec.xml {
    exports org.opendaylight.yangtools.yang.data.codec.xml;

    requires transitive java.xml;
    requires transitive org.codehaus.stax2;
    requires transitive org.opendaylight.yangtools.yang.data.api;
    requires transitive org.opendaylight.yangtools.yang.data.util;
    requires transitive org.opendaylight.yangtools.yang.model.api;

    requires com.google.common;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.data.impl;
    requires org.opendaylight.yangtools.yang.data.spi;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.opendaylight.yangtools.rfc7952.model.api;
    requires org.opendaylight.yangtools.rfc8528.model.api;
    requires org.slf4j;

    // Annotations
    requires static org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
}
