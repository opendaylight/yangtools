/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import com.google.common.base.VerifyException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedBelongsTo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;
import org.opendaylight.yangtools.yang.parser.source.ResolvedSourceInfo.SubmoduleBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

/**
 * A {@link SourceLinker} for a YANG {@code submodule}.
 */
final class SubmoduleLinker extends SourceLinker<SourceInfoRef.@NonNull OfSubmodule, @NonNull ResolvedSubmoduleInfo>
        implements SubmoduleBuilder {
    private @Nullable ModuleLinker parent;

    @NonNullByDefault
    SubmoduleLinker(final SourceInfoRef.OfSubmodule infoRef) {
        super(infoRef);
    }

    /**
     * {@return the module name specified by this submodule through {@link SourceInfo.Submodule#belongsTo()}}
     */
    @NonNullByDefault
    Unqualified parentName() {
        return sourceInfo().belongsTo().name();
    }

    /**
     * {@return the {@link ModuleLinker} corresponding to the parent module, or {@code null} if not yet determined}
     */
    @Nullable ModuleLinker parent() {
        return parent;
    }

    @Override
    boolean isResolved() {
        return parent != null && super.isResolved();
    }

    /**
     * Adds a {@link ModuleLinker} of the parent module this submodule belongs to.
     *
     * @param module {@link ModuleLinker} of the parent module.
     */
    @NonNullByDefault
    void resolveBelongsTo(final ModuleLinker module) throws ReactorException {
        final var local = parent;
        if (local != null) {
            throw new VerifyException("Attempted to re-resolve belongs-to from " + local + " to " + module);
        }

        // order of operations has implications on error reporting:
        // - we reject duplicate resolution, then
        // - we reject mismatch between proposed module and belongs-to module name, then
        // - we declare belongs-to resolved, and finally
        // - we inform the module of the include dependencies this submodule brings to the table
        final var parentName = parentName();
        if (!parentName.equals(module.name())) {
            throw new VerifyException("Attempted to resolve belongs-to " + parentName.getLocalName()
                + " with module " + module.humanName());
        }
        parent = module;
        module.requireIncludes(this);
    }

    @Override
    ResolvedSubmoduleInfo doBuild(final List<@NonNull ResolvedImport> resolvedImports,
            final List<@NonNull ResolvedInclude> resolveIncludes) {
        final var local = parent;
        if (local == null) {
            throw new VerifyException("Unresolved belongs-to in " + this);
        }
        final var parentRef = local.infoRef();
        final var infoRef = infoRef();
        return new ResolvedSubmoduleInfo(infoRef,
            new ResolvedBelongsTo(infoRef.info().belongsTo(), parentRef.ref(),
                parentRef.info().moduleName().getModule()), resolvedImports, resolveIncludes);
    }
}