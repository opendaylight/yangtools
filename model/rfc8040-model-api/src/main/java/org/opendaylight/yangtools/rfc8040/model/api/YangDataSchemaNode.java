/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.ContainerLikeCompat;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Represents 'yang-data' extension statement defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-8">RFC8040</a>. This statement must appear as a top-level
 * statement, otherwise it is ignored and does not appear in the final schema context.
 */
public interface YangDataSchemaNode extends UnknownSchemaNode, DataNodeContainer {
    @Override
    YangDataEffectiveStatement asEffectiveStatement();

    /**
     * {@return the {@link YangDataName} of this node}
     * @since 14.0.21
     */
    default @NonNull YangDataName name() {
        return asEffectiveStatement().argument();
    }

    /**
     * Return a {@link ContainerLike} backed by this definition's {@link #getChildNodes()}.
     *
     * @return A compatibility {@link ContainerLike}
     */
    default @NonNull ContainerLikeCompat toContainerLike() {
        return new YangDataAsContainer(this);
    }
}
