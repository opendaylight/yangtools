/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import java.net.URI;
import java.util.Date;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleImpl;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;

public class ModuleEffectiveStatementImpl extends
        AbstractEffectiveDocumentedDataNodeContainer<String, ModuleStatement>
        implements Module, Immutable {

    private QNameModule qnameModule;
    private String name;
    private String sourcePath;
    private String prefix;
    private String yangVersion;
    private String organization;
    private String contact;
    private Set<ModuleImport> imports;
    private Set<Module> submodules;
    private Set<FeatureDefinition> features;
    private Set<NotificationDefinition> notifications;
    private Set<AugmentationSchema> augmentations;
    private Set<RpcDefinition> rpcs;
    private Set<Deviation> deviations;
    private List<ExtensionDefinition> extensionNodes;
    private Set<IdentitySchemaNode> identities;
    private List<UnknownSchemaNode> unknownNodes;
    private String source;

    public ModuleEffectiveStatementImpl(
            StmtContext<String, ModuleStatement, ?> ctx) {
        super(ctx);

        name = argument();
        qnameModule = ctx.getFromNamespace(ModuleNameToModuleQName.class, name);

        initSubstatementCollections();

        // :TODO init other fields
    }

    private void initSubstatementCollections() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        unknownNodes = new LinkedList<UnknownSchemaNode>();
        augmentations = new HashSet<AugmentationSchema>();

        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodes.add(unknownNode);
            }
            if (effectiveStatement instanceof AugmentationSchema) {
                AugmentationSchema augmentationSchema = (AugmentationSchema) effectiveStatement;
                augmentations.add(augmentationSchema);
            }
        }

        // :TODO other substatement collections ...
    }

    @Override
    public String getModuleSourcePath() {
        return sourcePath;
    }

    @Override
    public URI getNamespace() {
        return qnameModule.getNamespace();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getRevision() {
        return qnameModule.getRevision();
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
    public String getSource() {
        return source;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((yangVersion == null) ? 0 : yangVersion.hashCode());
        result = prime * result + qnameModule.hashCode();
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
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (!qnameModule.equals(other.qnameModule)) {
            return false;
        }
        if (yangVersion == null) {
            if (other.yangVersion != null) {
                return false;
            }
        } else if (!yangVersion.equals(other.yangVersion)) {
            return false;
        }
        return true;
    }

    // private static <T extends SchemaNode> Set<T> toImmutableSortedSet(final
    // Set<T> original) {
    // TreeSet<T> sorted = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    // sorted.addAll(original);
    // return Collections.unmodifiableSet(sorted);
    // }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ModuleImpl.class.getSimpleName());
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
        return qnameModule;
    }

}
