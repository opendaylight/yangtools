/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.context;

import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;

/**
 * The context of a single {@code case}, holding only the children of that case. A case has no element of its own, so
 * the tree never steps into one: it finds the case's children through the choice. This context is only used where the
 * data starts at a case.
 */
public final class CaseContext extends AbstractCompositeContext {
    public CaseContext(final CaseSchemaNode schema) {
        // A case does not appear in a path to data either, so it has no path step
        super(null, schema, schema);
    }
}
