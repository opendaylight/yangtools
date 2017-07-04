/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import java.net.URI;
import java.util.Date;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.util.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.PreLinkageModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNamespaceForBelongsTo;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleQNameToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ModuleEffectiveStatementImpl;

public class ModuleStatementSupport extends
        AbstractStatementSupport<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .MODULE)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.AUGMENT)
            .addAny(YangStmtMapping.CHOICE)
            .addOptional(YangStmtMapping.CONTACT)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.DEVIATION)
            .addAny(YangStmtMapping.EXTENSION)
            .addAny(YangStmtMapping.FEATURE)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IDENTITY)
            .addAny(YangStmtMapping.IMPORT)
            .addAny(YangStmtMapping.INCLUDE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addMandatory(YangStmtMapping.NAMESPACE)
            .addAny(YangStmtMapping.NOTIFICATION)
            .addOptional(YangStmtMapping.ORGANIZATION)
            .addMandatory(YangStmtMapping.PREFIX)
            .addOptional(YangStmtMapping.REFERENCE)
            .addAny(YangStmtMapping.REVISION)
            .addAny(YangStmtMapping.RPC)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangStmtMapping.YANG_VERSION)
            .addOptional(SupportedExtensionsMapping.OPENCONFIG_VERSION)
            .build();

    public ModuleStatementSupport() {
        super(YangStmtMapping.MODULE);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public ModuleStatement createDeclared(final StmtContext<String, ModuleStatement, ?> ctx) {
        return new ModuleStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, ModuleStatement> createEffective(
            final StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> ctx) {
        return new ModuleEffectiveStatementImpl(ctx);
    }

    @Override
    public void onPreLinkageDeclared(final Mutable<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> stmt) {
        final String moduleName = stmt.getStatementArgument();

        final URI moduleNs = firstAttributeOf(stmt.declaredSubstatements(), NamespaceStatement.class);
        SourceException.throwIfNull(moduleNs, stmt.getStatementSourceReference(),
            "Namespace of the module [%s] is missing", stmt.getStatementArgument());
        stmt.addToNs(ModuleNameToNamespace.class, moduleName, moduleNs);

        final String modulePrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
        SourceException.throwIfNull(modulePrefix, stmt.getStatementSourceReference(),
            "Prefix of the module [%s] is missing", stmt.getStatementArgument());
        stmt.addToNs(ImpPrefixToNamespace.class, modulePrefix, moduleNs);

        stmt.addContext(PreLinkageModuleNamespace.class, moduleName, stmt);

        Optional<Date> revisionDate = Optional.ofNullable(StmtContextUtils.getLatestRevision(
            stmt.declaredSubstatements()));
        if (!revisionDate.isPresent()) {
            revisionDate = Optional.of(SimpleDateFormatUtil.DEFAULT_DATE_REV);
        }

        final QNameModule qNameModule = QNameModule.create(moduleNs, revisionDate.orElse(null)).intern();

        stmt.addToNs(ModuleCtxToModuleQName.class, stmt, qNameModule);
        stmt.setRootIdentifier(ModuleIdentifierImpl.create(stmt.getStatementArgument(),
                Optional.empty(), revisionDate));
    }

    @Override
    public void onLinkageDeclared(final Mutable<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> stmt) {

        final Optional<URI> moduleNs = Optional.ofNullable(firstAttributeOf(stmt.declaredSubstatements(),
                NamespaceStatement.class));
        SourceException.throwIf(!moduleNs.isPresent(), stmt.getStatementSourceReference(),
            "Namespace of the module [%s] is missing", stmt.getStatementArgument());

        Optional<Date> revisionDate = Optional.ofNullable(StmtContextUtils.getLatestRevision(
            stmt.declaredSubstatements()));
        if (!revisionDate.isPresent()) {
            revisionDate = Optional.of(SimpleDateFormatUtil.DEFAULT_DATE_REV);
        }

        final QNameModule qNameModule = QNameModule.create(moduleNs.get(), revisionDate.orElse(null)).intern();

        final StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>> possibleDuplicateModule =
                stmt.getFromNamespace(NamespaceToModule.class, qNameModule);
        if (possibleDuplicateModule != null && possibleDuplicateModule != stmt) {
            throw new SourceException(stmt.getStatementSourceReference(), "Module namespace collision: %s. At %s",
                    qNameModule.getNamespace(), possibleDuplicateModule.getStatementSourceReference());
        }

        final ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create(stmt.getStatementArgument(),
                Optional.empty(), revisionDate);

        stmt.addContext(ModuleNamespace.class, moduleIdentifier, stmt);
        stmt.addContext(ModuleNamespaceForBelongsTo.class, moduleIdentifier.getName(), stmt);
        stmt.addContext(NamespaceToModule.class, qNameModule, stmt);

        final String modulePrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
        SourceException.throwIfNull(modulePrefix, stmt.getStatementSourceReference(),
            "Prefix of the module [%s] is missing", stmt.getStatementArgument());

        stmt.addToNs(PrefixToModule.class, modulePrefix, qNameModule);
        stmt.addToNs(ModuleNameToModuleQName.class, stmt.getStatementArgument(), qNameModule);
        stmt.addToNs(ModuleCtxToModuleQName.class, stmt, qNameModule); // tu
        stmt.addToNs(ModuleCtxToModuleIdentifier.class, stmt, moduleIdentifier);
        stmt.addToNs(ModuleQNameToModuleName.class, qNameModule, stmt.getStatementArgument());
        stmt.addToNs(ModuleIdentifierToModuleQName.class, moduleIdentifier, qNameModule);
        stmt.addToNs(ImpPrefixToModuleIdentifier.class, modulePrefix, moduleIdentifier);

        if (stmt.isEnabledSemanticVersioning()) {
            addToSemVerModuleNamespace(stmt);
        }
    }

    private static void addToSemVerModuleNamespace(
            final Mutable<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> stmt) {
        final String moduleName = stmt.getStatementArgument();
        NavigableMap<SemVer, StmtContext<?, ?, ?>> modulesMap = stmt.getFromNamespace(
                SemanticVersionModuleNamespace.class, moduleName);
        if (modulesMap == null) {
            modulesMap = new TreeMap<>();
        }
        SemVer moduleSemVer = stmt.getFromNamespace(SemanticVersionNamespace.class, stmt);
        if(moduleSemVer == null) {
            moduleSemVer = Module.DEFAULT_SEMANTIC_VERSION;
        }
        modulesMap.put(moduleSemVer, stmt);
        stmt.addToNs(SemanticVersionModuleNamespace.class, moduleName, modulesMap);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
