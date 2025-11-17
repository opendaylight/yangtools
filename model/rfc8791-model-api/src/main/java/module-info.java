/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * YANG metamodel extensions to support YANG Data Structure Extensions, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8791">RFC8791</a>.
 *
 * @since 14.0.21
 */
module org.opendaylight.yangtools.rfc8791.model.api {
    exports org.opendaylight.yangtools.rfc8791.model.api;

    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;

    // Annotations
    requires static org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
}
