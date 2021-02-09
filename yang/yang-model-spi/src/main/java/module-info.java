/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.model.spi {
    exports org.opendaylight.yangtools.yang.model.spi.meta;
    exports org.opendaylight.yangtools.yang.model.spi.stmt;
    exports org.opendaylight.yangtools.yang.model.spi.type;

    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.xpath.api;
    requires com.google.common;
    requires org.opendaylight.yangtools.yang.common;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
}
