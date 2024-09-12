/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.common.CanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Mapping of YANG language constructs to Java.
 *
 * @provides CanonicalValueSupport for core YANG types
 */
module org.opendaylight.yangtools.yang.common {
    exports org.opendaylight.yangtools.data;
    exports org.opendaylight.yangtools.yang.common;

    provides CanonicalValueSupport with
        Decimal64.Support,
        Uint8.Support,
        Uint16.Support,
        Uint32.Support,
        Uint64.Support;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.concepts;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.osgi.annotation.bundle;
}
