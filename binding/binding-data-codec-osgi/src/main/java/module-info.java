/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.binding.data.codec.osgi {
    exports org.opendaylight.yangtools.binding.data.codec.osgi;

    requires transitive org.opendaylight.yangtools.binding.runtime.osgi;
    requires org.opendaylight.yangtools.binding.data.codec.api;
    requires org.opendaylight.yangtools.binding.data.codec.dynamic;
    requires org.opendaylight.yangtools.binding.data.codec.spi;
    requires org.opendaylight.yangtools.binding.spec;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.data.api;
    requires org.osgi.framework;
    requires org.osgi.service.component;
    requires org.slf4j;

    // Annotations
    requires static org.osgi.annotation.bundle;
    requires static org.osgi.service.component.annotations;
}
