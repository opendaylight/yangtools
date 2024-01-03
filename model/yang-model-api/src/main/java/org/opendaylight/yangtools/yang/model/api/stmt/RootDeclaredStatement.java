/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

/**
 * Common interface capturing general layout of a top-level YANG declared statement -- either a {@link ModuleStatement}
 * or a {@link SubmoduleStatement}.
 */
@Beta
public sealed interface RootDeclaredStatement
        extends DocumentedDeclaredStatement<Unqualified>, NotificationStatementAwareDeclaredStatement<Unqualified>,
                DataDefinitionAwareDeclaredStatement.WithReusableDefinitions<Unqualified>
        permits ModuleStatement, SubmoduleStatement {
    @Override
    Unqualified argument();

    default Optional<OrganizationStatement> getOrganization() {
        return findFirstDeclaredSubstatement(OrganizationStatement.class);
    }

    default Optional<ContactStatement> getContact() {
        return findFirstDeclaredSubstatement(ContactStatement.class);
    }

    default @NonNull Collection<? extends ImportStatement> getImports() {
        return declaredSubstatements(ImportStatement.class);
    }

    default @NonNull Collection<? extends IncludeStatement> getIncludes() {
        return declaredSubstatements(IncludeStatement.class);
    }

    default @NonNull Collection<? extends RevisionStatement> getRevisions() {
        return declaredSubstatements(RevisionStatement.class);
    }

    default @NonNull Collection<? extends ExtensionStatement> getExtensions() {
        return declaredSubstatements(ExtensionStatement.class);
    }

    default @NonNull Collection<? extends FeatureStatement> getFeatures() {
        return declaredSubstatements(FeatureStatement.class);
    }

    default @NonNull Collection<? extends IdentityStatement> getIdentities() {
        return declaredSubstatements(IdentityStatement.class);
    }

    default @NonNull Collection<? extends AugmentStatement> getAugments() {
        return declaredSubstatements(AugmentStatement.class);
    }

    default @NonNull Collection<? extends RpcStatement> getRpcs() {
        return declaredSubstatements(RpcStatement.class);
    }

    default @NonNull Collection<? extends DeviationStatement> getDeviations() {
        return declaredSubstatements(DeviationStatement.class);
    }
}
