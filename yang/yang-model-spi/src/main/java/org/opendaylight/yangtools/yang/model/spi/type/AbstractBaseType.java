/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

abstract class AbstractBaseType<T extends TypeDefinition<T>> extends AbstractTypeDefinition<T> {
    AbstractBaseType(final QName qname) {
        this(qname, ImmutableList.of());
    }

    AbstractBaseType(final QName qname, final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(qname, unknownSchemaNodes);
    }

    AbstractBaseType(final AbstractBaseType<T> original, final QName qname) {
        super(original, qname);
    }

    @Override
    public final T getBaseType() {
        return null;
    }

    @Override
    public final Optional<String> getUnits() {
        return Optional.empty();
    }

    @Override
    public final Optional<? extends Object> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public final Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public final Optional<String> getReference() {
        return Optional.empty();
    }

    @Override
    public final Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    T bindTo(final QName newName) {
        // Most implementations just return self
        // FIXME: or do we want to assert?
        return (T) this;
    }
}
