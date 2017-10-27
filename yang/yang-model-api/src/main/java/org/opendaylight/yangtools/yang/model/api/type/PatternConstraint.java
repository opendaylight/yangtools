/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;

/**
 * Contains the method for getting the data from the YANG <code>pattern</code> which is substatement
 * of <code>type</code> statement.
 */
public interface PatternConstraint extends ConstraintMetaDefinition {

    /**
     * Returns a Java {@link Pattern}-compatible regular expression (pattern). Returned string performs equivalent
     * matching in terms of enforcement, but it may have a structure completely different from the one in YANG model.
     * This string DOES NOT include the effects of the modifier (if present, as indicated by {@link #getModifier()}.
     *
     * @return string Java Pattern regular expression
     */
    // FIXME: should we be providing a Pattern instance? This, along with the other method is treading the fine
    //        balance between usability of the effective model, the purity of effective view model and memory
    //        overhead. We pick usability and memory footprint and expose both methods from effective model.
    String getJavaPatternString();

    /**
     * Returns a raw regular expression as it was declared in a source. This string conforms to XSD regular expression
     * syntax, which is notably different from Java's Pattern string.
     *
     * @return argument of pattern statement as it was declared in YANG model.
     */
    String getRegularExpressionString();

    /**
     * RFC7950 allows a pattern constraint to be inverted. For this purpose a general modifier concept has been
     * introduced. A pattern can have at most one such modifier.
     *
     * @return modifier, if present
     */
    Optional<ModifierKind> getModifier();
}
