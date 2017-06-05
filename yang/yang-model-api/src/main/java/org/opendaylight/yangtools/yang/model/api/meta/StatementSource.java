/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

/**
 * Origin of statement.
 * Represents origin of statement - if it was explicitly present
 * in model representation or if it was inferred from context.
 */
public enum StatementSource {

    /**
     * Statement was explicitly declared by author of the supplied model.
     */
    DECLARATION,

    /**
     * Statement was derived from context of YANG model / statement
     * and represents effective model.
     *
     * <p>
     * Effective context nodes are derived from applicable {@link #DECLARATION}
     * statements by interpreting their semantic meaning in context
     * of current statement.
     */
    CONTEXT
}
