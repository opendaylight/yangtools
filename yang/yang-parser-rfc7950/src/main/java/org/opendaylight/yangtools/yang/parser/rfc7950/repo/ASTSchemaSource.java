/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Abstract Syntax Tree representation of a schema source. This representation is internal to the YANG parser
 * implementation, as it relies on ANTLR types.
 *
 * <p>
 * Instances of this representation are used for caching purposes, as they are a natural intermediate step in YANG text
 * processing pipeline: the text has been successfully parsed, so we know it is syntactically correct. It also passes
 * basic semantic validation and we were able to extract dependency information.
 */
@Beta
public final class ASTSchemaSource implements SchemaSourceRepresentation {
    private final @NonNull YangModelDependencyInfo depInfo;
    private final @NonNull SemVerSourceIdentifier semVerId;
    private final @NonNull SourceIdentifier identifier;
    private final @NonNull ParserRuleContext tree;
    private final @Nullable String symbolicName;

    private ASTSchemaSource(final @NonNull SourceIdentifier identifier, final @NonNull SemVerSourceIdentifier semVerId,
            final @NonNull ParserRuleContext tree, final @NonNull YangModelDependencyInfo depInfo,
            @Nullable final String symbolicName) {
        this.depInfo = requireNonNull(depInfo);
        this.tree = requireNonNull(tree);
        this.identifier = requireNonNull(identifier);
        this.semVerId = requireNonNull(semVerId);
        this.symbolicName = symbolicName;
    }

    /**
     * Create a new instance of AST representation for a abstract syntax tree, performing minimal semantic analysis
     * to acquire dependency information.
     *
     * @param symbolicName
     *            Symbolic name
     * @param identifier
     *            SourceIdentifier of YANG schema source.
     * @param tree
     *            ANTLR abstract syntax tree
     * @return A new representation instance.
     * @throws YangSyntaxErrorException
     *             if we fail to extract dependency information.
     */
    static @NonNull ASTSchemaSource create(final @NonNull SourceIdentifier identifier,
            final @Nullable String symbolicName, final @NonNull ParserRuleContext tree)
                    throws YangSyntaxErrorException {
        final YangModelDependencyInfo depInfo = YangModelDependencyInfo.fromAST(identifier, tree);
        final SourceIdentifier id = getSourceId(depInfo);

        final SemVerSourceIdentifier semVerId;
        if (identifier instanceof SemVerSourceIdentifier && !depInfo.getSemanticVersion().isPresent()) {
            semVerId = (SemVerSourceIdentifier) identifier;
        } else {
            semVerId = getSemVerSourceId(depInfo);
        }

        return new ASTSchemaSource(id, semVerId, tree, depInfo, symbolicName);
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public Optional<String> getSymbolicName() {
        return Optional.ofNullable(symbolicName);
    }

    public @NonNull SemVerSourceIdentifier getSemVerIdentifier() {
        return semVerId;
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
    public @NonNull ParserRuleContext getAST() {
        return tree;
    }

    /**
     * Return the dependency information as extracted from the AST.
     *
     * @return Dependency information.
     */
    // FIXME: this method should be extracted into a public interface in the model.api.repo class, relying solely
    //        on model.api types.
    public @NonNull YangModelDependencyInfo getDependencyInformation() {
        return depInfo;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("identifier", identifier).toString();
    }

    private static @NonNull SourceIdentifier getSourceId(final @NonNull YangModelDependencyInfo depInfo) {
        final String name = depInfo.getName();
        return depInfo.getFormattedRevision() == null ? RevisionSourceIdentifier.create(name)
                : RevisionSourceIdentifier.create(name, depInfo.getRevision());
    }

    private static @NonNull SemVerSourceIdentifier getSemVerSourceId(final @NonNull YangModelDependencyInfo depInfo) {
        return depInfo.getFormattedRevision() == null
                ? SemVerSourceIdentifier.create(depInfo.getName(), depInfo.getSemanticVersion().orElse(null))
                        : SemVerSourceIdentifier.create(depInfo.getName(), depInfo.getRevision(),
                            depInfo.getSemanticVersion().orElse(null));
    }
}
