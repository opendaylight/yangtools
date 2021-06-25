/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * Mock Integer Type Definition designated to increase branch coverage in test cases.
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 */
final class TestIntegerTypeDefinition implements Int8TypeDefinition {
    @Override
    public Int8TypeDefinition getBaseType() {
        return null;
    }

    @Override
    public Optional<String> getUnits() {
        return Optional.empty();
    }

    @Override
    public Optional<Object> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public QName getQName() {
        return null;
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getReference() {
        return Optional.empty();
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public Optional<RangeConstraint<Byte>> getRangeConstraint() {
        return Optional.empty();
    }
}
