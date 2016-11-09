/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;

public interface DocumentationGroup {

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementations of
     * ImportStatement and IncludeStatement which do not allow a description statement.
     * These YANG statements have been changed in YANG 1.1 (RFC7950) and can now contain a description statement.
     *
     * @return description statement
     */
    // FIXME: version 2.0.0: make this method non-default
    @Nullable default DescriptionStatement getDescription() {
        return null;
    }

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementations of
     * ImportStatement and IncludeStatement which do not allow a reference statement.
     * These YANG statements have been changed in YANG 1.1 (RFC7950) and can now contain a reference statement.
     *
     * @return reference statement
     */
    // FIXME: version 2.0.0: make this method non-default
    @Nullable default ReferenceStatement getReference() {
        return null;
    }

    interface WithStatus extends DocumentationGroup {

        @Nullable StatusStatement getStatus();
    }
}
