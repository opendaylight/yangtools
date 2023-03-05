/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.data.codec.binfmt {
    exports org.opendaylight.yangtools.yang.data.codec.binfmt;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.data.api;
    requires transitive org.opendaylight.yangtools.yang.data.spi;
    requires transitive org.opendaylight.yangtools.yang.data.tree.api;
    requires transitive org.opendaylight.yangtools.yang.data.tree.spi;
    requires java.xml;
    requires org.opendaylight.yangtools.yang.data.impl;
    requires org.opendaylight.yangtools.rfc8528.data.api;
    requires org.slf4j;

    // Annotations
    requires static com.github.spotbugs.annotations;
    requires static org.eclipse.jdt.annotation;
}
