/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * YANG parser support for metamodel extensions defined in
 * <a href="https://github.com/openconfig/public/blob/master/release/models/openconfig-extensions.yang">OpenConfig</a>.
 * Add {@link org.opendaylight.yangtools.openconfig.parser.EncryptedValueStatementSupport},
 * {@link org.opendaylight.yangtools.openconfig.parser.HashedValueStatementSupport} and
 * {@link org.opendaylight.yangtools.openconfig.parser.OpenConfigVersionSupport} to your reactor to
 * add support for this extension, or wire it through
 * {@link org.opendaylight.yangtools.yang.parser.spi.ParserExtension}.
 */
@org.osgi.annotation.bundle.Export
package org.opendaylight.yangtools.openconfig.parser;
