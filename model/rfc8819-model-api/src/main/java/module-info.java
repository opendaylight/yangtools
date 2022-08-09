/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.rfc8819.model.api {
    exports org.opendaylight.yangtools.rfc8819.model.api;

    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires com.google.common;

    // Annotations
    requires static org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
}