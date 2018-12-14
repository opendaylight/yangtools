/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
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
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public abstract class AbstractEffectiveModule<D extends DeclaredStatement<String>> extends
        AbstractSchemaEffectiveDocumentedNode<String, D> implements Module, MutableStatement {
    private final String name;
    private final String prefix;
    private final YangVersion yangVersion;
    private final String organization;
    private final String contact;
    private final ImmutableSet<ModuleImport> imports;
    private final ImmutableSet<FeatureDefinition> features;
    private final @NonNull ImmutableSet<NotificationDefinition> notifications;
    private final ImmutableSet<AugmentationSchemaNode> augmentations;
    private final ImmutableSet<RpcDefinition> rpcs;
    private final ImmutableSet<Deviation> deviations;
    private final ImmutableList<ExtensionDefinition> extensionNodes;
    private final ImmutableSet<IdentitySchemaNode> identities;
    private final @NonNull ImmutableList<UnknownSchemaNode> unknownNodes;
    private final ImmutableMap<QName, DataSchemaNode> childNodes;
    private final ImmutableSet<GroupingDefinition> groupings;
    private final ImmutableSet<UsesNode> uses;
    private final ImmutableSet<TypeDefinition<?>> typeDefinitions;
    private final ImmutableSet<DataSchemaNode> publicChildNodes;
    private final SemVer semanticVersion;

    private Set<StmtContext<?, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>>>
        submoduleContextsToBuild;
    private ImmutableSet<Module> submodules;
    private boolean sealed;

    protected AbstractEffectiveModule(final StmtContext<String, D, ? extends EffectiveStatement<String, ?>> ctx) {
        super(ctx);

        this.name = argument();

        final EffectiveStatement<?, ?> parentOfPrefix;
        if (ctx.getPublicDefinition() == YangStmtMapping.SUBMODULE) {
            final Optional<BelongsToEffectiveStatement> optParent =
                    findFirstEffectiveSubstatement(BelongsToEffectiveStatement.class);
            SourceException.throwIf(!optParent.isPresent(), ctx.getStatementSourceReference(),
                    "Unable to find belongs-to statement in submodule %s.", ctx.getStatementArgument());
            parentOfPrefix = optParent.get();
        } else {
            parentOfPrefix = this;
        }

        final Optional<@NonNull PrefixEffectiveStatement> prefixStmt = parentOfPrefix.findFirstEffectiveSubstatement(
            PrefixEffectiveStatement.class);
        SourceException.throwIf(!prefixStmt.isPresent(), ctx.getStatementSourceReference(),
                "Unable to resolve prefix for module or submodule %s.", ctx.getStatementArgument());
        this.prefix = prefixStmt.get().argument();
        this.yangVersion = findFirstEffectiveSubstatementArgument(YangVersionEffectiveStatement.class)
                .orElse(YangVersion.VERSION_1);
        this.semanticVersion = findFirstEffectiveSubstatementArgument(OpenConfigVersionEffectiveStatement.class)
                .orElse(null);
        this.organization = findFirstEffectiveSubstatementArgument(OrganizationEffectiveStatement.class)
                .orElse(null);
        this.contact = findFirstEffectiveSubstatementArgument(ContactEffectiveStatement.class)
                .orElse(null);

        // init submodules and substatements of submodules
        final List<EffectiveStatement<?, ?>> substatementsOfSubmodules;
        final Map<String, StmtContext<?, ?, ?>> includedSubmodulesMap = ctx
                .getAllFromCurrentStmtCtxNamespace(IncludedSubmoduleNameToModuleCtx.class);

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
            final Set<Module> submodulesInit = new HashSet<>();
            final List<EffectiveStatement<?, ?>> substatementsOfSubmodulesInit = new ArrayList<>();
            for (final StmtContext<?, ?, ?> submoduleCtx : includedSubmodulesMap.values()) {
                final EffectiveStatement<?, ?> submodule = submoduleCtx.buildEffective();
                Verify.verify(submodule instanceof SubmoduleEffectiveStatement);
                Verify.verify(submodule instanceof Module, "Submodule statement %s is not a Module", submodule);
                submodulesInit.add((Module) submodule);
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
            final Set<StmtContext<?, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>>>
                submoduleContextsInit = new HashSet<>();
            for (final StmtContext<?, ?, ?> submoduleCtx : includedSubmodulesMap.values()) {
                submoduleContextsInit.add(
                    (StmtContext<?, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>>)submoduleCtx);
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
        final Set<AugmentationSchemaNode> augmentationsInit = new LinkedHashSet<>();
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
            if (effectiveStatement instanceof AugmentationSchemaNode) {
                augmentationsInit.add((AugmentationSchemaNode) effectiveStatement);
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
            if (effectiveStatement instanceof TypedefEffectiveStatement) {
                final TypedefEffectiveStatement typeDef = (TypedefEffectiveStatement) effectiveStatement;
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
    public URI getNamespace() {
        return getQNameModule().getNamespace();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Revision> getRevision() {
        return getQNameModule().getRevision();
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public YangVersion getYangVersion() {
        return yangVersion;
    }

    @Override
    public Optional<String> getOrganization() {
        return Optional.ofNullable(organization);
    }

    @Override
    public Optional<String> getContact() {
        return Optional.ofNullable(contact);
    }

    @Override
    public Set<ModuleImport> getImports() {
        return imports;
    }

    @Override
    public Set<Module> getSubmodules() {
        checkState(sealed, "Attempt to get base submodules from unsealed submodule effective statement %s",
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
    public Set<AugmentationSchemaNode> getAugmentations() {
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
    @SuppressWarnings("checkstyle:hiddenField")
    public final Optional<DataSchemaNode> findDataChildByName(final QName name) {
        // Child nodes are keyed by their container name, so we can do a direct lookup
        return Optional.ofNullable(childNodes.get(requireNonNull(name)));
    }

    @Override
    public Set<UsesNode> getUses() {
        return uses;
    }

    @Override
    public Optional<SemVer> getSemanticVersion() {
        return Optional.ofNullable(semanticVersion);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("name", name)
                .add("namespace", getNamespace())
                .add("revision", getRevision().orElse(null))
                .add("prefix", prefix)
                .add("yangVersion", yangVersion)
                .toString();
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
