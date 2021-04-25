/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * Contains the implementation of the parser reactor
 * {@link org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor}.
 *
 * <p>
 * Classes provided by this package are responsible for performing the essential parsing processes and building the
 * schema context {@link org.opendaylight.yangtools.yang.model.api.SchemaContext} as the result of the parsing process.
 *
 * <p>
 * This package also contains:
 * </p>
 * <ul>
 * <li> implementation of statement context which provides the information necessary for creation of declared and
 * effective statements</li>
 * <li> entry point to the parsing process provided by the cross source statement reactor
 * {@link org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor}</li>
 * </ul>
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;
