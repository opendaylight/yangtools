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
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

/**
 * Mock LeafrefDypeDefinition implementation with RevisionAwareXPath null reference.
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 */
public class LeafrefTypeWithNullXpath implements LeafrefTypeDefinition {
    @Override
    public PathExpression getPathStatement() {
        return null;
    }

    @Override
    public LeafrefTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public Optional<String> getUnits() {
        return Optional.empty();
    }

    @Override
    public Optional<? extends Object> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public QName getQName() {
        throw new UnsupportedOperationException();
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
    public boolean requireInstance() {
        return false;
    }
}
