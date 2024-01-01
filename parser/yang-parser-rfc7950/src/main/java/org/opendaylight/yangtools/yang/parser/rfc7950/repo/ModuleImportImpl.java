/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;

/**
 * Utility implementation of {@link ModuleImport} to be used by {@link YangModelDependencyInfo}.
 */
// FIXME: this is a rather nasty misuse of APIs :(
final class ModuleImportImpl implements ModuleImport {
    private final UnresolvedQName.@NonNull Unqualified moduleName;
    private final Revision revision;

    ModuleImportImpl(final UnresolvedQName.@NonNull Unqualified moduleName, final @Nullable Revision revision) {
        this.moduleName = requireNonNull(moduleName, "Module name must not be null.");
        this.revision = revision;
    }

    @Override
    public UnresolvedQName.Unqualified getModuleName() {
        return moduleName;
    }

    @Override
    public Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
    }

    @Override
    public String getPrefix() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getReference() {
        return Optional.empty();
    }

    @Override
    public ImportEffectiveStatement asEffectiveStatement() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(moduleName);
        result = prime * result + Objects.hashCode(revision);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof ModuleImportImpl other
            && moduleName.equals(other.moduleName) && Objects.equals(revision, other.revision);
    }

    @Override
    public String toString() {
        return "ModuleImportImpl [name=" + moduleName + ", revision=" + revision + "]";
    }
}