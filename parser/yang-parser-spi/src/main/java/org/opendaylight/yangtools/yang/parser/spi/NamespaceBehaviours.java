/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

/**
 * {@link NamespaceBehaviour}s corresponding to {@link ParserNamespaces}.
 */
public final class NamespaceBehaviours {
    public static final @NonNull NamespaceBehaviour<?, ?> EXTENSION =
        NamespaceBehaviour.global(ParserNamespaces.EXTENSION);

    public static final @NonNull NamespaceBehaviour<?, ?> FEATURE = NamespaceBehaviour.global(ParserNamespaces.FEATURE);

    public static final @NonNull NamespaceBehaviour<?, ?> GROUPING =
        NamespaceBehaviour.treeScoped(ParserNamespaces.GROUPING);

    public static final @NonNull NamespaceBehaviour<?, ?> IDENTITY =
        NamespaceBehaviour.global(ParserNamespaces.IDENTITY);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULE = NamespaceBehaviour.global(ParserNamespaces.MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?> SUBMODULE =
        NamespaceBehaviour.global(ParserNamespaces.SUBMODULE);

    public static final @NonNull NamespaceBehaviour<?, ?> TYPE = NamespaceBehaviour.treeScoped(ParserNamespaces.TYPE);

    public static final @NonNull NamespaceBehaviour<?, ?> SCHEMA_TREE = new SchemaTreeNamespaceBehaviour<>();

    public static final @NonNull NamespaceBehaviour<?, ?> BELONGSTO_PREFIX_TO_QNAME_MODULE =
        NamespaceBehaviour.sourceLocal(ParserNamespaces.BELONGSTO_PREFIX_TO_QNAME_MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?> BELONGSTO_PREFIX_TO_MODULECTX =
        NamespaceBehaviour.sourceLocal(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULECTX);

    public static final @NonNull NamespaceBehaviour<?, ?> INCLUDED_MODULE =
        NamespaceBehaviour.sourceLocal(ParserNamespaces.INCLUDED_MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULECTX_TO_QNAME =
        NamespaceBehaviour.global(ParserNamespaces.MODULECTX_TO_QNAME);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULECTX_TO_RESOLVED_INFO =
        NamespaceBehaviour.global(ParserNamespaces.MODULECTX_TO_RESOLVED_INFO);

    public static final @NonNull NamespaceBehaviour<?, ?> INCLUDED_SUBMODULE_NAME_TO_MODULECTX =
        NamespaceBehaviour.sourceLocal(ParserNamespaces.INCLUDED_SUBMODULE_NAME_TO_MODULECTX);

    public static final @NonNull NamespaceBehaviour<?, ?> IMPORT_PREFIX_TO_QNAME_MODULE =
        NamespaceBehaviour.rootStatementLocal(ParserNamespaces.IMPORT_PREFIX_TO_QNAME_MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?> IMPORT_PREFIX_TO_MODULECTX =
        NamespaceBehaviour.rootStatementLocal(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX);

    public static final @NonNull NamespaceBehaviour<?, ?> IMPORTED_MODULE =
        NamespaceBehaviour.sourceLocal(ParserNamespaces.IMPORTED_MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?> IMPORT_PREFIX_TO_SOURCE_ID =
        NamespaceBehaviour.rootStatementLocal(ParserNamespaces.IMPORT_PREFIX_TO_SOURCE_ID);

    public static final @NonNull NamespaceBehaviour<?, ?> SUPPORTED_FEATURES =
        NamespaceBehaviour.global(ParserNamespaces.SUPPORTED_FEATURES);

    public static final @NonNull NamespaceBehaviour<?, ?> MODULES_DEVIATED_BY =
        NamespaceBehaviour.global(ParserNamespaces.MODULES_DEVIATED_BY);

    private NamespaceBehaviours() {
        // Hidden on purpose
    }
}
