/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import com.google.common.collect.SetMultimap;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;

/**
 * Baseline {@link ParserNamespace}s mostly derived from YANG specification.
 */
public final class ParserNamespaces {
    /**
     * Extension namespace. All extension names defined in a module and its submodules share the same extension
     * identifier namespace, where each extension is identified by a QName formed from the defining module's QNameModule
     * and the identifier specified in extension statement's argument.
     */
    public static final @NonNull ParserNamespace<QName,
        StmtContext<QName, ExtensionStatement, ExtensionEffectiveStatement>> EXTENSION =
        new ParserNamespace<>("extension");

    /**
     * Feature namespace. All feature names defined in a module and its submodules share the same feature identifier
     * namespace. Each feature is identified by a QName formed from the defining module's QNameModule and the feature
     * name.
     */
    public static final @NonNull ParserNamespace<QName,
        StmtContext<QName, FeatureStatement, FeatureEffectiveStatement>> FEATURE = new ParserNamespace<>("feature");

    /**
     * Grouping namespace. * All grouping names defined within a parent node or at the top level of the module
     * or its submodules share the same grouping identifier namespace. This namespace is scoped to all
     * descendant nodes of the parent node or module.
     *
     * <p>
     * This means that any descendant node may use that grouping, and it MUST NOT define a grouping with the same name.
     */
    public static final @NonNull ParserNamespace<QName,
        StmtContext<QName, GroupingStatement, GroupingEffectiveStatement>> GROUPING = new ParserNamespace<>("grouping");

    /**
     * Identity namespace. All identity names defined in a module and its submodules share the same identity identifier
     * namespace.
     */
    public static final @NonNull ParserNamespace<QName,
        StmtContext<QName, IdentityStatement, IdentityEffectiveStatement>> IDENTITY = new ParserNamespace<>("identity");

    /**
     * Module namespace. All modules known to the reactor are populated to this namespace. Each module is identified
     * by a {@link SourceIdentifier}.
     */
    public static final @NonNull ParserNamespace<SourceIdentifier,
        StmtContext<Unqualified, ModuleStatement, ModuleEffectiveStatement>> MODULE = new ParserNamespace<>("module");

    /**
     * Submodule equivalent of {@link #MODULE}.
     */
    public static final @NonNull ParserNamespace<SourceIdentifier,
        StmtContext<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement>> SUBMODULE =
        new ParserNamespace<>("submodule");

    /**
     * Derived types namespace. All derived type names defined within a parent node or at the top level of the module
     * (or its submodules) share the same type identifier namespace.
     *
     * <p>
     * This namespace is scoped to all descendant nodes of the parent node or module. This means that any descendant
     * node may use that typedef, and it MUST NOT define a typedef with the same name.
     *
     * <p>
     * This namespace includes all type definitions implied by the language in which the current statement resides
     * (e.g. RFC6020/RFC7950 for YANG 1.0/1.1).
     */
    public static final @NonNull ParserNamespace<QName,
        StmtContext<QName, TypedefStatement, TypedefEffectiveStatement>> TYPE = new ParserNamespace<>("typedef");

    /**
     * A derived namespace allowing lookup of modules based on their {@link QNameModule}.
     */
    public static final @NonNull ParserNamespace<QNameModule,
        StmtContext<Unqualified, ModuleStatement, ModuleEffectiveStatement>> NAMESPACE_TO_MODULE =
        new ParserNamespace<>("namespace-to-module");

    /**
     * Intermediate-stage namespace equivalent to {@link #MODULE} except it is keyed by module names. This namespace is
     * used to resolve inter-module references before actual linkage occurs.
     */
    public static final @NonNull ParserNamespace<Unqualified,
        StmtContext<Unqualified, ModuleStatement, ModuleEffectiveStatement>> PRELINKAGE_MODULE =
        new ParserNamespace<>("prelinkage-module");

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
    public static final @NonNull ParserNamespace<Unqualified,
        StmtContext<Unqualified, ModuleStatement, ModuleEffectiveStatement>> MODULE_FOR_BELONGSTO =
        new ParserNamespace<>("module-belongsto");

    /**
     * Pre-linkage source-specific mapping of prefixes to module namespaces.
     */
    // FIXME: a better name?
    public static final @NonNull ParserNamespace<String, XMLNamespace> IMP_PREFIX_TO_NAMESPACE =
        new ParserNamespace<>("prefix-to-xmlnamespace");

    /**
     * Source-specific mapping of prefix strings to module context.
     */
    // FIXME: the context should expose ModuleStatement
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
    // FIXME: the context should expose SubmoduleStatement
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
    public static final @NonNull ParserNamespace<StmtContext<?, ?, ?>, QNameModule> MODULECTX_TO_QNAME =
        new ParserNamespace<>("modulectx-to-qnamemodule");

    public static final @NonNull ParserNamespace<Empty, FeatureSet> SUPPORTED_FEATURES =
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

    private static final @NonNull ParserNamespace<?, ?> SCHEMA_TREE = new ParserNamespace<>("schemaTree");

    /**
     * Statement local namespace, which holds direct schema node descendants. This corresponds to the contents of the
     * schema tree as exposed through {@link SchemaTreeAwareEffectiveStatement}.
     *
     * <p>
     * Unlike all other namespaces this namespaces is polymorphic, hence it is exposed throught this method.
     *
     * @return Schema tree namespace
     */
    @SuppressWarnings("unchecked")
    public static <D extends DeclaredStatement<QName>, E extends SchemaTreeEffectiveStatement<D>>
            @NonNull ParserNamespace<QName, StmtContext<QName, D, E>> schemaTree() {
        return (ParserNamespace<QName, StmtContext<QName, D, E>>) SCHEMA_TREE;
    }

    private ParserNamespaces() {
        // Hidden on purpose
    }

    /**
     * Find statement context identified by interpreting specified {@link SchemaNodeIdentifier} starting at specified
     * {@link StmtContext}.
     *
     * @param root Search root context
     * @param identifier {@link SchemaNodeIdentifier} relative to search root
     * @return Matching statement context, if present.
     * @throws NullPointerException if any of the arguments is null
     */
    public static Optional<StmtContext<?, ?, ?>> findSchemaTreeStatement(final StmtContext<?, ?, ?> root,
            final SchemaNodeIdentifier identifier) {
        final var iterator = identifier.getNodeIdentifiers().iterator();
        if (!iterator.hasNext()) {
            return Optional.of(root);
        }

        QName nextPath = iterator.next();
        var current = root.namespaceItem(schemaTree(), nextPath);
        if (current == null) {
            return Optional.ofNullable(tryToFindUnknownStatement(nextPath.getLocalName(), root));
        }
        while (current != null && iterator.hasNext()) {
            nextPath = iterator.next();
            final var nextNodeCtx = current.namespaceItem(schemaTree(), nextPath);
            if (nextNodeCtx == null) {
                return Optional.ofNullable(tryToFindUnknownStatement(nextPath.getLocalName(), current));
            }
            current = nextNodeCtx;
        }
        return Optional.ofNullable(current);
    }

    @SuppressWarnings("unchecked")
    private static StmtContext<?, ?, ?> tryToFindUnknownStatement(final String localName,
            final StmtContext<?, ?, ?> current) {
        final Collection<? extends StmtContext<?, ?, ?>> unknownSubstatements = StmtContextUtils.findAllSubstatements(
            current, UnknownStatement.class);
        for (final var unknownSubstatement : unknownSubstatements) {
            if (localName.equals(unknownSubstatement.rawArgument())) {
                return unknownSubstatement;
            }
        }
        return null;
    }
}
