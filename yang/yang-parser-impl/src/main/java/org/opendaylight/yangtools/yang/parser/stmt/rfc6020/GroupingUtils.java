/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GroupingUtils {

    private static final Logger LOG = LoggerFactory.getLogger(GroupingUtils.class);

    private GroupingUtils() {
        throw new UnsupportedOperationException();
    }

    private static final Set<Rfc6020Mapping> NOCOPY_FROM_GROUPING_SET = ImmutableSet.of(
        Rfc6020Mapping.DESCRIPTION,
        Rfc6020Mapping.REFERENCE,
        Rfc6020Mapping.STATUS);
    private static final Set<Rfc6020Mapping> REUSED_DEF_SET = ImmutableSet.of(
        Rfc6020Mapping.TYPE,
        Rfc6020Mapping.TYPEDEF,
        Rfc6020Mapping.USES);

    public static boolean needToCopyByUses(final StmtContext<?, ?, ?> stmtContext) {
        final StatementDefinition def = stmtContext.getPublicDefinition();
        if (REUSED_DEF_SET.contains(def)) {
            LOG.debug("Will reuse {} statement {}", def, stmtContext);
            return false;
        }
        if (NOCOPY_FROM_GROUPING_SET.contains(def)) {
            return !Rfc6020Mapping.GROUPING.equals(stmtContext.getParentContext().getPublicDefinition());
        }

        LOG.debug("Will copy {} statement {}", def, stmtContext);
        return true;
    }

    public static boolean isReusedByUses(final StmtContext<?, ?, ?> stmtContext) {
        return REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
    }
}
