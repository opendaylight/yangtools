/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class ChoiceCaseBuilder extends AbstractDocumentedDataNodeContainerBuilder implements DataSchemaNodeBuilder,
AugmentationTargetBuilder {
    private ChoiceCaseNodeImpl instance;
    // SchemaNode args
    private SchemaPath schemaPath;
    // DataSchemaNode args
    private boolean augmenting;
    private ChoiceCaseNode originalNode;
    private ChoiceCaseBuilder originalBuilder;
    private boolean addedByUses;
    private final ConstraintsBuilder constraints;
    // AugmentationTarget args
    private final List<AugmentationSchema> augmentations = new ArrayList<>();
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();

    public ChoiceCaseBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
        constraints = new ConstraintsBuilderImpl(moduleName, line);
    }

    public ChoiceCaseBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path,
            final ChoiceCaseNode base) {
        super(moduleName, line, qname, Preconditions.checkNotNull(path, "Schema Path must not be null"), base);
        this.schemaPath = path;
        constraints = new ConstraintsBuilderImpl(moduleName, line, base.getConstraints());

        augmenting = base.isAugmenting();
        addedByUses = base.isAddedByUses();
        originalNode = base;
        addedUnknownNodes.addAll(BuilderUtils.wrapUnknownNodes(moduleName, line, base.getUnknownSchemaNodes(), path,
                qname));
        augmentations.addAll(base.getAvailableAugmentations());
    }

    @Override
    public ChoiceCaseNode build() {
        if (instance != null) {
            return instance;
        }
        buildChildren();
        instance = new ChoiceCaseNodeImpl(qname, schemaPath,this);

        instance.augmenting = augmenting;
        instance.addedByUses = addedByUses;

        instance.constraints = constraints.build();

        // ORIGINAL NODE
        if (originalNode == null && originalBuilder != null) {
            originalNode = originalBuilder.build();
        }
        instance.original = originalNode;

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        // AUGMENTATIONS
        for (AugmentationSchemaBuilder builder : augmentationBuilders) {
            augmentations.add(builder.build());
        }
        instance.augmentations = ImmutableSet.copyOf(augmentations);

        return instance;
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
    public ChoiceCaseBuilder getOriginal() {
        return originalBuilder;
    }

    @Override
    public void setOriginal(final SchemaNodeBuilder builder) {
        Preconditions.checkArgument(builder instanceof ChoiceCaseBuilder, "Original of case cannot be " + builder);
        this.originalBuilder = (ChoiceCaseBuilder) builder;
    }

    @Override
    public void addTypedef(final TypeDefinitionBuilder typedefBuilder) {
        throw new YangParseException(getModuleName(), typedefBuilder.getLine(), "Can not add type definition to choice case.");
    }

    @Override
    public void addGrouping(final GroupingBuilder groupingBuilder) {
        throw new YangParseException(getModuleName(), groupingBuilder.getLine(), "Can not add grouping to choice case.");
    }

    @Override
    public boolean isConfiguration() {
        return false;
    }

    @Override
    public void setConfiguration(final boolean configuration) {
        throw new YangParseException(getModuleName(), getLine(), "Can not add config statement to choice case.");
    }

    @Override
    public ConstraintsBuilder getConstraints() {
        return constraints;
    }

    @Override
    public void addAugmentation(final AugmentationSchemaBuilder augment) {
        augmentationBuilders.add(augment);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(schemaPath);
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
        ChoiceCaseBuilder other = (ChoiceCaseBuilder) obj;
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
        return "case " + getQName().getLocalName();
    }

    @Override
    protected String getStatementName() {
        return "choice";
    }

}
