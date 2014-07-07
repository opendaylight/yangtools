/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import java.util.Stack;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class SchemaPathStack {
    private final Stack<Stack<QName>> actualPath = new Stack<>();

    void addNodeToPath(final QName name) {
        actualPath.peek().push(name);
    }

    QName removeNodeFromPath() {
        return actualPath.peek().pop();
    }

    SchemaPath currentSchemaPath() {
        return SchemaPath.create(actualPath.peek(), true);
    }

    void pop() {
        actualPath.pop();
    }

    void push() {
        actualPath.push(new Stack<QName>());
    }
}
