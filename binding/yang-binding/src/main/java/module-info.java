/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.binding {
    exports org.opendaylight.yangtools.yang.binding;
    exports org.opendaylight.yangtools.yang.binding.annotations;
    exports org.opendaylight.yangtools.yang.binding.contract;
    exports org.opendaylight.yangtools.yang.binding.util;

    requires transitive org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.util;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.checkerframework.checker.qual;
}
