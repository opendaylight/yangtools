/*
 * Copyright (c) 2018 Pantheon Technoglogies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

@Beta
public interface IfFeatureAwareDeclaredStatement<A> extends DeclaredStatement<A> {
    /**
     * Return attached if-feature statements. Metamodel differs here between RFC6020 and RFC7950: some nodes will be
     * returning an empty collection in YANG 1.0 mode.
     *
     * @return collection of if-feature statements
     */
    default @NonNull Collection<? extends IfFeatureStatement> getIfFeatures() {
        return declaredSubstatements(IfFeatureStatement.class);
    }
}
