/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A {@link QNameReferent} referencing a resolved QName.
 *
 * @author Robert Varga
 */
@Beta
public interface ResolvedQNameReferent extends QNameReferentBehavior<ResolvedQNameReferent> {
    /**
     * Return the referenced QName.
     *
     * @return A QName
     */
    @Override
    QName getQName();
}
