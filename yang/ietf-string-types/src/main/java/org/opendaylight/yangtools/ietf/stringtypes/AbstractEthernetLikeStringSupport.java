/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.AbstractDerivedStringSupport;

@NonNullByDefault
abstract class AbstractEthernetLikeStringSupport<T extends AbstractNetworkUnsigned64String<T>>
        extends AbstractDerivedStringSupport<T>  {


    AbstractEthernetLikeStringSupport(final Class<T> representationClass) {
        super(representationClass);
    }

}
