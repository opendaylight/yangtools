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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ExtendedTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BinaryEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BooleanEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EmptyEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int16EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int32EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int64EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int8EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.StringEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt16EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt32EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt64EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt8EffectiveStatementImpl;

public class TypeStatementImpl extends AbstractDeclaredStatement<String>
        implements TypeStatement {

    protected TypeStatementImpl(final StmtContext<String, TypeStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, TypeStatement, EffectiveStatement<String, TypeStatement>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value)
                throws SourceException {
            return value;
        }

        @Override
        public TypeStatement createDeclared(
                final StmtContext<String, TypeStatement, ?> ctx) {
            return new TypeStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement> createEffective(
                final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

            // :FIXME improve the test of isExtended - e.g. unknown statements,
            // etc..
            Collection<StatementContextBase<?, ?, ?>> declaredSubstatements = ctx
                    .declaredSubstatements();
            boolean isExtended = !declaredSubstatements.isEmpty();
            if (isExtended) {
                return new ExtendedTypeEffectiveStatementImpl(ctx, true);
            }

            switch (ctx.getStatementArgument()) {
            case TypeUtils.INT8:
                return new Int8EffectiveStatementImpl(ctx);
            case TypeUtils.INT16:
                return new Int16EffectiveStatementImpl(ctx);
            case TypeUtils.INT32:
                return new Int32EffectiveStatementImpl(ctx);
            case TypeUtils.INT64:
                return new Int64EffectiveStatementImpl(ctx);
            case TypeUtils.UINT8:
                return new UInt8EffectiveStatementImpl(ctx);
            case TypeUtils.UINT16:
                return new UInt16EffectiveStatementImpl(ctx);
            case TypeUtils.UINT32:
                return new UInt32EffectiveStatementImpl(ctx);
            case TypeUtils.UINT64:
                return new UInt64EffectiveStatementImpl(ctx);
            case TypeUtils.STRING:
                return new StringEffectiveStatementImpl(ctx);
            case TypeUtils.BOOLEAN:
                return new BooleanEffectiveStatementImpl(ctx);
            case TypeUtils.EMPTY:
                return new EmptyEffectiveStatementImpl(ctx);
            case TypeUtils.BINARY:
                return new BinaryEffectiveStatementImpl(ctx);
            default:
                // :FIXME try to resolve original typedef context here and
                // return buildEffective of original typedef context
                return new ExtendedTypeEffectiveStatementImpl(ctx, false);
            }
        }

        @Override
        public void onFullDefinitionDeclared(
                final Mutable<String, TypeStatement, EffectiveStatement<String, TypeStatement>> stmt)
                throws SourceException {

            // if it is yang built-in type, no prerequisite is needed, so simply return
            if (TypeUtils.isYangBuiltInTypeString(stmt.getStatementArgument())) {
                return;
            }

            final QName typeQName = Utils.qNameFromArgument(stmt, stmt.getStatementArgument());
            final ModelActionBuilder typeAction = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
            final Prerequisite<StmtContext<?, ?, ?>> typePrereq = typeAction.requiresCtx(stmt, TypeNamespace.class,
                    typeQName, ModelProcessingPhase.EFFECTIVE_MODEL);
            typeAction.mutatesEffectiveCtx(stmt.getParentContext());

            typeAction.apply(new InferenceAction() {

                @Override
                public void apply() throws InferenceException {
                    // NOOP
                }

                @Override
                public void prerequisiteFailed(Collection<? extends Prerequisite<?>> failed) throws InferenceException {
                    if (failed.contains(typePrereq)) {
                        throw new InferenceException(String.format("Type [%s] was not found.", typeQName), stmt
                                .getStatementSourceReference());
                    }
                }
            });
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }
}
