/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.base.Preconditions;

import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.YangContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

public final class ASTSchemaSource implements SchemaSourceRepresentation {
    private final SourceIdentifier id;
    private final YangContext ast;

    private ASTSchemaSource(final SourceIdentifier id, final YangContext ast) {
        this.ast = Preconditions.checkNotNull(ast);
        this.id = Preconditions.checkNotNull(id);
    }

    public static final ASTSchemaSource create(final YangContext ast) {
        final SourceIdentifier id = null;

        // FIXME walk the AST and extract info out of it:
        // - source id
        // - imports
        // - prefix map
        // - revision history? (may be useful to understand what we can expect)

        return new ASTSchemaSource(id, ast);
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return id;
    }

    @Override
    public Class<? extends SchemaSourceRepresentation> getType() {
        return ASTSchemaSource.class;
    }

    public YangContext getAST() {
        return ast;
    }
}
