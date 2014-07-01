/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.checkMissingBody;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.createActualSchemaPath;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.createListKey;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.getConfig;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.getIdentityrefBase;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseConstraints;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseDefault;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseRefine;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseSchemaNodeArgs;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseStatus;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseTypeWithBody;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseUnits;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseUnknownTypeWithBody;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseUserOrdered;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseYinValue;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.stringFromNode;

import com.google.common.base.Strings;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import org.antlr.v4.runtime.tree.ParseTree;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Argument_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Base_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Contact_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Container_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Default_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Description_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_add_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_delete_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_not_supported_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_replace_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Import_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Key_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Leaf_list_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Leaf_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.List_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_header_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Namespace_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Ordered_by_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Organization_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Prefix_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Presence_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Reference_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_date_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Status_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Type_body_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Units_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.When_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Yang_version_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParserBaseListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.YangTypesConverter;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.ExtensionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.AnyXmlBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.DeviationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.FeatureBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.NotificationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.RefineHolderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YangParserListenerImpl extends YangParserBaseListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(YangParserListenerImpl.class);
    private static final String AUGMENT_STR = "augment";

    private final String sourcePath;
    private ModuleBuilder moduleBuilder;
    private String moduleName;
    private URI namespace;
    private String yangModelPrefix;
    private Date revision = new Date(0L);

    private final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final Stack<Stack<QName>> actualPath = new Stack<>();
    private int augmentOrder;

    private void addNodeToPath(final QName name) {
        actualPath.peek().push(name);
    }

    private QName removeNodeFromPath() {
        return actualPath.peek().pop();
    }

    public YangParserListenerImpl(final String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Override
    public void enterModule_stmt(final YangParser.Module_stmtContext ctx) {
        moduleName = stringFromNode(ctx);
        LOGGER.trace("entering module " + moduleName);
        enterLog("module", moduleName, 0);
        actualPath.push(new Stack<QName>());

        moduleBuilder = new ModuleBuilder(moduleName, sourcePath);

        String description = null;
        String reference = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                description = stringFromNode(child);
            } else if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            } else {
                if (description != null && reference != null) {
                    break;
                }
            }
        }
        moduleBuilder.setDescription(description);
        moduleBuilder.setReference(reference);
    }

    @Override
    public void exitModule_stmt(final YangParser.Module_stmtContext ctx) {
        exitLog("module");
        actualPath.pop();
    }

    @Override public void enterSubmodule_stmt(final YangParser.Submodule_stmtContext ctx) {
        moduleName = stringFromNode(ctx);
        LOGGER.trace("entering submodule " + moduleName);
        enterLog("submodule", moduleName, 0);
        actualPath.push(new Stack<QName>());

        moduleBuilder = new ModuleBuilder(moduleName, true, sourcePath);

        String description = null;
        String reference = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                description = stringFromNode(child);
            } else if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            } else {
                if (description != null && reference != null) {
                    break;
                }
            }
        }
        moduleBuilder.setDescription(description);
        moduleBuilder.setReference(reference);
    }

    @Override public void exitSubmodule_stmt(final YangParser.Submodule_stmtContext ctx) {
        exitLog("submodule");
        actualPath.pop();
    }

    @Override public void enterBelongs_to_stmt(final YangParser.Belongs_to_stmtContext ctx) {
        moduleBuilder.setBelongsTo(stringFromNode(ctx));
    }

    @Override
    public void enterModule_header_stmts(final Module_header_stmtsContext ctx) {
        enterLog("module_header", "", ctx.getStart().getLine());
        String yangVersion = null;
        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree treeNode = ctx.getChild(i);
            if (treeNode instanceof Namespace_stmtContext) {
                final String namespaceStr = stringFromNode(treeNode);
                namespace = URI.create(namespaceStr);
                moduleBuilder.setNamespace(namespace);
                setLog("namespace", namespaceStr);
            } else if (treeNode instanceof Prefix_stmtContext) {
                yangModelPrefix = stringFromNode(treeNode);
                moduleBuilder.setPrefix(yangModelPrefix);
                setLog("prefix", yangModelPrefix);
            } else if (treeNode instanceof Yang_version_stmtContext) {
                yangVersion = stringFromNode(treeNode);
                setLog("yang-version", yangVersion);
            }
        }

        if (yangVersion == null) {
            yangVersion = "1";
        }
        moduleBuilder.setYangVersion(yangVersion);
    }

    @Override
    public void exitModule_header_stmts(final Module_header_stmtsContext ctx) {
        exitLog("module_header");
    }

    @Override
    public void enterMeta_stmts(final YangParser.Meta_stmtsContext ctx) {
        enterLog("meta_stmt", "", ctx.getStart().getLine());
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Organization_stmtContext) {
                final String organization = stringFromNode(child);
                moduleBuilder.setOrganization(organization);
                setLog("organization", organization);
            } else if (child instanceof Contact_stmtContext) {
                final String contact = stringFromNode(child);
                moduleBuilder.setContact(contact);
                setLog("contact", contact);
            } else if (child instanceof Description_stmtContext) {
                final String description = stringFromNode(child);
                moduleBuilder.setDescription(description);
                setLog("description", description);
            } else if (child instanceof Reference_stmtContext) {
                final String reference = stringFromNode(child);
                moduleBuilder.setReference(reference);
                setLog("reference", reference);
            }
        }
    }

    @Override
    public void exitMeta_stmts(final YangParser.Meta_stmtsContext ctx) {
        exitLog("meta_stmt");
    }

    @Override
    public void enterRevision_stmts(final Revision_stmtsContext ctx) {
        enterLog("revisions", "", ctx.getStart().getLine());
        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree treeNode = ctx.getChild(i);
            if (treeNode instanceof Revision_stmtContext) {
                updateRevisionForRevisionStatement(treeNode);
            }
        }
    }

    @Override
    public void exitRevision_stmts(final Revision_stmtsContext ctx) {
        exitLog("revisions");
    }

    private void updateRevisionForRevisionStatement(final ParseTree treeNode) {
        final String revisionDateStr = stringFromNode(treeNode);
        try {
            final Date revisionDate = SIMPLE_DATE_FORMAT.parse(revisionDateStr);
            if ((revisionDate != null) && (this.revision.compareTo(revisionDate) < 0)) {
                this.revision = revisionDate;
                moduleBuilder.setRevision(this.revision);
                setLog("revision", this.revision.toString());
                for (int i = 0; i < treeNode.getChildCount(); ++i) {
                    ParseTree child = treeNode.getChild(i);
                    if (child instanceof Reference_stmtContext) {
                        moduleBuilder.setReference(stringFromNode(child));
                    }
                }
            }
        } catch (ParseException e) {
            final String message = "Failed to parse revision string: " + revisionDateStr;
            LOGGER.warn(message);
        }
    }

    @Override
    public void enterImport_stmt(final Import_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String importName = stringFromNode(ctx);
        enterLog("import", importName, line);

        String importPrefix = null;
        Date importRevision = null;

        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree treeNode = ctx.getChild(i);
            if (treeNode instanceof Prefix_stmtContext) {
                importPrefix = stringFromNode(treeNode);
            }
            if (treeNode instanceof Revision_date_stmtContext) {
                String importRevisionStr = stringFromNode(treeNode);
                try {
                    importRevision = SIMPLE_DATE_FORMAT.parse(importRevisionStr);
                } catch (ParseException e) {
                    LOGGER.warn("Failed to parse import revision-date at line " + line + ": " + importRevisionStr);
                }
            }
        }
        moduleBuilder.addModuleImport(importName, importRevision, importPrefix);
        setLog("import", "(" + importName + "; " + importRevision + "; " + importPrefix + ")");
    }

    @Override
    public void exitImport_stmt(final Import_stmtContext ctx) {
        exitLog("import");
    }

    @Override
    public void enterAugment_stmt(final YangParser.Augment_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String augmentPath = stringFromNode(ctx);
        enterLog(AUGMENT_STR, augmentPath, line);
        actualPath.push(new Stack<QName>());

        AugmentationSchemaBuilder builder = moduleBuilder.addAugment(line, augmentPath, augmentOrder++);

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                builder.setDescription(stringFromNode(child));
            } else if (child instanceof Reference_stmtContext) {
                builder.setReference(stringFromNode(child));
            } else if (child instanceof Status_stmtContext) {
                builder.setStatus(parseStatus((Status_stmtContext) child));
            } else if (child instanceof When_stmtContext) {
                builder.addWhenCondition(stringFromNode(child));
            }
        }

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitAugment_stmt(final YangParser.Augment_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog(AUGMENT_STR);
        actualPath.pop();
    }

    @Override
    public void enterExtension_stmt(final YangParser.Extension_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String extName = stringFromNode(ctx);
        enterLog("extension", extName, line);
        QName qname = new QName(namespace, revision, yangModelPrefix, extName);
        addNodeToPath(qname);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        ExtensionBuilder builder = moduleBuilder.addExtension(qname, line, path);
        parseSchemaNodeArgs(ctx, builder);

        String argument = null;
        boolean yin = false;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Argument_stmtContext) {
                argument = stringFromNode(child);
                yin = parseYinValue((Argument_stmtContext) child);
                break;
            }
        }
        builder.setArgument(argument);
        builder.setYinElement(yin);

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitExtension_stmt(final YangParser.Extension_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("extension", removeNodeFromPath());
    }

    @Override
    public void enterTypedef_stmt(final YangParser.Typedef_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String typedefName = stringFromNode(ctx);
        enterLog("typedef", typedefName, line);
        QName typedefQName = new QName(namespace, revision, yangModelPrefix, typedefName);
        addNodeToPath(typedefQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        TypeDefinitionBuilder builder = moduleBuilder.addTypedef(line, typedefQName, path);
        parseSchemaNodeArgs(ctx, builder);
        builder.setUnits(parseUnits(ctx));
        builder.setDefaultValue(parseDefault(ctx));

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitTypedef_stmt(final YangParser.Typedef_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("typedef", removeNodeFromPath());
    }

    @Override
    public void enterType_stmt(final YangParser.Type_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String typeName = stringFromNode(ctx);
        enterLog("type", typeName, line);

        final QName typeQName = parseQName(typeName);

        TypeDefinition<?> type;
        Type_body_stmtsContext typeBody = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof Type_body_stmtsContext) {
                typeBody = (Type_body_stmtsContext) ctx.getChild(i);
                break;
            }
        }

        // if this is base yang type...
        if (YangTypesConverter.isBaseYangType(typeName)) {
            if (typeBody == null) {
                // check for types which must have body
                checkMissingBody(typeName, moduleName, line);
                // if there are no constraints, just grab default base yang type
                type = YangTypesConverter.javaTypeForBaseYangType(typeName);
                addNodeToPath(type.getQName());
                moduleBuilder.setType(type);
            } else {
                QName qname;
                switch (typeName) {
                    case "union":
                        qname = BaseTypes.constructQName("union");
                        addNodeToPath(qname);
                        UnionTypeBuilder unionBuilder = moduleBuilder.addUnionType(line, namespace, revision);
                        Builder parent = moduleBuilder.getActualNode();
                        unionBuilder.setParent(parent);
                        moduleBuilder.enterNode(unionBuilder);
                        break;
                    case "identityref":
                        qname = BaseTypes.constructQName("identityref");
                        addNodeToPath(qname);
                        SchemaPath path = createActualSchemaPath(actualPath.peek());
                        moduleBuilder.addIdentityrefType(line, path, getIdentityrefBase(typeBody));
                        break;
                    default:
                        type = parseTypeWithBody(typeName, typeBody, actualPath.peek(), namespace, revision,
                                yangModelPrefix, moduleBuilder.getActualNode());
                        moduleBuilder.setType(type);
                        addNodeToPath(type.getQName());
                }
            }
        } else {
            type = parseUnknownTypeWithBody(typeQName, typeBody, actualPath.peek(), namespace, revision,
                    yangModelPrefix, moduleBuilder.getActualNode());
            // add parent node of this type statement to dirty nodes
            moduleBuilder.markActualNodeDirty();
            moduleBuilder.setType(type);
            addNodeToPath(type.getQName());
        }

    }

    private QName parseQName(final String typeName) {
        QName typeQName;
        if (typeName.contains(":")) {
            String[] splittedName = typeName.split(":");
            String prefix = splittedName[0];
            String name = splittedName[1];
            if (prefix.equals(yangModelPrefix)) {
                typeQName = new QName(namespace, revision, prefix, name);
            } else {
                typeQName = new QName(null, null, prefix, name);
            }
        } else {
            typeQName = new QName(namespace, revision, yangModelPrefix, typeName);
        }
        return typeQName;
    }

    @Override
    public void exitType_stmt(final YangParser.Type_stmtContext ctx) {
        final String typeName = stringFromNode(ctx);
        if ("union".equals(typeName)) {
            moduleBuilder.exitNode();
        }
        exitLog("type", removeNodeFromPath());
    }

    @Override
    public void enterGrouping_stmt(final YangParser.Grouping_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String groupName = stringFromNode(ctx);
        enterLog("grouping", groupName, line);
        QName groupQName = new QName(namespace, revision, yangModelPrefix, groupName);
        addNodeToPath(groupQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        GroupingBuilder builder = moduleBuilder.addGrouping(ctx.getStart().getLine(), groupQName, path);
        parseSchemaNodeArgs(ctx, builder);

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitGrouping_stmt(final YangParser.Grouping_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("grouping", removeNodeFromPath());
    }

    @Override
    public void enterContainer_stmt(final Container_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String containerName = stringFromNode(ctx);
        enterLog("container", containerName, line);

        QName containerQName = new QName(namespace, revision, yangModelPrefix, containerName);
        addNodeToPath(containerQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        ContainerSchemaNodeBuilder builder = moduleBuilder.addContainerNode(line, containerQName, path);
        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, line));

        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree childNode = ctx.getChild(i);
            if (childNode instanceof Presence_stmtContext) {
                builder.setPresence(true);
                break;
            }
        }

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitContainer_stmt(final Container_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("container", removeNodeFromPath());
    }

    @Override
    public void enterLeaf_stmt(final Leaf_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String leafName = stringFromNode(ctx);
        enterLog("leaf", leafName, line);

        QName leafQName = new QName(namespace, revision, yangModelPrefix, leafName);
        addNodeToPath(leafQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        LeafSchemaNodeBuilder builder = moduleBuilder.addLeafNode(line, leafQName, path);
        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, line));

        String defaultStr = null;
        String unitsStr = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Default_stmtContext) {
                defaultStr = stringFromNode(child);
            } else if (child instanceof Units_stmtContext) {
                unitsStr = stringFromNode(child);
            }
        }
        builder.setDefaultStr(defaultStr);
        builder.setUnits(unitsStr);

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitLeaf_stmt(final YangParser.Leaf_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("leaf", removeNodeFromPath());
    }

    @Override
    public void enterUses_stmt(final YangParser.Uses_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String groupingPathStr = stringFromNode(ctx);
        enterLog("uses", groupingPathStr, line);

        UsesNodeBuilder builder = moduleBuilder.addUsesNode(line, groupingPathStr);

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitUses_stmt(final YangParser.Uses_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("uses");
    }

    @Override
    public void enterUses_augment_stmt(final YangParser.Uses_augment_stmtContext ctx) {
        actualPath.push(new Stack<QName>());
        final int line = ctx.getStart().getLine();
        final String augmentPath = stringFromNode(ctx);
        enterLog(AUGMENT_STR, augmentPath, line);

        AugmentationSchemaBuilder builder = moduleBuilder.addAugment(line, augmentPath, augmentOrder++);

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                builder.setDescription(stringFromNode(child));
            } else if (child instanceof Reference_stmtContext) {
                builder.setReference(stringFromNode(child));
            } else if (child instanceof Status_stmtContext) {
                builder.setStatus(parseStatus((Status_stmtContext) child));
            } else if (child instanceof When_stmtContext) {
                builder.addWhenCondition(stringFromNode(child));
            }
        }

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitUses_augment_stmt(final YangParser.Uses_augment_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog(AUGMENT_STR);
        actualPath.pop();
    }

    @Override
    public void enterRefine_stmt(final YangParser.Refine_stmtContext ctx) {
        final String refineString = stringFromNode(ctx);
        enterLog("refine", refineString, ctx.getStart().getLine());

        RefineHolderImpl refine = parseRefine(ctx, moduleName);
        moduleBuilder.addRefine(refine);
        moduleBuilder.enterNode(refine);
    }

    @Override
    public void exitRefine_stmt(final YangParser.Refine_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("refine");
    }

    @Override
    public void enterLeaf_list_stmt(final Leaf_list_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String leafListName = stringFromNode(ctx);
        enterLog("leaf-list", leafListName, line);
        QName leafListQName = new QName(namespace, revision, yangModelPrefix, leafListName);
        addNodeToPath(leafListQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        LeafListSchemaNodeBuilder builder = moduleBuilder.addLeafListNode(line, leafListQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, ctx.getStart().getLine()));

        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree childNode = ctx.getChild(i);
            if (childNode instanceof Ordered_by_stmtContext) {
                final Ordered_by_stmtContext orderedBy = (Ordered_by_stmtContext) childNode;
                final boolean userOrdered = parseUserOrdered(orderedBy);
                builder.setUserOrdered(userOrdered);
                break;
            }
        }
    }

    @Override
    public void exitLeaf_list_stmt(final YangParser.Leaf_list_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("leaf-list", removeNodeFromPath());
    }

    @Override
    public void enterList_stmt(final List_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String listName = stringFromNode(ctx);
        enterLog("list", listName, line);

        QName listQName = new QName(namespace, revision, yangModelPrefix, listName);
        addNodeToPath(listQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        ListSchemaNodeBuilder builder = moduleBuilder.addListNode(line, listQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, line));

        for (int i = 0; i < ctx.getChildCount(); ++i) {
            ParseTree childNode = ctx.getChild(i);
            if (childNode instanceof Ordered_by_stmtContext) {
                final Ordered_by_stmtContext orderedBy = (Ordered_by_stmtContext) childNode;
                final boolean userOrdered = parseUserOrdered(orderedBy);
                builder.setUserOrdered(userOrdered);
            } else if (childNode instanceof Key_stmtContext) {
                List<String> key = createListKey((Key_stmtContext) childNode);
                builder.setKeys(key);
            }
        }
    }

    @Override
    public void exitList_stmt(final List_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("list", removeNodeFromPath());
    }

    @Override
    public void enterAnyxml_stmt(final YangParser.Anyxml_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String anyXmlName = stringFromNode(ctx);
        enterLog("anyxml", anyXmlName, line);

        QName anyXmlQName = new QName(namespace, revision, yangModelPrefix, anyXmlName);
        addNodeToPath(anyXmlQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        AnyXmlBuilder builder = moduleBuilder.addAnyXml(line, anyXmlQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, line));
    }

    @Override
    public void exitAnyxml_stmt(final YangParser.Anyxml_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("anyxml", removeNodeFromPath());
    }

    @Override
    public void enterChoice_stmt(final YangParser.Choice_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String choiceName = stringFromNode(ctx);
        enterLog("choice", choiceName, line);

        QName choiceQName = new QName(namespace, revision, yangModelPrefix, choiceName);
        addNodeToPath(choiceQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        ChoiceBuilder builder = moduleBuilder.addChoice(line, choiceQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, line));

        // set 'default' case
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Default_stmtContext) {
                String defaultCase = stringFromNode(child);
                builder.setDefaultCase(defaultCase);
                break;
            }
        }
    }

    @Override
    public void exitChoice_stmt(final YangParser.Choice_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("choice", removeNodeFromPath());
    }

    @Override
    public void enterCase_stmt(final YangParser.Case_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String caseName = stringFromNode(ctx);
        enterLog("case", caseName, line);

        QName caseQName = new QName(namespace, revision, yangModelPrefix, caseName);
        addNodeToPath(caseQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        ChoiceCaseBuilder builder = moduleBuilder.addCase(line, caseQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
    }

    @Override
    public void exitCase_stmt(final YangParser.Case_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("case", removeNodeFromPath());
    }

    @Override
    public void enterNotification_stmt(final YangParser.Notification_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String notificationName = stringFromNode(ctx);
        enterLog("notification", notificationName, line);

        QName notificationQName = new QName(namespace, revision, yangModelPrefix, notificationName);
        addNodeToPath(notificationQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        NotificationBuilder builder = moduleBuilder.addNotification(line, notificationQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
    }

    @Override
    public void exitNotification_stmt(final YangParser.Notification_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("notification", removeNodeFromPath());
    }

    // Unknown nodes
    @Override
    public void enterIdentifier_stmt(final YangParser.Identifier_stmtContext ctx) {
        handleUnknownNode(ctx.getStart().getLine(), ctx);
    }

    @Override
    public void exitIdentifier_stmt(final YangParser.Identifier_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("unknown-node", removeNodeFromPath());
    }

    @Override public void enterUnknown_statement(final YangParser.Unknown_statementContext ctx) {
        handleUnknownNode(ctx.getStart().getLine(), ctx);
    }

    @Override public void exitUnknown_statement(final YangParser.Unknown_statementContext ctx) {
        moduleBuilder.exitNode();
        exitLog("unknown-node", removeNodeFromPath());
    }

    @Override public void enterUnknown_statement2(final YangParser.Unknown_statement2Context ctx) {
        handleUnknownNode(ctx.getStart().getLine(), ctx);
    }

    @Override public void exitUnknown_statement2(final YangParser.Unknown_statement2Context ctx) {
        moduleBuilder.exitNode();
        exitLog("unknown-node", removeNodeFromPath());
    }

    @Override public void enterUnknown_statement3(final YangParser.Unknown_statement3Context ctx) {
        handleUnknownNode(ctx.getStart().getLine(), ctx);
    }

    @Override public void exitUnknown_statement3(final YangParser.Unknown_statement3Context ctx) {
        moduleBuilder.exitNode();
        exitLog("unknown-node", removeNodeFromPath());
    }

    @Override
    public void enterRpc_stmt(final YangParser.Rpc_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String rpcName = stringFromNode(ctx);
        enterLog("rpc", rpcName, line);

        QName rpcQName = new QName(namespace, revision, yangModelPrefix, rpcName);
        addNodeToPath(rpcQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        RpcDefinitionBuilder rpcBuilder = moduleBuilder.addRpc(line, rpcQName, path);
        moduleBuilder.enterNode(rpcBuilder);


        parseSchemaNodeArgs(ctx, rpcBuilder);
    }

    @Override
    public void exitRpc_stmt(final YangParser.Rpc_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("rpc", removeNodeFromPath());
    }

    @Override
    public void enterInput_stmt(final YangParser.Input_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String input = "input";
        enterLog(input, input, line);

        QName rpcQName = new QName(namespace, revision, yangModelPrefix, input);
        addNodeToPath(rpcQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        ContainerSchemaNodeBuilder builder = moduleBuilder.addRpcInput(line, rpcQName, path);
        moduleBuilder.enterNode(builder);
        builder.setConfiguration(true);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
    }

    @Override
    public void exitInput_stmt(final YangParser.Input_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("input", removeNodeFromPath());
    }

    @Override
    public void enterOutput_stmt(final YangParser.Output_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String output = "output";
        enterLog(output, output, line);

        QName rpcQName = new QName(namespace, revision, yangModelPrefix, output);
        addNodeToPath(rpcQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        ContainerSchemaNodeBuilder builder = moduleBuilder.addRpcOutput(path, rpcQName, line);
        moduleBuilder.enterNode(builder);
        builder.setConfiguration(true);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
    }

    @Override
    public void exitOutput_stmt(final YangParser.Output_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("output", removeNodeFromPath());
    }

    @Override
    public void enterFeature_stmt(final YangParser.Feature_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String featureName = stringFromNode(ctx);
        enterLog("feature", featureName, line);

        QName featureQName = new QName(namespace, revision, yangModelPrefix, featureName);
        addNodeToPath(featureQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        FeatureBuilder featureBuilder = moduleBuilder.addFeature(line, featureQName, path);
        moduleBuilder.enterNode(featureBuilder);

        parseSchemaNodeArgs(ctx, featureBuilder);
    }

    @Override
    public void exitFeature_stmt(final YangParser.Feature_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("feature", removeNodeFromPath());
    }

    @Override
    public void enterDeviation_stmt(final YangParser.Deviation_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String targetPath = stringFromNode(ctx);
        enterLog("deviation", targetPath, line);

        String reference = null;
        String deviate = null;
        DeviationBuilder builder = moduleBuilder.addDeviation(line, targetPath);
        moduleBuilder.enterNode(builder);

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            } else if (child instanceof Deviate_not_supported_stmtContext) {
                deviate = stringFromNode(child);
            } else if (child instanceof Deviate_add_stmtContext) {
                deviate = stringFromNode(child);
            } else if (child instanceof Deviate_replace_stmtContext) {
                deviate = stringFromNode(child);
            } else if (child instanceof Deviate_delete_stmtContext) {
                deviate = stringFromNode(child);
            }
        }
        builder.setReference(reference);
        builder.setDeviate(deviate);
    }

    @Override
    public void exitDeviation_stmt(final YangParser.Deviation_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("deviation");
    }

    @Override
    public void enterIdentity_stmt(final YangParser.Identity_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String identityName = stringFromNode(ctx);
        enterLog("identity", identityName, line);

        final QName identityQName = new QName(namespace, revision, yangModelPrefix, identityName);
        addNodeToPath(identityQName);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        IdentitySchemaNodeBuilder builder = moduleBuilder.addIdentity(identityQName, line, path);
        moduleBuilder.enterNode(builder);


        parseSchemaNodeArgs(ctx, builder);

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Base_stmtContext) {
                String baseIdentityName = stringFromNode(child);
                builder.setBaseIdentityName(baseIdentityName);
            }
        }
    }

    @Override
    public void exitIdentity_stmt(final YangParser.Identity_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("identity", removeNodeFromPath());
    }

    public ModuleBuilder getModuleBuilder() {
        return moduleBuilder;
    }

    private void enterLog(final String p1, final String p2, final int line) {
        LOGGER.trace("entering {} {} ({})", p1, p2, line);
    }

    private void exitLog(final String p1) {
        LOGGER.trace("exiting {}", p1);
    }

    private void exitLog(final String p1, final QName p2) {
        LOGGER.trace("exiting {} {}", p1, p2.getLocalName());
    }

    private void setLog(final String p1, final String p2) {
        LOGGER.trace("setting {} {}", p1, p2);
    }

    private void handleUnknownNode(final int line, final ParseTree ctx) {
        final String nodeParameter = stringFromNode(ctx);
        enterLog("unknown-node", nodeParameter, line);

        QName nodeType;
        final String nodeTypeStr = ctx.getChild(0).getText();
        final String[] splittedElement = nodeTypeStr.split(":");
        if (splittedElement.length == 1) {
            nodeType = new QName(namespace, revision, yangModelPrefix, splittedElement[0]);
        } else {
            nodeType = new QName(namespace, revision, splittedElement[0], splittedElement[1]);
        }

        QName qname;
        try {
            if (!Strings.isNullOrEmpty(nodeParameter)) {
                String[] splittedName = nodeParameter.split(":");
                if (splittedName.length == 2) {
                    qname = new QName(null, null, splittedName[0], splittedName[1]);
                } else {
                    qname = new QName(namespace, revision, yangModelPrefix, splittedName[0]);
                }
            } else {
                qname = nodeType;
            }
        } catch (IllegalArgumentException e) {
            qname = nodeType;

        }
        addNodeToPath(qname);
        SchemaPath path = createActualSchemaPath(actualPath.peek());

        UnknownSchemaNodeBuilderImpl builder = moduleBuilder.addUnknownSchemaNode(line, qname, path);
        builder.setNodeType(nodeType);
        builder.setNodeParameter(nodeParameter);


        parseSchemaNodeArgs(ctx, builder);
        moduleBuilder.enterNode(builder);
    }

}
