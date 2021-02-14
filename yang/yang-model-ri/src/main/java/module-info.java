/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Reference implementation of {@code org.opendaylight.yangtools.yang.model.api} and related constructs.
 */
module org.opendaylight.yangtools.yang.model.ri {
    exports org.opendaylight.yangtools.yang.model.ri.stmt;
    exports org.opendaylight.yangtools.yang.model.ri.type;

    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.spi;
    requires com.google.common;
}
