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
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
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
        final var dataDefs = new HashSet<DataSchemaNode>();
        for (var module : getModules()) {
            dataDefs.addAll(module.getChildNodes());
        }
        return dataDefs;
    }

    @Override
    public Collection<? extends NotificationDefinition> getNotifications() {
        final var notifications = new HashSet<NotificationDefinition>();
        for (var module : getModules()) {
            notifications.addAll(module.getNotifications());
        }
        return notifications;
    }

    @Override
    public Collection<? extends RpcDefinition> getOperations() {
        final var rpcs = new HashSet<RpcDefinition>();
        for (var module : getModules()) {
            rpcs.addAll(module.getRpcs());
        }
        return rpcs;
    }

    @Override
    public Collection<? extends ExtensionDefinition> getExtensions() {
        final var extensions = new HashSet<ExtensionDefinition>();
        for (var module : getModules()) {
            extensions.addAll(module.getExtensionSchemaNodes());
        }
        return extensions;
    }

    @Override
    public Optional<? extends Module> findModule(final String name, final Optional<Revision> revision) {
        for (var module : getNameToModules().get(name)) {
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
    public Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        final var result = new LinkedHashSet<TypeDefinition<?>>();
        for (var module : getModules()) {
            result.addAll(module.getTypeDefinitions());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Collection<? extends DataSchemaNode> getChildNodes() {
        final var result = new LinkedHashSet<DataSchemaNode>();
        for (var module : getModules()) {
            result.addAll(module.getChildNodes());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Collection<? extends GroupingDefinition> getGroupings() {
        final var result = new LinkedHashSet<GroupingDefinition>();
        for (var module : getModules()) {
            result.addAll(module.getGroupings());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        requireNonNull(name);
        for (var module : getModules()) {
            final var result = module.dataChildByName(name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Collection<? extends IdentitySchemaNode> getDerivedIdentities(final IdentitySchemaNode identity) {
        var local = (ImmutableMap<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>>)
            DERIVED_IDENTITIES.getAcquire(this);
        if (local == null) {
            local = loadDerivedIdentities();
        }
        final var result = local.get(requireNonNull(identity));
        if (result == null) {
            throw new IllegalArgumentException("Identity " + identity + " not found");
        }
        return result;
    }

    private ImmutableMap<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>> loadDerivedIdentities() {
        final SetMultimap<IdentitySchemaNode, IdentitySchemaNode> tmp =
                Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
        final var identities = new ArrayList<IdentitySchemaNode>();
        for (var module : getModules()) {
            final var ids = module.getIdentities();
            for (var identity : ids) {
                for (var base : identity.getBaseIdentities()) {
                    tmp.put(base, identity);
                }
            }
            identities.addAll(ids);
        }

        final var builder = ImmutableMap.<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>>builderWithExpectedSize(
            identities.size());
        for (var identity : identities) {
            builder.put(identity, ImmutableSet.copyOf(tmp.get(identity)));
        }

        final var result = builder.build();
        final var witness = DERIVED_IDENTITIES.compareAndExchangeRelease(this, null, result);
        return witness == null ? result : (ImmutableMap<IdentitySchemaNode, ImmutableSet<IdentitySchemaNode>>) witness;
    }
}
