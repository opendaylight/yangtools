/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.base.Preconditions;

import java.util.Deque;
import java.util.LinkedList;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class SchemaPathStack {
    private final Deque<SchemaPath> paths = new LinkedList<>();

    SchemaPath addNodeToPath(final QName name) {
        SchemaPath sp = paths.pop();
        sp = sp.createChild(name);
        paths.push(sp);
        return sp;
    }

    QName removeNodeFromPath() {
        SchemaPath sp = paths.pop();
        QName ret = sp.getLastComponent();
        paths.push(Preconditions.checkNotNull(sp.getParent(), "Attempted to remove too many nodes from schemapath at stack %s", paths));
        return ret;
    }

    SchemaPath currentSchemaPath() {
        return paths.peek();
    }

    void pop() {
        paths.pop();
    }

    void push() {
        paths.push(SchemaPath.ROOT);
    }
}
