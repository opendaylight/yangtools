/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * An additional trait for {@link StatementSupport}, allowing a parent statement to override the support for a child
 * statement.
 */
@Beta
public interface OverrideChildStatementSupport {
    /**
     * Returns unknown statement form of a regular YANG statement supplied as a parameter to the method.
     *
     * @param childDef statement definition of a regular YANG statement
     * @return Optional of unknown statement form of a regular YANG statement, or {@code null} if it should not be
     *         overridden.
     */
    // FIXME: propagating definition
    @Nullable StatementSupport<?, ?, ?> statementDefinitionOverrideOf(@NonNull StatementDefinition<?, ?, ?> childDef);
}
