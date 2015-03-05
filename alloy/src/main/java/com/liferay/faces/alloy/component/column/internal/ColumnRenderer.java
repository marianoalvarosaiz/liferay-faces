/**
 * Copyright (c) 2000-2015 Liferay, Inc. All rights reserved.
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
package com.liferay.faces.alloy.component.column.internal;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import com.liferay.faces.alloy.component.AUICol;
import com.liferay.faces.alloy.component.column.Column;
import com.liferay.faces.util.lang.StringPool;


/**
 * @author  Juan Gonzalez
 */
public class ColumnRenderer extends Renderer {

	protected static final String OFFSET = "offset";
	protected static final String OFFSET_WIDTH = "offsetWidth";
	protected static final String SPAN = "span";
	protected static final String STYLE = "style";
	protected static final String STYLE_CLASS = "styleClass";
	protected static final String WIDTH = "width";

	@Override
	public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {
		super.encodeBegin(facesContext, uiComponent);

		ResponseWriter responseWriter = facesContext.getResponseWriter();
		responseWriter.startElement(StringPool.DIV, uiComponent);

		String clientId = uiComponent.getClientId(facesContext);
		responseWriter.writeAttribute(StringPool.ID, clientId, null);

		Column column = (Column) uiComponent;
		StringBuilder classNames = new StringBuilder();

		Integer span = column.getSpan();

		if (span != null) {

			if ((span < 1) || (span > Column.COLUMNS)) {
				throw new IOException("span number must be between 1 and " + Column.COLUMNS);
			}
		}

		Integer width = column.getWidth();

		if (width != null) {

			if ((width < 1) || (width > 100)) {
				throw new IOException("width must be between 1 and 100");
			}

			span = getColumnUnitSize(width);
		}

		classNames.append(StringPool.SPAN);
		classNames.append(span);

		Integer offset = column.getOffset();

		if (offset != null) {

			if ((offset < 1) || (offset > Column.COLUMNS)) {
				throw new IOException("offset must be between 1 and " + Column.COLUMNS);
			}
		}

		Integer offsetWidth = column.getOffsetWidth();

		if (offsetWidth != null) {

			if ((offsetWidth < 1) || (offsetWidth > 100)) {
				throw new IOException("offsetWidth must be between 1 and 100");
			}

			offset = getColumnUnitSize(offsetWidth);
		}

		if (offset != null) {
			classNames.append(StringPool.SPACE);
			classNames.append(OFFSET);
			classNames.append(offset);
		}

		String cssClass = column.getCssClass();

		if ((cssClass != null) && (cssClass.length() > 0)) {
			classNames.append(StringPool.SPACE);
			classNames.append(cssClass);
		}

		String styleClass = column.getStyleClass();

		if ((styleClass != null) && (styleClass.length() > 0)) {
			classNames.append(StringPool.SPACE);
			classNames.append(styleClass);
		}

		responseWriter.writeAttribute(StringPool.CLASS, classNames.toString(), null);
	}

	@Override
	public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		super.encodeEnd(facesContext, uiComponent);

		ResponseWriter responseWriter = facesContext.getResponseWriter();
		responseWriter.endElement(StringPool.DIV);
	}

	protected Integer getColumnUnitSize(Integer width) {
		return (int) Math.round(AUICol.COLUMNS * ((double) width / 100));
	}
}