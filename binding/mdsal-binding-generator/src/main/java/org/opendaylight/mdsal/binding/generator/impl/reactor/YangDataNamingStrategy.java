/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;

/**
 * Naming strategy for {@code ietf-restconf:yang-data} template which has a generic string not matching YANG identifier.
 */
@NonNullByDefault
final class YangDataNamingStrategy extends ClassNamingStrategy {
    private final String javaIdentifier;

    YangDataNamingStrategy(final String templateName) {
        javaIdentifier = BindingMapping.mapYangDataName(templateName);
    }

    @Override
    String simpleClassName() {
        return javaIdentifier;
    }

    @Override
    @Nullable ClassNamingStrategy fallback() {
        // javaIdentifier is guaranteed to be unique, there is no need for fallback
        return null;
    }

    @Override
    String rootName() {
        return javaIdentifier;
    }

    @Override
    String childPackage() {
        // javaIdentifier is always unique and provides an identifier which is suitable for package naming as well,
        //  except we need to further expand the package name so it does not class with the class.
        // Since the strategy escapes '$', appending one cannot clash.
        return javaIdentifier + '$';
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("javaIdentifier", javaIdentifier);
    }
}
