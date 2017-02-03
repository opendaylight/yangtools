/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import javax.xml.namespace.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Unique namespace
 *
 * Marker interface for unique namespaces. Attempt to add value with key that is
 * present in the namespace already will result in an error.
 *
 * K - type of key
 * V - type of value
 */
public interface UniqueNamespace<K extends QName, V extends StmtContext<?, ?, ?>> {
}
