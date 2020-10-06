/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rcf8528.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContext;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.model.api.SchemaMountConstants;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for MountPointContextFactory implementations, which can process RFC8525 mount point definitions.
 */
@Beta
@NonNullByDefault
public abstract class AbstractMountPointContextFactory extends AbstractDynamicMountPointContextFactory {
    /**
     * Definition of a MountPoint, as known to RFC8528.
     */
    protected static final class MountPointDefinition extends AbstractIdentifiable<MountPointIdentifier>
            implements Immutable {
        private final ImmutableSet<String> parentReferences;
        private final boolean config;

        MountPointDefinition(final MountPointIdentifier identifier, final boolean config,
                final ImmutableSet<String> parentReferences) {
            super(identifier);
            this.config = config;
            this.parentReferences = requireNonNull(parentReferences);
        }

        public boolean getConfig() {
            return config;
        }

        // FIXME: 7.0.0: make this return a set of XPath expressions
        public ImmutableSet<String> getParentReferences() {
            return parentReferences;
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return super.addToStringAttributes(toStringHelper)
                    .add("config", config)
                    .add("parentReferences", parentReferences);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMountPointContextFactory.class);
    private static final NodeIdentifier SCHEMA_MOUNTS = NodeIdentifier.create(
        QName.create(SchemaMountConstants.RFC8528_MODULE, "schema-mounts").intern());
    private static final NodeIdentifier MOUNT_POINT = NodeIdentifier.create(
        QName.create(SchemaMountConstants.RFC8528_MODULE, "mount-point").intern());
    private static final NodeIdentifier CONFIG = NodeIdentifier.create(
        QName.create(SchemaMountConstants.RFC8528_MODULE, "config").intern());
    private static final NodeIdentifier MODULE = NodeIdentifier.create(
        QName.create(SchemaMountConstants.RFC8528_MODULE, "module").intern());
    private static final NodeIdentifier LABEL = NodeIdentifier.create(
        QName.create(SchemaMountConstants.RFC8528_MODULE, "label").intern());
    private static final NodeIdentifier SCHEMA_REF = NodeIdentifier.create(
        QName.create(SchemaMountConstants.RFC8528_MODULE, "schema-ref").intern());
    private static final NodeIdentifier INLINE = NodeIdentifier.create(
        QName.create(SchemaMountConstants.RFC8528_MODULE, "inline").intern());
    private static final NodeIdentifier SHARED_SCHEMA = NodeIdentifier.create(
        QName.create(SchemaMountConstants.RFC8528_MODULE, "shared-schema").intern());
    private static final NodeIdentifier PARENT_REFERENCE = NodeIdentifier.create(
        QName.create(SchemaMountConstants.RFC8528_MODULE, "parent-reference").intern());

    protected AbstractMountPointContextFactory(final MountPointIdentifier mountId) {
        super(mountId);
    }

    @Override
    protected final MountPointContext createMountPointContext(final EffectiveModelContext schemaContext,
            final ContainerNode mountData) {
        checkArgument(SCHEMA_MOUNTS.equals(mountData.getIdentifier()), "Unexpected top-level container %s", mountData);

        final Optional<DataContainerChild<?, ?>> optMountPoint = mountData.getChild(MOUNT_POINT);
        if (optMountPoint.isEmpty()) {
            LOG.debug("mount-point list not present in {}", mountData);
            return new EmptyMountPointContext(schemaContext);
        }

        final DataContainerChild<?, ?> mountPoint = optMountPoint.get();
        checkArgument(mountPoint instanceof MapNode, "mount-point list %s is not a MapNode", mountPoint);

        return new ImmutableMountPointContext(schemaContext, ((MapNode) mountPoint).getValue().stream().map(entry -> {
            final String moduleName = entry.getChild(MODULE).map(mod -> {
                checkArgument(mod instanceof LeafNode, "Unexpected module leaf %s", mod);
                final Object value = mod.getValue();
                checkArgument(value instanceof String, "Unexpected module leaf value %s", value);
                return (String) value;
            }).orElseThrow(() -> new IllegalArgumentException("Mount module missing in " + entry));
            final Iterator<? extends Module> it = schemaContext.findModules(moduleName).iterator();
            checkArgument(it.hasNext(), "Failed to find a module named %s", moduleName);
            final QNameModule module = it.next().getQNameModule();

            return new MountPointDefinition(
                MountPointIdentifier.of(QName.create(module, entry.getChild(LABEL).map(lbl -> {
                    checkArgument(lbl instanceof LeafNode, "Unexpected label leaf %s", lbl);
                    final Object value = lbl.getValue();
                    checkArgument(value instanceof String, "Unexpected label leaf value %s", value);
                    return (String) value;
                }).orElseThrow(() -> new IllegalArgumentException("Mount module missing in " + entry)))),
                entry.getChild(CONFIG).map(cfg -> {
                    checkArgument(cfg instanceof LeafNode, "Unexpected config leaf %s", cfg);
                    final Object value = cfg.getValue();
                    checkArgument(value instanceof Boolean, "Unexpected config leaf value %s", cfg);
                    return (Boolean) value;
                }).orElse(Boolean.TRUE),
                getSchema(entry.getChild(SCHEMA_REF)
                    .orElseThrow(() -> new IllegalArgumentException("Missing schema-ref choice in " + entry))));
        }).collect(Collectors.toList()), this::createContextFactory);
    }

    private static ImmutableSet<String> getSchema(final DataContainerChild<?, ?> child) {
        checkArgument(child instanceof ChoiceNode, "Unexpected schema-ref choice %s", child);
        final ChoiceNode schemaRef = (ChoiceNode) child;

        return schemaRef.getChild(SHARED_SCHEMA).map(sharedSchema -> {
            checkArgument(sharedSchema instanceof ContainerNode, "Unexpected shared-schema container %s", sharedSchema);
            return ((ContainerNode) sharedSchema).getChild(PARENT_REFERENCE).map(parentRef -> {
                // FIXME: 7.0.0: parse XPaths. Do we have enough context for that?
                return ImmutableSet.<String>of();
            }).orElseGet(ImmutableSet::of);
        }).orElseGet(() -> {
            checkArgument(schemaRef.getChild(INLINE).isPresent(), "Unhandled schema-ref type in %s", schemaRef);
            return ImmutableSet.of();
        });
    }

    /**
     * Create a fresh {@link MountPointContextFactory} for a nested {@link MountPointDefinition}.
     *
     * @param mountPoint Mount point definition
     * @return A new factory, dealing with mount points nested within the mount point.
     */
    protected abstract MountPointContextFactory createContextFactory(MountPointDefinition mountPoint);
}
