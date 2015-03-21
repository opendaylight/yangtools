/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.base.Optional;
import java.net.URI;
import java.util.Date;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class ModuleStatementSupport extends AbstractStatementSupport<String, ModuleStatement, EffectiveStatement<String,ModuleStatement>> {

    public ModuleStatementSupport() {
        super(Rfc6020Mapping.Module);
    }

    @Override
    public String parseArgumentValue(StmtContext<?,?,?> ctx, String value) {
        return value;
    }

    @Override
    public ModuleStatement createDeclared(StmtContext<String, ModuleStatement,?> ctx) {
        return new ModuleStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String,ModuleStatement> createEffective(StmtContext<String, ModuleStatement,EffectiveStatement<String,ModuleStatement>> ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onLinkageDeclared(Mutable<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> stmt)
            throws InferenceException, SourceException {
        URI moduleNs = firstAttributeOf(stmt.declaredSubstatements(),NamespaceStatement.class);

        QNameModule qnameNamespace = QNameModule.create(moduleNs, null);
        ModuleIdentifierImpl moduleIdentifier = new ModuleIdentifierImpl(stmt.getStatementArgument(), Optional.<URI>absent(), Optional.<Date>absent());

        stmt.addContext(ModuleNamespace.class,moduleIdentifier ,stmt);
        stmt.addContext(NamespaceToModule.class, qnameNamespace, stmt);
    }

}