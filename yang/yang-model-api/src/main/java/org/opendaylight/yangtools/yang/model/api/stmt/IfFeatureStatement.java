/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Represents YANG if-feature statement.
 *
 * The "if-feature" statement makes its parent statement conditional.
 */
// FIXME: IfFeatureStatement should extend DeclaredStatement<Predicate<QName>> and
// IfFeatureStatementImpl for Yang1.0 reworked to extend AbstractDeclaredStatement<Predicate<QName>>.
public interface IfFeatureStatement extends DeclaredStatement<QName> {

    /**
     * Used in YANG 1.0 (RFC6020) implementation of IfFeatureStatement
     * where the argument is the name of a feature
     * as defined by a "feature" statement.
     *
     * To be replaced by {@link #getIfFeaturePredicate() getIfFeaturePredicate} method.
     *
     * @return QName object for the feature
     */
    @Deprecated
    @Nonnull QName getName();

    /**
     * This method should be overridden for all Yang 1.1 implementation.
     * The default implementation is only applicable for Yang 1.0 (RFC6020).
     * In Yang 1.1 (RFC7950) implementation of IfFeatureStatement, the
     * argument is a boolean expression over feature names defined by
     * "feature" statements. Hence, add implementation to return a
     * predicate on a collection of features against which to evaluate.
     *
     * @return a list of predicates for QNames
     */
    @Beta
    @Nonnull default Predicate<QName> getIfFeaturePredicate() {
        return qName -> true;
    }
}