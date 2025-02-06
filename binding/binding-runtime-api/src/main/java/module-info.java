/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Binding run-time APIs. These are centred around
 * {@link org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext} operating on
 * {@link org.opendaylight.yangtools.binding.runtime.api.RuntimeType}s.
 */
module org.opendaylight.yangtools.binding.runtime.api {
    exports org.opendaylight.yangtools.binding.runtime.api;

    requires transitive org.opendaylight.yangtools.binding.model;
    requires transitive org.opendaylight.yangtools.binding.spec;
    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.repo.api;
    requires transitive org.opendaylight.yangtools.rfc8040.model.api;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
}
