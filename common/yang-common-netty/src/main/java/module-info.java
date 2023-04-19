/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Common utilities bridging common YANG constructs with Netty.
 */
module org.opendaylight.yangtools.yang.common.netty {
    exports org.opendaylight.yangtools.yang.common.netty;

    requires transitive io.netty.buffer;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires com.google.common;
    requires io.netty.common;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
}
