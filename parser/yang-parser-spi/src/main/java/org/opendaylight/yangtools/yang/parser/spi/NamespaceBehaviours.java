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
    public static final @NonNull NamespaceBehaviour<?, ?, ?> EXTENSION =
        NamespaceBehaviour.global(ParserNamespaces.EXTENSION);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> FEATURE =
        NamespaceBehaviour.global(ParserNamespaces.FEATURE);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> GROUPING =
        NamespaceBehaviour.treeScoped(ParserNamespaces.GROUPING);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> BEHAVIOUR =
        NamespaceBehaviour.global(ParserNamespaces.IDENTITY);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> MODULE =
        NamespaceBehaviour.global(ParserNamespaces.MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> SUBMODULE =
        NamespaceBehaviour.global(ParserNamespaces.SUBMODULE);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> TYPE =
        NamespaceBehaviour.treeScoped(ParserNamespaces.TYPE);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> NAMESPACE_TO_MODULE =
        NamespaceBehaviour.global(ParserNamespaces.NAMESPACE_TO_MODULE);

    public static final @NonNull NamespaceBehaviour<?, ?, ?> PRELINKAGE_MODULE =
        NamespaceBehaviour.global(ParserNamespaces.PRELINKAGE_MODULE);

    private NamespaceBehaviours() {
        // Hidden on purpose
    }
}
