/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviate;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

/**
 * Class providing necessary support for processing YANG 1.1 deviate statement.
 */
@Beta
public final class DeviateStatementRFC7950Support extends AbstractDeviateStatementSupport {

    private static final SubstatementValidator DEVIATE_ADD_SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.DEVIATE)
                .addOptional(YangStmtMapping.CONFIG)
                .addAny(YangStmtMapping.DEFAULT)
                .addOptional(YangStmtMapping.MANDATORY)
                .addOptional(YangStmtMapping.MAX_ELEMENTS)
                .addOptional(YangStmtMapping.MIN_ELEMENTS)
                .addAny(YangStmtMapping.MUST)
                .addAny(YangStmtMapping.UNIQUE)
                .addOptional(YangStmtMapping.UNITS)
                .build();

    private static final SubstatementValidator DEVIATE_DELETE_SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.DEVIATE)
                .addAny(YangStmtMapping.DEFAULT)
                .addAny(YangStmtMapping.MUST)
                .addAny(YangStmtMapping.UNIQUE)
                .addOptional(YangStmtMapping.UNITS)
                .build();
    private static final DeviateStatementRFC7950Support INSTANCE = new DeviateStatementRFC7950Support();

    private DeviateStatementRFC7950Support() {
        // Hidden
    }

    public static DeviateStatementRFC7950Support getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidatorForDeviate(final DeviateKind deviateKind) {
        switch (deviateKind) {
            case NOT_SUPPORTED:
                return super.getSubstatementValidatorForDeviate(deviateKind);
            case ADD:
                return DEVIATE_ADD_SUBSTATEMENT_VALIDATOR;
            case REPLACE:
                return super.getSubstatementValidatorForDeviate(deviateKind);
            case DELETE:
                return DEVIATE_DELETE_SUBSTATEMENT_VALIDATOR;
            default:
                throw new IllegalStateException(String.format(
                        "Substatement validator for deviate %s has not been defined.", deviateKind));
        }
    }
}
