/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.parser.spi {
    exports org.opendaylight.yangtools.yang.parser.spi;
    exports org.opendaylight.yangtools.yang.parser.spi.meta;
    exports org.opendaylight.yangtools.yang.parser.spi.source;
    exports org.opendaylight.yangtools.yang.parser.spi.validation;

    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires org.slf4j;
}
