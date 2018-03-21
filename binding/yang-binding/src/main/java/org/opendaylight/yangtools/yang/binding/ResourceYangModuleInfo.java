/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jdt.annotation.NonNullByDefault;

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
        final InputStream ret = ResourceYangModuleInfo.this.getClass()
                .getResourceAsStream(verifyNotNull(resourceName()));
        if (ret == null) {
            throw new IOException("Failed to open resource " + resourceName());
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
