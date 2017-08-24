/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;

abstract class AbstractRangeRestrictedBaseType<T extends RangeRestrictedTypeDefinition<T>> extends AbstractBaseType<T>
        implements RangeRestrictedTypeDefinition<T> {
    private final List<RangeConstraint> rangeConstraints;

    AbstractRangeRestrictedBaseType(final QName qname, final Number minValue, final Number maxValue) {
        super(qname);
        this.rangeConstraints = ImmutableList.of(BaseConstraints.newRangeConstraint(
                minValue, maxValue, Optional.absent(), Optional.absent()));
    }

    AbstractRangeRestrictedBaseType(final SchemaPath path, final List<UnknownSchemaNode> unknownSchemaNodes,
        final List<RangeConstraint> rangeConstraints) {
        super(path, unknownSchemaNodes);
        this.rangeConstraints = ImmutableList.copyOf(rangeConstraints);
    }

    @Override
    @Nonnull
    public final List<RangeConstraint> getRangeConstraints() {
        return rangeConstraints;
    }
}
