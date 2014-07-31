/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

import org.antlr.v4.runtime.ParserRuleContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
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
public final class ASTSchemaSource implements SchemaSourceRepresentation {
    private final YangModelDependencyInfo depInfo;
    private final ParserRuleContext tree;
    private final SourceIdentifier id;

    private ASTSchemaSource(final @Nonnull SourceIdentifier id, @Nonnull final ParserRuleContext tree, final @Nonnull YangModelDependencyInfo depInfo) {
        this.depInfo = Preconditions.checkNotNull(depInfo);
        this.tree = Preconditions.checkNotNull(tree);
        this.id = Preconditions.checkNotNull(id);
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
    public static final ASTSchemaSource create(final @Nonnull String name, final @Nonnull ParserRuleContext tree) throws YangSyntaxErrorException {
        final YangModelDependencyInfo depInfo = YangModelDependencyInfo.fromAST(name, tree);
        final SourceIdentifier id = new SourceIdentifier(depInfo.getName(), Optional.of(depInfo.getFormattedRevision()));
        return new ASTSchemaSource(id, tree, depInfo);
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return id;
    }

    @Override
    public Class<? extends SchemaSourceRepresentation> getType() {
        return ASTSchemaSource.class;
    }

    /**
     * Return the underlying abstract syntax tree.
     *
     * @return Underlying AST.
     */
    public @Nonnull ParserRuleContext getAST() {
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
    public @Nonnull YangModelDependencyInfo getDependencyInformation() {
        return depInfo;
    }
}
