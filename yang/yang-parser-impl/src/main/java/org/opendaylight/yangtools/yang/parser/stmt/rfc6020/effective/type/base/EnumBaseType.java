/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public class EnumBaseType implements EnumTypeDefinition {

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.ENUMERATION);

    private static final String UNITS = "";

    private static final String DESCRIPTION = "The enumeration built-in type represents values from a set of assigned names.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.6";

    private static final List<UnknownSchemaNode> UNKNOWN_SCHEMA_NODES = Collections.emptyList();

    private static final Status STATUS = Status.CURRENT;

    private final SchemaPath schemaPath;

    private final List<EnumPair> enumValues;
    private final EnumPair defaultEnum;

    private EnumBaseType(final SchemaPath schemaPath, final List<EnumPair> enumValues, Optional<EnumPair> defaultEnum) {

        this.schemaPath = Preconditions.checkNotNull(schemaPath,
                String.format("SchemaPath in type %s must not be null", QNAME.getLocalName()));
        this.enumValues = Preconditions.checkNotNull(enumValues,
                "When the type is 'union', at least one 'type' substatement MUST be present.");

        if (defaultEnum.isPresent()) {

            Preconditions.checkArgument(enumValues.contains(defaultEnum.get()), String.format(
                    "Default enum value must be contained in defined enumerations in path %s.", schemaPath));

            this.defaultEnum = defaultEnum.get();
        } else {
            this.defaultEnum = null;
        }
    }

    public static EnumBaseType create(final SchemaPath schemaPath, final List<EnumPair> enumValues,
            final EnumPair defaultEnum) {

        return new EnumBaseType(schemaPath, enumValues, Optional.fromNullable(defaultEnum));
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
    public EnumTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return defaultEnum;
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
    public List<EnumPair> getValues() {
        return enumValues;
    }
}
