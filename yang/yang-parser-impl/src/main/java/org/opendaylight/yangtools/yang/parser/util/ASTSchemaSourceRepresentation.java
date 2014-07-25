/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.base.Preconditions;

import org.antlr.v4.runtime.tree.ParseTree;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;

public final class ASTSchemaSourceRepresentation implements SchemaSourceRepresentation {
    private final YangModelDependencyInfo depInfo;
    private final SourceIdentifier id;
    private final ParseTree tree;

    private ASTSchemaSourceRepresentation(final SourceIdentifier id, final ParseTree tree, final YangModelDependencyInfo depInfo) {
        this.depInfo = Preconditions.checkNotNull(depInfo);
        this.tree = Preconditions.checkNotNull(tree);
        this.id = Preconditions.checkNotNull(id);
    }

    public static final ASTSchemaSourceRepresentation create(final ParseTree tree) {
        final YangModelDependencyInfo depInfo = null;
        final SourceIdentifier id = null;

        // FIXME walk the AST and extract info out of it:
        // - source id
        // - imports
        // - prefix map
        // - history (may be useful to understand what we can expect)

        return new ASTSchemaSourceRepresentation(id, tree, depInfo);
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return id;
    }

    @Override
    public Class<? extends SchemaSourceRepresentation> getType() {
        return ASTSchemaSourceRepresentation.class;
    }

    public ParseTree getAST() {
        return tree;
    }

    public YangModelDependencyInfo getDependencyInformation() {
        return depInfo;
    }
}
