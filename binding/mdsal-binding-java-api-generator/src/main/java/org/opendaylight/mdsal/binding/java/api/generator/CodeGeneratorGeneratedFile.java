/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import java.io.Writer;
import org.opendaylight.mdsal.binding.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.plugin.generator.api.AbstractGeneratedTextFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileLifecycle;

final class CodeGeneratorGeneratedFile extends AbstractGeneratedTextFile {
    private final CodeGenerator generator;
    private final Type type;

    CodeGeneratorGeneratedFile(final GeneratedFileLifecycle lifecycle, final CodeGenerator generator, final Type type) {
        super(lifecycle);
        this.generator = requireNonNull(generator);
        this.type = requireNonNull(type);
    }

    @Override
    protected void writeBody(final Writer output) throws IOException {
        output.write(generator.generate(type));
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("generator", generator).add("type", type);
    }
}
