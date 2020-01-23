/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verifyNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

interface ChildStmtContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends Mutable<A, D, E> {
    @NonNull AbstractStmtContext<?, ?, ?> parent();

    @Override
    default @NonNull AbstractStmtContext<?, ?, ?> getParentContext() {
        return verifyNotNull(parent());
    }

    @Override
    default void setRootVersion(final YangVersion version) {
        getRoot().setRootVersion(version);
    }

    @Override
    default void addMutableStmtToSeal(final MutableStatement mutableStatement) {
        getRoot().addMutableStmtToSeal(mutableStatement);
    }

    @Override
    default void addRequiredSource(final SourceIdentifier dependency) {
        getRoot().addRequiredSource(dependency);
    }

    @Override
    default void setRootIdentifier(final SourceIdentifier identifier) {
        getRoot().setRootIdentifier(identifier);
    }

    @Override
    default boolean isEnabledSemanticVersioning() {
        return parent().isEnabledSemanticVersioning();
    }

    @Override
    default YangVersion getRootVersion() {
        return getRoot().getRootVersion();
    }

    @Override
    default Mutable<?, ?, ?> getRoot() {
        return parent().getRoot();
    }

}
