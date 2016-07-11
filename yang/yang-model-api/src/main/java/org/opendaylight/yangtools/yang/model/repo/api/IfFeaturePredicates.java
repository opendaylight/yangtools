/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
/**
 * If-feature predicate constants.
 */
public final class IfFeaturePredicates {
    /**
     * All features predicate constant. Using of this constant improves
     * performance of schema context resolution.
     */
    public static final Predicate<QName> ALL_FEATURES = t -> true;

    private IfFeaturePredicates() {
        throw new UnsupportedOperationException("Utility class");
    }
}
