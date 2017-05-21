/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;

@Beta
public final class JsonAnydataNode<T extends JsonAnydataValue<T>> implements AnydataNode<T> {
    private final NodeIdentifier identifier;
    private final T representation;

    private JsonAnydataNode(final NodeIdentifier identifier, final T representation) {
        this.identifier = checkNotNull(identifier);
        this.representation = checkNotNull(representation);
    }

    public static <T extends JsonAnydataValue<T>> JsonAnydataNode<T> create(final NodeIdentifier identifier,
            final T representation) {
        return new JsonAnydataNode<>(identifier, representation);
    }

    @Override
    public NodeIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public QName getNodeType() {
        return identifier.getNodeType();
    }

    @Override
    public T getValue() {
        return representation;
    }
}
