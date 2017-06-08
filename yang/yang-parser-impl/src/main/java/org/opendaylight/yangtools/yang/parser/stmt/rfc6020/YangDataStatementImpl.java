/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.YangDataEffectiveStatementImpl;

/**
 * Declared statement representation of 'yang-data' extension defined in https://tools.ietf.org/html/rfc8040#section-8
 */
@Beta
public class YangDataStatementImpl extends AbstractDeclaredStatement<String> implements UnknownStatement<String> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
            SupportedExtensionsMapping.YANG_DATA)
            .addMandatory(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.USES)
            .build();

    YangDataStatementImpl(final StmtContext<String, UnknownStatement<String>, ?> ctx) {
        super(ctx);
    }

    public static class YangDataSupport extends AbstractStatementSupport<String, UnknownStatement<String>,
            EffectiveStatement<String, UnknownStatement<String>>> {

        public YangDataSupport() {
            super(SupportedExtensionsMapping.YANG_DATA);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public UnknownStatement<String> createDeclared(final StmtContext<String, UnknownStatement<String>, ?> ctx) {
            return new YangDataStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, UnknownStatement<String>> createEffective(final StmtContext<String,
                UnknownStatement<String>, EffectiveStatement<String, UnknownStatement<String>>> ctx) {
            // in case of yang-data node we need to perform substatement validation at the point when we have
            // effective substatement contexts already available - if the node has only a uses statement declared in it,
            // one top-level container node may very well be added to the yang-data as an effective statement
            SUBSTATEMENT_VALIDATOR.validate(ctx);
            return new YangDataEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<String, UnknownStatement<String>,
                EffectiveStatement<String, UnknownStatement<String>>> ctx) {
            // as per https://tools.ietf.org/html/rfc8040#section-8,
            // yang-data is ignored unless it appears as a top-level statement
            if (ctx.getParentContext().getParentContext() != null) {
                ctx.setIsSupportedToBuildEffective(false);
            }
        }
    }

    @Override
    public String getArgument() {
        return argument();
    }
}
