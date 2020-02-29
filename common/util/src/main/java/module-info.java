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

    requires transitive java.xml;

    requires org.opendaylight.yangtools.concepts;
    requires org.slf4j;
    requires tech.pantheon.triemap;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static java.compiler;
    requires static java.management;
    requires static org.checkerframework.checker.qual;
    requires static org.gaul.modernizer_maven_annotations;
    requires static org.immutables.value.annotations;
}
