/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * Meta model of YANG model as was defined in RFC6020 and extracted by analysis
 * of YANG text.
 *
 * <p>
 * Existence of meta-model allows for better evolution of YANG language as it evolves
 * and allows for better support of different serializations of YANG model.
 *
 * <h2>Statements</h2>
 * YANG source is defined as sequence of statement in
 * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-6.3">RFC6020, Section 6.3</a>.
 * this model is also correct for YIN, which is XML serialisation of YANG source.
 *
 * <p>
 * Statements are represented as instances / subclasses of
 * {@link org.opendaylight.yangtools.yang.model.api.meta.ModelStatement} concept and its two subconcepts which are:
 * <ul>
 * <li>
 * {@link org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement} - this contains navigable
 * set of statements model as they was defined / present in original processed
 * sources.
 * </li>
 * <li>{@link org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement} - navigable set of statements
 * which represents effective model of parsed YANG sources, which is derived by rules
 * present in YANG specification and/or was introduced in form of extensions.
 * </li>
 * </ul>
 *
 * <p>
 * Clear separation of declared / effective model is needed, since statement definition also
 * contains information how effective model is computed and there is no one to one mapping
 * between declared and effective model thanks to statements such as {@code uses},
 * {@code augment},{@code deviate},{@code refine}.
 *
 * <h2>Identifiers and Namespaces</h2>
 * Effective model of YANG has several identifier types and namespaces, which behaves differently
 * and are mostly used during data processing and transformation. Namespaces are typically exposed as a pair of methods
 * in an appropriate {@code SomethingAwareEffectiveStatement} -- one for enumeration and one for lookups.
 */
@org.osgi.annotation.bundle.Export
package org.opendaylight.yangtools.yang.model.api.meta;
