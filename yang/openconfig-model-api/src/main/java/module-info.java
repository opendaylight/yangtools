/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.openconfig.model.api {
    exports org.opendaylight.yangtools.openconfig.model.api;

    requires transitive org.opendaylight.yangtools.yang.model.api;

    // Annotations
    requires static com.github.spotbugs.annotations;
}
