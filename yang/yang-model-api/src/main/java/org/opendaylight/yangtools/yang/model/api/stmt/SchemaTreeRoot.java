/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Common interface implemented by entities which act as the root of the {@code schema tree} and are able to resolve an
 * {@code Absolute absolute schema node identifier} to a {@link SchemaTreeEffectiveStatement}.
 */
@Beta
public interface SchemaTreeRoot {
    /**
     * Find a {@code schema tree} node based on its absolute schema node identifier.
     *
     * @param path Absolute schema node identifier
     * @return Found node, or empty
     * @throws NullPointerException if {@code path} is null
     */
    @NonNull Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(@NonNull Absolute path);
}
