/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.model.util {
    exports org.opendaylight.yangtools.yang.model.util;
    exports org.opendaylight.yangtools.yang.model.util.type;
    exports org.opendaylight.yangtools.yang.model.repo.util;

    requires transitive org.opendaylight.yangtools.rfc7952.model.api;

    requires org.opendaylight.yangtools.util;
    requires org.slf4j;
}
