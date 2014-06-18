package org.opendaylight.yangtools.yang.parser.builder.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.builder.util.Comparators;

public final class ModuleImpl extends AbstractDocumentedDataNodeContainer implements Module, Immutable {

    private final URI namespace;
    private final String name;
    private final String sourcePath;
    private final Optional<Date> revision;
    private final String prefix;
    private final String yangVersion;
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
    private final String source;

    /**
     *
     *
     * <b>Note</b>Constructor has intentionality limited visibility to package, since
     * this class should be only instantiated via builders.
     *
     * @param name
     * @param sourcePath
     * @param builder
     */
    ModuleImpl(final String name, final String sourcePath, final ModuleBuilder builder) {
        super(builder);
        this.name = checkNotNull(name, "Missing name");
        this.sourcePath = sourcePath; //TODO: can this be nullable?
        this.imports = ImmutableSet.<ModuleImport> copyOf(builder.imports);
        this.namespace = builder.getNamespace();
        this.prefix = builder.getPrefix();
        this.revision = builder.getRevision() == null ? Optional.<Date>absent():
                Optional.of(new Date(builder.getRevision().getTime()));
        this.yangVersion = builder.getYangVersion();
        this.organization = builder.getOrganization();
        this.contact = builder.getContact();
        this.features = toImmutableSortedSet(builder.getFeatures());
        this.notifications = toImmutableSortedSet(builder.getNotifications());
        this.augmentations = ImmutableSet.copyOf(builder.getAugments());
        this.rpcs = toImmutableSortedSet(builder.getRpcs());
        this.deviations = ImmutableSet.copyOf(builder.getDeviations());
        this.extensionNodes = ImmutableList.copyOf(builder.getExtensions());
        this.identities = ImmutableSet.copyOf(builder.getIdentities());
        this.unknownNodes = ImmutableList.copyOf(builder.getExtensionInstances());
        this.source = checkNotNull(builder.getSource(), "Missing source");

    }

    @Override
    public String getModuleSourcePath() {
        return sourcePath;
    }

    @Override
    public URI getNamespace() {
        return namespace;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getRevision() {
        if (revision.isPresent()) {
            return new Date(revision.get().getTime());
        } else {
            return null;
        }
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

    public String getSource() {
        return source;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((revision == null) ? 0 : revision.hashCode());
        result = prime * result + ((yangVersion == null) ? 0 : yangVersion.hashCode());
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
        ModuleImpl other = (ModuleImpl) obj;
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (revision == null) {
            if (other.revision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
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

    private static <T extends SchemaNode> Set<T> toImmutableSortedSet(final Set<T> original) {
        TreeSet<T> sorted = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        sorted.addAll(original);
        return Collections.unmodifiableSet(sorted);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ModuleImpl.class.getSimpleName());
        sb.append("[");
        sb.append("name=" + name);
        sb.append(", namespace=" + namespace);
        sb.append(", revision=" + revision);
        sb.append(", prefix=" + prefix);
        sb.append(", yangVersion=" + yangVersion);
        sb.append("]");
        return sb.toString();
    }
}