/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.binding.data.codec.spi {
    exports org.opendaylight.yangtools.binding.data.codec.spi;

    requires transitive org.opendaylight.yangtools.binding.data.codec.api;
    requires transitive org.opendaylight.yangtools.binding.runtime.api;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.checkerframework.checker.qual;
    requires static org.osgi.annotation.bundle;
}
