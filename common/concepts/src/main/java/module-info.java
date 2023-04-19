/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Concepts used widely across OpenDaylight code base.
 */
module org.opendaylight.yangtools.concepts {
    exports org.opendaylight.yangtools.concepts;

    requires transitive com.google.common;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static transitive org.checkerframework.checker.qual;
    requires static com.github.spotbugs.annotations;
    requires static org.osgi.annotation.bundle;
}
