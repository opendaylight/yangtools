/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.util.ModuleImportImpl;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToIdentifier;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

abstract class AbstractEffectiveModule<D extends DeclaredStatement<String>> extends
        AbstractEffectiveDocumentedNode<String, D> implements Module, Immutable {

    private final String name;
    private final String sourcePath;
    private final String prefix;
    private final String yangVersion;
    private final String organization;
    private final String contact;
    private final Set<ModuleImport> imports;
    private final Set<Module> submodules;
    private final Set<FeatureDefinition> features;
    private final Set<NotificationDefinition> notifications;
    private final Set<AugmentationSchema> augmentations;
    private final Set<RpcDefinition> rpcs;
    private final Set<Deviation> deviations;
    private final List<ExtensionDefinition> extensionNodes;
    private final Set<IdentitySchemaNode> identities;
    private final List<UnknownSchemaNode> unknownNodes;
    private final Map<QName, DataSchemaNode> childNodes;
    private final Set<GroupingDefinition> groupings;
    private final Set<UsesNode> uses;
    private final Set<TypeDefinition<?>> typeDefinitions;
    private final Set<DataSchemaNode> publicChildNodes;

    AbstractEffectiveModule(final StmtContext<String, D, ? extends EffectiveStatement<String, ?>> ctx) {
        super(ctx);

        this.name = argument();

        PrefixEffectiveStatementImpl prefixStmt = firstEffective(PrefixEffectiveStatementImpl.class);
        this.prefix = (prefixStmt == null) ? null : prefixStmt.argument();

        YangVersionEffectiveStatementImpl yangVersionStmt = firstEffective(YangVersionEffectiveStatementImpl.class);
        this.yangVersion = (yangVersionStmt == null) ? "1" : yangVersionStmt.argument();

        OrganizationEffectiveStatementImpl organizationStmt = firstEffective(OrganizationEffectiveStatementImpl.class);
        this.organization = (organizationStmt == null) ? null : organizationStmt.argument();

        ContactEffectiveStatementImpl contactStmt = firstEffective(ContactEffectiveStatementImpl.class);
        this.contact = (contactStmt == null) ? null : contactStmt.argument();

        if (ctx.getStatementSourceReference() instanceof DeclarationInTextSource) {
            this.sourcePath = ((DeclarationInTextSource) ctx.getStatementSourceReference()).getSourceName();
        } else {
            this.sourcePath = null;
        }

        // init submodules and substatements of submodules
        final List<EffectiveStatement<?, ?>> substatementsOfSubmodules;
        final Map<String, ModuleIdentifier> includedSubmodulesMap = ctx
                .getAllFromCurrentStmtCtxNamespace(IncludedSubmoduleNameToIdentifier.class);

        if (includedSubmodulesMap == null || includedSubmodulesMap.isEmpty()) {
            this.submodules = ImmutableSet.of();
            substatementsOfSubmodules = ImmutableList.of();
        } else {
            Collection<ModuleIdentifier> includedSubmodules = includedSubmodulesMap.values();
            Set<Module> submodulesInit = new HashSet<>();
            List<EffectiveStatement<?, ?>> substatementsOfSubmodulesInit = new ArrayList<>();
            for (ModuleIdentifier submoduleIdentifier : includedSubmodules) {
                @SuppressWarnings("unchecked")
                Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> submoduleCtx =
                    (Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>>)
                        ctx.getFromNamespace(SubmoduleNamespace.class, submoduleIdentifier);
                SubmoduleEffectiveStatementImpl submodule = (SubmoduleEffectiveStatementImpl) submoduleCtx
                        .buildEffective();
                submodulesInit.add(submodule);
                substatementsOfSubmodulesInit.addAll(submodule.effectiveSubstatements());
            }

            this.submodules = ImmutableSet.copyOf(submodulesInit);
            substatementsOfSubmodules = ImmutableList.copyOf(substatementsOfSubmodulesInit);
        }

        // init substatements collections
        List<EffectiveStatement<?, ?>> effectiveSubstatements = new ArrayList<>();
        effectiveSubstatements.addAll(effectiveSubstatements());
        effectiveSubstatements.addAll(substatementsOfSubmodules);

        List<UnknownSchemaNode> unknownNodesInit = new ArrayList<>();
        Set<AugmentationSchema> augmentationsInit = new LinkedHashSet<>();
        Set<ModuleImport> importsInit = new HashSet<>();
        Set<NotificationDefinition> notificationsInit = new HashSet<>();
        Set<RpcDefinition> rpcsInit = new HashSet<>();
        Set<Deviation> deviationsInit = new HashSet<>();
        Set<IdentitySchemaNode> identitiesInit = new HashSet<>();
        Set<FeatureDefinition> featuresInit = new HashSet<>();
        List<ExtensionDefinition> extensionNodesInit = new ArrayList<>();

        Map<QName, DataSchemaNode> mutableChildNodes = new LinkedHashMap<>();
        Set<GroupingDefinition> mutableGroupings = new HashSet<>();
        Set<UsesNode> mutableUses = new HashSet<>();
        Set<TypeDefinition<?>> mutableTypeDefinitions = new LinkedHashSet<>();
        Set<DataSchemaNode> mutablePublicChildNodes = new LinkedHashSet<>();

        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                unknownNodesInit.add((UnknownSchemaNode) effectiveStatement);
            }
            if (effectiveStatement instanceof AugmentationSchema) {
                augmentationsInit.add((AugmentationSchema) effectiveStatement);
            }
            if (effectiveStatement instanceof ModuleImport) {
                importsInit.add((ModuleImport) effectiveStatement);
            }
            if (effectiveStatement instanceof NotificationDefinition) {
                notificationsInit.add((NotificationDefinition) effectiveStatement);
            }
            if (effectiveStatement instanceof RpcDefinition) {
                rpcsInit.add((RpcDefinition) effectiveStatement);
            }
            if (effectiveStatement instanceof Deviation) {
                deviationsInit.add((Deviation) effectiveStatement);
            }
            if (effectiveStatement instanceof IdentitySchemaNode) {
                identitiesInit.add((IdentitySchemaNode) effectiveStatement);
            }
            if (effectiveStatement instanceof FeatureDefinition) {
                featuresInit.add((FeatureDefinition) effectiveStatement);
            }
            if (effectiveStatement instanceof ExtensionEffectiveStatementImpl) {
                extensionNodesInit.add((ExtensionEffectiveStatementImpl) effectiveStatement);
            }
            if (effectiveStatement instanceof DataSchemaNode) {
                DataSchemaNode dataSchemaNode = (DataSchemaNode) effectiveStatement;
                if (!mutableChildNodes.containsKey(dataSchemaNode.getQName())) {
                    mutableChildNodes.put(dataSchemaNode.getQName(), dataSchemaNode);
                    mutablePublicChildNodes.add(dataSchemaNode);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
                }
            }
            if (effectiveStatement instanceof UsesNode) {
                UsesNode usesNode = (UsesNode) effectiveStatement;
                if (!mutableUses.contains(usesNode)) {
                    mutableUses.add(usesNode);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
                }
            }
            if (effectiveStatement instanceof TypeDefEffectiveStatementImpl) {
                TypeDefEffectiveStatementImpl typeDef = (TypeDefEffectiveStatementImpl) effectiveStatement;
                TypeDefinition<?> type = typeDef.getTypeDefinition();
                if (!mutableTypeDefinitions.contains(type)) {
                    mutableTypeDefinitions.add(type);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
                }
            }
            if (effectiveStatement instanceof GroupingDefinition) {
                GroupingDefinition grp = (GroupingDefinition) effectiveStatement;
                if (!mutableGroupings.contains(grp)) {
                    mutableGroupings.add(grp);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
                }
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.imports = ImmutableSet.copyOf(resolveModuleImports(importsInit, ctx));
        this.notifications = ImmutableSet.copyOf(notificationsInit);
        this.rpcs = ImmutableSet.copyOf(rpcsInit);
        this.deviations = ImmutableSet.copyOf(deviationsInit);
        this.identities = ImmutableSet.copyOf(identitiesInit);
        this.features = ImmutableSet.copyOf(featuresInit);
        this.extensionNodes = ImmutableList.copyOf(extensionNodesInit);

        this.childNodes = ImmutableMap.copyOf(mutableChildNodes);
        this.groupings = ImmutableSet.copyOf(mutableGroupings);
        this.publicChildNodes = ImmutableSet.copyOf(mutablePublicChildNodes);
        this.typeDefinitions = ImmutableSet.copyOf(mutableTypeDefinitions);
        this.uses = ImmutableSet.copyOf(mutableUses);

    }

    private static Set<ModuleImport> resolveModuleImports(final Set<ModuleImport> importsInit,
            final StmtContext<String, ? extends DeclaredStatement<String>, ? extends EffectiveStatement<String, ?>> ctx) {
        Set<ModuleImport> resolvedModuleImports = new LinkedHashSet<>();
        for (ModuleImport moduleImport : importsInit) {
            if (moduleImport.getRevision().equals(SimpleDateFormatUtil.DEFAULT_DATE_IMP)) {
                QNameModule impModuleQName = Utils.getModuleQNameByPrefix(ctx, moduleImport.getPrefix());
                if (!impModuleQName.getRevision().equals(SimpleDateFormatUtil.DEFAULT_DATE_REV)) {
                    ModuleImport resolvedModuleImport = new ModuleImportImpl(moduleImport.getModuleName(),
                            impModuleQName.getRevision(), moduleImport.getPrefix());
                    resolvedModuleImports.add(resolvedModuleImport);
                }
            } else {
                resolvedModuleImports.add(moduleImport);
            }
        }
        return resolvedModuleImports;
    }

    @Override
    public String getModuleSourcePath() {
        return sourcePath;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public URI getNamespace() {
        return getQNameModule().getNamespace();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getRevision() {
        return getQNameModule().getRevision();
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getYangVersion() {
        return yangVersion;
    }

    @Override
    public String getOrganization() {
        return organization;
    }

    @Override
    public String getContact() {
        return contact;
    }

    @Override
    public Set<ModuleImport> getImports() {
        return imports;
    }

    @Override
    public Set<Module> getSubmodules() {
        return submodules;
    }

    @Override
    public Set<FeatureDefinition> getFeatures() {
        return features;
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        return notifications;
    }

    @Override
    public Set<AugmentationSchema> getAugmentations() {
        return augmentations;
    }

    @Override
    public Set<RpcDefinition> getRpcs() {
        return rpcs;
    }

    @Override
    public Set<Deviation> getDeviations() {
        return deviations;
    }

    @Override
    public List<ExtensionDefinition> getExtensionSchemaNodes() {
        return extensionNodes;
    }

    @Override
    public Set<IdentitySchemaNode> getIdentities() {
        return identities;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public final Set<TypeDefinition<?>> getTypeDefinitions() {
        return typeDefinitions;
    }

    @Override
    public final Set<DataSchemaNode> getChildNodes() {
        return publicChildNodes;
    }

    @Override
    public final Set<GroupingDefinition> getGroupings() {
        return groupings;
    }

    @Override
    public final DataSchemaNode getDataChildByName(final QName name) {
        // Child nodes are keyed by their container name, so we can do a direct
        // lookup
        return childNodes.get(name);
    }

    @Override
    public final DataSchemaNode getDataChildByName(final String name) {
        for (DataSchemaNode node : childNodes.values()) {
            if (node.getQName().getLocalName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public Set<UsesNode> getUses() {
        return uses;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append("[");
        sb.append("name=").append(name);
        sb.append(", namespace=").append(getNamespace());
        sb.append(", revision=").append(getRevision());
        sb.append(", prefix=").append(prefix);
        sb.append(", yangVersion=").append(yangVersion);
        sb.append("]");
        return sb.toString();
    }

}
