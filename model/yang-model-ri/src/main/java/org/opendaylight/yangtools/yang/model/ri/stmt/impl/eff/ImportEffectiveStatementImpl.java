/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;

public final class ImportEffectiveStatementImpl extends WithSubstatements<Unqualified, @NonNull ImportStatement>
        implements ImportEffectiveStatement, ModuleImport {
    private final @NonNull ModuleEffectiveStatement importedModule;

    public ImportEffectiveStatementImpl(final @NonNull ImportStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @NonNull ModuleEffectiveStatement importedModule) {
        super(declared, substatements);
        this.importedModule = requireNonNull(importedModule);
    }

    @Override
    public ModuleEffectiveStatement importedModule() {
        return importedModule;
    }

    @Override
    public Optional<Revision> getRevision() {
        return importedModule.localQNameModule().findRevision();
    }

    @Override
    public String getPrefix() {
        return declared().getPrefixStatement().argument();
    }

    @Override
    public ImportEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
