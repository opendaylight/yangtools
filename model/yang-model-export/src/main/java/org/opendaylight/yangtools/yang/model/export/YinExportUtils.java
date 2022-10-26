/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static java.util.Objects.requireNonNull;

import java.io.OutputStream;
import java.util.Optional;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;

public final class YinExportUtils {
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private YinExportUtils() {
        // Hidden on purpose
    }

    /**
     * Returns well-formed file name of YIN file as defined in RFC6020.
     *
     * @param name Module or submodule name
     * @param revision Revision of module or submodule
     * @return well-formed file name of YIN file as defined in RFC6020.
     */
    public static String wellFormedYinName(final String name, final Optional<Revision> revision) {
        return wellFormedYinName(name, revision.map(Revision::toString).orElse(null));
    }

    /**
     * Returns well-formed file name of YIN file as defined in RFC6020.
     *
     * @param name name Module or submodule name
     * @param revision Revision of module or submodule
     * @return well-formed file name of YIN file as defined in RFC6020.
     */
    public static String wellFormedYinName(final String name, final String revision) {
        if (revision == null) {
            return name + YangConstants.RFC6020_YIN_FILE_EXTENSION;
        }
        return requireNonNull(name) + '@' + revision +  YangConstants.RFC6020_YIN_FILE_EXTENSION;
    }

    /**
     * Write a module as a YIN text into specified {@link OutputStream}. Supplied module must have the
     * {@link ModuleEffectiveStatement} trait.
     *
     * @param module Module to be exported
     * @throws IllegalArgumentException if the module's declared representation is not available.
     * @throws NullPointerException if any of of the parameters is null
     * @throws XMLStreamException if an input-output error occurs
     */
    public static void writeModuleAsYinText(final ModuleEffectiveStatement module, final OutputStream output)
            throws XMLStreamException {
        writeReaderToOutput(YinXMLEventReaderFactory.defaultInstance().createXMLEventReader(module), output);
    }

    /**
     * Write a submodule as a YIN text into specified {@link OutputStream}. Supplied submodule must have the
     * {@link SubmoduleEffectiveStatement} trait.
     *
     * @param parentModule Parent module
     * @param submodule Submodule to be exported
     * @throws IllegalArgumentException if the parent module's or submodule's declared representation is not available
     * @throws NullPointerException if any of of the parameters is null
     * @throws XMLStreamException if an input-output error occurs
     */
    public static void writeSubmoduleAsYinText(final ModuleEffectiveStatement parentModule,
            final SubmoduleEffectiveStatement submodule, final OutputStream output) throws XMLStreamException {
        writeReaderToOutput(YinXMLEventReaderFactory.defaultInstance().createXMLEventReader(parentModule, submodule),
            output);
    }

    private static void writeReaderToOutput(final XMLEventReader reader, final OutputStream output)
            throws XMLStreamException {
        try {
            final var transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.transform(new StAXSource(reader), new StreamResult(output));
        } catch (TransformerException e) {
            throw new XMLStreamException("Failed to stream XML events", e);
        }
    }
}
