/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification;

import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class NotificationEffectiveStatementImpl
        extends AbstractEffectiveDocumentedDataNodeContainer<QName, NotificationStatement>
        implements NotificationDefinition, NotificationEffectiveStatement {
    private static final VarHandle MUST_CONSTRAINTS;

    static {
        try {
            MUST_CONSTRAINTS = MethodHandles.lookup().findVarHandle(NotificationEffectiveStatementImpl.class,
                "mustConstraints", ImmutableSet.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull QName qname;
    private final @NonNull SchemaPath path;
    private final ImmutableSet<AugmentationSchemaNode> augmentations;
    private final boolean augmenting;
    private final boolean addedByUses;

    @SuppressWarnings("unused")
    private volatile ImmutableSet<MustDefinition> mustConstraints;

    NotificationEffectiveStatementImpl(
            final StmtContext<QName, NotificationStatement, EffectiveStatement<QName, NotificationStatement>> ctx) {
        super(ctx);
        this.qname = ctx.coerceStatementArgument();
        this.path = ctx.getSchemaPath().get();

        // initSubstatementCollections
        final Set<AugmentationSchemaNode> augmentationsInit = new LinkedHashSet<>();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof AugmentationSchemaNode) {
                final AugmentationSchemaNode augmentationSchema = (AugmentationSchemaNode) effectiveStatement;
                augmentationsInit.add(augmentationSchema);
            }
        }
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);

        // initCopyType
        final CopyHistory copyTypesFromOriginal = ctx.getCopyHistory();
        if (copyTypesFromOriginal.contains(CopyType.ADDED_BY_USES_AUGMENTATION)) {
            this.augmenting = true;
            this.addedByUses = true;
        } else {
            this.augmenting = copyTypesFromOriginal.contains(CopyType.ADDED_BY_AUGMENTATION);
            this.addedByUses = copyTypesFromOriginal.contains(CopyType.ADDED_BY_USES);
        }
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
    public ImmutableSet<MustDefinition> getMustConstraints() {
        return derivedSet(MUST_CONSTRAINTS, MustDefinition.class);
    }

    @Override
    public Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return augmentations;
    }

    @Deprecated
    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Deprecated
    @Override
    public boolean isAddedByUses() {
        return addedByUses;
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
