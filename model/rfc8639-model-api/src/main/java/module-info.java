/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
// 'rfc8639' in the name ends with a digit
@SuppressWarnings("module")
module org.opendaylight.yangtools.rfc8639.model.api {
    exports org.opendaylight.yangtools.rfc8639.model.api;

    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
}
