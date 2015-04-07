/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;

import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
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

public class ModuleStatementSupport
        extends
        AbstractStatementSupport<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> {

    public ModuleStatementSupport() {
        super(Rfc6020Mapping.Module);
    }

    @Override
    public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
        return value;
    }

    @Override
    public ModuleStatement createDeclared(
            StmtContext<String, ModuleStatement, ?> ctx) {
        return new ModuleStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, ModuleStatement> createEffective(
            StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onLinkageDeclared(
            Mutable<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> stmt)
            throws InferenceException, SourceException {

        Optional<URI> moduleNs = Optional.fromNullable(firstAttributeOf(
                stmt.declaredSubstatements(), NamespaceStatement.class));
        if (!moduleNs.isPresent()) {
            throw new IllegalArgumentException("Namespace of the module ["
                    + stmt.getStatementArgument() + "] is missing.");
        }

        Optional<Date> revisionDate = Optional.fromNullable(firstAttributeOf(
                stmt.declaredSubstatements(), RevisionStatement.class));

        QNameModule qNameModule = QNameModule.create(moduleNs.get(),
                revisionDate.orNull());
        ModuleIdentifier moduleIdentifier = new ModuleIdentifierImpl(
                stmt.getStatementArgument(), Optional.<URI> absent(),
                revisionDate);

        stmt.addContext(ModuleNamespace.class, moduleIdentifier, stmt);
        stmt.addContext(NamespaceToModule.class, qNameModule, stmt);

        String modulePrefix = firstAttributeOf(stmt.declaredSubstatements(),
                PrefixStatement.class);
        if (modulePrefix == null) {
            throw new IllegalArgumentException("Prefix of the module ["
                    + stmt.getStatementArgument() + "] is missing.");
        }

        stmt.addToNs(PrefixToModule.class, modulePrefix, qNameModule);
        stmt.addToNs(ModuleNameToModuleQName.class, stmt.getStatementArgument(), qNameModule);
        stmt.addToNs(ModuleIdentifierToModuleQName.class, moduleIdentifier, qNameModule);

        stmt.addToNs(ImpPrefixToModuleIdentifier.class, modulePrefix, moduleIdentifier);

    }



//    @Override
//    public void onFullDefinitionDeclared(
//            Mutable<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> stmt)
//            throws InferenceException, SourceException {
//
//        Optional<Date> revisionDate = Optional.fromNullable(firstAttributeOf(
//                stmt.declaredSubstatements(), RevisionStatement.class));
//
//        ModuleIdentifier moduleIdentifier = new ModuleIdentifierImpl(
//                stmt.getStatementArgument(), Optional.<URI> absent(),
//                revisionDate);
//
//        String modulePrefix = firstAttributeOf(stmt.declaredSubstatements(),
//                PrefixStatement.class);
//
//        stmt.addToNs(ImpPrefixToModuleIdentifier.class, modulePrefix, moduleIdentifier);
//    }

}