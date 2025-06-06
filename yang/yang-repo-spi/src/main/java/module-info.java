/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.repo.spi {
    exports org.opendaylight.yangtools.yang.model.repo.spi;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.repo.api;

    requires org.opendaylight.yangtools.util;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.checkerframework.checker.qual;
    requires static org.osgi.annotation.bundle;
}
