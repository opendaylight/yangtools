/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Utility classes and implementations for concepts defined in yang-model-api.
 *
 *
 * <h2>Base Types</h2>
 *
 * YANG specification defines several base types, for which YANG model does not
 * exists, but have same properties as derived types. This package provides
 * implementation of {@link org.opendaylight.yangtools.yang.model.api.TypeDefinition}
 * interface and it's subinterfaces which represent YANG base types and
 * types derived from them.
 * <p>
 * YANG Specification implicitly defines two types of base types - ones with default version,
 * and ones which needs to be derived.
 *
 * <h3>Base built-in types with default instance and semantics</h3>
 *
 *    <dl>
 *       <dt>empty</dt>
 *       <dd>A leaf that does not have any value - {@link org.opendaylight.yangtools.yang.model.util.EmptyType}</dd>
 *       <dt>binary</dt>
 *       <dd>Any binary data - {@link org.opendaylight.yangtools.yang.model.util.BinaryType}
 *       <dt>boolean</dt>
 *       <dd>"true" or "false" - {@link org.opendaylight.yangtools.yang.model.util.BinaryType}</dd>
 *       <dt>int8</dt>
 *       <dd>8-bit signed integer - {@link org.opendaylight.yangtools.yang.model.util.Int8}</dd>
 *       <dt>int16</dt>
 *       <dd>16-bit signed integer - {@link org.opendaylight.yangtools.yang.model.util.Int16}</dd>
 *       <dt>int32</dt>
 *       <dd>32-bit signed integer - {@link org.opendaylight.yangtools.yang.model.util.Int32}</dd>
 *       <dt>int64</dt>
 *       <dd>64-bit signed integer - {@link org.opendaylight.yangtools.yang.model.util.Int64}</dd>
 *       <dt>uint8</dt>
 *       <dd>8-bit unsigned integer -{@link org.opendaylight.yangtools.yang.model.util.Uint8}</dd>
 *       <dt>uint16</dt>
 *       <dd>16-bit unsigned integer - {@link org.opendaylight.yangtools.yang.model.util.Int16}</dd>
 *       <dt>uint32</dt>
 *       <dd>32-bit unsigned integer - {@link org.opendaylight.yangtools.yang.model.util.Int32}</dd>
 *       <dt>uint64</dt>
 *       <dd>64-bit unsigned integer -{@link org.opendaylight.yangtools.yang.model.util.Int64}</dd>
 *       <dt>instance-identifier</dt>
 *       <dd>References a data tree node - {@link org.opendaylight.yangtools.yang.model.util.InstanceIdentifierType}</dd>
 *       <dt>string</dt>
 *       <dd>{@link org.opendaylight.yangtools.yang.model.util.StringType}</dd>
 *     </dl>
 *
 * Common trait of base types with default instance is, that there is no requirement
 * for user input in YANG schema to further modify this types.
 * <p>
 * The implementation classes for these base types contains static method <code>getInstance()</code>
 * which provides reusable {@link org.opendaylight.yangtools.concepts.Immutable} instance of type.
 *
 * <h3>Base built-in types without default instance</h3>
 *
 *     <dl>
 *       <dt>bits</dt>
 *       <dd>A set of bits or flags - {@link org.opendaylight.yangtools.yang.model.util.BitsType}</dd>
 *       <dt>decimal64</dt>
 *       <dd>64-bit signed decimal number - {@link org.opendaylight.yangtools.yang.model.util.Decimal64}</dd>
 *       <dt>enumeration</dt>
 *       <dd>Enumerated strings - {@link org.opendaylight.yangtools.yang.model.util.EnumerationType}</dd>
 *       <dt>union</dt>
 *       <dd>Choice of member types - {@link org.opendaylight.yangtools.yang.model.util.UnionType}</dd>
 *       <dt>identity-ref</dt>
 *       <dd>A reference to an abstract identity - {@link org.opendaylight.yangtools.yang.model.util.IdentityrefType}</dd>
 *       <dt>leafref</dt>
 *       <dd>A reference to a leaf instance - {@link org.opendaylight.yangtools.yang.model.util.Leafref}</dd>
 *     </dl>
 *
 * Common trait of these base types without default instance is, that they require
 * user input in YANG schema to create instance of this types, and may have infinity number of
 * possible permutations.
 * <p>
 * The implementations have static factory method <code>create(SchemaPath,...)</code>
 * which provides {@link org.opendaylight.yangtools.concepts.Immutable} instance of type.
 *
 */
package org.opendaylight.yangtools.yang.model.util;