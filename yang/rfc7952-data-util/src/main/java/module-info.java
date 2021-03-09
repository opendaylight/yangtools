/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
// 'rfc7952' in the name ends with a digit
@SuppressWarnings("module")
module org.opendaylight.yangtools.rfc7952.data.util {
    exports org.opendaylight.yangtools.rfc7952.data.util;

    requires  org.opendaylight.yangtools.yang.data.util;
    requires com.google.common;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.rfc7952.data.api;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.data.api;
    requires org.opendaylight.yangtools.yang.model.api;

    // Annotations
    requires static org.eclipse.jdt.annotation;
}
