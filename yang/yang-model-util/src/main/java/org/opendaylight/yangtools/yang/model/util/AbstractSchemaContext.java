/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.SetMultimap;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public abstract class AbstractSchemaContext implements SchemaContext {
    protected static final Comparator<Module> REVISION_COMPARATOR =
        (first, second) -> Revision.compare(first.getRevision(), second.getRevision());

    protected static final TreeSet<Module> createModuleSet() {
        return new TreeSet<>(REVISION_COMPARATOR);
    }

    /**
     * Returns the namespace-to-module mapping.
     *
     * @return Map of modules where key is namespace
     */
    protected abstract SetMultimap<URI, Module> getNamespaceToModules();

    /**
     * Returns the module name-to-module mapping.
     *
     * @return Map of modules where key is name of module
     */
    protected abstract SetMultimap<String, Module> getNameToModules();

    @Override
    public Set<DataSchemaNode> getDataDefinitions() {
        final Set<DataSchemaNode> dataDefs = new HashSet<>();
        for (Module m : getModules()) {
            dataDefs.addAll(m.getChildNodes());
        }
        return dataDefs;
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        final Set<NotificationDefinition> notifications = new HashSet<>();
        for (Module m : getModules()) {
            notifications.addAll(m.getNotifications());
        }
        return notifications;
    }

    @Override
    public Set<RpcDefinition> getOperations() {
        final Set<RpcDefinition> rpcs = new HashSet<>();
        for (Module m : getModules()) {
            rpcs.addAll(m.getRpcs());
        }
        return rpcs;
    }

    @Override
    public Set<ExtensionDefinition> getExtensions() {
        final Set<ExtensionDefinition> extensions = new HashSet<>();
        for (Module m : getModules()) {
            extensions.addAll(m.getExtensionSchemaNodes());
        }
        return extensions;
    }


    @Override
    public Optional<Module> findModule(final String name, final Optional<Revision> revision) {
        for (final Module module : getNameToModules().get(name)) {
            if (revision.equals(module.getRevision())) {
                return Optional.of(module);
            }
        }

        return Optional.empty();
    }

    @Override
    public Set<Module> findModules(final URI namespace) {
        final Set<Module> ret = getNamespaceToModules().get(namespace);
        return ret == null ? Collections.emptySet() : ret;
    }

    @Override
    public boolean isAugmenting() {
        return false;
    }

    @Override
    public boolean isAddedByUses() {
        return false;
    }

    @Override
    public boolean isConfiguration() {
        return false;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return null;
    }

    @Nonnull
    @Override
    public QName getQName() {
        return SchemaContext.NAME;
    }

    @Nonnull
    @Override
    public SchemaPath getPath() {
        return SchemaPath.ROOT;
    }

    @Nonnull
    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Nonnull
    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        final List<UnknownSchemaNode> result = new ArrayList<>();
        for (Module module : getModules()) {
            result.addAll(module.getUnknownSchemaNodes());
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Set<TypeDefinition<?, ?>> getTypeDefinitions() {
        final Set<TypeDefinition<?, ?>> result = new LinkedHashSet<>();
        for (Module module : getModules()) {
            result.addAll(module.getTypeDefinitions());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Set<DataSchemaNode> getChildNodes() {
        final Set<DataSchemaNode> result = new LinkedHashSet<>();
        for (Module module : getModules()) {
            result.addAll(module.getChildNodes());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        final Set<GroupingDefinition> result = new LinkedHashSet<>();
        for (Module module : getModules()) {
            result.addAll(module.getGroupings());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(final QName name) {
        requireNonNull(name);
        for (Module module : getModules()) {
            final Optional<DataSchemaNode> result = module.findDataChildByName(name);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    @Override
    public Set<UsesNode> getUses() {
        return Collections.emptySet();
    }

    @Override
    public boolean isPresenceContainer() {
        return false;
    }

    @Override
    public Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return Collections.emptySet();
    }
}
