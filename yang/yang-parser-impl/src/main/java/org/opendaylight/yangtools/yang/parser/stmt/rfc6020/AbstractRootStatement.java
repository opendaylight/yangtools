/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BodyGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LinkageGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.MetaGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractRootStatement<T extends DeclaredStatement<String>>
        extends AbstractDeclaredStatement<String> implements LinkageGroup, MetaGroup, RevisionGroup, BodyGroup {

    protected AbstractRootStatement(final StmtContext<String, T,?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public final Collection<? extends ImportStatement> getImports() {
        return allDeclared(ImportStatement.class);
    }

    @Nonnull
    @Override
    public final Collection<? extends IncludeStatement> getIncludes() {
        return allDeclared(IncludeStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Override
    public final OrganizationStatement getOrganization() {
        return firstDeclared(OrganizationStatement.class);
    }

    @Override
    public final ContactStatement getContact() {
        return firstDeclared(ContactStatement.class);
    }

    @Nonnull
    @Override
    public final Collection<? extends RevisionStatement> getRevisions() {
        return allDeclared(RevisionStatement.class);
    }

    @Nonnull
    @Override
    public final Collection<? extends ExtensionStatement> getExtensions() {
        return allDeclared(ExtensionStatement.class);
    }

    @Nonnull
    @Override
    public final Collection<? extends FeatureStatement> getFeatures() {
        return allDeclared(FeatureStatement.class);
    }

    @Nonnull
    @Override
    public final Collection<? extends IdentityStatement> getIdentities() {
        return allDeclared(IdentityStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends TypedefStatement> getTypedefs() {
        return allDeclared(TypedefStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends GroupingStatement> getGroupings() {
        return allDeclared(GroupingStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends DataDefinitionStatement> getDataDefinitions() {
        return allDeclared(DataDefinitionStatement.class);
    }

    @Nonnull
    @Override
    public final Collection<? extends AugmentStatement> getAugments() {
        return allDeclared(AugmentStatement.class);
    }

    @Nonnull
    @Override
    public final Collection<? extends RpcStatement> getRpcs() {
        return allDeclared(RpcStatement.class);
    }

    @Nonnull
    @Override
    public final Collection<? extends NotificationStatement> getNotifications() {
        return allDeclared(NotificationStatement.class);
    }

    @Nonnull
    @Override
    public final Collection<? extends DeviationStatement> getDeviations() {
        return allDeclared(DeviationStatement.class);
    }
}
