/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public class BitsBaseType implements BitsTypeDefinition {

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.BITS);

    private static final String UNITS = "";

    private static final String DESCRIPTION = "The bits built-in type represents a bit set. "
            + "That is, a bits value is a set of flags identified by small integer position "
            + "numbers starting at 0. Each bit number has an assigned name.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.7";

    private static final List<UnknownSchemaNode> UNKNOWN_SCHEMA_NODES = Collections.emptyList();

    private static final Status STATUS = Status.CURRENT;

    private final SchemaPath schemaPath;
    private final List<Bit> bits;

    private BitsBaseType(final SchemaPath schemaPath, final List<Bit> bits) {

        this.schemaPath = Preconditions.checkNotNull(schemaPath,
                String.format("SchemaPath in type %s must not be null", QNAME.getLocalName()));
        this.bits = Preconditions.checkNotNull(bits,
                "When the type is 'union', at least one 'type' substatement MUST be present.");
    }

    public static BitsBaseType create(final SchemaPath schemaPath, final List<Bit> bits) {
        return new BitsBaseType(schemaPath, bits);
    }

    @Override
    public QName getQName() {
        return QNAME;
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public BitsTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return bits;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return UNKNOWN_SCHEMA_NODES;
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
        return STATUS;
    }

    @Override
    public List<Bit> getBits() {
        return bits;
    }
}
