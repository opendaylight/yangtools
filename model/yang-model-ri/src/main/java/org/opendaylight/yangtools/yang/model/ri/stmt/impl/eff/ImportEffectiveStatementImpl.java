/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin;

public final class ImportEffectiveStatementImpl extends WithSubstatements<String, ImportStatement>
        implements ImportEffectiveStatement, ModuleImport, DocumentedNodeMixin<String, ImportStatement> {
    private final @Nullable Revision revision;

    public ImportEffectiveStatementImpl(final ImportStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @NonNull SourceIdentifier importedSource) {
        super(declared, substatements);
        revision = importedSource.getRevision().orElse(null);
    }

    @Override
    public Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
    }

    @Override
    public Optional<SemVer> getSemanticVersion() {
        return Optional.empty();
    }

    @Override
    public String getPrefix() {
        return getDeclared().getPrefix().argument();
    }

    @Override
    public ImportEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
