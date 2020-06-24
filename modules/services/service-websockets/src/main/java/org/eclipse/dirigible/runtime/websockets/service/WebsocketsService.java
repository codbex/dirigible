/**
 * Copyright (c) 2010-2020 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *   SAP - initial API and implementation
 */
package org.eclipse.dirigible.runtime.websockets.service;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.dirigible.commons.api.module.StaticInjector;
import org.eclipse.dirigible.core.websockets.api.WebsocketsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Websockets Service.
 */
@ServerEndpoint("/websockets/v4/service/{endpoint}")
public class WebsocketsService {

	private static final Logger logger = LoggerFactory.getLogger(WebsocketsService.class);
	
	@Inject
	private WebsocketHandler handler = StaticInjector.getInjector().getInstance(WebsocketHandler.class);
	
	/**
	 * On open callback.
	 *
	 * @param session
	 *            the session
	 * @param endpoint
	 *            the endpoint
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("endpoint") String endpoint) {
		logger.debug(String.format("[websocket] Endpoint '%s' openned.", endpoint));
		Map<Object, Object> context = new HashMap<>();
    	context.put("METHOD", "onopen");
    	try {
			handler.processEvent(endpoint, context);
		} catch (WebsocketsException e) {
			logger.error(e.getMessage(), e);
		}
	}
	

	/**
	 * On message callback.
	 *
	 * @param message
	 *            the message
	 * @param session
	 *            the session
	 * @param endpoint
	 *            the endpoint
	 */
	@OnMessage
	public void onMessage(String message, Session session, @PathParam("endpoint") String endpoint) {
		logger.trace(String.format("[websocket] Endpoint '%s' received message:%s ", endpoint, message));
		Map<Object, Object> context = new HashMap<>();
		context.put("MESSAGE", message);
    	context.put("METHOD", "onmessage");
    	try {
			handler.processEvent(endpoint, context);
		} catch (WebsocketsException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * On error callback.
	 *
	 * @param session
	 *            the session
	 * @param throwable
	 *            the throwable
	 * @param endpoint
	 *            the endpoint
	 */
	@OnError
	public void onError(Session session, Throwable throwable, @PathParam("endpoint") String endpoint) {
		logger.error(String.format("[ws:console] Endpoint '%s' error %s", endpoint, throwable.getMessage()));
		logger.error("[websocket] " + throwable.getMessage(), throwable);
		Map<Object, Object> context = new HashMap<>();
		context.put("ERROR", throwable.getMessage());
    	context.put("METHOD", "onerror");
    	try {
			handler.processEvent(endpoint, context);
		} catch (WebsocketsException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * On close callback.
	 *
	 * @param session
	 *            the session
	 * @param closeReason
	 *            the close reason
	 * @param endpoint
	 *            the endpoint
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason, @PathParam("endpoint") String endpoint) {
		logger.debug(String.format("[websocket] Endpoint '%s' closed because of %s", endpoint, closeReason));
		Map<Object, Object> context = new HashMap<>();
    	context.put("METHOD", "onclose");
    	try {
			handler.processEvent(endpoint, context);
		} catch (WebsocketsException e) {
			logger.error(e.getMessage(), e);
		}
	}

}