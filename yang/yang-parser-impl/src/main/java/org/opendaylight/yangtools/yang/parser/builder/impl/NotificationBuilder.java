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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class NotificationBuilder extends AbstractDocumentedDataNodeContainerBuilder implements SchemaNodeBuilder,
AugmentationTargetBuilder {
    private NotificationDefinitionImpl instance;
    // SchemaNode args
    private SchemaPath schemaPath;
    // AugmentationTarget args
    private final List<AugmentationSchema> augmentations = new ArrayList<>();
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();

    NotificationBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
    }

    NotificationBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path,
            final NotificationDefinition base) {
        super(moduleName, line, qname, path, base);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");

        addedUnknownNodes.addAll(BuilderUtils.wrapUnknownNodes(moduleName, line, base.getUnknownSchemaNodes(), path,
                qname));
        augmentations.addAll(base.getAvailableAugmentations());

    }

    @Override
    public NotificationDefinition build() {
        if (!(getParent() instanceof ModuleBuilder)) {
            throw new YangParseException(getModuleName(), getLine(),
                    "Notification can be defined only under module (was " + getParent() + ")");
        }
        if (instance != null) {
            return instance;
        }
        buildChildren();

        instance = new NotificationDefinitionImpl(qname, schemaPath, this);

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

    @Override
    public String toString() {
        return "notification " + getQName().getLocalName();
    }

    @Override
    protected String getStatementName() {
        return "notification";
    }

}
