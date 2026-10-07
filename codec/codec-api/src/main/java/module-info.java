/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * YANG-modeled data marshalling APIs.
 */
module org.opendaylight.yangtools.codec.api {
    exports org.opendaylight.yangtools.codec.api;

    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.rfc8040.model.api;
    requires transitive org.opendaylight.yangtools.rfc8791.model.api;
    requires transitive org.opendaylight.yangtools.yang.model.api;

    requires org.opendaylight.yangtools.yang.model.util;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
}
