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
 *
 * <p>
 * The openconfig-version extension is integrated in the parser, as it supports the semantic version import resolution,
 * for other extensions use {@link org.opendaylight.yangtools.openconfig.parser.EncryptedValueStatementSupport}
 * and {@link org.opendaylight.yangtools.openconfig.parser.HashedValueStatementSupport}.
 *
 * @author Robert Varga
 */
package org.opendaylight.yangtools.openconfig.parser;
