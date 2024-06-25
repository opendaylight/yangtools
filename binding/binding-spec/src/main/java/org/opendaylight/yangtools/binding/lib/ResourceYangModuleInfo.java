/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;

/**
 * Base utility class for providing YANG module info backed by class resources.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class ResourceYangModuleInfo implements YangModuleInfo {
    @Override
    public final InputStream openYangTextStream() throws IOException {
        final Class<?> subclass = getClass();
        final String name = verifyNotNull(resourceName(), "%s provided a null resource name", subclass);
        final InputStream ret = subclass.getResourceAsStream(name);
        if (ret == null) {
            String message = "Failed to open resource " + name + " in context of " + subclass;
            final ClassLoader loader = subclass.getClassLoader();
            if (!ResourceYangModuleInfo.class.getClassLoader().equals(loader)) {
                message = message + " (loaded in " + loader + ")";
            }
            throw new IOException(message);
        }
        return ret;
    }

    @Override
    public final String toString() {
        return addToStringHelperAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringHelperAttributes(final ToStringHelper helper) {
        return helper.add("resource", verifyNotNull(resourceName()));
    }

    protected abstract String resourceName();
}
