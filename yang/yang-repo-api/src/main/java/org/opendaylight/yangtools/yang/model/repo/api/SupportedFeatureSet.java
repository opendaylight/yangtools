/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Set of features.
 */
// FIXME: 12.0.0: should have two implementations (Set-based and module/feature based via a builder). This should be
//                named 'FeatureSet' and live in yang-model-api, where it has a tie-in with IfFeatureExpr
@Beta
public abstract class SupportedFeatureSet implements Immutable {

    public abstract boolean contains(@NonNull QName qname);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
