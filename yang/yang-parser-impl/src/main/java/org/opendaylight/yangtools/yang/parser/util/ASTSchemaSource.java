/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;

public final class ASTSchemaSource implements SchemaSourceRepresentation {
    private final YangModelDependencyInfo depInfo;
    private final ParserRuleContext tree;
    private final SourceIdentifier id;

    private ASTSchemaSource(final SourceIdentifier id, final ParserRuleContext tree, final YangModelDependencyInfo depInfo) {
        this.depInfo = Preconditions.checkNotNull(depInfo);
        this.tree = Preconditions.checkNotNull(tree);
        this.id = Preconditions.checkNotNull(id);
    }

    public static final ASTSchemaSource create(final String name, final ParserRuleContext tree) throws YangSyntaxErrorException {
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

    public ParserRuleContext getAST() {
        return tree;
    }

    public YangModelDependencyInfo getDependencyInformation() {
        return depInfo;
    }
}
