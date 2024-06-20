/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.binding.dom.codec.osgi {
    exports org.opendaylight.mdsal.binding.dom.codec.osgi;

    requires transitive org.opendaylight.mdsal.dom.schema.osgi;
    requires org.opendaylight.mdsal.binding.dom.codec.api;
    requires org.opendaylight.mdsal.binding.dom.codec.spi;
    requires org.opendaylight.mdsal.binding.runtime.osgi;
    requires org.opendaylight.yangtools.binding.lib;
    requires org.opendaylight.yangtools.yang.data.api;
    requires org.osgi.framework;
    requires org.osgi.service.component;
    requires org.slf4j;

    // Annotations
    requires static org.checkerframework.checker.qual;
    requires static org.osgi.service.component.annotations;
}
