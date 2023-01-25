/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Model statement. There are two base types of model statements:
 * <ul>
 *   <li>{@link DeclaredStatement}, which is to say, a statement as was defined in original source. This representation
 *       can be used during computation of effective model or during transformations of the YANG model from one
 *       serialization format to another -- for example creating a {@code .yin} file from a {@code .yang} file, or vice
 *       versa.
 *   </li>
 *   <li>{@link EffectiveStatement}, which is to say, a statement in its canonical form after all YANG language
 *       constructs have been applied to it. This representation has different traits as compared to the declared form,
 *       such as:
 *       <ul>
 *         <li>its substatement layout may differ, for example to account for implicit {@code case}, {@code input} and
 *             {@code output} statements</li>
 *         <li>it will omit {@code feature} and statements predicated on {@code if-feature} if the feature in question
 *             is not supported</li>
 *         <li>it will contain magic entries, like those in {@code ietf-restconf.yang}'s {@code operations} container
 *         </li>
 *         <li>the effects of a {@code uses} or {@code augment} statement being present<li>
 *       </ul>
 *
 *       This object model lends itself for processing YANG-modeled data without too much hustle. There are two
 *       RFC7950-based exceptions to this rule. These are driven by scarceness of use and scalability concerns:
 *       <ol>
 *         <li>{@code config} statements</li>
 *         <li>{@code status} statements</li>
 *       </ol>
 *
 *       While the {@link EffectiveStatement} model implies effective statements should be created so that any statement
 *       can be examined for the value, this has a significant scalability impact: for example in the case of a
 *       {@code grouping} definition, {@code config} is ignored, whereas in other contexts, even when introduced via
 *       a {@code uses} statement, it becomes either {@code true} or {@code false}. A similar situation occurs in case
 *       of {@code status} statements, yet it is less severe.
 *
 *       In both these cases real-life users are very scarce and this information can be computed given a particular
 *       {@link EffectiveStatement} tree position and is a sort of a parent reference (albeit very weak). For these two,
 *       and perhaps some other, statements the object model manifestation is subject to API contract.
 *   </li>
 * </ul>
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 */
public sealed interface ModelStatement<A> permits DeclaredStatement, EffectiveStatement, AbstractModelStatement {
    /**
     * Statement Definition of this statement.
     *
     * @return definition of this statement.
     */
    @NonNull StatementDefinition statementDefinition();

    /**
     * Returns statement argument.
     *
     * @return statement argument.
     */
    @NonNull A argument();
}
