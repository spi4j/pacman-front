/*******************************************************************************
 * Copyright (c) 2008, 2010 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package fr.pacman.front.start.ui.activator;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * The plug-in ID.
	 */
	public static final String c_pluginId = "fr.pacman.front.start.ui";

	/**
	 * The plug-in LOGO.
	 */
	public static final String c_pluginLogo = "icons/pacmug-wizard.png";

	/**
	 * The shared instance.
	 */
	private static Activator plugin;
	private BundleContext context;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 * @generated
	 */
	@Override
	public void start(final BundleContext p_context) throws Exception {
		super.start(p_context);
		this.context = p_context;
		plugin = this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 * @generated
	 */
	@Override
	public void stop(final BundleContext p_context) throws Exception {
		plugin = null;
		this.context = null;
		super.stop(p_context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public BundleContext getContext() {
		return context;
	}

	public static <T> T getService(Class<T> clazz) {
		BundleContext ctx = getDefault().getContext();
		if (ctx == null)
			return null;
		ServiceReference<T> ref = ctx.getServiceReference(clazz);
		return (ref != null) ? ctx.getService(ref) : null;
	}

}
