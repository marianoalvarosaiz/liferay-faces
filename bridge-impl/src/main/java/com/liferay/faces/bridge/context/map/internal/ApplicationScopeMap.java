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
package com.liferay.faces.bridge.context.map.internal;

import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;

import com.liferay.faces.bridge.BridgeFactoryFinder;
import com.liferay.faces.bridge.bean.internal.BeanManager;
import com.liferay.faces.bridge.bean.internal.BeanManagerFactory;
import com.liferay.faces.bridge.bean.internal.PreDestroyInvoker;
import com.liferay.faces.bridge.bean.internal.PreDestroyInvokerFactory;
import com.liferay.faces.bridge.config.internal.PortletConfigParam;
import com.liferay.faces.bridge.context.BridgeContext;
import com.liferay.faces.util.config.ApplicationConfig;
import com.liferay.faces.util.map.AbstractPropertyMap;
import com.liferay.faces.util.map.AbstractPropertyMapEntry;


/**
 * @author  Neil Griffin
 */
public class ApplicationScopeMap extends AbstractPropertyMap<Object> {

	// Private Data Members
	private BeanManager beanManager;
	private PortletContext portletContext;
	private PreDestroyInvoker preDestroyInvoker;
	private boolean preferPreDestroy;

	public ApplicationScopeMap(BridgeContext bridgeContext) {

		BeanManagerFactory beanManagerFactory = (BeanManagerFactory) BridgeFactoryFinder.getFactory(
				BeanManagerFactory.class);
		this.portletContext = bridgeContext.getPortletContext();

		String appConfigAttrName = ApplicationConfig.class.getName();
		ApplicationConfig applicationConfig = (ApplicationConfig) this.portletContext.getAttribute(appConfigAttrName);
		this.beanManager = beanManagerFactory.getBeanManager(applicationConfig.getFacesConfig());

		// Determines whether or not methods annotated with the @PreDestroy annotation are preferably invoked
		// over the @BridgePreDestroy annotation.
		PortletConfig portletConfig = bridgeContext.getPortletConfig();
		this.preferPreDestroy = PortletConfigParam.PreferPreDestroy.getBooleanValue(portletConfig);

		PreDestroyInvokerFactory preDestroyInvokerFactory = (PreDestroyInvokerFactory) BridgeFactoryFinder.getFactory(
				PreDestroyInvokerFactory.class);
		this.preDestroyInvoker = preDestroyInvokerFactory.getPreDestroyInvoker(this);
	}

	/**
	 * According to the JSF 2.0 JavaDocs for {@link ExternalContext#getApplicationMap}, before a managed-bean is removed
	 * from the map, any public no-argument void return methods annotated with javax.annotation.PreDestroy must be
	 * called first.
	 */
	@Override
	public void clear() {
		Set<Map.Entry<String, Object>> mapEntries = entrySet();

		if (mapEntries != null) {

			for (Map.Entry<String, Object> mapEntry : mapEntries) {
				String potentialManagedBeanName = mapEntry.getKey();
				Object potentialManagedBeanValue = mapEntry.getValue();

				if (beanManager.isManagedBean(potentialManagedBeanName, potentialManagedBeanValue)) {
					preDestroyInvoker.invokeAnnotatedMethods(potentialManagedBeanValue, preferPreDestroy);
				}
			}
		}

		super.clear();
	}

	/**
	 * According to the JSF 2.0 JavaDocs for {@link ExternalContext#getApplicationMap}, before a managed-bean is removed
	 * from the map, any public no-argument void return methods annotated with javax.annotation.PreDestroy must be
	 * called first.
	 */
	@Override
	public Object remove(Object key) {
		String potentialManagedBeanName = (String) key;
		Object potentialManagedBeanValue = super.remove(key);

		if (beanManager.isManagedBean(potentialManagedBeanName, potentialManagedBeanValue)) {
			preDestroyInvoker.invokeAnnotatedMethods(potentialManagedBeanValue, preferPreDestroy);
		}

		return potentialManagedBeanValue;
	}

	@Override
	protected AbstractPropertyMapEntry<Object> createPropertyMapEntry(String name) {
		return new ApplicationScopeMapEntry(portletContext, name);
	}

	@Override
	protected void removeProperty(String name) {
		portletContext.removeAttribute(name);
	}

	@Override
	protected Object getProperty(String name) {
		return portletContext.getAttribute(name);
	}

	@Override
	protected void setProperty(String name, Object value) {
		portletContext.setAttribute(name, value);
	}

	@Override
	protected Enumeration<String> getPropertyNames() {
		return portletContext.getAttributeNames();
	}
}
