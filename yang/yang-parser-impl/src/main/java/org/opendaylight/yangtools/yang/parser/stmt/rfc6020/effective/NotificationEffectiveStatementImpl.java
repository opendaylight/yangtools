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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class NotificationEffectiveStatementImpl extends
        AbstractEffectiveDocumentedDataNodeContainer<QName, NotificationStatement> implements NotificationDefinition {
    private final QName qname;
    private final SchemaPath path;
    private final ConstraintDefinition constraints;
    private final Set<AugmentationSchema> augmentations;
    private final List<UnknownSchemaNode> unknownNodes;

    public NotificationEffectiveStatementImpl(
            final StmtContext<QName, NotificationStatement, EffectiveStatement<QName, NotificationStatement>> ctx) {
        super(ctx);
        this.qname = ctx.getStatementArgument();
        this.path = ctx.getSchemaPath().get();

        this.constraints = EffectiveConstraintDefinitionImpl.forParent(this);

        // initSubstatementCollections
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();
        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();
        Set<AugmentationSchema> augmentationsInit = new LinkedHashSet<>();
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
            if (effectiveStatement instanceof AugmentationSchema) {
                AugmentationSchema augmentationSchema = (AugmentationSchema) effectiveStatement;
                augmentationsInit.add(augmentationSchema);
            }
        }
        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
    }

    @Nonnull
    @Override
    public QName getQName() {
        return qname;
    }

    @Nonnull
    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return constraints;
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return augmentations;
    }

    @Nonnull
    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(qname);
        result = prime * result + Objects.hashCode(path);
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
        final NotificationEffectiveStatementImpl other = (NotificationEffectiveStatementImpl) obj;
        return Objects.equals(qname, other.qname) && Objects.equals(path, other.path);
    }

    @Override
    public String toString() {
        return NotificationEffectiveStatementImpl.class.getSimpleName() + "[qname=" + qname + ", path=" + path + "]";
    }
}
