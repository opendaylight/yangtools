/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.data.api {
    exports org.opendaylight.yangtools.yang.data.api;
    exports org.opendaylight.yangtools.yang.data.api.codec;
    exports org.opendaylight.yangtools.yang.data.api.schema;
    exports org.opendaylight.yangtools.yang.data.api.schema.stream;
    exports org.opendaylight.yangtools.yang.data.api.schema.tree;
    exports org.opendaylight.yangtools.yang.data.api.schema.tree.spi;
    exports org.opendaylight.yangtools.yang.data.api.schema.xpath;

    // FIXME: Audit whether we get these as transitive
    requires transitive java.xml;
    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;

    // FIXME: Audit whether we should make these transitive
    requires org.opendaylight.yangtools.util;

    // Annotations
    requires static com.github.spotbugs.annotations;
    requires static org.eclipse.jdt.annotation;
    requires static org.slf4j;
}
