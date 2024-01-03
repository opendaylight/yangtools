/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.data.api {
    exports org.opendaylight.yangtools.yang.data.api;
    exports org.opendaylight.yangtools.yang.data.api.codec;
    exports org.opendaylight.yangtools.yang.data.api.schema;
    exports org.opendaylight.yangtools.yang.data.api.schema.builder;
    exports org.opendaylight.yangtools.yang.data.api.schema.stream;
    exports org.opendaylight.yangtools.yang.data.api.schema.tree;

    requires transitive java.xml;
    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.util;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires org.slf4j;

    // Annotations
    requires static transitive java.compiler;
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.immutables.value.annotations;
}
