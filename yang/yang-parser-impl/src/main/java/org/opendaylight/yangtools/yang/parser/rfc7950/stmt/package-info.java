/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Statement library for YANG version 1 and 1.1, as defined in RFC6020 and RFC7950. Since YANG 1.1 is built on top
 * of YANG 1 and our semantic model follows version 1.1, base statements are organized in a single package hierarchy.
 *
 * <p>
 * Each statement has its own package underneath this package, from which it exports the StatementSupport instance,
 * which can be wired into the statement reactor. Other classes should be kept package-private, so inter-statement
 * interactions follow properly-exposed API interfaces.
 *
 * <p>
 * Common base and utility classes for individual statement implementations are maintained in this package.
 *
 * @author Robert Varga
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;