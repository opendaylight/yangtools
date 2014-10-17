/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.NamespaceRevisionAware;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainerBuilder;

public final class AugmentationSchemaBuilderImpl extends AbstractDocumentedDataNodeContainerBuilder implements AugmentationSchemaBuilder {
    private final int order;
    private AugmentationSchemaImpl instance;
    private String whenCondition;

    private final String augmentTargetStr;
    private final SchemaPath targetPath;

    private boolean resolved;
    private boolean unsupportedTarget = false;

    private AugmentationSchemaBuilder copyOf;

    public AugmentationSchemaBuilderImpl(final String moduleName, final int line, final String augmentTargetStr,
            final SchemaPath targetPath, final int order) {
        super(moduleName, line, null);
        this.order = order;
        this.augmentTargetStr = augmentTargetStr;
        this.targetPath = targetPath;
    }

    @Override
    protected String getStatementName() {
        return "augment";
    }

    @Override
    public SchemaPath getPath() {
        return targetPath;
    }

    @Override
    public SchemaPath getTargetPath() {
        return targetPath;
    }

    @Override
    public AugmentationSchema build() {
        if (instance != null) {
            return instance;
        }

        buildChildren();

        instance = new AugmentationSchemaImpl(targetPath, order,this);

        Builder parent = getParent();
        if (parent instanceof ModuleBuilder) {
            ModuleBuilder moduleBuilder = (ModuleBuilder) parent;
            instance.namespace = moduleBuilder.getNamespace();
            instance.revision = moduleBuilder.getRevision();
        }

        if (copyOf != null) {
            instance.setCopyOf(copyOf.build());
        }

        RevisionAwareXPath whenStmt;
        if (whenCondition == null) {
            whenStmt = null;
        } else {
            whenStmt = new RevisionAwareXPathImpl(whenCondition, false);
        }
        instance.whenCondition = whenStmt;

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    @Override
    public boolean isResolved() {
        return resolved;
    }

    @Override
    public void setResolved(final boolean resolved) {
        this.resolved = resolved;
    }

    /**
     *  Set true if target of augment is unsupported (e.g. node in body of extension).
     *  In such case, augmentation is skipped and AugmentationSchema is not built.
     */
    @Override
    public void setUnsupportedTarget(boolean unsupportedTarget) {
        this.unsupportedTarget = unsupportedTarget;
    }

    /**
     *  Return true if target of augment is unsupported (e.g. node in body of extension).
     *  In such case, augmentation is skipped and AugmentationSchema is not built.
     */
    @Override
    public boolean isUnsupportedTarget() {
        return unsupportedTarget;
    }

    @Override
    public String getWhenCondition() {
        return whenCondition;
    }

    @Override
    public void addWhenCondition(final String whenCondition) {
        this.whenCondition = whenCondition;
    }

    @Override
    public String getTargetPathAsString() {
        return augmentTargetStr;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + ((augmentTargetStr == null) ? 0 : augmentTargetStr.hashCode());
        result = prime * result + ((whenCondition == null) ? 0 : whenCondition.hashCode());
        result = prime * result + getChildNodeBuilders().hashCode();
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
        AugmentationSchemaBuilderImpl other = (AugmentationSchemaBuilderImpl) obj;
        if (augmentTargetStr == null) {
            if (other.augmentTargetStr != null) {
                return false;
            }
        } else if (!augmentTargetStr.equals(other.augmentTargetStr)) {
            return false;
        }
        if (whenCondition == null) {
            if (other.whenCondition != null) {
                return false;
            }
        } else if (!whenCondition.equals(other.whenCondition)) {
            return false;
        }
        if (!getChildNodeBuilders().equals(other.getChildNodeBuilders())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "augment " + augmentTargetStr;
    }

    public void setCopyOf(final AugmentationSchemaBuilder old) {
        copyOf = old;
    }

    private static final class AugmentationSchemaImpl extends AbstractDocumentedDataNodeContainer implements AugmentationSchema, NamespaceRevisionAware, Comparable<AugmentationSchemaImpl> {
        private final int order;
        private final SchemaPath targetPath;
        private RevisionAwareXPath whenCondition;

        private URI namespace;
        private Date revision;
        private ImmutableList<UnknownSchemaNode> unknownNodes;
        private AugmentationSchema copyOf;

        public AugmentationSchemaImpl(final SchemaPath targetPath, final int order, final AugmentationSchemaBuilderImpl builder) {
            super(builder);
            this.targetPath = targetPath;
            this.order = order;
        }

        public void setCopyOf(final AugmentationSchema build) {
            this.copyOf = build;
        }

        @Override
        public Optional<AugmentationSchema> getOriginalDefinition() {
            return Optional.fromNullable(this.copyOf);
        }

        @Override
        public SchemaPath getTargetPath() {
            return targetPath;
        }

        @Override
        public RevisionAwareXPath getWhenCondition() {
            return whenCondition;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        @Override
        public URI getNamespace() {
            return namespace;
        }

        @Override
        public Date getRevision() {
            return revision;
        }

        @Override
        public int hashCode() {
            final int prime = 17;
            int result = 1;
            result = prime * result + ((targetPath == null) ? 0 : targetPath.hashCode());
            result = prime * result + ((whenCondition == null) ? 0 : whenCondition.hashCode());
            result = prime * result + getChildNodes().hashCode();
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
            AugmentationSchemaImpl other = (AugmentationSchemaImpl) obj;
            if (targetPath == null) {
                if (other.targetPath != null) {
                    return false;
                }
            } else if (!targetPath.equals(other.targetPath)) {
                return false;
            }
            if (whenCondition == null) {
                if (other.whenCondition != null) {
                    return false;
                }
            } else if (!whenCondition.equals(other.whenCondition)) {
                return false;
            }
            if (!getChildNodes().equals(other.getChildNodes())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(AugmentationSchemaImpl.class.getSimpleName());
            sb.append("[");
            sb.append("targetPath=").append(targetPath);
            sb.append(", when=").append(whenCondition);
            sb.append("]");
            return sb.toString();
        }

        @Override
        public int compareTo(final AugmentationSchemaImpl o) {
            checkNotNull(o);
            Iterator<QName> thisIt = this.targetPath.getPathFromRoot().iterator();
            Iterator<QName> otherIt = o.getTargetPath().getPathFromRoot().iterator();
            while (thisIt.hasNext()) {
                if (otherIt.hasNext()) {
                    int comp = thisIt.next().compareTo(otherIt.next());
                    if (comp != 0) {
                        return comp;
                    }
                } else {
                    return 1;
                }
            }
            if (otherIt.hasNext()) {
                return -1;
            }
            return this.order - o.order;
        }
    }
}
