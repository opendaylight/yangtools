/**
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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.ModuleImportImpl;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class ModuleEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<String, ModuleStatement> implements
        Module, Immutable {

    private final QNameModule qNameModule;
    private final String name;
    private final String sourcePath;
    private String prefix;
    private String yangVersion;
    private String organization;
    private String contact;
    private ImmutableSet<ModuleImport> imports;
    private ImmutableSet<Module> submodules;
    private ImmutableSet<FeatureDefinition> features;
    private ImmutableSet<NotificationDefinition> notifications;
    private ImmutableSet<AugmentationSchema> augmentations;
    private ImmutableSet<RpcDefinition> rpcs;
    private ImmutableSet<Deviation> deviations;
    private ImmutableList<ExtensionDefinition> extensionNodes;
    private ImmutableSet<IdentitySchemaNode> identities;
    private ImmutableList<UnknownSchemaNode> unknownNodes;
    private final String source;
    private ImmutableList<EffectiveStatement<?, ?>> substatementsOfSubmodules;

    private ImmutableMap<QName, DataSchemaNode> childNodes;
    private ImmutableSet<GroupingDefinition> groupings;
    private ImmutableSet<UsesNode> uses;
    private ImmutableSet<TypeDefinition<?>> typeDefinitions;
    private ImmutableSet<DataSchemaNode> publicChildNodes;

    public ModuleEffectiveStatementImpl(
            final StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> ctx) {
        super(ctx);

        name = argument();
        QNameModule qNameModuleInit = ctx.getFromNamespace( ModuleCtxToModuleQName.class, ctx);
        qNameModule = qNameModuleInit.getRevision() == null
                ? QNameModule.create(qNameModuleInit.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV)
                : qNameModuleInit;

        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof PrefixEffectiveStatementImpl) {
                prefix = ((PrefixEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof YangVersionEffectiveStatementImpl) {
                yangVersion = ((YangVersionEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof OrganizationEffectiveStatementImpl) {
                organization = ((OrganizationEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof ContactEffectiveStatementImpl) {
                contact = ((ContactEffectiveStatementImpl) effectiveStatement).argument();
            }
        }

        if (yangVersion == null) {
            yangVersion = "1";
        }

        DeclarationInTextSource sourceReference = (DeclarationInTextSource) ctx.getStatementSourceReference();
        sourcePath = sourceReference.getSourceName();
        source = sourceReference.getSourceText();

        initSubmodules(ctx);
        initSubstatementCollections(ctx);
    }

    private void initSubmodules(
            final StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> ctx) {
        Map<String, ModuleIdentifier> includedSubmodulesMap = ctx
                .getAllFromCurrentStmtCtxNamespace(IncludedSubmoduleNameToIdentifier.class);

        if (includedSubmodulesMap == null || includedSubmodulesMap.isEmpty()) {
            this.submodules = ImmutableSet.of();
            this.substatementsOfSubmodules = ImmutableList.of();
            return;
        }

        Collection<ModuleIdentifier> includedSubmodules = includedSubmodulesMap.values();

        Set<Module> submodulesInit = new HashSet<>();
        List<EffectiveStatement<?, ?>> substatementsOfSubmodulesInit = new LinkedList<>();
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
        this.substatementsOfSubmodules = ImmutableList
                .copyOf(substatementsOfSubmodulesInit);
    }

    private void initSubstatementCollections(
            final StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> ctx) {
        List<EffectiveStatement<?, ?>> effectiveSubstatements = new LinkedList<>();

        effectiveSubstatements.addAll(effectiveSubstatements());
        effectiveSubstatements.addAll(substatementsOfSubmodules);

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();
        Set<AugmentationSchema> augmentationsInit = new HashSet<>();
        Set<ModuleImport> importsInit = new HashSet<>();
        Set<NotificationDefinition> notificationsInit = new HashSet<>();
        Set<RpcDefinition> rpcsInit = new HashSet<>();
        Set<Deviation> deviationsInit = new HashSet<>();
        Set<IdentitySchemaNode> identitiesInit = new HashSet<>();
        Set<FeatureDefinition> featuresInit = new HashSet<>();
        List<ExtensionDefinition> extensionNodesInit = new LinkedList<>();

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
            if (effectiveStatement instanceof ExtensionDefinition) {
                extensionNodesInit.add((ExtensionDefinition) effectiveStatement);
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
                ExtendedType extendedType = typeDef.buildType();
                if (!mutableTypeDefinitions.contains(extendedType)) {
                    mutableTypeDefinitions.add(extendedType);
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
            final StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> ctx) {
        Set<ModuleImport> resolvedModuleImports = new LinkedHashSet<>();
        for (ModuleImport moduleImport : importsInit) {
            if (moduleImport.getRevision().equals(
                    SimpleDateFormatUtil.DEFAULT_DATE_IMP)) {
                QNameModule impModuleQName = Utils.getModuleQNameByPrefix(ctx,
                        moduleImport.getPrefix());
                if (!impModuleQName.getRevision().equals(
                        SimpleDateFormatUtil.DEFAULT_DATE_REV)) {
                    ModuleImport resolvedModuleImport = new ModuleImportImpl(
                            moduleImport.getModuleName(),
                            impModuleQName.getRevision(),
                            moduleImport.getPrefix());
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
    public URI getNamespace() {
        return qNameModule.getNamespace();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getRevision() {
        return qNameModule.getRevision();
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
    public String getSource() {
        return source;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(yangVersion);
        result = prime * result + qNameModule.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ModuleEffectiveStatementImpl other = (ModuleEffectiveStatementImpl) obj;
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (!qNameModule.equals(other.qNameModule)) {
            return false;
        }
        if (!Objects.equals(yangVersion, other.yangVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ModuleEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("name=").append(name);
        sb.append(", namespace=").append(getNamespace());
        sb.append(", revision=").append(getRevision());
        sb.append(", prefix=").append(prefix);
        sb.append(", yangVersion=").append(yangVersion);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public QNameModule getQNameModule() {
        return qNameModule;
    }

}
