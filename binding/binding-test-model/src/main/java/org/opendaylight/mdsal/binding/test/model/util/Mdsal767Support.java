/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model.util;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yang.gen.v1.mdsal767.norev.Mdsal767Data;
import org.opendaylight.yang.gen.v1.mdsal767.norev.One$F;
import org.opendaylight.yangtools.binding.YangFeature;
import org.opendaylight.yangtools.binding.YangFeatureProvider;

@MetaInfServices
@NonNullByDefault
public final class Mdsal767Support implements YangFeatureProvider<Mdsal767Data> {
    @Override
    public Class<Mdsal767Data> boundModule() {
        return Mdsal767Data.class;
    }

    @Override
    public Set<? extends YangFeature<?, Mdsal767Data>> supportedFeatures() {
        return Set.of(One$F.VALUE);
    }
}
