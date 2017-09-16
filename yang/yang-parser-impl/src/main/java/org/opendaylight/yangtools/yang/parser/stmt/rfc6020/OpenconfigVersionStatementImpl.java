/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.OpenconfigVersionEffectiveStatementImpl;

@Beta
public final class OpenconfigVersionStatementImpl extends AbstractDeclaredStatement<SemVer>
        implements UnknownStatement<SemVer> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
            SupportedExtensionsMapping.OPENCONFIG_VERSION).build();

    OpenconfigVersionStatementImpl(
            final StmtContext<SemVer, UnknownStatement<SemVer>, ?> context) {
        super(context);
    }

    public static class OpenconfigVersionSupport extends AbstractStatementSupport<SemVer, UnknownStatement<SemVer>,
            EffectiveStatement<SemVer, UnknownStatement<SemVer>>> {

        public OpenconfigVersionSupport() {
            super(SupportedExtensionsMapping.OPENCONFIG_VERSION);
        }

        @Override
        public SemVer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return SemVer.valueOf(value) ;
        }

        @Override
        public void onLinkageDeclared(final Mutable<SemVer, UnknownStatement<SemVer>,
                EffectiveStatement<SemVer, UnknownStatement<SemVer>>> stmt) {
            stmt.addToNs(SemanticVersionNamespace.class, stmt.getParentContext(), stmt.getStatementArgument());
        }

        @Override
        public UnknownStatement<SemVer> createDeclared(final StmtContext<SemVer, UnknownStatement<SemVer>, ?> ctx) {
            return new OpenconfigVersionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<SemVer, UnknownStatement<SemVer>> createEffective(
                final StmtContext<SemVer, UnknownStatement<SemVer>,
                EffectiveStatement<SemVer, UnknownStatement<SemVer>>> ctx) {
            return new OpenconfigVersionEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Override
    public SemVer getArgument() {
        return argument();
    }
}
