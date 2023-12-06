/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public abstract class AbstractSchemaContext implements SchemaContext {
    /**
     * A {@link Module} comparator based on {@link Module#getRevision()}, placing latest revision first. Note this
     * comparator does not take into account module name and so two modules with different names but same revisions
     * compare as equal.
     */
    public static final Comparator<Module> REVISION_COMPARATOR =
        (first, second) -> Revision.compare(second.getRevision(), first.getRevision());

    /**
     * A {@link Module} comparator based on {@link Module#getName()} and {@link Module#getRevision()}, ordering modules
     * lexicographically by their name and then in order of descending revision. This comparator assumes that
     * the combination of these two attributes is sufficient to be consistent with hashCode/equals.
     */
    public static final Comparator<Module> NAME_REVISION_COMPARATOR = (first, second) -> {
        final int cmp = first.getName().compareTo(second.getName());
        return cmp != 0 ? cmp : REVISION_COMPARATOR.compare(first, second);
    };

    /**
     * Create a TreeSet for containing Modules with the same name, such that the set is ordered
     * by {@link #REVISION_COMPARATOR}.
     *
     * @return A fresh TreeSet instance.
     */
    protected static final TreeSet<Module> createModuleSet() {
        return new TreeSet<>(REVISION_COMPARATOR);
    }

    private static final VarHandle DERIVED_IDENTITIES;

    static {
        try {
            DERIVED_IDENTITIES = MethodHandles.lookup().findVarHandle(AbstractSchemaContext.class, "derivedIdentities",
                ImmutableMap.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Accessed via DERIVED_IDENTITIES
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile ImmutableMap<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>> derivedIdentities = null;

    /**
     * Returns the namespace-to-module mapping.
     *
     * @return Map of modules where key is namespace
     */
    protected abstract SetMultimap<XMLNamespace, Module> getNamespaceToModules();

    /**
     * Returns the module name-to-module mapping.
     *
     * @return Map of modules where key is name of module
     */
    protected abstract SetMultimap<String, Module> getNameToModules();

    /**
     * Returns the namespace+revision-to-module mapping.
     *
     * @return Map of modules where key is Module's QNameModule.
     */
    protected abstract Map<QNameModule, Module> getModuleMap();

    @Override
    public Collection<? extends DataSchemaNode> getDataDefinitions() {
        final Set<DataSchemaNode> dataDefs = new HashSet<>();
        for (Module m : getModules()) {
            dataDefs.addAll(m.getChildNodes());
        }
        return dataDefs;
    }

    @Override
    public Collection<? extends NotificationDefinition> getNotifications() {
        final Set<NotificationDefinition> notifications = new HashSet<>();
        for (Module m : getModules()) {
            notifications.addAll(m.getNotifications());
        }
        return notifications;
    }

    @Override
    public Collection<? extends RpcDefinition> getOperations() {
        final Set<RpcDefinition> rpcs = new HashSet<>();
        for (Module m : getModules()) {
            rpcs.addAll(m.getRpcs());
        }
        return rpcs;
    }

    @Override
    public Collection<? extends ExtensionDefinition> getExtensions() {
        final Set<ExtensionDefinition> extensions = new HashSet<>();
        for (Module m : getModules()) {
            extensions.addAll(m.getExtensionSchemaNodes());
        }
        return extensions;
    }

    @Override
    public Optional<? extends Module> findModule(final String name, final Optional<Revision> revision) {
        for (final Module module : getNameToModules().get(name)) {
            if (revision.equals(module.getRevision())) {
                return Optional.of(module);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Module> findModule(final QNameModule qnameModule) {
        return Optional.ofNullable(getModuleMap().get(qnameModule));
    }

    @Override
    public Collection<? extends Module> findModules(final XMLNamespace namespace) {
        return getNamespaceToModules().get(namespace);
    }

    @Override
    public Collection<? extends Module> findModules(final String name) {
        return getNameToModules().get(name);
    }

    @Override
    public Collection<? extends UnknownSchemaNode> getUnknownSchemaNodes() {
        final List<UnknownSchemaNode> result = new ArrayList<>();
        for (Module module : getModules()) {
            result.addAll(module.getUnknownSchemaNodes());
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        final Set<TypeDefinition<?>> result = new LinkedHashSet<>();
        for (Module module : getModules()) {
            result.addAll(module.getTypeDefinitions());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Collection<? extends DataSchemaNode> getChildNodes() {
        final Set<DataSchemaNode> result = new LinkedHashSet<>();
        for (Module module : getModules()) {
            result.addAll(module.getChildNodes());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Collection<? extends GroupingDefinition> getGroupings() {
        final Set<GroupingDefinition> result = new LinkedHashSet<>();
        for (Module module : getModules()) {
            result.addAll(module.getGroupings());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        requireNonNull(name);
        for (Module module : getModules()) {
            final DataSchemaNode result = module.dataChildByName(name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Collection<? extends IdentitySchemaNode> getDerivedIdentities(final IdentitySchemaNode identity) {
        ImmutableMap<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>> local =
                (ImmutableMap<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>>)
                DERIVED_IDENTITIES.getAcquire(this);
        if (local == null) {
            local = loadDerivedIdentities();
        }
        final ImmutableSet<IdentitySchemaNode> result = local.get(requireNonNull(identity));
        if (result == null) {
            throw new IllegalArgumentException("Identity " + identity + " not found");
        }
        return result;
    }

    private ImmutableMap<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>> loadDerivedIdentities() {
        final SetMultimap<IdentitySchemaNode, IdentitySchemaNode> tmp =
                Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
        final List<IdentitySchemaNode> identities = new ArrayList<>();
        for (Module module : getModules()) {
            final Collection<? extends @NonNull IdentitySchemaNode> ids = module.getIdentities();
            for (IdentitySchemaNode identity : ids) {
                for (IdentitySchemaNode base : identity.getBaseIdentities()) {
                    tmp.put(base, identity);
                }
            }
            identities.addAll(ids);
        }

        final ImmutableMap.Builder<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>> builder =
                ImmutableMap.builderWithExpectedSize(identities.size());
        for (IdentitySchemaNode identity : identities) {
            builder.put(identity, ImmutableSet.copyOf(tmp.get(identity)));
        }

        final ImmutableMap<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>> result = builder.build();
        final Object witness = DERIVED_IDENTITIES.compareAndExchangeRelease(this, null, result);
        return witness == null ? result : (ImmutableMap<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>>) witness;
    }
}
