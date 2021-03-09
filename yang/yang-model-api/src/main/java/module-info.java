/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.model.api {
    exports org.opendaylight.yangtools.yang.model.api;
    exports org.opendaylight.yangtools.yang.model.api.meta;
    exports org.opendaylight.yangtools.yang.model.api.stmt;
    exports org.opendaylight.yangtools.yang.model.api.stmt.compat;
    exports org.opendaylight.yangtools.yang.model.api.type;

    requires  org.opendaylight.yangtools.yang.xpath.api;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.common;
    requires org.slf4j;

    // Annotations
    requires static  org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
}
