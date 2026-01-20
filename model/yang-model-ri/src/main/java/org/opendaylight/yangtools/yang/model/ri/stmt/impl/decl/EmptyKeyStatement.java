/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyArgument;

@NonNullByDefault
public final class EmptyKeyStatement extends AbstractKeyStatement {
    public EmptyKeyStatement(final String rawArgument, final KeyArgument argument) {
        super(rawArgument, argument);
    }

    public static Object maskArgument(final KeyArgument argument) {
        return switch (argument) {
            case KeyArgument.OfOne one -> one.item();
            case KeyArgument.OfMore more -> more;
        };
    }

    public static KeyArgument unmaskArgument(final Object obj) {
        return obj instanceof QName qname ? KeyArgument.of(qname) : (KeyArgument.OfMore) obj;
    }
}
