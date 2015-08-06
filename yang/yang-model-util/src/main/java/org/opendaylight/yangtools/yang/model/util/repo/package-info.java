/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * This package introduces concepts, generic interfaces and implementations for
 * creating and working with YANG Schema source repositories and working with them.
 *
 * <h2>Concepts</h2>
 * <dl>
 * <dt>Schema Source</dt>
 * <dd>Source of YANG Schema, which is not processed, not resolved against other schemas
 *    and from contents of <i>Schema Source</i> in combination with other Schema sources
 *    it is possible to create resolved YANG Schema context.
 * </dd>
 * <dt>{@link org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier}</dt>
 * <dd>Identifier of Schema Source. Identifier is not tied with source or representation.</dd>
 * <dt>Schema Source Representation</dt>
 * <dd>Representation of schema source. Representation of schema source could exists in various
 * formats (Java types), depending on stage of processing, but representation MUST BE
 * still result of processing only single unit of schema source (e.g. file, input stream). E.g.:
 * <ul>
 * <li>{@link java.lang.String} - textual representation of source code
 * <li>{@link java.io.InputStream} - input stream containing source code
 * <li>{@link com.google.common.io.ByteSource} - source for input streams containing source code
 * <li>Parsed AST - abstract syntax tree, which is result of a parser, but still it is not linked
 * against other schemas.
 * </ul>
 * </dd>
 * <dt>{@link org.opendaylight.yangtools.yang.model.util.repo.AdvancedSchemaSourceProvider}</dt>
 * <dd>
 *    Service which provides query API to obtain <i>Schema Source Representation</i> associated
 *    with {@link org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier}.
 * </dd>
 * <dt>{@link org.opendaylight.yangtools.yang.model.util.repo.SchemaSourceTransformation}</dt>
 * <dd>
 * Function (service) which provides transformation from one <i>Schema Source Representation</i>
 * type to another.
 * </dd>
 * </dl>
 *
 *
 */
package org.opendaylight.yangtools.yang.model.util.repo;
