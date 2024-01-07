/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Intermediate representation of a YANG file. This is an Abstract Syntax Tree equivalent to ParseTree we get from
 * ANTLR, except it is immutable and has a denser in-memory representation due to it not containing any metadata which
 * is not required for the purposes of statement inference.
 *
 * <p>
 * The main entry point into this package is {@link org.opendaylight.yangtools.yang.ir.IRStatement},
 * which represents a single YANG statement. Every YANG file is required to contain exactly one top-level statement,
 * {@code module} or {@code submodule}, hence an IRStatement also represents the significant contents of a YANG file.
 */
@org.osgi.annotation.bundle.Export
package org.opendaylight.yangtools.yang.ir;
