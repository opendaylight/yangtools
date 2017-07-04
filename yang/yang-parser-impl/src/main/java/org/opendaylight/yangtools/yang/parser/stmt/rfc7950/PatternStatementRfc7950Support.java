/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.PatternStatementImpl;

/**
 * Class providing necessary support for processing YANG 1.1 Pattern statement.
 */
@Beta
public final class PatternStatementRfc7950Support extends PatternStatementImpl.Definition {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .PATTERN)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.ERROR_APP_TAG)
            .addOptional(YangStmtMapping.ERROR_MESSAGE)
            .addOptional(YangStmtMapping.MODIFIER)
            .addOptional(YangStmtMapping.REFERENCE)
            .build();

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
