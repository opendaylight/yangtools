/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public class UInt8BaseType extends UnsignedIntegerBaseType {

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.UINT8);
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(true, QNAME);

    private static final Number MAX_RANGE = 255;

    private static final UInt8BaseType INSTANCE = new UInt8BaseType();

    private UInt8BaseType() {
        super(QNAME, SCHEMA_PATH, MAX_RANGE);
    }

    public static UInt8BaseType getInstance() {
        return INSTANCE;
    }
}
