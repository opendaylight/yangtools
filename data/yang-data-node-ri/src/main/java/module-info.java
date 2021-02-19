/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.data.ri.node {
    requires transitive org.opendaylight.yangtools.yang.data.api;

    // FIXME: hide this
    exports org.opendaylight.yangtools.yang.data.ri.node.impl;

    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.data.spi;
//  requires transitive org.opendaylight.yangtools.yang.data.api;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;


//    requires static com.github.spotbugs.annotations;
//    requires static org.osgi.service.component.annotations;
//    requires static javax.inject;
//    requires static metainf.services;
}
