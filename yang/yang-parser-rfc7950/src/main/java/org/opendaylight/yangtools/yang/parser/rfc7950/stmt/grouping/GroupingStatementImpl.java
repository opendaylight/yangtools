/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.grouping;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class GroupingStatementImpl extends AbstractDeclaredStatement<QName>
        implements GroupingStatement {
    GroupingStatementImpl(final StmtContext<QName, GroupingStatement, ?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
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
    public Collection<? extends ActionStatement> getActions() {
        return allDeclared(ActionStatement.class);
    }

    @Override
    public final Collection<? extends NotificationStatement> getNotifications() {
        return allDeclared(NotificationStatement.class);
    }
}
