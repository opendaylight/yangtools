/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.binding.runtime.osgi {
    exports org.opendaylight.yangtools.binding.runtime.osgi;

    requires transitive org.opendaylight.yangtools.binding.runtime.api;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires com.google.common;
    requires org.opendaylight.yangtools.binding.runtime.spi;
    requires org.osgi.framework;
    requires org.osgi.service.component;
    requires org.osgi.util.tracker;
    requires org.slf4j;
    requires static org.apache.karaf.features.core;

    // Annotations
    requires static org.checkerframework.checker.qual;
    requires static org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
    requires static org.osgi.service.component.annotations;
}
