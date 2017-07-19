/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc8040;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.SupportedExtensionsMapping;

/**
 * Declared statement representation of 'yang-data' extension defined in https://tools.ietf.org/html/rfc8040#section-8
 */
@Beta
public final class YangDataStatement extends AbstractDeclaredStatement<String> implements UnknownStatement<String> {
    static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
            SupportedExtensionsMapping.YANG_DATA)
            .addMandatory(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.USES)
            .build();

    YangDataStatement(final StmtContext<String, UnknownStatement<String>, ?> ctx) {
        super(ctx);
    }

    @Override
    public String getArgument() {
        return argument();
    }
}
