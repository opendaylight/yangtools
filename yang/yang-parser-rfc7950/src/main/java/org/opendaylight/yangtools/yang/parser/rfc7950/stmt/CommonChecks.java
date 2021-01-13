/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Common checks shared by multiple statements.
 */
@Beta
public final class CommonChecks {
    private static final ImmutableSet<StatementDefinition> ILLEGAL_PARENTS = ImmutableSet.of(
        YangStmtMapping.NOTIFICATION, YangStmtMapping.RPC, YangStmtMapping.ACTION);

    private CommonChecks() {
        // Hidden on purpose
    }

    /**
     * Check whether an {@code action} or {@code notification} does not violate RFC7950 rules. This method potentially
     * creates an inference check.
     *
     * @param stmt Statement to check
     * @param name Statement's human-readable name
     * @throws SourceException if any parent is illegal
     */
    public static void checkActionNotificationNesting(final Mutable<QName, ?, ?> stmt, final String name) {
        final QName argument = stmt.getArgument();
        SourceException.throwIf(StmtContextUtils.hasAncestorOfType(stmt, ILLEGAL_PARENTS), stmt,
            "%s %s is defined within a notification, rpc or another action", name, argument);
        SourceException.throwIf(StmtContextUtils.hasParentOfType(stmt, YangStmtMapping.CASE), stmt,
            "%s %s is defined within a case statement", name, argument);

        // FIXME: hook a full declaration check for 'list' parents to ensure the key is present
//        SourceException.throwIf(
//            !StmtContextUtils.hasAncestorOfTypeWithChildOfType(stmt, YangStmtMapping.LIST, YangStmtMapping.KEY), stmt,
//            "Notification %s is defined within a list that has no key statement", argument);

    }

}
