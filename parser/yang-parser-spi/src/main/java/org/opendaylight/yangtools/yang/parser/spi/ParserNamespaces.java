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
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace.TreeScoped;

/**
 * Baseline {@link ParserNamespace}s mostly derived from YANG specification.
 */
public final class ParserNamespaces {
    /**
     * Extension namespace. All extension names defined in a module and its submodules share the same extension
     * identifier namespace, where each extension is identified by a QName formed from the defining module's QNameModule
     * and the identifier specified in extension statement's argument.
     */
    public static final @NonNull StatementNamespace<QName, ExtensionStatement, ExtensionEffectiveStatement> EXTENSION =
        new StatementNamespace<>();

    /**
     * Feature namespace. All feature names defined in a module and its submodules share the same feature identifier
     * namespace. Each feature is identified by a QName formed from the defining module's QNameModule and the feature name.
     */
    public static final @NonNull StatementNamespace<QName, FeatureStatement, FeatureEffectiveStatement> FEATURE =
        new StatementNamespace<>();

    /**
     * Grouping namespace. * All grouping names defined within a parent node or at the top level of the module
     * or its submodules share the same grouping identifier namespace. This namespace is scoped to all
     * descendant nodes of the parent node or module.
     *
     * <p>
     * This means that any descendant node may use that grouping, and it MUST NOT define a grouping with the same name.
     */
    public static final @NonNull TreeScoped<QName, GroupingStatement, GroupingEffectiveStatement> GROUPING =
        new TreeScoped<>();

    /**
     * Identity namespace. All identity names defined in a module and its submodules share the same identity identifier
     * namespace.
     */
    public static final @NonNull StatementNamespace<QName, IdentityStatement, IdentityEffectiveStatement> IDENTITY =
        new StatementNamespace<>();

    /**
     * Module namespace. All modules known to the reactor are populated to this namespace. Each module is identified
     * by a {@link SourceIdentifier}.
     */
    public static final @NonNull StatementNamespace<SourceIdentifier, ModuleStatement, ModuleEffectiveStatement> MODULE
        = new StatementNamespace<>();

    /**
     * Submodule equivalent of {@link #MODULE}.
     */
    public static final @NonNull StatementNamespace<SourceIdentifier, SubmoduleStatement, SubmoduleEffectiveStatement>
        SUBMODULE = new StatementNamespace<>();

    /**
     * Derived types namespace. All derived type names defined within a parent node or at the top level of the module
     * (or its submodules) share the same type identifier namespace.
     *
     * <p>
     * This namespace is scoped to all descendant nodes of the parent node or module. This means that any descendant node
     * may use that typedef, and it MUST NOT define a typedef with the same name.
     *
     * <p>
     * This namespace includes all type definitions implied by the language in which the current statement resides
     * (e.g. RFC6020/RFC7950 for YANG 1.0/1.1).
     */
    public static final @NonNull TreeScoped<QName, TypedefStatement, TypedefEffectiveStatement> TYPE =
        new TreeScoped<>();

    /**
     * A derived namespace allowing lookup of modules based on their {@link QNameModule}.
     */
    public static final @NonNull StatementNamespace<QNameModule, ModuleStatement, ModuleEffectiveStatement>
        NAMESPACE_TO_MODULE = new StatementNamespace<>();

    /**
     * Intermediate-stage namespace equivalent to {@link #MODULE} except it is keyed by module names. This namespace is
     * used to resolve inter-module references before actual linkage occurs.
     */
    public static final @NonNull StatementNamespace<Unqualified, ModuleStatement, ModuleEffectiveStatement>
        PRELINKAGE_MODULE = new StatementNamespace<>();

    private ParserNamespaces() {
        // Hidden on purpose
    }
}
