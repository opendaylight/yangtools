/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.binding.model.api {
    exports org.opendaylight.mdsal.binding.model.api;
    exports org.opendaylight.mdsal.binding.model.api.type.builder;

    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
}
