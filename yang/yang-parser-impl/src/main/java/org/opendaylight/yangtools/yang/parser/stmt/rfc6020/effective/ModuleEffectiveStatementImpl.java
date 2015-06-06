/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;

public class ModuleEffectiveStatementImpl extends AbstractEffectiveDocumentedDataNodeContainer<String, ModuleStatement>
        implements Module, Immutable {

    private final QNameModule qNameModule;
    private final String name;
    private String sourcePath;
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

    public ModuleEffectiveStatementImpl(final StmtContext<String, ModuleStatement, ?> ctx) {
        super(ctx);

        name = argument();
        qNameModule = ctx.getFromNamespace(ModuleNameToModuleQName.class, name);

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

        source = ctx.getStatementSource().name();

        //ctx.getFromNamespace(IncludedModuleContext.class, ) //ModuleIdentifier

        initSubstatementCollections();
    }

    private void initSubstatementCollections() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();
        Set<AugmentationSchema> augmentationsInit = new HashSet<>();
        Set<ModuleImport> importsInit = new HashSet<>();
        Set<Module> submodulesInit = new HashSet<>();
        Set<NotificationDefinition> notificationsInit = new HashSet<>();
        Set<RpcDefinition> rpcsInit = new HashSet<>();
        Set<Deviation> deviationsInit = new HashSet<>();
        Set<IdentitySchemaNode> identitiesInit = new HashSet<>();
        Set<FeatureDefinition> featuresInit = new HashSet<>();
        List<ExtensionDefinition> extensionNodesInit = new LinkedList<>();



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
            if (effectiveStatement instanceof IncludeEffectiveStatementImpl) {
//                ((IncludeEffectiveStatementImpl) effectiveStatement).
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
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.imports = ImmutableSet.copyOf(importsInit);
        this.submodules = ImmutableSet.copyOf(submodulesInit);
        this.notifications = ImmutableSet.copyOf(notificationsInit);
        this.rpcs = ImmutableSet.copyOf(rpcsInit);
        this.deviations = ImmutableSet.copyOf(deviationsInit);
        this.identities = ImmutableSet.copyOf(identitiesInit);
        this.features = ImmutableSet.copyOf(featuresInit);
        this.extensionNodes = ImmutableList.copyOf(extensionNodesInit);
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
    public String getSource() {
        return source;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((yangVersion == null) ? 0 : yangVersion.hashCode());
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
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (!qNameModule.equals(other.qNameModule)) {
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
        return qNameModule;
    }

}
