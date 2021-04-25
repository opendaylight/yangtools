/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.AbstractQName;

/**
 * An object referencing a QName, either resolved or unresolved.
 *
 * @author Robert Varga
 */
@Beta
public interface QNameReferent extends Immutable {
    /**
     * Return the referenced {@link AbstractQName}.
     *
     * @return An AbstractQName
     */
    AbstractQName getQName();

    /**
     * Return local name part of the referenced QName.
     *
     * @return Local name string.
     */
    default String getLocalName() {
        return getQName().getLocalName();
    }
}
