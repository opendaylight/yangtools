/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

public final class EmptyType implements EmptyTypeDefinition {
    private static EmptyType instance;
    private static final QName NAME = BaseTypes.constructQName("empty");
    private static final SchemaPath PATH = new SchemaPath(Collections.singletonList(NAME), true);
    private static final String DESCRIPTION = "The empty built-in type represents a leaf that does not have any value, it conveys information by its presence or absence.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#page-131";

    private EmptyType() {
    }

    public static EmptyType getInstance() {
        if (instance == null) {
            instance = new EmptyType();
        }
        return instance;
    }

    @Override
    public EmptyTypeDefinition getBaseType() {
        return this;
    }

    @Override
    public String getUnits() {
        return null;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public QName getQName() {
        return NAME;
    }

    @Override
    public SchemaPath getPath() {
        return PATH;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getReference() {
        return REFERENCE;
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "type empty " + NAME;
    }

}
