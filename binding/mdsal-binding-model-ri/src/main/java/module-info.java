/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.binding.model.ri {
    exports org.opendaylight.mdsal.binding.model.ri;
    exports org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

    requires transitive org.opendaylight.mdsal.binding.model.api;
    requires org.opendaylight.mdsal.binding.spec.util;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.binding;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.ri;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
}
