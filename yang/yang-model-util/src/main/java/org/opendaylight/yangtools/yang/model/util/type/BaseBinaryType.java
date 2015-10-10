/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseBinaryType extends AbstractBaseType<BinaryTypeDefinition> implements BinaryTypeDefinition {
    static final BaseBinaryType INSTANCE = new BaseBinaryType();

    private BaseBinaryType() {
        super(BaseTypes.BINARY_QNAME);
    }

    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return ImmutableList.of();
    }
}
