/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition;

abstract class AbstractLengthRestrictedBaseType<T extends LengthRestrictedTypeDefinition<T>> extends AbstractBaseType<T>
        implements LengthRestrictedTypeDefinition<T> {

    AbstractLengthRestrictedBaseType(final QName qname) {
        super(qname);
    }

    @Override
    public final List<LengthConstraint> getLengthConstraints() {
        return ImmutableList.of();
    }
}
