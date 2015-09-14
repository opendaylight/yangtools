/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class ListSchemaNodeBuilder extends AbstractDocumentedDataNodeContainerBuilder implements
        DataSchemaNodeBuilder, AugmentationTargetBuilder {
    private ListSchemaNodeImpl instance;
    private boolean userOrdered;
    private Set<String> keys;
    private List<QName> keyDefinition;
    // SchemaNode args
    private SchemaPath schemaPath;
    // DataSchemaNode args
    private boolean augmenting;
    private boolean addedByUses;
    private ListSchemaNodeBuilder originalBuilder;
    private ListSchemaNode originalNode;
    private boolean configuration;
    private final ConstraintsBuilder constraints;
    // AugmentationTarget args
    private final List<AugmentationSchema> augmentations = new ArrayList<>();
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();

    public ListSchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
        constraints = new ConstraintsBuilderImpl(moduleName, line);
    }

    public ListSchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path,
            final ListSchemaNode base) {
        super(moduleName, line, qname, path, base);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
        constraints = new ConstraintsBuilderImpl(moduleName, line, base.getConstraints());

        keyDefinition = ImmutableList.copyOf(base.getKeyDefinition());
        userOrdered = base.isUserOrdered();

        augmenting = base.isAugmenting();
        addedByUses = base.isAddedByUses();
        originalNode = base;
        configuration = base.isConfiguration();

        addedUnknownNodes.addAll(BuilderUtils.wrapUnknownNodes(moduleName, line, base.getUnknownSchemaNodes(), path,
                qname));
        augmentations.addAll(base.getAvailableAugmentations());
    }

    @Override
    public ListSchemaNode build() {
        if (instance != null) {
            return instance;
        }
        buildChildren();
        instance = new ListSchemaNodeImpl(qname, schemaPath, this);

        instance.augmenting = augmenting;
        instance.addedByUses = addedByUses;
        instance.configuration = configuration;
        instance.constraints = constraints.build();
        instance.userOrdered = userOrdered;

        // KEY
        if (keys != null) {
            keyDefinition = new ArrayList<>();
            for (String key : keys) {
                DataSchemaNode keyPart = instance.getDataChildByName(key);
                if (keyPart == null) {
                    throw new YangParseException(getModuleName(), getLine(), "Failed to resolve list key for name "
                            + key);
                }

                if (!(keyPart instanceof LeafSchemaNode)) {
                    throw new YangParseException(getModuleName(), getLine(), "List key : \"" + key
                            + "\" does not reference any Leaf of the List");
                }

                final QName qname = keyPart.getQName();
                if (!keyDefinition.contains(qname)) {
                    keyDefinition.add(qname);
                }
            }
            instance.keyDefinition = ImmutableList.copyOf(keyDefinition);
        } else {
            instance.keyDefinition = ImmutableList.of();
        }

        // ORIGINAL NODE
        if (originalNode == null && originalBuilder != null) {
            originalNode = originalBuilder.build();
        }
        instance.original = originalNode;

        // AUGMENTATIONS
        for (AugmentationSchemaBuilder builder : augmentationBuilders) {
            augmentations.add(builder.build());
        }
        instance.augmentations = ImmutableSet.copyOf(augmentations);

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    @Override
    protected String getStatementName() {
        return "list";
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public void setPath(final SchemaPath path) {
        this.schemaPath = path;
    }

    @Override
    public void addAugmentation(final AugmentationSchemaBuilder augment) {
        augmentationBuilders.add(augment);
    }

    public List<AugmentationSchemaBuilder> getAugmentationBuilders() {
        return augmentationBuilders;
    }

    public Set<String> getKeys() {
        return keys;
    }

    public void setKeys(final Set<String> keys) {
        this.keys = keys;
    }

    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public void setAugmenting(final boolean augmenting) {
        this.augmenting = augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public void setAddedByUses(final boolean addedByUses) {
        this.addedByUses = addedByUses;
    }

    @Override
    public ListSchemaNodeBuilder getOriginal() {
        return originalBuilder;
    }

    @Override
    public void setOriginal(final SchemaNodeBuilder builder) {
        Preconditions.checkArgument(builder instanceof ListSchemaNodeBuilder, "Original of list cannot be " + builder);
        this.originalBuilder = (ListSchemaNodeBuilder) builder;
    }

    @Override
    public boolean isConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(final boolean configuration) {
        this.configuration = configuration;
    }

    @Override
    public ConstraintsBuilder getConstraints() {
        return constraints;
    }

    public boolean isUserOrdered() {
        return userOrdered;
    }

    public void setUserOrdered(final boolean userOrdered) {
        this.userOrdered = userOrdered;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
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
        ListSchemaNodeBuilder other = (ListSchemaNodeBuilder) obj;
        if (schemaPath == null) {
            if (other.schemaPath != null) {
                return false;
            }
        } else if (!schemaPath.equals(other.schemaPath)) {
            return false;
        }
        if (getParent() == null) {
            if (other.getParent() != null) {
                return false;
            }
        } else if (!getParent().equals(other.getParent())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "list " + qname.getLocalName();
    }

}
