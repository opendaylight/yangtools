/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import java.util.Iterator;
import java.util.Objects;

class Utils {

    //FIXME: Probably should be moved to utils bundle.
    static <T> boolean  isPrefix(final Iterable<T> prefix, final Iterable<T> other) {
        final Iterator<T> prefixIt = prefix.iterator();
        final Iterator<T> otherIt = other.iterator();
        while(prefixIt.hasNext()) {
            if(!otherIt.hasNext()) {
                return false;
            }
            if(!Objects.deepEquals(prefixIt.next(), otherIt.next())) {
                return false;
            }
        }
        return true;
    }

}
