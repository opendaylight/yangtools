/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Represents 'yang-data' extension statement defined in https://tools.ietf.org/html/rfc8040#section-8
 * This statement must appear as a top-level statement, otherwise it is ignored and does not appear in the final
 * schema context. It must contain exactly one top-level container node (directly or indirectly via a uses statement).
 */
@Beta
public interface YangDataStatement extends DeclaredStatement<String> {
}
