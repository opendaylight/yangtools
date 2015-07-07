/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import static com.google.common.base.Preconditions.checkNotNull;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import java.util.Collection;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
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

public class AugmentEffectiveStatementImpl
        extends
        AbstractEffectiveDocumentedDataNodeContainer<SchemaNodeIdentifier, AugmentStatement>
        implements AugmentationSchema, NamespaceRevisionAware,
        Comparable<AugmentEffectiveStatementImpl> {
    private final int order;
    private final SchemaPath targetPath;
    RevisionAwareXPath whenCondition;

    URI namespace;
    Date revision;
    ImmutableList<UnknownSchemaNode> unknownNodes;
    private AugmentationSchema copyOf;

    public AugmentEffectiveStatementImpl(
            StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx){

        super(ctx);

        SchemaNodeIdentifier schemaNodeIdentifier = ctx.getStatementArgument();
        this.targetPath = SchemaPath.create(
                schemaNodeIdentifier.getPathFromRoot(),
                schemaNodeIdentifier.isAbsolute());

        QNameModule rootModuleQName = Utils.getRootModuleQName(ctx);
        this.namespace = rootModuleQName.getNamespace();
        this.revision = rootModuleQName.getRevision();

        this.order = ctx.getOrder();

        initCopyOf(ctx);
        initSubstatementCollections();
    }

    private void initCopyOf(
            StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx) {
        StatementContextBase<?, ?, ?> originalCtx = ctx.getOriginalCtx();
        if (originalCtx != null) {
            this.copyOf = (AugmentationSchema) originalCtx.buildEffective();
        }
    }

    private void initSubstatementCollections() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();

        boolean initWhen = false;
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
            if(!initWhen && effectiveStatement instanceof WhenEffectiveStatementImpl) {
                WhenEffectiveStatementImpl whenStmt = (WhenEffectiveStatementImpl) effectiveStatement;
                whenCondition = whenStmt.argument();
                initWhen = true;
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
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
        result = prime * result
                + ((targetPath == null) ? 0 : targetPath.hashCode());
        result = prime * result
                + ((whenCondition == null) ? 0 : whenCondition.hashCode());
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
        AugmentEffectiveStatementImpl other = (AugmentEffectiveStatementImpl) obj;
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
        StringBuilder sb = new StringBuilder(
                AugmentEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("targetPath=").append(targetPath);
        sb.append(", when=").append(whenCondition);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int compareTo(final AugmentEffectiveStatementImpl o) {
        checkNotNull(o);
        Iterator<QName> thisIt = this.targetPath.getPathFromRoot().iterator();
        Iterator<QName> otherIt = o.getTargetPath().getPathFromRoot()
                .iterator();
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