/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

public final class SupportedFeaturesNamespace
        extends AbstractParserNamespace<SupportedFeaturesNamespace.SupportedFeatures, Set<QName>> {
    public static final @NonNull SupportedFeaturesNamespace INSTANCE = new SupportedFeaturesNamespace();

    private SupportedFeaturesNamespace() {
        super(ModelProcessingPhase.INIT, NamespaceBehaviour.global(SupportedFeaturesNamespace.class));
    }

    public enum SupportedFeatures {
        SUPPORTED_FEATURES
    }
}
