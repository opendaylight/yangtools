/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 *
 * Definition / model of YANG {@link DeclaredStatement} and {@link EffectiveStatement}.
 *
 * Statement concept is defined in RFC6020 section 6.3:
 * <blockquote> A YANG
 * module contains a sequence of statements. Each statement starts with a
 * keyword, followed by zero or one argument
 * </blockquote>
 *
 * Source: <a href="https://tools.ietf.org/html/rfc6020#section-6.3"> </a>
 *
 *
 */
public interface StatementDefinition extends Immutable {

    /**
     *
     * Returns name of the statement
     *
     * @return Name of the statement
     */
    @Nonnull
    QName getStatementName();

    /**
     *
     * Returns name of statement argument or null, if statement does not have
     * argument.
     *
     * @return argument name or null, if statement does not take argument.
     */
    @Nullable
    QName getArgumentName();

    /**
     *
     * Returns class which represents declared version of statement associated
     * with this definition.
     *
     * This class should be interface, which provides convenience access to
     * declared substatements.
     *
     * @return class which represents declared version of statement associated
     *         with this definition.
     */
    @Nonnull
    Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass();

    /**
     *
     * Returns class which represents derived behaviour from supplied statement.
     *
     * This class should be interface, which defines convenience access to
     * statement properties, namespace items and substatements.
     *
     * @return class which represents effective version of statement associated
     *         with this definition
     */
    @Nonnull
    Class<? extends EffectiveStatement<?,?>> getEffectiveRepresentationClass();

}
