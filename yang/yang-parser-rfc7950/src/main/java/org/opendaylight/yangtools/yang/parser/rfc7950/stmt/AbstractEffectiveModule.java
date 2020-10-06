/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public abstract class AbstractEffectiveModule<D extends DeclaredStatement<UnqualifiedQName>,
        E extends DataTreeAwareEffectiveStatement<UnqualifiedQName, D>>
        extends WithSubstatements<UnqualifiedQName, D, E>
        implements ModuleLike, DocumentedNodeMixin<UnqualifiedQName, D>,
            NotificationNodeContainerCompat<UnqualifiedQName, D, E> {
    private final String prefix;
    private final ImmutableSet<GroupingDefinition> groupings;
    private final ImmutableSet<UsesNode> uses;
    private final ImmutableSet<TypeDefinition<?>> typeDefinitions;
    private final ImmutableMap<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace;

    protected AbstractEffectiveModule(final D declared,
            final StmtContext<UnqualifiedQName, D, ? extends EffectiveStatement<UnqualifiedQName, ?>> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final String prefix) {
        super(declared, ctx, substatements);

        // This check is rather weird, but comes from our desire to lower memory footprint while providing both
        // EffectiveStatements and SchemaNode interfaces -- which do not overlap completely where child lookups are
        // concerned. This ensures that we have SchemaTree index available for use with child lookups.
        final Map<QName, SchemaTreeEffectiveStatement<?>> schemaTree =
                createSchemaTreeNamespace(ctx.getStatementSourceReference(), effectiveSubstatements());
        schemaTreeNamespace = ImmutableMap.copyOf(schemaTree);

        // Data tree check, not currently used
        createDataTreeNamespace(ctx.getStatementSourceReference(), schemaTree.values(), schemaTreeNamespace);

        this.prefix = requireNonNull(prefix);

        final Set<GroupingDefinition> mutableGroupings = new LinkedHashSet<>();
        final Set<UsesNode> mutableUses = new LinkedHashSet<>();
        final Set<TypeDefinition<?>> mutableTypeDefinitions = new LinkedHashSet<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
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

        this.groupings = ImmutableSet.copyOf(mutableGroupings);
        this.typeDefinitions = ImmutableSet.copyOf(mutableTypeDefinitions);
        this.uses = ImmutableSet.copyOf(mutableUses);
    }

    @Override
    public UnqualifiedQName argument() {
        return getDeclared().argument();
    }

    @Override
    public String getName() {
        return argument().getLocalName();
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public YangVersion getYangVersion() {
        return findFirstEffectiveSubstatementArgument(YangVersionEffectiveStatement.class)
                .orElse(YangVersion.VERSION_1);
    }

    @Override
    public Optional<String> getOrganization() {
        return findFirstEffectiveSubstatementArgument(OrganizationEffectiveStatement.class);
    }

    @Override
    public Optional<String> getContact() {
        return findFirstEffectiveSubstatementArgument(ContactEffectiveStatement.class);
    }

    @Override
    public Collection<? extends ModuleImport> getImports() {
        return filterSubstatements(ModuleImport.class);
    }

    @Override
    public Collection<? extends FeatureDefinition> getFeatures() {
        return filterSubstatements(FeatureDefinition.class);
    }

    @Override
    public Collection<? extends NotificationDefinition> getNotifications() {
        return filterSubstatements(NotificationDefinition.class);
    }

    @Override
    public Collection<? extends AugmentationSchemaNode> getAugmentations() {
        return filterSubstatements(AugmentationSchemaNode.class);
    }

    @Override
    public Collection<? extends RpcDefinition> getRpcs() {
        return filterSubstatements(RpcDefinition.class);
    }

    @Override
    public Collection<? extends Deviation> getDeviations() {
        return filterSubstatements(Deviation.class);
    }

    @Override
    public Collection<? extends ExtensionDefinition> getExtensionSchemaNodes() {
        return filterSubstatements(ExtensionDefinition.class);
    }

    @Override
    public Collection<? extends IdentitySchemaNode> getIdentities() {
        return filterSubstatements(IdentitySchemaNode.class);
    }

    @Override
    public final Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        return typeDefinitions;
    }

    @Override
    public final Collection<? extends DataSchemaNode> getChildNodes() {
        return filterSubstatements(DataSchemaNode.class);
    }

    @Override
    public final Collection<? extends GroupingDefinition> getGroupings() {
        return groupings;
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenField")
    public final Optional<DataSchemaNode> findDataChildByName(final QName name) {
        final SchemaTreeEffectiveStatement<?> child = schemaTreeNamespace.get(requireNonNull(name));
        return child instanceof DataSchemaNode ? Optional.of((DataSchemaNode) child) : Optional.empty();
    }

    @Override
    public Collection<? extends UsesNode> getUses() {
        return uses;
    }

    @Override
    public Optional<SemVer> getSemanticVersion() {
        return findFirstEffectiveSubstatementArgument(OpenConfigVersionEffectiveStatement.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
            final Class<N> namespace) {
        if (SchemaTreeAwareEffectiveStatement.Namespace.class.equals(namespace)) {
            return Optional.of((Map<K, V>) schemaTreeNamespace);
        }
        return super.getNamespaceContents(namespace);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("name", getName())
                .add("namespace", getNamespace())
                .add("revision", getRevision().orElse(null))
                .add("prefix", prefix)
                .add("yangVersion", getYangVersion())
                .toString();
    }

    protected static final @NonNull String findPrefix(final @NonNull StmtContext<?, ?, ?> ctx,
            final String type, final String name) {
        return SourceException.throwIfNull(
            StmtContextUtils.firstAttributeOf(ctx.declaredSubstatements(), PrefixStatement.class),
            ctx.getStatementSourceReference(), "Unable to resolve prefix for %s %s.", type, name);
    }

    // Alright. this is quite ugly
    protected final void appendPrefixes(final StmtContext<?, ?, ?> ctx,
            final Builder<String, ModuleEffectiveStatement> builder) {
        streamEffectiveSubstatements(ImportEffectiveStatement.class)
            .map(imp -> imp.findFirstEffectiveSubstatementArgument(PrefixEffectiveStatement.class).get())
            .forEach(pfx -> {
                final StmtContext<?, ?, ?> importedCtx =
                        verifyNotNull(ctx.getFromNamespace(ImportPrefixToModuleCtx.class, pfx),
                            "Failed to resolve prefix %s", pfx);
                builder.put(pfx, (ModuleEffectiveStatement) importedCtx.buildEffective());
            });
    }

    @SuppressWarnings("unchecked")
    private <T> @NonNull Collection<? extends T> filterSubstatements(final Class<T> type) {
        return (Collection<? extends T>) Collections2.filter(effectiveSubstatements(), type::isInstance);
    }
}
