/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;

/**
 * Abstract Syntax Tree representation of a schema source. This representation
 * is internal to the YANG parser implementation, as it relies on ANTLR types.
 *
 * Instances of this representation are used for caching purposes, as they
 * are a natural intermediate step in YANG text processing pipeline: the text
 * has been successfully parsed, so we know it is syntactically correct. It also
 * passes basic semantic validation and we were able to extract dependency
 * information.
 */
@Beta
public final class ASTSchemaSource implements SchemaSourceRepresentation {
    @Deprecated
    public static final Function<ASTSchemaSource, SourceIdentifier> GET_IDENTIFIER = ASTSchemaSource::getIdentifier;
    @Deprecated
    public static final Function<ASTSchemaSource, SourceIdentifier> GET_SEMVER_IDENTIFIER = ASTSchemaSource::getSemVerIdentifier;
    @Deprecated
    public static final Function<ASTSchemaSource, YangModelDependencyInfo> GET_DEPINFO = ASTSchemaSource::getDependencyInformation;
    @Deprecated
    public static final Function<ASTSchemaSource, ParserRuleContext> GET_AST = ASTSchemaSource::getAST;

    private final YangModelDependencyInfo depInfo;
    private final ParserRuleContext tree;
    private final SourceIdentifier id;
    private final SemVerSourceIdentifier semVerId;

    private ASTSchemaSource(@Nonnull final SourceIdentifier id, @Nonnull final SemVerSourceIdentifier semVerId, @Nonnull final ParserRuleContext tree, @Nonnull final YangModelDependencyInfo depInfo) {
        this.depInfo = Preconditions.checkNotNull(depInfo);
        this.tree = Preconditions.checkNotNull(tree);
        this.id = Preconditions.checkNotNull(id);
        this.semVerId = Preconditions.checkNotNull(semVerId);
    }

    /**
     * Create a new instance of AST representation for a abstract syntax tree,
     * performing minimal semantic analysis to acquire dependency information.
     *
     * @param name YANG source name. Used only for error reporting.
     * @param tree ANTLR abstract syntax tree
     * @return A new representation instance.
     * @throws YangSyntaxErrorException if we fail to extract dependency information.
     */
    public static ASTSchemaSource create(@Nonnull final String name, @Nonnull final ParserRuleContext tree) throws YangSyntaxErrorException {
        final YangModelDependencyInfo depInfo = YangModelDependencyInfo.fromAST(name, tree);
        final SourceIdentifier id = getSourceId(depInfo);
        final SemVerSourceIdentifier semVerId = getSemVerSourceId(depInfo);
        return new ASTSchemaSource(id, semVerId, tree, depInfo);
    }

    private static SourceIdentifier getSourceId(final YangModelDependencyInfo depInfo) {
        final String name = depInfo.getName();
        return depInfo.getFormattedRevision() == null
                ? RevisionSourceIdentifier.create(name)
                : RevisionSourceIdentifier.create(name, depInfo.getFormattedRevision());
    }

    private static SemVerSourceIdentifier getSemVerSourceId(final YangModelDependencyInfo depInfo) {
        return depInfo.getFormattedRevision() == null ? SemVerSourceIdentifier.create(depInfo.getName(), depInfo
                .getSemanticVersion().or(Module.DEFAULT_SEMANTIC_VERSION)) : SemVerSourceIdentifier.create(
                depInfo.getName(), depInfo.getFormattedRevision(),
                depInfo.getSemanticVersion().or(Module.DEFAULT_SEMANTIC_VERSION));
    }

    /**
     * Create a new instance of AST representation for a abstract syntax tree,
     * performing minimal semantic analysis to acquire dependency information.
     *
     * @param identifier
     *            SourceIdentifier of yang schema source.
     * @param tree
     *            ANTLR abstract syntax tree
     * @param text
     *            YANG text source
     * @return A new representation instance.
     * @throws YangSyntaxErrorException
     *             if we fail to extract dependency information.
     *
     * @deprecated Use {@link #create(SourceIdentifier, ParserRuleContext)} instead.
     */
    @Deprecated
    public static ASTSchemaSource create(@Nonnull final SourceIdentifier identifier,
            @Nonnull final ParserRuleContext tree, final String text) throws YangSyntaxErrorException {
        return create(identifier, tree);
    }

    /**
     * Create a new instance of AST representation for a abstract syntax tree,
     * performing minimal semantic analysis to acquire dependency information.
     *
     * @param identifier
     *            SourceIdentifier of yang schema source.
     * @param tree
     *            ANTLR abstract syntax tree
     * @return A new representation instance.
     * @throws YangSyntaxErrorException
     *             if we fail to extract dependency information.
     */
    public static ASTSchemaSource create(@Nonnull final SourceIdentifier identifier,
            @Nonnull final ParserRuleContext tree) throws YangSyntaxErrorException {
        final YangModelDependencyInfo depInfo = YangModelDependencyInfo.fromAST(identifier.getName(), tree);
        final SourceIdentifier id = getSourceId(depInfo);

        final SemVerSourceIdentifier semVerId;
        if (identifier instanceof SemVerSourceIdentifier && !depInfo.getSemanticVersion().isPresent()) {
            semVerId = (SemVerSourceIdentifier) identifier;
        } else {
            semVerId = getSemVerSourceId(depInfo);
        }

        return new ASTSchemaSource(id, semVerId, tree, depInfo);
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return id;
    }

    public SemVerSourceIdentifier getSemVerIdentifier() {
        return semVerId;
    }

    @Nonnull
    @Override
    public Class<? extends SchemaSourceRepresentation> getType() {
        return ASTSchemaSource.class;
    }

    /**
     * Return the underlying abstract syntax tree.
     *
     * @return Underlying AST.
     */
    @Nonnull public ParserRuleContext getAST() {
        return tree;
    }

    /**
     * Return the dependency information as extracted from the AST.
     *
     * FIXME: this method should be extracted into a public interface in the
     *        model.api.repo class, relying solely on model.api types.
     *
     * @return Dependency information.
     */
    @Nonnull public YangModelDependencyInfo getDependencyInformation() {
        return depInfo;
    }
}
