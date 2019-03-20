/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

/**
 * Common interface for {@link ResolvedQNameReference} and {@link UnresolvedQNameReference}, ensuring that only one of
 * them is implemented by any class.
 *
 * @param <T> {@link QNameReference} behavior subclass
 */
interface QNameReferenceBehavior<T extends QNameReferenceBehavior<T>> extends QNameReference {

}
