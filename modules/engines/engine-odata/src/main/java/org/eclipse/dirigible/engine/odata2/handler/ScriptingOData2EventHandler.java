/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.engine.odata2.handler;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.eclipse.dirigible.commons.api.scripting.ScriptingException;
import org.eclipse.dirigible.commons.config.Configuration;
import org.eclipse.dirigible.engine.api.script.ScriptEngineExecutorsManager;
import org.eclipse.dirigible.engine.js.api.IJavascriptEngineExecutor;
import org.eclipse.dirigible.engine.odata2.api.ODataException;
import org.eclipse.dirigible.engine.odata2.definition.ODataHandlerDefinition;
import org.eclipse.dirigible.engine.odata2.definition.ODataHandlerMethods;
import org.eclipse.dirigible.engine.odata2.definition.ODataHandlerTypes;
import org.eclipse.dirigible.engine.odata2.service.ODataCoreService;
import org.eclipse.dirigible.engine.odata2.sql.api.OData2EventHandler;
import org.eclipse.dirigible.engine.odata2.sql.processor.DefaultSQLProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ScriptingOData2EventHandler.
 */
public class ScriptingOData2EventHandler implements OData2EventHandler {

    /** The Constant DIRIGIBLE_ODATA_HANDLER_EXECUTOR_TYPE. */
    private static final String DIRIGIBLE_ODATA_HANDLER_EXECUTOR_TYPE = "DIRIGIBLE_ODATA_HANDLER_EXECUTOR_TYPE";
    
    /** The Constant DIRIGIBLE_ODATA_HANDLER_EXECUTOR_ON_EVENT. */
    private static final String DIRIGIBLE_ODATA_HANDLER_EXECUTOR_ON_EVENT = "DIRIGIBLE_ODATA_HANDLER_EXECUTOR_ON_EVENT";

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(DefaultSQLProcessor.class);

    /** The Constant DIRIGIBLE_ODATA_WRAPPER_MODULE_ON_EVENT. */
    private static final String DIRIGIBLE_ODATA_WRAPPER_MODULE_ON_EVENT = "odata/wrappers/onEvent";

    /** The Constant ERROR_EXECUTING_SCRIPTING_HANDLER. */
    private static final String ERROR_EXECUTING_SCRIPTING_HANDLER = "Error executing scripting handler: ";

    /** The odata core service. */
    private ODataCoreService odataCoreService = new ODataCoreService();

