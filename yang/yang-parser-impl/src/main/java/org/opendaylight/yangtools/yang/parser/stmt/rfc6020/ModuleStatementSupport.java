/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator.MAX;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.base.Optional;
import java.net.URI;
import java.util.Date;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNamespaceForBelongsTo;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleQNameToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ModuleEffectiveStatementImpl;

public class ModuleStatementSupport extends
        AbstractStatementSupport<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .MODULE)
            .add(Rfc6020Mapping.ANYXML, 0, MAX)
            .add(Rfc6020Mapping.AUGMENT, 0, MAX)
            .add(Rfc6020Mapping.CHOICE, 0, MAX)
            .add(Rfc6020Mapping.CONTACT, 0, 1)
            .add(Rfc6020Mapping.CONTAINER, 0, MAX)
            .add(Rfc6020Mapping.DESCRIPTION, 0, 1)
            .add(Rfc6020Mapping.DEVIATION, 0, MAX)
            .add(Rfc6020Mapping.EXTENSION, 0, MAX)
            .add(Rfc6020Mapping.FEATURE, 0, MAX)
            .add(Rfc6020Mapping.GROUPING, 0, MAX)
            .add(Rfc6020Mapping.IDENTITY, 0, MAX)
            .add(Rfc6020Mapping.IMPORT, 0, MAX)
            .add(Rfc6020Mapping.INCLUDE, 0, MAX)
            .add(Rfc6020Mapping.LEAF, 0, MAX)
            .add(Rfc6020Mapping.LEAF_LIST, 0, MAX)
            .add(Rfc6020Mapping.LIST, 0, MAX)
            .add(Rfc6020Mapping.NAMESPACE, 1, 1)
            .add(Rfc6020Mapping.NOTIFICATION, 0, MAX)
            .add(Rfc6020Mapping.ORGANIZATION, 0, 1)
            .add(Rfc6020Mapping.PREFIX, 1, 1)
            .add(Rfc6020Mapping.REFERENCE, 0, 1)
            .add(Rfc6020Mapping.REVISION, 0, MAX)
            .add(Rfc6020Mapping.RPC, 0, MAX)
            .add(Rfc6020Mapping.TYPEDEF, 0, MAX)
            .add(Rfc6020Mapping.USES, 0, MAX)
            .add(Rfc6020Mapping.YANG_VERSION, 0, 1)
            .build();

    public ModuleStatementSupport() {
        super(Rfc6020Mapping.MODULE);
    }

    @Override
    public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
        return value;
    }

    @Override
    public ModuleStatement createDeclared(StmtContext<String, ModuleStatement, ?> ctx) {
        return new ModuleStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, ModuleStatement> createEffective(
            StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> ctx) {
        return new ModuleEffectiveStatementImpl(ctx);
    }

    @Override
    public void onLinkageDeclared(Mutable<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> stmt)
            throws SourceException {

        Optional<URI> moduleNs = Optional.fromNullable(firstAttributeOf(stmt.declaredSubstatements(),
                NamespaceStatement.class));
        if (!moduleNs.isPresent()) {
            throw new SourceException(String.format("Namespace of the module [%s] is missing",
                    stmt.getStatementArgument()), stmt.getStatementSourceReference());
        }

        Optional<Date> revisionDate = Optional.fromNullable(Utils.getLatestRevision(stmt.declaredSubstatements()));
        if (!revisionDate.isPresent()) {
            revisionDate = Optional.of(SimpleDateFormatUtil.DEFAULT_DATE_REV);
        }

        QNameModule qNameModule = QNameModule.create(moduleNs.get(), revisionDate.orNull()).intern();
        ModuleIdentifier moduleIdentifier = new ModuleIdentifierImpl(stmt.getStatementArgument(),
                Optional.<URI> absent(), revisionDate);

        stmt.addContext(ModuleNamespace.class, moduleIdentifier, stmt);
        stmt.addContext(ModuleNamespaceForBelongsTo.class, moduleIdentifier.getName(), stmt);
        stmt.addContext(NamespaceToModule.class, qNameModule, stmt);

        String modulePrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
        if (modulePrefix == null) {
            throw new SourceException(String.format("Prefix of the module [%s] is missing",
                    stmt.getStatementArgument()), stmt.getStatementSourceReference());
        }

        stmt.addToNs(PrefixToModule.class, modulePrefix, qNameModule);
        stmt.addToNs(ModuleNameToModuleQName.class, stmt.getStatementArgument(), qNameModule);
        stmt.addToNs(ModuleCtxToModuleQName.class, stmt, qNameModule);
        stmt.addToNs(ModuleQNameToModuleName.class, qNameModule, stmt.getStatementArgument());
        stmt.addToNs(ModuleIdentifierToModuleQName.class, moduleIdentifier, qNameModule);

        stmt.addToNs(ImpPrefixToModuleIdentifier.class, modulePrefix, moduleIdentifier);
    }

    @Override
    public void onFullDefinitionDeclared(Mutable<String, ModuleStatement,
            EffectiveStatement<String, ModuleStatement>> stmt) throws SourceException {
        super.onFullDefinitionDeclared(stmt);
        SUBSTATEMENT_VALIDATOR.validate(stmt);
    }
}
