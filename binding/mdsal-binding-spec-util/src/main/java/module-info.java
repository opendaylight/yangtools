/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.binding.spec.util {
    exports org.opendaylight.mdsal.binding.spec.reflect;

    uses org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;

    requires transitive org.opendaylight.yangtools.yang.binding;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.util;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.checkerframework.checker.qual;
}
