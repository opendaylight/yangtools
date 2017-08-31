/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Throwables;
import com.google.common.collect.ForwardingObject;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.io.File;
import java.io.InputStream;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YinDomSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YinTextToDomTransformer;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

/**
 * This class represents implementation of StatementStreamSource
 * in order to emit YIN statements using supplied StatementWriter.
 *
 * @deprecated Scheduled for removal. Use {@link YinStatementStreamSource} instead.
 */
@Deprecated
public final class YinStatementSourceImpl extends ForwardingObject implements StatementStreamSource {
    private final StatementStreamSource delegate;

    private YinStatementSourceImpl(final YinDomSchemaSource source) {
        this.delegate = YinStatementStreamSource.create(source);
    }

    @Override
    public StatementStreamSource delegate() {
        return delegate;
    }

    public YinStatementSourceImpl(final InputStream inputStream) {
        this(newStreamSource(inputStream));
    }

    private static YinDomSchemaSource newStreamSource(final InputStream inputStream) {
        final SourceIdentifier id = YinTextSchemaSource.identifierFromFilename(inputStream.toString());

        try {
            final YinTextSchemaSource text = YinTextSchemaSource.delegateForByteSource(id,
                ByteSource.wrap(ByteStreams.toByteArray(inputStream)));
            return YinTextToDomTransformer.TRANSFORMATION.apply(text).get();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static YinDomSchemaSource newStreamSource(final String fileName, final boolean isAbsolute) {
        try {
            final File file;
            if (isAbsolute) {
                file = new File(fileName);
            } else {
                file = new File(YinStatementSourceImpl.class.getResource(fileName).toURI());
            }

            final YinTextSchemaSource text = YinTextSchemaSource.delegateForByteSource(
                YinTextSchemaSource.identifierFromFilename(file.getName()), Files.asByteSource(file));

            return YinTextToDomTransformer.TRANSFORMATION.apply(text).get();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public YinStatementSourceImpl(final String fileName, final boolean isAbsolute) {
        this(newStreamSource(fileName, isAbsolute));
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        delegate.writePreLinkage(writer, stmtDef);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule preLinkagePrefixes) {
        delegate.writeLinkage(writer, stmtDef, preLinkagePrefixes);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes) {
        delegate.writeLinkageAndStatementDefinitions(writer, stmtDef, prefixes);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes) {
        delegate().writeFull(writer, stmtDef, prefixes);
    }
}
