/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImportedNamespaceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

/**
 * Namespaces related to YANG/YIN source processing.
 */
public final class SourceParserNamespaces {
    /**
     * Source-specific mapping of belongsTo prefixes to module identifiers. This mapping allows source-specific context
     * to correctly populate prefixes map for actual parsing phase and eventually, resolve QName for any valid declared
     * statement.
     */
    public static final @NonNull ImportedNamespaceContext<String> BELONGSTO_PREFIX_TO_MODULECTX =
        new ImportedNamespaceContext<>();

    /**
     * Source-specific mapping of prefixes to namespaces.
     */
    // FIXME: bad javadoc
    public static final @NonNull ParserNamespace<String, Unqualified> BELONGSTO_PREFIX_TO_MODULE_NAME =
        new ParserNamespace<>();

    // FIXME: document this
    public static final @NonNull ImportedNamespaceContext<SourceIdentifier> IMPORTED_MODULE =
        new ImportedNamespaceContext<>();

    // FIXME: document this
    // FIXME: is this 'included submodule' instead?
    public static final @NonNull ImportedNamespaceContext<SourceIdentifier> INCLUDED_MODULE =
        new ImportedNamespaceContext<>();

    /**
     * Source-specific mapping of prefixes to namespaces.
     */
    // FIXME: bad javadoc
    public static final @NonNull ParserNamespace<Unqualified, QNameModule> MODULE_NAME_TO_QNAME =
        new ParserNamespace<>();

    private SourceParserNamespaces() {
        // Hidden on purpose
    }
}
