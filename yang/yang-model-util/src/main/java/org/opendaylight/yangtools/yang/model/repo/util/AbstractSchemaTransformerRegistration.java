/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaTransformerRegistration;

public abstract class AbstractSchemaTransformerRegistration extends AbstractObjectRegistration<SchemaSourceTransformer<?, ?>> implements SchemaTransformerRegistration {
    protected AbstractSchemaTransformerRegistration(
            final SchemaSourceTransformer<?, ?> transformer) {
        super(transformer);
    }
}
