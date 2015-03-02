/**
 * Copyright (c) 2000-2014 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.faces.alloy.component.popover.internal;

import java.io.IOException;

import javax.faces.application.ProjectStage;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import com.liferay.faces.alloy.component.button.Button;
import com.liferay.faces.alloy.component.popover.Popover;
import com.liferay.faces.util.component.ClientComponent;
import com.liferay.faces.util.component.ComponentUtil;
import com.liferay.faces.util.lang.StringPool;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;


/**
 * @author  Vernon Singleton
 */
//J-
@FacesRenderer(componentFamily = Popover.COMPONENT_FAMILY, rendererType = Popover.RENDERER_TYPE)
@ResourceDependencies(
	{
		@ResourceDependency(library = "liferay-faces-alloy", name = "alloy.css"),
		@ResourceDependency(library = "liferay-faces-reslib", name = "build/aui-css/css/bootstrap.min.css"),
		@ResourceDependency(library = "liferay-faces-reslib", name = "build/aui/aui-min.js"),
		@ResourceDependency(library = "liferay-faces-reslib", name = "liferay.js")
	}
)
//J+
public class PopoverRenderer extends PopoverRendererBase {

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(PopoverRenderer.class);

	// Private Constants
	private static final String ALIGN = "align";
	private static final String[] MODULES = { "aui-popover", "event-outside" };
	private static final String NODE = "node";

	@Override
	public void encodeJavaScriptCustom(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		ResponseWriter responseWriter = facesContext.getResponseWriter();

		Popover popover = (Popover) uiComponent;
		ClientComponent clientComponent = (ClientComponent) uiComponent;
		String clientVarName = ComponentUtil.getClientVarName(facesContext, clientComponent);
		String clientKey = clientComponent.getClientKey();

		if (clientKey == null) {
			clientKey = clientVarName;
		}

		if (popover.isHideIconRendered()) {

			// Add an "x" toolbar icon so that the popover can be hidden just like alloy:dialog can.
			responseWriter.write(LIFERAY_COMPONENT);
			responseWriter.write(StringPool.OPEN_PARENTHESIS);
			responseWriter.write(StringPool.APOSTROPHE);
			responseWriter.write(clientKey);
			responseWriter.write(StringPool.APOSTROPHE);
			responseWriter.write(StringPool.CLOSE_PARENTHESIS);
			responseWriter.write(
				".addToolbar([{cssClass:'close',label:'\u00D7',on:{click:function(event){Liferay.component('");
			responseWriter.write(clientKey);
			responseWriter.write("').hide();}},render:true}],'header')");
			responseWriter.write(StringPool.SEMICOLON);
		}

		encodeOverlayDismissible(responseWriter, popover, clientKey);
		encodeOverlayJavaScriptCustom(responseWriter, facesContext, popover);

		if ((popover.getFor() == null) && facesContext.isProjectStage(ProjectStage.Development)) {
			logger.error("The 'for' attribute is required for alloy:popover");
		}
	}

	protected void encodeAlign(ResponseWriter responseWriter, Popover popover, boolean first) throws IOException {

		encodeNonEscapedObject(responseWriter, ALIGN, StringPool.BLANK, first);
		responseWriter.write(StringPool.OPEN_CURLY_BRACE);

		String for_ = popover.getFor();
		encodeClientId(responseWriter, NODE, for_, popover, true);
		responseWriter.write(StringPool.CLOSE_CURLY_BRACE);

		UIComponent forComponent = popover.findComponent(for_);

		if (forComponent != null) {

			if (forComponent instanceof Button) {
				Button button = (Button) forComponent;

				if ((button.getOnclick() == null) && (button.getOnmouseover() == null)) {
					logger.warn(
						"Popover [{0}] is *for* button [{1}] but the button does not have an onclick or onmouseover attribute.",
						popover.getClientKey(), for_);
				}
			}

		}
	}

	@Override
	protected void encodeHiddenAttributes(FacesContext facesContext, ResponseWriter responseWriter, Popover popover,
		boolean first) throws IOException {

		// Encode the "align" Alloy hidden attribute.
		encodeAlign(responseWriter, popover, first);

		first = false;

		encodeOverlayHiddenAttributes(facesContext, responseWriter, popover, first);
	}

	@Override
	protected void encodeZIndex(ResponseWriter responseWriter, Popover popover, Integer zIndex, boolean first)
		throws IOException {
		encodeOverlayZIndex(responseWriter, popover, zIndex, LIFERAY_Z_INDEX_OVERLAY, first);
	}

	@Override
	public String getDelegateComponentFamily() {
		return Popover.DELEGATE_COMPONENT_FAMILY;
	}

	@Override
	public String getDelegateRendererType() {
		return Popover.DELEGATE_RENDERER_TYPE;
	}

	@Override
	protected String[] getModules(FacesContext facesContext, UIComponent uiComponent) {
		return MODULES;
	}
}
