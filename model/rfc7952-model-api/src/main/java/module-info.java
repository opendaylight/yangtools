/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Object model of RFC7952 metadata statements.
 */
// 'rfc7952' in the name ends with a digit
@SuppressWarnings("module")
module org.opendaylight.yangtools.rfc7952.model.api {
    exports org.opendaylight.yangtools.rfc7952.model.api;

    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires com.google.common;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
}
