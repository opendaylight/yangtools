/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

/**
 * Namespaces and beviours common to all RFC7950 parsers.
 */
public final class Namespaces {
    /**
     * Extension namespace. All extension names defined in a module and its submodules share the same extension
     * identifier namespace, where each extension is identified by a QName formed from the defining module's QNameModule
     * and the identifier specified in extension statement's argument.
     */
    public static final @NonNull StatementNamespace<QName, ExtensionStatement, ExtensionEffectiveStatement> EXTENSION =
        new StatementNamespace<>();

    public static final @NonNull NamespaceBehaviour<?, ?, ?> EXTENSION_BEHAVIOUR =
        NamespaceBehaviour.global(EXTENSION);

    /**
     * Feature namespace. All feature names defined in a module and its submodules share the same feature identifier
     * namespace. Each feature is identified by a QName formed from the defining module's QNameModule and the feature
     * name.
     */
    public static final @NonNull StatementNamespace<QName, FeatureStatement, FeatureEffectiveStatement> FEATURE =
        new StatementNamespace<>();

    public static final @NonNull NamespaceBehaviour<?, ?, ?> FEATURE_BEHAVIOUR =
        NamespaceBehaviour.global(FEATURE);

    private Namespaces() {
        // Hidden on purpose
    }
}
