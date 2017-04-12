/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Contains implementation of parser reactor {link @CrossSourceStatementReactor}.
 * Classes provided by this package are responsible for performing of essential
 * parsing processes and building of schema context {@link SchemaContext} as result
 * of parsing process.
 *
 * <p>
 * This package also contains:
 * </p>
 * <ul>
 * <li> implementation of statement context which provides
 * information necessary to creation of declared and effective statement </li>
 * <li> custom statement parser builder {@link CustomStatementParserBuilder} which
 * provides methods and implementation useful for building of custom statement
 * parser</li>
 * <li> entry point to parsing process provided by cross source statement reactor
 * {link @CrossSourceStatementReactor}</li>
 * </ul>
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;