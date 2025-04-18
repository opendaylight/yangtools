/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.binding.spec {
    exports org.opendaylight.yangtools.binding;
    exports org.opendaylight.yangtools.binding.annotations;
    exports org.opendaylight.yangtools.binding.contract;
    exports org.opendaylight.yangtools.binding.lib;
    exports org.opendaylight.yangtools.binding.meta;
    exports org.opendaylight.yangtools.binding.util;
    // FIXME: inhume this package
    exports org.opendaylight.yangtools.yang.binding;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.common;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.osgi.annotation.bundle;
}
