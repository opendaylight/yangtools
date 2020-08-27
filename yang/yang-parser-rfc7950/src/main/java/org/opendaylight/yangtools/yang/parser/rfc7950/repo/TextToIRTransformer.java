/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.repo.util.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.AntlrSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRStatement;

@Beta
public final class TextToIRTransformer extends SchemaSourceTransformer<YangTextSchemaSource, IRSchemaSource> {
    private TextToIRTransformer(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        super(provider, YangTextSchemaSource.class, consumer, IRSchemaSource.class,
            input -> Futures.immediateFuture(transformText(input)));
    }

    public static @NonNull TextToIRTransformer create(final SchemaRepository provider,
            final SchemaSourceRegistry consumer) {
        return new TextToIRTransformer(provider, consumer);
    }

    public static @NonNull IRSchemaSource transformText(final YangTextSchemaSource text)
            throws YangSyntaxErrorException, IOException {
        final IRStatement rootStatement = AntlrSupport.createStatement(YangStatementStreamSource.parseYangSource(text));
        final String name = YangModelDependencyInfo.safeStringArgument(text.getIdentifier(), rootStatement, "name");
        final String latestRevision = YangModelDependencyInfo.getLatestRevision(rootStatement, text.getIdentifier());
        final RevisionSourceIdentifier sourceId = latestRevision == null ? RevisionSourceIdentifier.create(name)
                : RevisionSourceIdentifier.create(name, Revision.of(latestRevision));

        return new IRSchemaSource(sourceId, rootStatement);
    }
}
