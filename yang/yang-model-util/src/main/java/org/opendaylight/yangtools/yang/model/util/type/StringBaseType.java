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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class StringBaseType extends BaseType<StringTypeDefinition> implements StringTypeDefinition {
    static final StringBaseType INSTANCE = new StringBaseType();

    private StringBaseType() {
        super(BaseTypes.STRING_QNAME);
    }

    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return ImmutableList.of();
    }

    @Override
    public List<PatternConstraint> getPatternConstraints() {
        return ImmutableList.of();
    }

    @Override
    public StringConstrainedTypeBuilder newConstrainedTypeBuilder(final SchemaPath path) {
        return new StringConstrainedTypeBuilder(this, path);
    }
}
