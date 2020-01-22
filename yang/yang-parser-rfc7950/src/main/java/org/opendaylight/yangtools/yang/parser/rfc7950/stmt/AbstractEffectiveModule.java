/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
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
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public abstract class AbstractEffectiveModule<D extends DeclaredStatement<String>> extends
        AbstractSchemaEffectiveDocumentedNode<String, D> implements Module,
        NotificationNodeContainerCompat<String, D> {
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
    private final ImmutableSet<GroupingDefinition> groupings;
    private final ImmutableSet<UsesNode> uses;
    private final ImmutableSet<TypeDefinition<?>> typeDefinitions;
    private final ImmutableSet<DataSchemaNode> publicChildNodes;
    private final SemVer semanticVersion;

    protected AbstractEffectiveModule(
            final @NonNull StmtContext<String, D, ? extends EffectiveStatement<String, ?>> ctx,
            final @NonNull String prefix) {
        super(ctx);

        this.name = argument();
        this.prefix = requireNonNull(prefix);
        this.yangVersion = findFirstEffectiveSubstatementArgument(YangVersionEffectiveStatement.class)
                .orElse(YangVersion.VERSION_1);
        this.semanticVersion = findFirstEffectiveSubstatementArgument(OpenConfigVersionEffectiveStatement.class)
                .orElse(null);
        this.organization = findFirstEffectiveSubstatementArgument(OrganizationEffectiveStatement.class)
                .orElse(null);
        this.contact = findFirstEffectiveSubstatementArgument(ContactEffectiveStatement.class)
                .orElse(null);

        final Set<AugmentationSchemaNode> augmentationsInit = new LinkedHashSet<>();
        final Set<ModuleImport> importsInit = new LinkedHashSet<>();
        final Set<NotificationDefinition> notificationsInit = new LinkedHashSet<>();
        final Set<RpcDefinition> rpcsInit = new LinkedHashSet<>();
        final Set<Deviation> deviationsInit = new LinkedHashSet<>();
        final Set<IdentitySchemaNode> identitiesInit = new LinkedHashSet<>();
        final Set<FeatureDefinition> featuresInit = new LinkedHashSet<>();
        final List<ExtensionDefinition> extensionNodesInit = new ArrayList<>();

        final Set<GroupingDefinition> mutableGroupings = new LinkedHashSet<>();
        final Set<UsesNode> mutableUses = new LinkedHashSet<>();
        final Set<TypeDefinition<?>> mutableTypeDefinitions = new LinkedHashSet<>();
        final Set<DataSchemaNode> mutablePublicChildNodes = new LinkedHashSet<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
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
                mutablePublicChildNodes.add((DataSchemaNode) effectiveStatement);
            }
            if (effectiveStatement instanceof UsesNode && !mutableUses.add((UsesNode) effectiveStatement)) {
                throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
            }
            if (effectiveStatement instanceof TypedefEffectiveStatement) {
                final TypeDefinition<?> type = ((TypedefEffectiveStatement) effectiveStatement).getTypeDefinition();
                if (!mutableTypeDefinitions.add(type)) {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
                }
            }
            if (effectiveStatement instanceof GroupingDefinition
                    && !mutableGroupings.add((GroupingDefinition) effectiveStatement)) {
                throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
            }
        }

        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.imports = ImmutableSet.copyOf(importsInit);
        this.notifications = ImmutableSet.copyOf(notificationsInit);
        this.rpcs = ImmutableSet.copyOf(rpcsInit);
        this.deviations = ImmutableSet.copyOf(deviationsInit);
        this.identities = ImmutableSet.copyOf(identitiesInit);
        this.features = ImmutableSet.copyOf(featuresInit);
        this.extensionNodes = ImmutableList.copyOf(extensionNodesInit);

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
    public Collection<? extends ModuleImport> getImports() {
        return imports;
    }

    @Override
    public Collection<? extends FeatureDefinition> getFeatures() {
        return features;
    }

    @Override
    public Collection<? extends NotificationDefinition> getNotifications() {
        return notifications;
    }

    @Override
    public Collection<? extends AugmentationSchemaNode> getAugmentations() {
        return augmentations;
    }

    @Override
    public Collection<? extends RpcDefinition> getRpcs() {
        return rpcs;
    }

    @Override
    public Collection<? extends Deviation> getDeviations() {
        return deviations;
    }

    @Override
    public Collection<? extends ExtensionDefinition> getExtensionSchemaNodes() {
        return extensionNodes;
    }

    @Override
    public Collection<? extends IdentitySchemaNode> getIdentities() {
        return identities;
    }

    @Override
    public final Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        return typeDefinitions;
    }

    @Override
    public final Collection<? extends DataSchemaNode> getChildNodes() {
        return publicChildNodes;
    }

    @Override
    public final Collection<? extends GroupingDefinition> getGroupings() {
        return groupings;
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenField")
    public final Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return findDataSchemaNode(name);
    }

    @Override
    public Collection<? extends UsesNode> getUses() {
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

    protected static final @NonNull String findPrefix(final @NonNull StmtContext<?, ?, ?> ctx,
            final String type, final String name) {
        return SourceException.throwIfNull(
            StmtContextUtils.firstAttributeOf(ctx.declaredSubstatements(), PrefixStatement.class),
            ctx.getStatementSourceReference(), "Unable to resolve prefix for %s %s.", type, name);
    }
}
