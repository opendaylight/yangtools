/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.rfc8791.model.api.YangDataStructureStatements;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class AugmentStructureStatementRFC7950Support extends AbstractAugmentStructureStatementSupport {
    private static final AugmentStructureStatementRFC7950Support INSTANCE =
            new AugmentStructureStatementRFC7950Support();
    private static final SubstatementValidator VALIDATOR =
            SubstatementValidator.builder(YangDataStructureStatements.AUGMENT_STRUCTURE)
                .addOptional(YangStmtMapping.STATUS)
                .addOptional(YangStmtMapping.DESCRIPTION)
                .addOptional(YangStmtMapping.REFERENCE)
                .addAny(YangStmtMapping.TYPEDEF)
                .addAny(YangStmtMapping.GROUPING)
                .addAny(YangStmtMapping.CONTAINER)
                .addAny(YangStmtMapping.LEAF)
                .addAny(YangStmtMapping.LEAF_LIST)
                .addAny(YangStmtMapping.LIST)
                .addAny(YangStmtMapping.CHOICE)
                .addAny(YangStmtMapping.ANYXML)
                .addAny(YangStmtMapping.USES)
                .addAny(YangStmtMapping.CASE)
                .build();

    private AugmentStructureStatementRFC7950Support() {
        // Hidden on purpose
    }

    public static AugmentStructureStatementRFC7950Support getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return VALIDATOR;
    }
}
