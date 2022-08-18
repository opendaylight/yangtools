/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.MoreObjects;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * {@link ParserNamespace} serves as common superclass for namespaces used during parser operation. Each such namespace,
 * is a type-captured subclass. This type capture of namespace allows for handy type-safe reading methods such as
 * {@link NamespaceStmtCtx#getFromNamespace(ParserNamespace, Object)} and still allows introduction of new namespaces
 * without need to change APIs.
 *
 * @param <K> Identifier type
 * @param <V> Value type
 */
@NonNullByDefault
public class ParserNamespace<K, V> implements Identifier {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
