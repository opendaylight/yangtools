/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.concepts {
    exports org.opendaylight.yangtools.concepts;

    requires transitive com.google.common;
    requires org.slf4j;

    // Optional OSGi integration
    requires static transitive org.osgi.core;

    // Annotations
    requires static com.github.spotbugs.annotations;
    requires static org.eclipse.jdt.annotation;
    requires static org.checkerframework.checker.qual;
}
