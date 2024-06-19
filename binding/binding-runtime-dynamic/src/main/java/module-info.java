/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * A ClassLoader for binding constructs. It allows loading of runtime-generated classes with a combined visibility with
 * a set of parent class loaders.
 */
module org.opendaylight.mdsal.binding.loader {
    exports org.opendaylight.mdsal.binding.loader;

    requires transitive net.bytebuddy;

    requires com.google.common;
    requires org.opendaylight.yangtools.yang.binding;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
}
