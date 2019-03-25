/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * YANG parser support for OpenDaylight extensions as defined in yang-ext.yang.
 *
 * <p>
 * To access this extension, add {@link org.opendaylight.yangtools.odlext.parser.AnyxmlSchemaLocationStatementSupport},
 * {@link org.opendaylight.yangtools.odlext.parser.AnyxmlStatementSupportOverride} and
 * {@link org.opendaylight.yangtools.odlext.parser.AnyxmlSchemaLocationNamespace#BEHAVIOUR} to the reactor.
 *
 * @author Robert Varga
 */
package org.opendaylight.yangtools.odlext.parser;
