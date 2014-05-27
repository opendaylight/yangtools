/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class BaseTypes {

    private BaseTypes() {
    }

    public static final URI BASE_TYPES_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:1");
    public static final QName BINARY_QNAME = constructQName("binary");
    public static final QName BITS_QNAME = constructQName("bits");
    public static final QName BOOLEAN_QNAME = constructQName("boolean");
    public static final QName DECIMAL64_QNAME = constructQName("decimal64");
    public static final QName EMPTY_QNAME = constructQName("empty");
    public static final QName ENUMERATION_QNAME = constructQName("enumeration");
    public static final QName INT8_QNAME = constructQName("int8");
    public static final QName INT16_QNAME = constructQName("int16");
    public static final QName INT32_QNAME = constructQName("int32");
    public static final QName INT64_QNAME = constructQName("int64");
    public static final QName LEAFREF_QNAME = constructQName("leafref");
    public static final QName STRING_QNAME = constructQName("string");
    public static final QName UINT8_QNAME = constructQName("uint8");
    public static final QName UINT16_QNAME = constructQName("uint16");
    public static final QName UINT32_QNAME = constructQName("uint32");
    public static final QName UINT64_QNAME = constructQName("uint64");

    /**
     * Construct QName for Built-in base Yang type. The namespace for built-in
     * base yang types is defined as: urn:ietf:params:xml:ns:yang:1
     *
     * @param typeName
     *            yang type name
     * @return built-in base yang type QName.
     */
    public static QName constructQName(final String typeName) {
        return new QName(BASE_TYPES_NAMESPACE, typeName);
    }

    /**
     * Creates Schema Path from Qname.
     *
     * @param typeName
     *            yang type QName
     * @return Schema Path from Qname.
     */
    public static SchemaPath schemaPath(final QName typeName) {
        return SchemaPath.create(true, typeName);
    }

    /**
     * Creates Schema Path from List of partial paths defined as Strings, module
     * Namespace and module latest Revision Date.
     *
     * @param actualPath
     *            List of partial paths
     * @param namespace
     *            Module Namespace
     * @param revision
     *            Revision Date
     * @return Schema Path
     */
    public static SchemaPath schemaPath(final List<String> actualPath, final URI namespace, final Date revision) {
        if (actualPath == null) {
            throw new IllegalArgumentException("The actual path List MUST be specified.");
        }
        final List<QName> pathList = new ArrayList<QName>();
        for (final String path : actualPath) {
            final QName qname = new QName(namespace, revision, path);
            if (qname != null) {
                pathList.add(qname);
            }
        }
        return SchemaPath.create(pathList, true);
    }
}
