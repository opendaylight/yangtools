/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin;

final class ImportEffectiveStatementImpl extends WithSubstatements<String, ImportStatement>
        implements ImportEffectiveStatement, ModuleImport, DocumentedNodeMixin<String, ImportStatement> {
    private final @Nullable Revision revision;
    private final @Nullable SemVer semVer;

    ImportEffectiveStatementImpl(final ImportStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable Revision revision, final @Nullable SemVer semVer) {
        super(declared, substatements);
        this.revision = revision;
        this.semVer = semVer;
    }

    @Override
    public String getModuleName() {
        return argument();
    }

    @Override
    public Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
    }

    @Override
    public Optional<SemVer> getSemanticVersion() {
        return Optional.ofNullable(semVer);
    }

    @Override
    public String getPrefix() {
        return getDeclared().getPrefix().getValue();
    }

    @Override
    public ImportEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("moduleName", getModuleName())
                .add("revision", revision)
                .add("version", semVer)
                .add("prefix", getPrefix())
                .add("description", getDescription().orElse(null))
                .add("reference", getReference().orElse(null))
                .toString();
    }
}