    /**
     * Before create entity.
     *
     * @param uriInfo the uri info
     * @param requestContentType the request content type
     * @param contentType the content type
     * @param entry the entry
     * @param context the context
     * @return the o data response
     * @throws ODataException the o data exception
     */
    @Override
    public ODataResponse beforeCreateEntity(PostUriInfo uriInfo, String requestContentType,
                                            String contentType, ODataEntry entry, Map<Object, Object> context) throws ODataException {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.create.name();
            String type = ODataHandlerTypes.before.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            context.put("uriInfo", uriInfo);
            context.put("requestContentType", requestContentType);
            context.put("contentType", contentType);
            context.put("entry", entry);
            executeHandlers(handlers, context);
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptingException e) {
            throw new ODataException(ERROR_EXECUTING_SCRIPTING_HANDLER + e.getMessage(), e);
        }
        return null;
    }

    /**
     * After create entity.
     *
     * @param uriInfo the uri info
     * @param requestContentType the request content type
     * @param contentType the content type
     * @param entry the entry
     * @param context the context
     * @return the o data response
     * @throws ODataException the o data exception
     */
    @Override
    public ODataResponse afterCreateEntity(PostUriInfo uriInfo, String requestContentType,
                                           String contentType, ODataEntry entry, Map<Object, Object> context) throws ODataException {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.create.name();
            String type = ODataHandlerTypes.after.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            context.put("uriInfo", uriInfo);
            context.put("requestContentType", requestContentType);
            context.put("contentType", contentType);
            context.put("entry", entry);
            executeHandlers(handlers, context);
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptingException e) {
            throw new ODataException(ERROR_EXECUTING_SCRIPTING_HANDLER + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Checks if is using on create entity.
     *
     * @param uriInfo the uri info
     * @param requestContentType the request content type
     * @param contentType the content type
     * @return true, if is using on create entity
     */
    @Override
    public boolean isUsingOnCreateEntity(PostUriInfo uriInfo, String requestContentType,
                                         String contentType) {
        return isUsingEvent((UriInfo) uriInfo, ODataHandlerMethods.create.name(), ODataHandlerTypes.on.name());
    }

    /**
     * Checks if is using after create entity.
     *
     * @param uriInfo the uri info
     * @param requestContentType the request content type
     * @param contentType the content type
     * @return true, if is using after create entity
     */
    @Override
    public boolean isUsingAfterCreateEntity(PostUriInfo uriInfo, String requestContentType,
                                         String contentType) {
        return isUsingEvent((UriInfo) uriInfo, ODataHandlerMethods.create.name(), ODataHandlerTypes.after.name());
    }

    /**
     * On create entity.
     *
     * @param uriInfo the uri info
     * @param content the content
     * @param requestContentType the request content type
     * @param contentType the content type
     * @param context the context
     * @return the o data response
     * @throws ODataException the o data exception
     */
    @Override
    public ODataResponse onCreateEntity(PostUriInfo uriInfo, InputStream content, String requestContentType,
                                        String contentType, Map<Object, Object> context) throws ODataException {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.create.name();
            String type = ODataHandlerTypes.on.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            context.put("uriInfo", uriInfo);
            context.put("requestContentType", requestContentType);
            context.put("contentType", contentType);
            context.put("content", content);
            String responseMessage = executeHandler(handlers, context);
            ODataResponse response = EntityProvider.writeText(responseMessage);
            return response;
        } catch (EdmException | ODataException | EntityProviderException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptingException e) {
            throw new ODataException(ERROR_EXECUTING_SCRIPTING_HANDLER + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Forbid create entity.
     *
     * @param uriInfo the uri info
     * @param requestContentType the request content type
     * @param contentType the content type
     * @return true, if successful
     */
    @Override
    public boolean forbidCreateEntity(PostUriInfo uriInfo, String requestContentType,
                                      String contentType) {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.create.name();
            String type = ODataHandlerTypes.forbid.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            return handlers.size() > 0;
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Before update entity.
     *
     * @param uriInfo the uri info
     * @param requestContentType the request content type
     * @param merge the merge
     * @param contentType the content type
     * @param entry the entry
     * @param context the context
     * @return the o data response
     * @throws ODataException the o data exception
     */
    @Override
    public ODataResponse beforeUpdateEntity(PutMergePatchUriInfo uriInfo, String requestContentType,
                                            boolean merge, String contentType, ODataEntry entry, Map<Object, Object> context) throws ODataException {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.update.name();
            String type = ODataHandlerTypes.before.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            context.put("uriInfo", uriInfo);
            context.put("requestContentType", requestContentType);
            context.put("merge", merge);
            context.put("contentType", contentType);
            context.put("entry", entry);
            executeHandlers(handlers, context);
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptingException e) {
            throw new ODataException(ERROR_EXECUTING_SCRIPTING_HANDLER + e.getMessage(), e);
        }
        return null;
    }

    /**
     * After update entity.
     *
     * @param uriInfo the uri info
     * @param requestContentType the request content type
     * @param merge the merge
     * @param contentType the content type
     * @param entry the entry
     * @param context the context
     * @return the o data response
     * @throws ODataException the o data exception
     */
    @Override
    public ODataResponse afterUpdateEntity(PutMergePatchUriInfo uriInfo, String requestContentType,
                                           boolean merge, String contentType, ODataEntry entry, Map<Object, Object> context) throws ODataException {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.update.name();
            String type = ODataHandlerTypes.after.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            context.put("uriInfo", uriInfo);
            context.put("requestContentType", requestContentType);
            context.put("merge", merge);
            context.put("contentType", contentType);
            context.put("entry", entry);
            executeHandlers(handlers, context);
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptingException e) {
            throw new ODataException(ERROR_EXECUTING_SCRIPTING_HANDLER + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Checks if is using on update entity.
     *
     * @param uriInfo the uri info
     * @param requestContentType the request content type
     * @param merge the merge
     * @param contentType the content type
     * @return true, if is using on update entity
     */
    @Override
    public boolean isUsingOnUpdateEntity(PutMergePatchUriInfo uriInfo, String requestContentType,
                                         boolean merge, String contentType) {
        return isUsingEvent((UriInfo) uriInfo, ODataHandlerMethods.update.name(), ODataHandlerTypes.on.name());
    }

    /**
     * On update entity.
     *
     * @param uriInfo the uri info
     * @param content the content
     * @param requestContentType the request content type
     * @param merge the merge
     * @param contentType the content type
     * @param context the context
     * @return the o data response
     * @throws ODataException the o data exception
     */
    @Override
    public ODataResponse onUpdateEntity(PutMergePatchUriInfo uriInfo, InputStream content, String requestContentType,
                                        boolean merge, String contentType, Map<Object, Object> context) throws ODataException {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.update.name();
            String type = ODataHandlerTypes.on.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            context.put("uriInfo", uriInfo);
            context.put("requestContentType", requestContentType);
            context.put("merge", merge);
            context.put("contentType", contentType);
            context.put("content", content);
            String responseMessage = executeHandler(handlers, context);
            ODataResponse response = EntityProvider.writeText(responseMessage);
            return response;
        } catch (EdmException | ODataException | EntityProviderException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptingException e) {
            throw new ODataException(ERROR_EXECUTING_SCRIPTING_HANDLER + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Forbid update entity.
     *
     * @param uriInfo the uri info
     * @param requestContentType the request content type
     * @param merge the merge
     * @param contentType the content type
     * @return true, if successful
     */
    @Override
    public boolean forbidUpdateEntity(PutMergePatchUriInfo uriInfo, String requestContentType,
                                      boolean merge, String contentType) {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.update.name();
            String type = ODataHandlerTypes.forbid.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            return handlers.size() > 0;
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Before delete entity.
     *
     * @param uriInfo the uri info
     * @param contentType the content type
     * @param context the context
     * @return the o data response
     * @throws ODataException the o data exception
     */
    @Override
    public ODataResponse beforeDeleteEntity(DeleteUriInfo uriInfo, String contentType, Map<Object, Object> context) throws ODataException {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.delete.name();
            String type = ODataHandlerTypes.before.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            context.put("uriInfo", uriInfo);
            context.put("contentType", contentType);
            executeHandlers(handlers, context);
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptingException e) {
            throw new ODataException(ERROR_EXECUTING_SCRIPTING_HANDLER + e.getMessage(), e);
        }
        return null;
    }

    /**
     * After delete entity.
     *
     * @param uriInfo the uri info
     * @param contentType the content type
     * @param context the context
     * @return the o data response
     * @throws ODataException the o data exception
     */
    @Override
    public ODataResponse afterDeleteEntity(DeleteUriInfo uriInfo, String contentType, Map<Object, Object> context) throws ODataException {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.delete.name();
            String type = ODataHandlerTypes.after.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            context.put("uriInfo", uriInfo);
            context.put("contentType", contentType);
            executeHandlers(handlers, context);
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptingException e) {
            throw new ODataException(ERROR_EXECUTING_SCRIPTING_HANDLER + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Checks if is using on delete entity.
     *
     * @param uriInfo the uri info
     * @param contentType the content type
     * @return true, if is using on delete entity
     */
    @Override
    public boolean isUsingOnDeleteEntity(DeleteUriInfo uriInfo, String contentType) {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.delete.name();
            String type = ODataHandlerTypes.on.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            return handlers.size() > 0;
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * On delete entity.
     *
     * @param uriInfo the uri info
     * @param contentType the content type
     * @param context the context
     * @return the o data response
     * @throws ODataException the o data exception
     */
    @Override
    public ODataResponse onDeleteEntity(DeleteUriInfo uriInfo, String contentType, Map<Object, Object> context) throws ODataException {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.delete.name();
            String type = ODataHandlerTypes.on.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            context.put("uriInfo", uriInfo);
            context.put("contentType", contentType);
            String responseMessage = executeHandler(handlers, context);
            ODataResponse response = EntityProvider.writeText(responseMessage);
            return response;
        } catch (EdmException | ODataException | EntityProviderException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptingException e) {
            throw new ODataException(ERROR_EXECUTING_SCRIPTING_HANDLER + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Forbid delete entity.
     *
     * @param uriInfo the uri info
     * @param contentType the content type
     * @return true, if successful
     */
    @Override
    public boolean forbidDeleteEntity(DeleteUriInfo uriInfo, String contentType) {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            String method = ODataHandlerMethods.delete.name();
            String type = ODataHandlerTypes.forbid.name();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            return handlers.size() > 0;
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return DEFAULT_ODATA_EVENT_HANDLER_NAME;
    }

    /**
     * Execute handlers.
     *
     * @param handlers the handlers
     * @param context the context
     */
    private void executeHandlers(List<ODataHandlerDefinition> handlers, Map<Object, Object> context) {
        handlers.forEach(handler -> {
            setHandlerParametersInContext(context, handler);
            executeHandlerByExecutor(context);
        });
    }

    /**
     * Execute handler.
     *
     * @param handlers the handlers
     * @param context the context
     * @return the string
     */
    private String executeHandler(List<ODataHandlerDefinition> handlers, Map<Object, Object> context) {
        if (handlers.size() > 0) {
            ODataHandlerDefinition handler = handlers.get(0);
            setHandlerParametersInContext(context, handler);
            Object response = executeHandlerByExecutor(context);
            return response != null ? response.toString() : "Empty response.";
        }
        ;
        return "No response.";
    }

    /**
     * Execute handler by executor.
     *
     * @param context the context
     * @return the object
     * @throws ScriptingException the scripting exception
     */
    private Object executeHandlerByExecutor(Map<Object, Object> context) throws ScriptingException {
        String odataHandlerExecutorType = Configuration.get(DIRIGIBLE_ODATA_HANDLER_EXECUTOR_TYPE);
        String odataHandlerExecutorOnEvent = Configuration.get(DIRIGIBLE_ODATA_HANDLER_EXECUTOR_ON_EVENT);
        if (odataHandlerExecutorType != null && odataHandlerExecutorOnEvent != null) {
            Object response = ScriptEngineExecutorsManager.executeServiceModule(
                    odataHandlerExecutorType, odataHandlerExecutorOnEvent, context);
            return response;
        }
        Object response = ScriptEngineExecutorsManager.executeServiceModule(
                IJavascriptEngineExecutor.JAVASCRIPT_TYPE_DEFAULT, DIRIGIBLE_ODATA_WRAPPER_MODULE_ON_EVENT, context);
        return response;
    }

    /**
     * Sets the handler parameters in context.
     *
     * @param context the context
     * @param handler the handler
     */
    private void setHandlerParametersInContext(Map<Object, Object> context, ODataHandlerDefinition handler) {
        context.put("location", handler.getLocation());
        context.put("namespace", handler.getNamespace());
        context.put("name", handler.getName());
        context.put("method", handler.getMethod());
        context.put("type", handler.getType());
        context.put("handler", handler.getHandler());
    }

    /**
     * Checks if is using event.
     *
     * @param uriInfo the uri info
     * @param method the method
     * @param type the type
     * @return true, if is using event
     */
    private boolean isUsingEvent(UriInfo uriInfo, String method, String type) {
        try {
            String namespace = uriInfo.getTargetType().getNamespace();
            String name = uriInfo.getTargetType().getName();
            List<ODataHandlerDefinition> handlers = odataCoreService.getHandlers(namespace, name, method, type);
            return handlers.size() > 0;
        } catch (EdmException | ODataException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

}
