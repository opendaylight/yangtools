/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

module org.opendaylight.yangtools.yang.data.tree {
    exports org.opendaylight.yangtools.yang.data.tree.impl.di;
    exports org.opendaylight.yangtools.yang.data.tree.leafref;

    provides DataTreeFactory with InMemoryDataTreeFactory;

    requires transitive org.opendaylight.yangtools.yang.data.tree.api;
    requires org.opendaylight.yangtools.yang.data.tree.spi;
    requires org.opendaylight.yangtools.yang.data.impl;
    requires org.opendaylight.yangtools.yang.data.spi;
    requires org.opendaylight.yangtools.yang.data.util;
    requires org.slf4j;

    // Annotations
    requires static org.eclipse.jdt.annotation;
    requires static org.osgi.service.component.annotations;
    requires static javax.inject;
    requires static metainf.services;
}
