/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Common interface for statements which contain either a description/reference or a description/reference/status combo.
 *
 * @deprecated Use {@link DocumentedDeclaredStatement} instead.
 */
@Deprecated
public interface DocumentationGroup {
    /**
     * Return description statement, if available.
     *
     * @return description statement
     */
    @Nullable DescriptionStatement getDescription();

    /**
     * Return description statement, if available.
     *
     * @return description statement
     */
    @Nullable ReferenceStatement getReference();

    interface WithStatus extends DocumentationGroup {

        @Nullable StatusStatement getStatus();
    }
}
