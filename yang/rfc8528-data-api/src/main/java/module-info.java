/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
// 'rfc8528' in the name ends with a digit
@SuppressWarnings("module")
module org.opendaylight.yangtools.rfc8528.data.api {
    exports org.opendaylight.yangtools.rfc8528.data.api;

    requires transitive org.opendaylight.yangtools.yang.data.api;
    requires transitive org.opendaylight.yangtools.yang.parser.api;

    requires org.opendaylight.yangtools.util;
}
