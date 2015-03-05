/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

class DerivedStringType extends DerivedType<StringTypeDefinition> implements StringTypeDefinition {

    public DerivedStringType(final ExtendedType definition) {
        super(StringTypeDefinition.class, definition);
    }

    @Override
    StringTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedStringType(base);
    }

    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return delegate().getLengthConstraints();
    }

    @Override
    public List<PatternConstraint> getPatternConstraints() {
        return delegate().getPatternConstraints();
    }


}