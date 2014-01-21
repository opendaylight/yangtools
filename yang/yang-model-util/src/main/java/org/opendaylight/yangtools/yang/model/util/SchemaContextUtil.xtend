/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode
import java.util.Date
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition
import java.util.Set

/**
 * The Schema Context Util contains support methods for searching through Schema Context modules for specified schema
 * nodes via Schema Path or Revision Aware XPath. The Schema Context Util is designed as mixin,
 * so it is not instantiable.
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public  class SchemaContextUtil {

    private new() {
    }

    /**
     * Method attempts to find DataSchemaNode in Schema Context via specified Schema Path. The returned
     * DataSchemaNode from method will be the node at the end of the SchemaPath. If the DataSchemaNode is not present
     * in the Schema Context the method will return <code>null</code>.
     * <br>
     * In case that Schema Context or Schema Path are not specified correctly (i.e. contains <code>null</code>
     * values) the method will return IllegalArgumentException.
     *
     * @throws IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param schemaPath
     *            Schema Path to search for
     * @return DataSchemaNode from the end of the Schema Path or
     *         <code>null</code> if the Node is not present.
     */
    public static def SchemaNode findDataSchemaNode( SchemaContext context,  SchemaPath schemaPath) {
        if (context === null) {
            throw new IllegalArgumentException("Schema Context reference cannot be NULL!");
        }
        if (schemaPath === null) {
            throw new IllegalArgumentException("Schema Path reference cannot be NULL");
        }
        val prefixedPath = (schemaPath.getPath());
        if (prefixedPath != null) {
            return findNodeInSchemaContext(context,prefixedPath);
        }
        return null;
    }

    /**
     * Method attempts to find DataSchemaNode inside of provided Schema Context and Yang Module accordingly to
     * Non-conditional Revision Aware XPath. The specified Module MUST be present in Schema Context otherwise the
     * operation would fail and return <code>null</code>.
     * <br>
     * The Revision Aware XPath MUST be specified WITHOUT the conditional statement (i.e. without [cond]) in path,
     * because in this state the Schema Context is completely unaware of data state and will be not able to properly
     * resolve XPath. If the XPath contains condition the method will return IllegalArgumentException.
     * <br>
     * In case that Schema Context or Module or Revision Aware XPath contains <code>null</code> references the method
     * will throw IllegalArgumentException
     * <br>
     * If the Revision Aware XPath is correct and desired Data Schema Node is present in Yang module or in depending
     * module in Schema Context the method will return specified Data Schema Node, otherwise the operation will fail
     * and method will return <code>null</code>.
     *
     * @throws IllegalArgumentException
     *
     * @param context Schema Context
     * @param module Yang Module
     * @param nonCondXPath Non Conditional Revision Aware XPath
     * @return Returns Data Schema Node for specified Schema Context for given Non-conditional Revision Aware XPath,
     * or <code>null</code> if the DataSchemaNode is not present in Schema Context.
     */
    public static def SchemaNode findDataSchemaNode( SchemaContext context,  Module module,
             RevisionAwareXPath nonCondXPath) {
        if (context === null) {
            throw new IllegalArgumentException("Schema Context reference cannot be NULL!");
        }
        if (module === null) {
            throw new IllegalArgumentException("Module reference cannot be NULL!");
        }
        if (nonCondXPath === null) {
            throw new IllegalArgumentException("Non Conditional Revision Aware XPath cannot be NULL!");
        }

         val  strXPath = nonCondXPath.toString();
        if (strXPath != null) {
            if (strXPath.contains("[")) {
                throw new IllegalArgumentException("Revision Aware XPath cannot contains condition!");
            }
            if (nonCondXPath.isAbsolute()) {
                 val qnamedPath = xpathToQNamePath(context, module, strXPath);
                if (qnamedPath != null) {
                    return findNodeInSchemaContext(context,qnamedPath);
                }
            }
        }
        return null;
    }

    /**
     * Method attempts to find DataSchemaNode inside of provided Schema Context and Yang Module accordingly to
     * Non-conditional relative Revision Aware XPath. The specified Module MUST be present in Schema Context otherwise
     * the operation would fail and return <code>null</code>.
     * <br>
     * The relative Revision Aware XPath MUST be specified WITHOUT the conditional statement (i.e. without [cond]) in
     * path, because in this state the Schema Context is completely unaware of data state and will be not able to
     * properly resolve XPath. If the XPath contains condition the method will return IllegalArgumentException.
     * <br>
     * The Actual Schema Node MUST be specified correctly because from this Schema Node will search starts. If the
     * Actual Schema Node is not correct the operation will simply fail, because it will be unable to find desired
     * DataSchemaNode.
     * <br>
     * In case that Schema Context or Module or Actual Schema Node or relative Revision Aware XPath contains
     * <code>null</code> references the method will throw IllegalArgumentException
     * <br>
     * If the Revision Aware XPath doesn't have flag <code>isAbsolute == false</code> the method will
     * throw IllegalArgumentException.
     * <br>
     * If the relative Revision Aware XPath is correct and desired Data Schema Node is present in Yang module or in
     * depending module in Schema Context the method will return specified Data Schema Node,
     * otherwise the operation will fail
     * and method will return <code>null</code>.
     *
     * @throws IllegalArgumentException
     *
     * @param context Schema Context
     * @param module Yang Module
     * @param actualSchemaNode Actual Schema Node
     * @param relativeXPath Relative Non Conditional Revision Aware XPath
     * @return DataSchemaNode if is present in specified Schema Context for given relative Revision Aware XPath,
     * otherwise will return <code>null</code>.
     */
    public static def SchemaNode findDataSchemaNodeForRelativeXPath( SchemaContext context,  Module module,
             SchemaNode actualSchemaNode,  RevisionAwareXPath relativeXPath) {
        if (context === null) {
            throw new IllegalArgumentException("Schema Context reference cannot be NULL!");
        }
        if (module === null) {
            throw new IllegalArgumentException("Module reference cannot be NULL!");
        }
        if (actualSchemaNode === null) {
            throw new IllegalArgumentException("Actual Schema Node reference cannot be NULL!");
        }
        if (relativeXPath === null) {
            throw new IllegalArgumentException("Non Conditional Revision Aware XPath cannot be NULL!");
        }
        if (relativeXPath.isAbsolute()) {
            throw new IllegalArgumentException("Revision Aware XPath MUST be relative i.e. MUST contains ../, "
                    + "for non relative Revision Aware XPath use findDataSchemaNode method!");
        }

         val actualNodePath = actualSchemaNode.getPath();
        if (actualNodePath != null) {
             val qnamePath = resolveRelativeXPath(context, module, relativeXPath, actualSchemaNode);

            if (qnamePath != null) {
                return findNodeInSchemaContext(context,qnamePath);
            }
        }
        return null;
    }

    /**
     * Returns parent Yang Module for specified Schema Context in which Schema Node is declared. If the Schema Node
     * is not present in Schema Context the operation will return <code>null</code>.
     * <br>
     * If Schema Context or Schema Node contains <code>null</code> references the method will throw IllegalArgumentException
     *
     * @throws IllegalArgumentException
     *
     * @param context Schema Context
     * @param schemaNode Schema Node
     * @return Yang Module for specified Schema Context and Schema Node, if Schema Node is NOT present,
     * the method will returns <code>null</code>
     */
    public static def Module findParentModule( SchemaContext context,  SchemaNode schemaNode) {
        if (context === null) {
            throw new IllegalArgumentException("Schema Context reference cannot be NULL!");
        }
        if (schemaNode === null) {
            throw new IllegalArgumentException("Schema Node cannot be NULL!");
        }

        val schemaPath = schemaNode.getPath();
        if (schemaPath === null) {
            throw new IllegalStateException("Schema Path for Schema Node is not "
                    + "set properly (Schema Path is NULL)");
        }
        val qnamedPath = schemaPath.path;
        if (qnamedPath === null || qnamedPath.empty) {
            throw new IllegalStateException("Schema Path contains invalid state of path parts."
                    + "The Schema Path MUST contain at least ONE QName which defines namespace and Local name"
                    + "of path.");
        }
        val qname = qnamedPath.get(qnamedPath.size() - 1);
        return context.findModuleByNamespaceAndRevision(qname.namespace,qname.revision);
    }

    /**
     * Method will attempt to find DataSchemaNode from specified Module and Queue of QNames through the Schema
     * Context. The QNamed path could be defined across multiple modules in Schema Context so the method is called
     * recursively. If the QNamed path contains QNames that are not part of any Module or Schema Context Path the
     * operation will fail and returns <code>null</code>
     * <br>
     * If Schema Context, Module or Queue of QNames refers to <code>null</code> values,
     * the method will throws IllegalArgumentException
     *
     * @throws IllegalArgumentException
     *
     * @param context Schema Context
     * @param module Yang Module
     * @param qnamedPath Queue of QNames
     * @return DataSchemaNode if is present in Module(s) for specified Schema Context and given QNamed Path,
     * otherwise will return <code>null</code>.
     */
    private static def SchemaNode findSchemaNodeForGivenPath( SchemaContext context,  Module module,
             Queue<QName> qnamedPath) {
        if (context === null) {
            throw new IllegalArgumentException("Schema Context reference cannot be NULL!");
        }
        if (module === null) {
            throw new IllegalArgumentException("Module reference cannot be NULL!");
        }
        if (module.getNamespace() === null) {
            throw new IllegalArgumentException("Namespace for Module cannot contains NULL reference!");
        }
        if (qnamedPath === null || qnamedPath.isEmpty()) {
            throw new IllegalStateException("Schema Path contains invalid state of path parts."
                    + "The Schema Path MUST contain at least ONE QName which defines namespace and Local name"
                    + "of path.");
        }

        var DataNodeContainer nextNode = module;
        val moduleNamespace = module.getNamespace();

        var QName childNodeQName;
        var SchemaNode schemaNode = null;
        while ((nextNode != null) && !qnamedPath.isEmpty()) {
            childNodeQName = qnamedPath.peek();
            if (childNodeQName != null) {
                schemaNode = nextNode.getDataChildByName(childNodeQName.getLocalName());
                if(schemaNode === null && nextNode instanceof Module) {
                    schemaNode = (nextNode as Module).getNotificationByName(childNodeQName);
                }
                if(schemaNode === null && nextNode instanceof Module) {
                    
                }
                val URI childNamespace = childNodeQName.getNamespace();
                val Date childRevision = childNodeQName.getRevision();
                
                if (schemaNode != null) {
                    if (schemaNode instanceof ContainerSchemaNode) {
                        nextNode = schemaNode as ContainerSchemaNode;
                    } else if (schemaNode instanceof ListSchemaNode) {
                        nextNode = schemaNode as ListSchemaNode;
                    } else if (schemaNode instanceof ChoiceNode) {
                        val choice =  schemaNode as ChoiceNode;
                        qnamedPath.poll();
                        if (!qnamedPath.isEmpty()) {
                            childNodeQName = qnamedPath.peek();
                            nextNode = choice.getCaseNodeByName(childNodeQName);
                            schemaNode = nextNode as DataSchemaNode;
                        }
                    } else {
                        nextNode = null;
                    }
                } else if (!childNamespace.equals(moduleNamespace)) {
                    val Module nextModule = context.findModuleByNamespaceAndRevision(childNamespace,childRevision);
                    schemaNode = findSchemaNodeForGivenPath(context, nextModule, qnamedPath);
                    return schemaNode;
                }
                qnamedPath.poll();
            }
        }
        return schemaNode;
    }


    public static def SchemaNode findNodeInSchemaContext(SchemaContext context, List<QName> path) {
        val current = path.get(0);
        val module = context.findModuleByNamespaceAndRevision(current.namespace,current.revision);
        if(module === null) return null;
        return findNodeInModule(module,path);
    }

    public static def GroupingDefinition findGrouping(SchemaContext context, Module module, List<QName> path) {
        var first = path.get(0);
        var Module m = context.findModuleByNamespace(first.namespace).iterator().next();
        var DataNodeContainer currentParent = m;
        for (qname : path) {
            var boolean found = false;
            var DataNodeContainer node = currentParent.getDataChildByName(qname.localName) as DataNodeContainer;
            if (node == null) {
                var Set<GroupingDefinition> groupings = currentParent.getGroupings();
                for (gr : groupings) {
                    if(gr.getQName().localName.equals(qname.localName)) {
                        currentParent = gr;
                        found = true;
                    }
                }
            } else {
                found = true;
                currentParent = node;
            }
            if (!found) {
                throw new IllegalArgumentException("Failed to find referenced grouping: " + path + "(" + qname.localName + ")");
            }
        }
        
        return currentParent as GroupingDefinition;
    }

    private static def SchemaNode findNodeInModule(Module module, List<QName> path) {
        val current = path.get(0)
        var SchemaNode node = module.getDataChildByName(current);
        if (node != null) return findNode(node as DataSchemaNode,path.nextLevel);
        node = module.getRpcByName(current);
        if (node != null) return findNodeInRpc(node as RpcDefinition,path.nextLevel)
        node = module.getNotificationByName(current);
        if (node != null) return findNodeInNotification(node as NotificationDefinition,path.nextLevel)
        node = module.getGroupingByName(current);
        if (node != null) return findNodeInGrouping(node as GroupingDefinition, path.nextLevel);
        //return null
        return node
    }

    private static def SchemaNode findNodeInGrouping(GroupingDefinition grouping, List<QName> path) {
        if (path.empty) return grouping;
        val current = path.get(0)
        val node = grouping.getDataChildByName(current);
        if (node != null) return findNode(node, path.nextLevel);
        return null;
    }

    private static def SchemaNode findNodeInRpc(RpcDefinition rpc,List<QName> path) {
        if(path.empty) return rpc;
        val current = path.get(0);
        switch (current.localName) {
            case "input": return findNode(rpc.input,path.nextLevel)
            case "output": return  findNode(rpc.output,path.nextLevel)
        }
        return null
    }
    
    private static def SchemaNode findNodeInNotification(NotificationDefinition rpc,List<QName> path) {
        if(path.empty) return rpc;
        val current = path.get(0)
        val node = rpc.getDataChildByName(current)
        if(node != null) return findNode(node,path.nextLevel)
        return null
    }
    
    private static dispatch def SchemaNode findNode(ChoiceNode parent,List<QName> path) {
        if(path.empty) return parent;
        val current = path.get(0)
        val node = parent.getCaseNodeByName(current)
        if (node != null) return findNodeInCase(node,path.nextLevel)
        return null
    }
    
    private static dispatch def SchemaNode findNode(ContainerSchemaNode parent,List<QName> path) {
        if(path.empty) return parent;
         val current = path.get(0)
        val node = parent.getDataChildByName(current)
        if (node != null) return findNode(node,path.nextLevel)
        return null
    }
    
    private static dispatch def SchemaNode findNode(ListSchemaNode parent,List<QName> path) {
        if(path.empty) return parent;
         val current = path.get(0)
        val node = parent.getDataChildByName(current)
        if (node != null) return findNode(node,path.nextLevel)
        return null
    }
    
    private static dispatch def SchemaNode findNode(DataSchemaNode parent,List<QName> path){
        if(path.empty) {
            return parent
        } else {
            throw new IllegalArgumentException("Path nesting violation");
        }
    }
    
    public static  def SchemaNode findNodeInCase(ChoiceCaseNode parent,List<QName> path) {
        if(path.empty) return parent;
         val current = path.get(0)
        val node = parent.getDataChildByName(current)
        if (node != null) return findNode(node,path.nextLevel)
        return null
    }
    
     
    public static def RpcDefinition getRpcByName(Module module, QName name) {
        for(rpc : module.rpcs) {
            if(rpc.QName.equals(name)) {
                return rpc;
            }
        }
        return null;
    }
    
    
    private static def nextLevel(List<QName> path){
        return path.subList(1,path.size)
    }
    
    public static def NotificationDefinition getNotificationByName(Module module, QName name) {
        for(notification : module.notifications) {
            if(notification.QName.equals(name)) {
                return notification;
            }
        }
        return null;
    }
    
    public static def GroupingDefinition getGroupingByName(Module module, QName name) {
        for (grouping : module.groupings) {
            if (grouping.QName.equals(name)) {
                return grouping;
            }
        }
        return null;
    }

    /**
     * Transforms string representation of XPath to Queue of QNames. The XPath is split by "/" and for each part of
     * XPath is assigned correct module in Schema Path.
     * <br>
     * If Schema Context, Parent Module or XPath string contains <code>null</code> values,
     * the method will throws IllegalArgumentException
     *
     * @throws IllegalArgumentException
     *
     * @param context Schema Context
     * @param parentModule Parent Module
     * @param xpath XPath String
     * @return return a list of QName
     */
    private static def xpathToQNamePath( SchemaContext context,  Module parentModule,
             String xpath) {
        if (context === null) {
            throw new IllegalArgumentException("Schema Context reference cannot be NULL!");
        }
        if (parentModule === null) {
            throw new IllegalArgumentException("Parent Module reference cannot be NULL!");
        }
        if (xpath === null) {
            throw new IllegalArgumentException("XPath string reference cannot be NULL!");
        }

        val path = new LinkedList<QName>();
        val String[] prefixedPath = xpath.split("/");
        for (pathComponent : prefixedPath) {
            if (!pathComponent.isEmpty()) {
                path.add(stringPathPartToQName(context, parentModule, pathComponent));
            }
        }
        return path;
    }

    /**
     * Transforms part of Prefixed Path as java String to QName.
     * <br>
     * If the string contains module prefix separated by ":" (i.e. mod:container) this module is provided from from
     * Parent Module list of imports. If the Prefixed module is present in Schema Context the QName can be
     * constructed.
     * <br>
     * If the Prefixed Path Part does not contains prefix the Parent's Module namespace is taken for construction of
     * QName.
     * <br>
     * If Schema Context, Parent Module or Prefixed Path Part refers to <code>null</code> the method will throw
     * IllegalArgumentException
     *
     * @throws IllegalArgumentException
     *
     * @param context Schema Context
     * @param parentModule Parent Module
     * @param prefixedPathPart Prefixed Path Part string
     * @return QName from prefixed Path Part String.
     */
    private static def QName stringPathPartToQName( SchemaContext context,  Module parentModule,
             String prefixedPathPart) {
        if (context === null) {
            throw new IllegalArgumentException("Schema Context reference cannot be NULL!");
        }
        if (parentModule === null) {
            throw new IllegalArgumentException("Parent Module reference cannot be NULL!");
        }
        if (prefixedPathPart === null) {
            throw new IllegalArgumentException("Prefixed Path Part cannot be NULL!");
        }

        if (prefixedPathPart.contains(":")) {
            val String[] prefixedName = prefixedPathPart.split(":");
            val module = resolveModuleForPrefix(context, parentModule, prefixedName.get(0));
            if (module == null) {
                throw new IllegalArgumentException(
                    "Failed to resolve xpath: no module found for prefix " + prefixedName.get(0) + " in module " +
                        parentModule.name)
            } else {
                return new QName(module.getNamespace(), module.getRevision(), prefixedName.get(1));
            }
        } else {
            return new QName(parentModule.getNamespace(), parentModule.getRevision(), prefixedPathPart);
        }
    }

    /**
     * Method will attempt to resolve and provide Module reference for specified module prefix. Each Yang module
     * could contains multiple imports which MUST be associated with corresponding module prefix. The method simply
     * looks into module imports and returns the module that is bounded with specified prefix. If the prefix is not
     * present in module or the prefixed module is not present in specified Schema Context,
     * the method will return <code>null</code>.
     * <br>
     * If String prefix is the same as prefix of the specified Module the reference to this module is returned.
     * <br>
     * If Schema Context, Module or Prefix are referring to <code>null</code> the method will return
     * IllegalArgumentException
     *
     * @throws IllegalArgumentException
     *
     * @param context Schema Context
     * @param module Yang Module
     * @param prefix Module Prefix
     * @return Module for given prefix in specified Schema Context if is present, otherwise returns <code>null</code>
     */
    private static def Module resolveModuleForPrefix( SchemaContext context,  Module module,  String prefix) {
        if (context === null) {
            throw new IllegalArgumentException("Schema Context reference cannot be NULL!");
        }
        if (module === null) {
            throw new IllegalArgumentException("Module reference cannot be NULL!");
        }
        if (prefix === null) {
            throw new IllegalArgumentException("Prefix string cannot be NULL!");
        }

        if (prefix.equals(module.getPrefix())) {
            return module;
        }

        val imports = module.getImports();
        for ( ModuleImport mi : imports) {
            if (prefix.equals(mi.getPrefix())) {
                return context.findModuleByName(mi.getModuleName(), mi.getRevision());
            }
        }
        return null;
    }

    /**
     * @throws IllegalArgumentException
     *
     * @param context Schema Context
     * @param module Yang Module
     * @param relativeXPath Non conditional Revision Aware Relative XPath
     * @param leafrefSchemaPath Schema Path for Leafref
     * @return list of QName
     */
    private static def resolveRelativeXPath( SchemaContext context,  Module module,
             RevisionAwareXPath relativeXPath,  SchemaNode leafrefParentNode) {

        if (context === null) {
            throw new IllegalArgumentException("Schema Context reference cannot be NULL!");
        }
        if (module === null) {
            throw new IllegalArgumentException("Module reference cannot be NULL!");
        }
        if (relativeXPath === null) {
            throw new IllegalArgumentException("Non Conditional Revision Aware XPath cannot be NULL!");
        }
        if (relativeXPath.isAbsolute()) {
            throw new IllegalArgumentException("Revision Aware XPath MUST be relative i.e. MUST contains ../, "
                    + "for non relative Revision Aware XPath use findDataSchemaNode method!");
        }
        if (leafrefParentNode.getPath() === null) {
            throw new IllegalArgumentException("Schema Path reference for Leafref cannot be NULL!");
        }
        val absolutePath = new LinkedList<QName>();
        val String strXPath = relativeXPath.toString();
        if (strXPath != null) {
            val String[] xpaths = strXPath.split("/");
            if (xpaths != null) {
                var int colCount = 0;
                while (xpaths.get(colCount).contains("..")) {
                    colCount = colCount+ 1;
                }
                val path = leafrefParentNode.getPath().getPath();
                if (path != null) {
                    val int lenght = path.size() - colCount;
                    absolutePath.addAll(path.subList(0,lenght));
                    val List<QName> sublist = xpaths.subList(colCount,xpaths.length).map[stringPathPartToQName(context, module,it)]
                    absolutePath.addAll(sublist)
                }
            }
        }
        return absolutePath;
    }
}
