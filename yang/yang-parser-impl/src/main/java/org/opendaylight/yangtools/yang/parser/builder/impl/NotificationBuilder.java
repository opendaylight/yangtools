/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public final class NotificationBuilder extends AbstractDocumentedDataNodeContainerBuilder implements SchemaNodeBuilder,
        AugmentationTargetBuilder {
    private NotificationDefinitionImpl instance;
    // SchemaNode args
    private SchemaPath schemaPath;
    private String description;
    private String reference;
    private Status status = Status.CURRENT;
    // AugmentationTarget args
    private final List<AugmentationSchema> augmentations = new ArrayList<>();
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();

    NotificationBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = path;
    }

    NotificationBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path, final NotificationDefinition base) {
        super(moduleName, line, qname,path,base);
        this.schemaPath = path;

        description = base.getDescription();
        reference = base.getReference();
        status = base.getStatus();

        URI ns = qname.getNamespace();
        Date rev = qname.getRevision();
        String pref = qname.getPrefix();
        addedUnknownNodes.addAll(BuilderUtils.wrapUnknownNodes(moduleName, line, base.getUnknownSchemaNodes(), path, ns,
                rev, pref));

        augmentations.addAll(base.getAvailableAugmentations());

    }

    @Override
    public NotificationDefinition build() {
        if (!(getParent() instanceof ModuleBuilder)) {
            throw new YangParseException(getModuleName(), getLine(), "Notification can be defined only under module (was " + getParent() + ")");
        }
        if (instance != null) {
            return instance;
        }
        buildChildren();

        instance = new NotificationDefinitionImpl(qname, schemaPath,this);

        instance.description = description;
        instance.reference = reference;
        instance.status = status;


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

    private static final class NotificationDefinitionImpl extends AbstractDocumentedDataNodeContainer implements NotificationDefinition {
        private final QName qname;
        private final SchemaPath path;
        private String description;
        private String reference;
        private Status status;
        private ImmutableSet<AugmentationSchema> augmentations;
        private ImmutableList<UnknownSchemaNode> unknownNodes;

        NotificationDefinitionImpl(final QName qname, final SchemaPath path, final NotificationBuilder builder) {
            super(builder);
            // TODO Auto-generated constructor stub
            this.qname = qname;
            this.path = path;
        }

        @Override
        public QName getQName() {
            return qname;
        }

        @Override
        public SchemaPath getPath() {
            return path;
        }

        @Override
        public Set<AugmentationSchema> getAvailableAugmentations() {
            return augmentations;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((qname == null) ? 0 : qname.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
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
            final NotificationDefinitionImpl other = (NotificationDefinitionImpl) obj;
            if (qname == null) {
                if (other.qname != null) {
                    return false;
                }
            } else if (!qname.equals(other.qname)) {
                return false;
            }
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(NotificationDefinitionImpl.class.getSimpleName());
            sb.append("[qname=" + qname + ", path=" + path + "]");
            return sb.toString();
        }
    }

}
