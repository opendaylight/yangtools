/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
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
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToIdentifier;

abstract class AbstractEffectiveModule<D extends DeclaredStatement<String>> extends
        AbstractEffectiveDocumentedNode<String, D> implements Module, MutableStatement {
    private final String name;
    private final String sourcePath;
    private final String prefix;
    private final YangVersion yangVersion;
    private final String organization;
    private final String contact;
    private final Set<ModuleImport> imports;
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
    private final SemVer semanticVersion;
    private Set<StmtContext<?, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>>> submoduleContextsToBuild;
    private Set<Module> submodules;
    private boolean sealed;

    AbstractEffectiveModule(final StmtContext<String, D, ? extends EffectiveStatement<String, ?>> ctx) {
        super(ctx);

        this.name = argument();

        final PrefixEffectiveStatementImpl prefixStmt = firstEffective(PrefixEffectiveStatementImpl.class);
        this.prefix = (prefixStmt == null) ? null : prefixStmt.argument();

        final YangVersionEffectiveStatementImpl yangVersionStmt = firstEffective(YangVersionEffectiveStatementImpl.class);
        this.yangVersion = (yangVersionStmt == null) ? YangVersion.VERSION_1 : yangVersionStmt.argument();

        final OpenconfigVersionEffectiveStatementImpl semanticVersionStmt =
                firstEffective(OpenconfigVersionEffectiveStatementImpl.class);
        this.semanticVersion = (semanticVersionStmt == null) ? DEFAULT_SEMANTIC_VERSION : semanticVersionStmt.argument();

        final OrganizationEffectiveStatementImpl organizationStmt = firstEffective(OrganizationEffectiveStatementImpl.class);
        this.organization = (organizationStmt == null) ? null : organizationStmt.argument();

        final ContactEffectiveStatementImpl contactStmt = firstEffective(ContactEffectiveStatementImpl.class);
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
            this.submoduleContextsToBuild = ImmutableSet.of();
            substatementsOfSubmodules = ImmutableList.of();
        } else if (YangStmtMapping.MODULE.equals(ctx.getPublicDefinition())) {
            /*
             * Aggregation of substatements from submodules should be done only
             * for modules. In case of submodules it does not make sense because
             * of possible circular chains of includes between submodules.
             */
            final Collection<ModuleIdentifier> includedSubmodules = includedSubmodulesMap.values();
            final Set<Module> submodulesInit = new HashSet<>();
            final List<EffectiveStatement<?, ?>> substatementsOfSubmodulesInit = new ArrayList<>();
            for (final ModuleIdentifier submoduleIdentifier : includedSubmodules) {
                @SuppressWarnings("unchecked")
                final Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> submoduleCtx = (Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>>) ctx
                        .getFromNamespace(SubmoduleNamespace.class, submoduleIdentifier);
                final SubmoduleEffectiveStatementImpl submodule = (SubmoduleEffectiveStatementImpl) submoduleCtx
                        .buildEffective();
                submodulesInit.add(submodule);
                substatementsOfSubmodulesInit.addAll(submodule.effectiveSubstatements().stream()
                        .filter(sub -> sub instanceof SchemaNode || sub instanceof DataNodeContainer)
                        .collect(Collectors.toList()));
            }

            this.submodules = ImmutableSet.copyOf(submodulesInit);
            this.submoduleContextsToBuild = ImmutableSet.of();
            substatementsOfSubmodules = ImmutableList.copyOf(substatementsOfSubmodulesInit);
        } else {
            /*
             * Because of possible circular chains of includes between submodules we can
             * collect only submodule contexts here and then build them during
             * sealing of this statement.
             */
            final Set<StmtContext<?, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>>> submoduleContextsInit = new HashSet<>();
            for (final ModuleIdentifier submoduleIdentifier : includedSubmodulesMap.values()) {
                final StmtContext<?, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> submoduleCtx = ctx
                        .getFromNamespace(SubmoduleNamespace.class, submoduleIdentifier);
                submoduleContextsInit.add(submoduleCtx);
            }

            this.submoduleContextsToBuild = ImmutableSet.copyOf(submoduleContextsInit);
            substatementsOfSubmodules = ImmutableList.of();
        }

        if (!submoduleContextsToBuild.isEmpty()) {
            ((Mutable<?, ?, ?>) ctx).addMutableStmtToSeal(this);
            sealed = false;
        } else {
            sealed = true;
        }

        // init substatements collections
        final List<EffectiveStatement<?, ?>> effectiveSubstatements = new ArrayList<>();
        effectiveSubstatements.addAll(effectiveSubstatements());
        effectiveSubstatements.addAll(substatementsOfSubmodules);

        final List<UnknownSchemaNode> unknownNodesInit = new ArrayList<>();
        final Set<AugmentationSchema> augmentationsInit = new LinkedHashSet<>();
        final Set<ModuleImport> importsInit = new HashSet<>();
        final Set<NotificationDefinition> notificationsInit = new HashSet<>();
        final Set<RpcDefinition> rpcsInit = new HashSet<>();
        final Set<Deviation> deviationsInit = new HashSet<>();
        final Set<IdentitySchemaNode> identitiesInit = new HashSet<>();
        final Set<FeatureDefinition> featuresInit = new HashSet<>();
        final List<ExtensionDefinition> extensionNodesInit = new ArrayList<>();

        final Map<QName, DataSchemaNode> mutableChildNodes = new LinkedHashMap<>();
        final Set<GroupingDefinition> mutableGroupings = new HashSet<>();
        final Set<UsesNode> mutableUses = new HashSet<>();
        final Set<TypeDefinition<?>> mutableTypeDefinitions = new LinkedHashSet<>();
        final Set<DataSchemaNode> mutablePublicChildNodes = new LinkedHashSet<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
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
                final DataSchemaNode dataSchemaNode = (DataSchemaNode) effectiveStatement;
                if (!mutableChildNodes.containsKey(dataSchemaNode.getQName())) {
                    mutableChildNodes.put(dataSchemaNode.getQName(), dataSchemaNode);
                    mutablePublicChildNodes.add(dataSchemaNode);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
                }
            }
            if (effectiveStatement instanceof UsesNode) {
                final UsesNode usesNode = (UsesNode) effectiveStatement;
                if (!mutableUses.contains(usesNode)) {
                    mutableUses.add(usesNode);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
                }
            }
            if (effectiveStatement instanceof TypeDefEffectiveStatementImpl) {
                final TypeDefEffectiveStatementImpl typeDef = (TypeDefEffectiveStatementImpl) effectiveStatement;
                final TypeDefinition<?> type = typeDef.getTypeDefinition();
                if (!mutableTypeDefinitions.contains(type)) {
                    mutableTypeDefinitions.add(type);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
                }
            }
            if (effectiveStatement instanceof GroupingDefinition) {
                final GroupingDefinition grp = (GroupingDefinition) effectiveStatement;
                if (!mutableGroupings.contains(grp)) {
                    mutableGroupings.add(grp);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
                }
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.imports = ImmutableSet.copyOf(importsInit);
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
        return yangVersion.toString();
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
        Preconditions.checkState(sealed, "Attempt to get base submodules from unsealed submodule effective statement %s",
                getQNameModule());
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

    @Nonnull
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
    public Set<UsesNode> getUses() {
        return uses;
    }

    @Override
    public SemVer getSemanticVersion() {
        return semanticVersion;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" +
                "name=" + name +
                ", namespace=" + getNamespace() +
                ", revision=" + getQNameModule().getFormattedRevision() +
                ", prefix=" + prefix +
                ", yangVersion=" + yangVersion +
                "]";
    }

    @Override
    public void seal() {
        if (!sealed) {
            submodules = ImmutableSet.copyOf(Iterables.transform(submoduleContextsToBuild,
                    ctx -> (Module) ctx.buildEffective()));
            submoduleContextsToBuild = ImmutableSet.of();
            sealed = true;
        }
    }
}
