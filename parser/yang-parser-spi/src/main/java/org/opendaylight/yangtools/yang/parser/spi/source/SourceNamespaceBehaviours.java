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
    public static final @NonNull NamespaceBehaviour<?, ?> BELONGSTO_PREFIX_TO_MODULECTX =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.BELONGSTO_PREFIX_TO_MODULECTX);

    public static final @NonNull NamespaceBehaviour<?, ?> BELONGSTO_PREFIX_TO_MODULE_NAME =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULE_NAME_TO_QNAME =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.MODULE_NAME_TO_QNAME);

    public static final @NonNull NamespaceBehaviour<?, ?> IMPORTED_MODULE =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.IMPORTED_MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?> INCLUDED_MODULE =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.INCLUDED_MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULECTX_TO_QNAME =
        NamespaceBehaviour.global(SourceParserNamespaces.MODULECTX_TO_QNAME);

    public static final @NonNull NamespaceBehaviour<?, ?> INCLUDED_SUBMODULE_NAME_TO_MODULECTX =
        NamespaceBehaviour.sourceLocal(SourceParserNamespaces.INCLUDED_SUBMODULE_NAME_TO_MODULECTX);

    public static final @NonNull NamespaceBehaviour<?, ?> IMP_PREFIX_TO_NAMESPACE =
        NamespaceBehaviour.rootStatementLocal(SourceParserNamespaces.IMP_PREFIX_TO_NAMESPACE);

    public static final @NonNull NamespaceBehaviour<?, ?> IMPORT_PREFIX_TO_MODULECTX =
        NamespaceBehaviour.rootStatementLocal(SourceParserNamespaces.IMPORT_PREFIX_TO_MODULECTX);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULE_FOR_BELONGSTO =
        NamespaceBehaviour.global(SourceParserNamespaces.MODULE_FOR_BELONGSTO);

    public static final @NonNull NamespaceBehaviour<?, ?> SUPPORTED_FEATURES =
        NamespaceBehaviour.global(SourceParserNamespaces.SUPPORTED_FEATURES);

    public static final @NonNull NamespaceBehaviour<?, ?> PREFIX_TO_MODULE =
        NamespaceBehaviour.global(SourceParserNamespaces.PREFIX_TO_MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULES_DEVIATED_BY =
        NamespaceBehaviour.global(SourceParserNamespaces.MODULES_DEVIATED_BY);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULE_NAMESPACE_TO_NAME =
        NamespaceBehaviour.global(SourceParserNamespaces.MODULE_NAMESPACE_TO_NAME);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULE_NAME_TO_NAMESPACE =
        NamespaceBehaviour.global(SourceParserNamespaces.MODULE_NAME_TO_NAMESPACE);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULECTX_TO_SOURCE =
        NamespaceBehaviour.global(SourceParserNamespaces.MODULECTX_TO_SOURCE);

    private SourceNamespaceBehaviours() {
        // Hidden on purpose
    }
}
