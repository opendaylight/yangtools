/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public abstract class AbstractYangTextSchemaSourceRegistration extends AbstractObjectRegistration<YangTextSchemaSource>
        implements YangTextSchemaSourceRegistration {
    protected AbstractYangTextSchemaSourceRegistration(final YangTextSchemaSource instance) {
        super(instance);
    }
}
