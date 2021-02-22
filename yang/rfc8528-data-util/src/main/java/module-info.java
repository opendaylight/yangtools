/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
// 'rfc8528' in the name ends with a digit
@SuppressWarnings("module")
module org.opendaylight.yangtools.rfc8528.data.util {
    // FIXME: 7.0.0: correct this typo: rcf8525.data.util -> rfc8525.data.util
    exports org.opendaylight.yangtools.rcf8528.data.util;

    requires  org.opendaylight.yangtools.yang.data.api;
    requires  org.opendaylight.yangtools.rfc8528.data.api;
    requires com.google.common;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.rfc8528.model.api;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.data.util;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.spi;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.opendaylight.yangtools.yang.parser.api;
    requires org.slf4j;

    // Annotations
    requires static org.eclipse.jdt.annotation;
}
