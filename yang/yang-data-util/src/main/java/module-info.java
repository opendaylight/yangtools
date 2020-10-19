/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.data.util {
    exports org.opendaylight.yangtools.yang.data.util;
    exports org.opendaylight.yangtools.yang.data.util.codec;

    requires transitive org.opendaylight.yangtools.odlext.model.api;
    requires transitive org.opendaylight.yangtools.rfc7952.data.api;
    requires transitive org.opendaylight.yangtools.rfc8528.data.api;

    requires org.opendaylight.yangtools.yang.model.util;
    requires org.opendaylight.yangtools.util;
}
