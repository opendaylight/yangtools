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

//    requires com.github.spotbugs.annotations;

    requires java.xml;
    // OMG, really?!
    requires java.desktop;

    requires com.google.common;
    requires modernizer.maven.annotations;
    requires org.immutables.value;
    requires org.opendaylight.yangtools.concepts;
    requires org.slf4j;
    requires tech.pantheon.triemap;
//    requires org.eclipse.jdt.annotation;
//    requires org.checkerframework.checker.qual;

}
