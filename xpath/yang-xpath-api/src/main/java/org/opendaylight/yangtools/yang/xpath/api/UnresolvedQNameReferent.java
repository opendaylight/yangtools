/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import org.opendaylight.yangtools.yang.common.UnresolvedQName;

/**
 * A {@link QNameReferent} referencing an unresolved QName.
 */
public non-sealed interface UnresolvedQNameReferent extends QNameReferent {
    @Override
    UnresolvedQName getQName();
}
