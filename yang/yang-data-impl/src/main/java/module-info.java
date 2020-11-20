/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;

module org.opendaylight.yangtools.yang.data.impl {
    // FIXME: do not export data.impl.*
    exports org.opendaylight.yangtools.yang.data.impl.codec;
    exports org.opendaylight.yangtools.yang.data.impl.leafref;
    exports org.opendaylight.yangtools.yang.data.impl.schema;
    exports org.opendaylight.yangtools.yang.data.impl.schema.builder.api;
    exports org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;
    exports org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid;
    exports org.opendaylight.yangtools.yang.data.impl.schema.nodes;
    exports org.opendaylight.yangtools.yang.data.impl.schema.tree;

    provides DataTreeFactory with InMemoryDataTreeFactory;

    requires transitive org.opendaylight.yangtools.yang.data.api;
    requires transitive org.opendaylight.yangtools.rfc7952.data.api;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.rfc7952.data.util;
    requires org.opendaylight.yangtools.rfc8528.data.util;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.osgi.service.component.annotations;
    requires static javax.inject;
    requires static metainf.services;
}
