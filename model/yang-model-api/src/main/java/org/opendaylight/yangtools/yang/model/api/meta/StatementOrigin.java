/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

/**
 * Enumeration of possible origins of a statement.
 */
public enum StatementOrigin {
    /**
     * Statement was explicitly declared by author of the supplied model, such as spelled out in the text of a YANG
     * module.
     */
    DECLARATION,
    /**
     * Statement was inferred to exist based on effective semantics of some other statement. As an example, the
     * {@code rpc} statement implies presence of both {@code input} and {@code output} statements, even when they are
     * not explicitly declared.
     */
    CONTEXT
}
