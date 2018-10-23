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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseStringType extends AbstractLengthRestrictedBaseType<StringTypeDefinition>
        implements StringTypeDefinition {
    static final @NonNull BaseStringType INSTANCE = new BaseStringType();

    private BaseStringType() {
        super(BaseTypes.STRING_QNAME);
    }

    @Override
    public List<PatternConstraint> getPatternConstraints() {
        return ImmutableList.of();
    }

    @Override
    public int hashCode() {
        return StringTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return StringTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return StringTypeDefinition.toString(this);
    }
}
