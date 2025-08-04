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

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
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
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithTypedefNamespace;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.source.ResolvedSourceInfo;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public abstract class AbstractEffectiveModule<D extends DeclaredStatement<Unqualified>,
        E extends DataTreeAwareEffectiveStatement<Unqualified, D>>
        extends WithTypedefNamespace<Unqualified, D>
        implements ModuleLike, DocumentedNodeMixin<Unqualified, D>, NotificationNodeContainerCompat<Unqualified, D, E> {
    private final @NonNull String prefix;
    private final ImmutableSet<GroupingDefinition> groupings;
    private final ImmutableSet<UsesNode> uses;
    private final ImmutableSet<TypeDefinition<?>> typeDefinitions;

    protected AbstractEffectiveModule(final Current<Unqualified, D> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final String prefix) {
        super(stmt.declared(), substatements);
        this.prefix = requireNonNull(prefix);

        final var mutableGroupings = new LinkedHashSet<GroupingDefinition>();
        final var mutableUses = new LinkedHashSet<UsesNode>();
        final var mutableTypeDefinitions = new LinkedHashSet<TypeDefinition<?>>();

        // FIXME: collisions reported here should be detected in inference
        for (var effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof UsesNode usesNode && !mutableUses.add(usesNode)) {
                throwSourceException(stmt, effectiveStatement);
            }
            if (effectiveStatement instanceof TypedefEffectiveStatement typedef
                    && !mutableTypeDefinitions.add(typedef.getTypeDefinition())) {
                throwSourceException(stmt, effectiveStatement);
            }
            if (effectiveStatement instanceof GroupingDefinition grouping && !mutableGroupings.add(grouping)) {
                throwSourceException(stmt, effectiveStatement);
            }
        }

        groupings = ImmutableSet.copyOf(mutableGroupings);
        typeDefinitions = ImmutableSet.copyOf(mutableTypeDefinitions);
        uses = ImmutableSet.copyOf(mutableUses);
    }

    // Split out to fool SpotBugs
    private static void throwSourceException(final EffectiveStmtCtx.Current<?, ?> stmt,
            final EffectiveStatement<?, ?> effectiveStatement) {
        throw EffectiveStmtUtils.createNameCollisionSourceException(stmt, effectiveStatement);
    }

    @Override
    public final Unqualified argument() {
        return getDeclared().argument();
    }

    @Override
    public final @NonNull SourceIdentifier getSourceIdentifier() {
        return new SourceIdentifier(argument(), getQNameModule().revision());
    }

    @Override
    public final String getName() {
        return argument().getLocalName();
    }

    @Override
    public final String getPrefix() {
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
    public Collection<? extends @NonNull ModuleImport> getImports() {
        return filterSubstatements(ModuleImport.class);
    }

    @Override
    public Collection<? extends @NonNull FeatureDefinition> getFeatures() {
        return filterSubstatements(FeatureDefinition.class);
    }

    @Override
    public Collection<? extends @NonNull NotificationDefinition> getNotifications() {
        return filterSubstatements(NotificationDefinition.class);
    }

    @Override
    public Collection<? extends @NonNull AugmentationSchemaNode> getAugmentations() {
        return filterSubstatements(AugmentationSchemaNode.class);
    }

    @Override
    public Collection<? extends @NonNull RpcDefinition> getRpcs() {
        return filterSubstatements(RpcDefinition.class);
    }

    @Override
    public Collection<? extends @NonNull Deviation> getDeviations() {
        return filterSubstatements(Deviation.class);
    }

    @Override
    public Collection<? extends @NonNull ExtensionDefinition> getExtensionSchemaNodes() {
        return filterSubstatements(ExtensionDefinition.class);
    }

    @Override
    public Collection<? extends @NonNull IdentitySchemaNode> getIdentities() {
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
    public final DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public Collection<? extends UsesNode> getUses() {
        return uses;
    }

    protected static final @NonNull String findPrefix(final CommonStmtCtx stmt,
            final Collection<? extends EffectiveStatement<?, ?>> substatements, final String type, final String name) {
        return substatements.stream()
            .filter(PrefixEffectiveStatement.class::isInstance)
            .map(prefix -> ((PrefixEffectiveStatement) prefix).argument())
            .findAny()
            .orElseThrow(() -> new SourceException(stmt, "Unable to resolve prefix for %s %s.", type, name));
    }

    // Alright. this is quite ugly
    protected final void appendPrefixes(final Current<?, ?> stmt,
            final Builder<String, ModuleEffectiveStatement> builder) {
        final ResolvedSourceInfo resolvedInfo =
            verifyNotNull(stmt.namespaceItem(ParserNamespaces.RESOLVED_INFO, Empty.value()));
        streamEffectiveSubstatements(ImportEffectiveStatement.class)
            .map(imp -> imp.prefix().argument())
            .forEach(pfx -> {
                final var resolvedImport = verifyNotNull(resolvedInfo.imports().get(pfx),
                    "Failed to resolve prefix %s", pfx);
                builder.put(pfx, (ModuleEffectiveStatement) resolvedImport.root().buildEffective());
            });
    }

    @SuppressWarnings("unchecked")
    private <T> @NonNull Collection<? extends T> filterSubstatements(final Class<T> type) {
        return (Collection<? extends T>) Collections2.filter(effectiveSubstatements(), type::isInstance);
    }
}
