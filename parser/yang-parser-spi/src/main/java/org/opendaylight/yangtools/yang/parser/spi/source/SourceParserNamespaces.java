/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.collect.SetMultimap;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Namespaces related to YANG/YIN source processing.
 */
public final class SourceParserNamespaces {
    /**
     * Source-specific mapping of belongsTo prefixes to module identifiers. This mapping allows source-specific context
     * to correctly populate prefixes map for actual parsing phase and eventually, resolve QName for any valid declared
     * statement.
     */
    public static final @NonNull ParserNamespace<String, StmtContext<?, ?, ?>> BELONGSTO_PREFIX_TO_MODULECTX =
        new ParserNamespace<>("belongsto-prefix-to-module");

    /**
     * Source-specific mapping of prefixes to namespaces.
     */
    // FIXME: bad javadoc
    public static final @NonNull ParserNamespace<String, Unqualified> BELONGSTO_PREFIX_TO_MODULE_NAME =
        new ParserNamespace<>("belongsto-prefix-to-name");

    /**
     * Namespace similar to {@link ParserNamespaces#MODULE} for storing modules into Yang model storage but keyed by
     * plain name.
     */
    // FIXME: Better name?
    public static final @NonNull StatementNamespace<Unqualified, ModuleStatement, ModuleEffectiveStatement>
        MODULE_FOR_BELONGSTO = new StatementNamespace<>("module-belongsto");

    /**
     * Pre-linkage source-specific mapping of prefixes to module namespaces.
     */
    // FIXME: a better name?
    public static final @NonNull ParserNamespace<String, XMLNamespace> IMP_PREFIX_TO_NAMESPACE =
        new ParserNamespace<>("prefix-to-xmlnamespace");

    /**
     * Source-specific mapping of prefix strings to module context.
     */
    public static final @NonNull ParserNamespace<String, StmtContext<?, ?, ?>> IMPORT_PREFIX_TO_MODULECTX =
        new ParserNamespace<>("import-prefix-to-modulectx");

    // FIXME: document this
    public static final @NonNull ParserNamespace<SourceIdentifier, StmtContext<?, ?, ?>> IMPORTED_MODULE =
        new ParserNamespace<>("imported-module");

    // FIXME: document this
    // FIXME: is this 'included submodule' instead?
    public static final @NonNull ParserNamespace<SourceIdentifier, StmtContext<?, ?, ?>> INCLUDED_MODULE =
        new ParserNamespace<>("included-module");

    /**
     * Source-specific mapping of prefixes to namespaces.
     */
    // FIXME: bad javadoc
    public static final @NonNull ParserNamespace<Unqualified, StmtContext<?, ?, ?>> INCLUDED_SUBMODULE_NAME_TO_MODULECTX
        = new ParserNamespace<>("included-submodule-to-modulectx");

    /**
     * Source-specific mapping of prefixes to namespaces.
     */
    // FIXME: bad javadoc
    public static final @NonNull ParserNamespace<Unqualified, QNameModule> MODULE_NAME_TO_QNAME =
        new ParserNamespace<>("module-name-to-qnamemodule");

    /**
     * Global mapping of modules to QNameModules.
     */
    public static final @NonNull ParserNamespace<StmtContext<?,?,?>, QNameModule> MODULECTX_TO_QNAME =
        new ParserNamespace<>("modulectx-to-qnamemodule");

    public static final @NonNull ParserNamespace<Empty, Set<QName>> SUPPORTED_FEATURES =
        new ParserNamespace<>("supportedFeatures");

    /**
     * Source-specific mapping of prefixes to namespaces. This namespace is populated by all statements which have
     * impact on the XML namespace, for example {@code import}, {@code belongs-to} and really anywhere a {@code prefix}
     * statement is present.
     *
     * @see PrefixResolver
     */
    public static final @NonNull ParserNamespace<String, QNameModule> PREFIX_TO_MODULE =
        new ParserNamespace<>("prefix-to-qnamemodule");

    /**
     * Namespace used for storing information about modules that support deviation resolution.
     * Map key (QNameModule) denotes a module which can be deviated by the modules specified in the Map value.
     */
    public static final @NonNull ParserNamespace<Empty, SetMultimap<QNameModule, QNameModule>> MODULES_DEVIATED_BY =
        new ParserNamespace<>("moduleDeviations");

    /**
     * Source-specific mapping of prefixes to namespaces.
     */
    // FIXME: bad javadoc
    public static final @NonNull ParserNamespace<QNameModule, Unqualified> MODULE_NAMESPACE_TO_NAME =
        new ParserNamespace<>("qnamemodule-to-name");

    /**
     * Pre-linkage global mapping of module names to namespaces.
     */
    public static final @NonNull ParserNamespace<Unqualified, XMLNamespace> MODULE_NAME_TO_NAMESPACE =
        new ParserNamespace<>("module-name-to-xmlnamespace");

    /**
     * Global mapping of modules to source identifier.
     */
    public static final @NonNull ParserNamespace<StmtContext<?, ?, ?>, SourceIdentifier> MODULECTX_TO_SOURCE =
        new ParserNamespace<>("modulectx-to-source");

    private SourceParserNamespaces() {
        // Hidden on purpose
    }
}
