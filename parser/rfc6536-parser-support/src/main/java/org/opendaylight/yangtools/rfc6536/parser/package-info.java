/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * YANG parser support for metamodel extensions defined in <a href="https://www.rfc-editor.org/rfc/rfc6536">RFC6536</a>.
 * Add {@link org.opendaylight.yangtools.rfc6536.parser.DefaultDenyAllStatementSupport} and
 * {@link org.opendaylight.yangtools.rfc6536.parser.DefaultDenyWriteStatementSupport} to your reactor to add support
 * for this extension.
 */
// FIXME: 15.0.0: do not export this package
@org.osgi.annotation.bundle.Export
package org.opendaylight.yangtools.rfc6536.parser;
