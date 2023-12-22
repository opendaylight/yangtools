/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.data.impl {
    // FIXME: do not export data.impl.*
    exports org.opendaylight.yangtools.yang.data.impl.codec;
    exports org.opendaylight.yangtools.yang.data.impl.schema;
    exports org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;
    exports org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid;
    exports org.opendaylight.yangtools.yang.data.impl.schema.nodes;

    provides org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeBuilderFactory
        with org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeBuilderFactory;

    requires transitive java.xml;
    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.data.api;
    requires transitive org.opendaylight.yangtools.yang.data.spi;
    requires transitive org.opendaylight.yangtools.yang.data.util;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.model.spi;

    requires org.opendaylight.yangtools.yang.model.util;
    requires org.opendaylight.yangtools.util;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static javax.inject;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.service.component.annotations;
}
