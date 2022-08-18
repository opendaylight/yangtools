/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

/**
 * {@link NamespaceBehaviour}s corresponding to {@link SourceParserNamespaces}.
 */
public final class SourceNamespaceBehaviours {
    public static final @NonNull NamespaceBehaviour<?, ?, ?> BELONGSTO_PREFIX_TO_MODULECTX =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.BELONGSTO_PREFIX_TO_MODULECTX);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> BELONGSTO_PREFIX_TO_MODULE_NAME =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> MODULE_NAME_TO_QNAME =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.MODULE_NAME_TO_QNAME);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> IMPORTED_MODULE =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.IMPORTED_MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> INCLUDED_MODULE =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.INCLUDED_MODULE);

//    public static final @NonNull NamespaceBehaviour<?, ?, ?>
//
//    public static final @NonNull NamespaceBehaviour<?, ?, ?>
//
//    public static final @NonNull NamespaceBehaviour<?, ?, ?>
//
//    public static final @NonNull NamespaceBehaviour<?, ?, ?>
//
//    public static final @NonNull NamespaceBehaviour<?, ?, ?>

    private SourceNamespaceBehaviours() {
        // Hidden on purpose
    }
}
