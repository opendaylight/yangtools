/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.wadl.generator;

import static org.opendaylight.yangtools.yang.unified.doc.generator.util.GeneratorUtil.NEW_LINE;
import static org.opendaylight.yangtools.yang.unified.doc.generator.util.GeneratorUtil.concat;
import static org.opendaylight.yangtools.yang.unified.doc.generator.util.GeneratorUtil.indentWithNewLine;

import com.google.common.collect.Iterables;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

public class WadlRestconfGenerator1 {

    private final File path;
    static final BuildContext CTX = new DefaultBuildContext();
    private SchemaContext context;
    private List<DataSchemaNode> configData;
    private List<DataSchemaNode> operationalData;
    private Module module;
    private List<LeafSchemaNode> pathListParams;
    private final String PATH_DELIMITER = "/";

    public WadlRestconfGenerator1(final File targetPath) {
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }
        this.path = targetPath;
    }

    public Set<File> generate(final SchemaContext context, final Set<Module> modules) throws IOException {
        final Set<File> result = new HashSet<>();
        final List<DataSchemaNode> dataContainers = new ArrayList<>();
        this.context = context;

        for (final Module module : modules) {
            for (final DataSchemaNode dataSchemaNode :module.getChildNodes()) {
                if (isListOrContainer(dataSchemaNode))
                    dataContainers.add(dataSchemaNode);
            }

            if (!dataContainers.isEmpty() || !isNullOrEmpty(module.getRpcs())) {
                configData = new ArrayList<>();
                operationalData = new ArrayList<>();

                for (final DataSchemaNode schemaNode : dataContainers) {
                    if (schemaNode.isConfiguration()) {
                        configData.add(schemaNode);
                    } else {
                        operationalData.add(schemaNode);
                    }
                }

                this.module = module;
                final File destination = new File(path, module.getName() + ".wadl");
                final OutputStreamWriter fw = new OutputStreamWriter(CTX.newFileOutputStream(destination));
                final BufferedWriter bw = new BufferedWriter(fw);
                bw.append(application());
                bw.close();
                fw.close();
                result.add(destination);
            }
        }
        return result;
    }

    private String application() {
        final StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\"?>");
        sb.append(NEW_LINE);
        sb.append("<application xmlns=\"http://wadl.dev.java.net/2009/02\" ");
        sb.append(importsAsNamespaces(module));
        sb.append(" xmlns:");
        sb.append(module.getPrefix());
        sb.append("=\"");
        sb.append(module.getNamespace());
        sb.append("\">");
        sb.append(NEW_LINE);
        sb.append(grammars());
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(resources());
        sb.append("</application>");

        return sb.toString();
    }

    private String resources() {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(1, "<resources base=\"http://localhost:9998/restconf\""));
        sb.append(NEW_LINE);
        sb.append(resourceOperational(2));
        sb.append(NEW_LINE);
        sb.append(resourceConfig(2));
        sb.append(NEW_LINE);
        sb.append(resourceOperations(2));

        return sb.toString();
    }

    private String resourceOperational(final int numOfindentWithNewLine) {
        final StringBuilder sb = new StringBuilder();

        if (!isNullOrEmpty(operationalData)) {
            sb.append(indentWithNewLine(numOfindentWithNewLine, "<resource path=\"operational\">"));
            sb.append(NEW_LINE);
            for (final DataSchemaNode schemaNode : operationalData) {
                sb.append(firstResource(numOfindentWithNewLine + 1, schemaNode, false));
                sb.append(NEW_LINE);
            }
            sb.append(indentWithNewLine(numOfindentWithNewLine, "</resource>"));
        }

        return sb.toString();
    }

    private String resourceConfig(final int numOfindentWithNewLine) {
        final StringBuilder sb = new StringBuilder();

        return sb.toString();
    }

    private String resourceOperations(final int numOfindentWithNewLine) {
        final StringBuilder sb = new StringBuilder();

        return sb.toString();
    }

    private String firstResource(final int numOfindentWithNewLine, final DataSchemaNode schemaNode, final boolean config) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfindentWithNewLine, concat("<resource path=\"", toQuote(concat(module.getName(), ":", createPath(schemaNode))), "\">")));
            sb.append(resourceBody(numOfindentWithNewLine + 1, schemaNode, config));
        sb.append(indentWithNewLine(numOfindentWithNewLine, "</resource>"));

        return sb.toString();
    }

    private String resourceBody(final int numOfindentWithNewLine, final DataSchemaNode schemaNode, final boolean config) {
        final StringBuilder sb = new StringBuilder();
        final List<DataSchemaNode> dataContainers = getContainersOrLists(schemaNode);

        if (!isNullOrEmpty(pathListParams)) {
            sb.append(resourceParams(numOfindentWithNewLine));
            sb.append(NEW_LINE);
        }
        sb.append(methodGet(numOfindentWithNewLine, schemaNode));
        sb.append(NEW_LINE);

        if (config) {
            sb.append(indentWithNewLine(numOfindentWithNewLine, methodDelete()));
            sb.append(NEW_LINE);
            sb.append(methodPut(numOfindentWithNewLine, schemaNode));
            sb.append(NEW_LINE);
            for (final DataSchemaNode childNode : dataContainers) {
                sb.append(methodPost(numOfindentWithNewLine, childNode));
                sb.append(NEW_LINE);
            }
        }
        for (final DataSchemaNode childNode : dataContainers) {
            sb.append(resource(numOfindentWithNewLine, childNode, config));
        }

        return sb.toString();
    }

    private String resource(final int numOfindentWithNewLine, final DataSchemaNode schemaNode, final boolean config) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfindentWithNewLine, concat("<resource path=\"", createPath(schemaNode), "\">")));
        sb.append(resourceBody(numOfindentWithNewLine + 1, schemaNode, config));
        sb.append(indentWithNewLine(numOfindentWithNewLine, "</resource>"));

        return sb.toString();
    }

    private List<DataSchemaNode> getContainersOrLists(final DataSchemaNode schemaNode) {
        final List<DataSchemaNode> dataContainers = new ArrayList<>();

        for (final DataSchemaNode node : ((DataNodeContainer)schemaNode).getChildNodes()) {
            if (isListOrContainer(node)) {
                dataContainers.add(node);
            }
        }
        return dataContainers;
    }

    private String resourceParams(final int numOfindentWithNewLine) {
        final StringBuilder sb = new StringBuilder();
        for (final LeafSchemaNode pathParam : pathListParams) {
            if (pathParam != null) {
                final String prefix = pathParam.getQName().getPrefix();
                final String nameWithPrefix = isNullOrEmpty(prefix) ? pathParam.getQName().getLocalName() : concat(prefix, ":", pathParam.getQName().getLocalName());
                sb.append(indentWithNewLine(numOfindentWithNewLine, concat("<param required=\"true\" style=\"template\" name=\"", pathParam.getQName().getLocalName(), "\" type=\"", nameWithPrefix, "\"/>")));
            }
        }

        return sb.toString();
    }

    private String methodGet(final int numOfindentWithNewLine, final DataSchemaNode schemaNode) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfindentWithNewLine, "<method name=\"GET\">"));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine + 1, "<response>"));
        sb.append(representation(numOfindentWithNewLine + 2, schemaNode.getQName().getPrefix(), schemaNode.getQName().getLocalName()));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine + 1, "</response>"));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine, "</method>"));

        return sb.toString();
    }

    private String methodPut(final int numOfindentWithNewLine, final DataSchemaNode schemaNode) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfindentWithNewLine, "<method name=\"PUT\">"));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine + 1, "<request>"));
        sb.append(representation(numOfindentWithNewLine + 2, schemaNode.getQName().getPrefix(), schemaNode.getQName().getLocalName()));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine + 1, "</request>"));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine, "</method>"));

        return sb.toString();
    }

    private String methodPost(final int numOfindentWithNewLine, final DataSchemaNode schemaNode) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfindentWithNewLine, "<method name=\"POST\">"));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine + 1, "<request>"));
        sb.append(representation(numOfindentWithNewLine + 2, schemaNode.getQName().getPrefix(), schemaNode.getQName().getLocalName()));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine + 1, "</request>"));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine, "</method>"));

        return sb.toString();
    }

    private String methodPostRpc(final int numOfindentWithNewLine, final boolean input, final boolean output) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfindentWithNewLine, "<method name=\"POST\">"));
        sb.append(NEW_LINE);
        if (input) {
            sb.append(indentWithNewLine(numOfindentWithNewLine + 1, "<request>"));
            sb.append(NEW_LINE);
            sb.append(representation(numOfindentWithNewLine + 2, null, "input"));
            sb.append(NEW_LINE);
            sb.append(indentWithNewLine(numOfindentWithNewLine + 1, "</request>"));
        }
        if (output) {
            sb.append(indentWithNewLine(numOfindentWithNewLine + 1, "<response>"));
            sb.append(NEW_LINE);
            sb.append(representation(numOfindentWithNewLine + 2, null, "output"));
            sb.append(NEW_LINE);
            sb.append(indentWithNewLine(numOfindentWithNewLine + 1, "</response>"));
        }
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine, "</method>"));

        return sb.toString();
    }

    private String methodDelete() {
        return "<method name=\"DELETE\" />";
    }

    private String representation(final int numOfindentWithNewLine, final String prefix, final String localName) {
        final StringBuilder sb = new StringBuilder();
        final String elementData = isNullOrEmpty(prefix) ? localName : concat(prefix, ":", localName);

        sb.append(indentWithNewLine(numOfindentWithNewLine, concat("<representation mediaType=\"application/xml\" element=\"", elementData, "\">")));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine, concat("<representation mediaType=\"text/xml\" element=\"", elementData, "\">")));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine, concat("<representation mediaType=\"application/json\" element=\"", elementData, "\">")));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine, concat("<representation mediaType=\"application/yang.data+xml\" element=\"", elementData, "\">")));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfindentWithNewLine, concat("<representation mediaType=\"application/yang.data+json\" element=\"", elementData, "\">")));

        return sb.toString();
    }

    private String createPath(final DataSchemaNode schemaNode) {
        final StringBuilder sb = new StringBuilder();

        return sb.toString();
    }

    private String grammars() {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(1, "<grammars>"));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(2, concat("<include href=", toQuote(concat(module.getName(), ".yang")), "/>")));
        sb.append(NEW_LINE);
        for (final ModuleImport moduleImport : module.getImports()) {
            sb.append(indentWithNewLine(3, concat("<include href=", toQuote(concat(moduleImport.getModuleName(), ".yang")), "/>")));
            sb.append(NEW_LINE);
        }
        sb.append(indentWithNewLine(1, "</grammars>"));

        return sb.toString();
    }

    private String importsAsNamespaces(final Module module) {
        final StringBuilder sb = new StringBuilder();

        for (final ModuleImport moduleImport : module.getImports()) {
            sb.append("xmlns:");
            sb.append(moduleImport.getPrefix());
            sb.append(toQuote(context.findModuleByName(moduleImport.getModuleName(), moduleImport.getRevision()).getNamespace().toString()));
            sb.append(NEW_LINE);
        }
        return sb.toString();
    }

    private String toQuote(final String text) {
        final StringBuilder sb = new StringBuilder();

        sb.append("\"");
        sb.append(text);
        sb.append("\"");

        return sb.toString();
    }

    private <T extends Collection<?>> boolean isNullOrEmpty(final T objects) {
        return (objects == null ? true : Iterables.isEmpty(objects));
    }

    private boolean isNullOrEmpty(final String text) {
        return (text == null ? true : text.isEmpty());
    }

    private boolean isListOrContainer(final DataSchemaNode schemaNode) {
        return (schemaNode instanceof ListSchemaNode || schemaNode instanceof ContainerSchemaNode);
    }
}
