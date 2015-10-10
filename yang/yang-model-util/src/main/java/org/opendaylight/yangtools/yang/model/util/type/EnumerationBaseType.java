/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

public final class EnumerationBaseType extends BaseType<EnumTypeDefinition> implements EnumTypeDefinition {
    private final List<EnumPair> values;

    EnumerationBaseType(final SchemaPath path, final Collection<EnumPair> values) {
        super(path);
        this.values = ImmutableList.copyOf(values);
    }

    @Override
    public List<EnumPair> getValues() {
        return values;
    }

    @Override
    public EnumerationConstrainedTypeBuilder newConstrainedTypeBuilder(final SchemaPath path) {
        return new EnumerationConstrainedTypeBuilder(this, path);
    }
}
