/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.util {
    exports org.opendaylight.yangtools.util;
    exports org.opendaylight.yangtools.util.concurrent;
    exports org.opendaylight.yangtools.util.xml;

    requires java.xml;

    requires com.google.common;
    requires org.immutables.value;
    requires org.opendaylight.yangtools.concepts;
    requires org.slf4j;
    requires tech.pantheon.triemap;

    // Annotations
    // FIXME: deal with these
    requires static error.prone.annotations;
    requires static modernizer.maven.annotations;

    requires static com.github.spotbugs.annotations;
    requires static java.compiler;
    requires static java.management;
    requires static org.eclipse.jdt.annotation;
    requires static org.checkerframework.checker.qual;
}
