/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SourceLinkage;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import java.net.URI;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import com.google.common.base.Optional;

public class IncludeStatementImpl extends AbstractDeclaredStatement<String> implements IncludeStatement {

    protected IncludeStatementImpl(StmtContext<String, IncludeStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<String, IncludeStatement, EffectiveStatement<String, IncludeStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Include);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public IncludeStatement createDeclared(StmtContext<String, IncludeStatement, ?> ctx) {
            return new IncludeStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, IncludeStatement> createEffective(
                StmtContext<String, IncludeStatement, EffectiveStatement<String, IncludeStatement>> ctx) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onLinkageDeclared(
                final Mutable<String, IncludeStatement, EffectiveStatement<String, IncludeStatement>> stmt)
                throws InferenceException, SourceException {
            final ModuleIdentifier includeSubmoduleIdentifier = getIncludeSubmoduleIdentifier(stmt);

            ModelActionBuilder includeAction = stmt.newInferenceAction(SourceLinkage);
            final Prerequisite<StmtContext<?, ?, ?>> requiresCtxPrerequisite = includeAction.requiresCtx(stmt,
                    SubmoduleNamespace.class, includeSubmoduleIdentifier, SourceLinkage);
            final Prerequisite<Mutable<?, ?, ?>> mutatesCtxPrerequisite = includeAction.mutatesCtx(stmt.getRoot(),
                    SourceLinkage);

            includeAction.apply(new InferenceAction() {

                @Override
                public void apply() throws InferenceException {
                    StmtContext<?, ?, ?> includedSubmoduleStmt = requiresCtxPrerequisite.get();

                    mutatesCtxPrerequisite.get().addToNs(IncludedModuleContext.class, includeSubmoduleIdentifier,
                            includedSubmoduleStmt);
                }

                @Override
                public void prerequisiteFailed(Collection<? extends Prerequisite<?>> failed) throws InferenceException {
                    System.out.println("");
                    if (failed.contains(requiresCtxPrerequisite)) {
                        throw new InferenceException("Included submodule was not found.", stmt
                                .getStatementSourceReference());
                    }
                }
            });
        }

        private static ModuleIdentifier getIncludeSubmoduleIdentifier(Mutable<String, IncludeStatement, ?> stmt) {

            String subModuleName = stmt.getStatementArgument();
            String revisionArg = firstAttributeOf(stmt.declaredSubstatements(), RevisionDateStatement.class);
            final Optional<Date> revision;
            if (revisionArg != null) {
                try {
                    revision = Optional.of(SimpleDateFormatUtil.getRevisionFormat().parse(revisionArg));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            } else {
                revision = Optional.of(SimpleDateFormatUtil.DEFAULT_DATE_IMP);
            }

            return new ModuleIdentifierImpl(subModuleName, Optional.<URI> absent(), revision);
        }
    }

    @Override
    public String getModule() {
        return argument();
    }

    @Override
    public PrefixStatement getPrefix() {
        return firstDeclared(PrefixStatement.class);
    }

    @Override
    public RevisionDateStatement getRevisionDate() {
        return firstDeclared(RevisionDateStatement.class);
    }

}