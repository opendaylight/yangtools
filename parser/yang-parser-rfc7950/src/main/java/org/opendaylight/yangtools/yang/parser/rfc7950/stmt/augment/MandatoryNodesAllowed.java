/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

/**
 * The semantics to apply to mandatory nodes introduced via {@code augment} statement when the target node is in a
 * different module.
 */
enum MandatoryNodesAllowed {
    /**
     * RFC6020 semantics: mandatory nodes must not be introduced.
     */
    NEVER,
    /**
     * RFC7950 semantics and the augmentation is unconditional: in may introduce mandatory nodes only to target nodes
     * which do not represent configuration.
     */
    NON_CONFIG,
    /**
     * RFC7950 semantics and the augmentation is conditional via {@code when}: in may introduce mandatory nodes to any
     * target node.
     */
    ALWAYS,
}
