package fr.pacman.front.core.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.obeonetwork.dsl.cinematic.CinematicRoot;
import org.obeonetwork.dsl.cinematic.flow.Flow;
import org.obeonetwork.dsl.cinematic.flow.FlowState;
import org.obeonetwork.dsl.cinematic.flow.ViewState;
import org.obeonetwork.dsl.cinematic.view.AbstractViewElement;
import org.obeonetwork.dsl.cinematic.view.Layout;
import org.obeonetwork.dsl.cinematic.view.LayoutDirection;
import org.obeonetwork.dsl.cinematic.view.ViewContainer;

public class CinematicUtils {

	private static ViewState _header;
	private static ViewState _footer;
	private static ViewState _root;
	private static ViewState _error;
	private static ViewState _referential;

	private static Boolean _formLayout;
	private static Layout _virtualLayout;

	/**
	 * Conserve le temps de la génération les controleurs pour l'en-tete, le
	 * pied-de-page, le referentiel et l'index s'ils existent (sauf pour l'index qui
	 * est obligatoire).
	 * 
	 * @param p_root : le diagramme de cinématique root.
	 */
	public static boolean init(final CinematicRoot p_root) {

		_header = null;
		_footer = null;

		for (Flow v_flow : p_root.getFlows()) {
			for (FlowState v_viewState : v_flow.getStates()) {
				if (v_viewState instanceof ViewState) {
					for (ViewContainer v_viewContainer : ((ViewState) v_viewState).getViewContainers()) {
						if ("HeaderPanel".equalsIgnoreCase(v_viewContainer.getWidget().getName())) {
							_header = (ViewState) v_viewState;
						}
						if ("FooterPanel".equalsIgnoreCase(v_viewContainer.getWidget().getName())) {
							_footer = (ViewState) v_viewState;
						}
						if ("ReferentialPanel".equalsIgnoreCase(v_viewContainer.getWidget().getName())) {
							_referential = (ViewState) v_viewState;
						}
						if ("MainPanel".equalsIgnoreCase(v_viewContainer.getWidget().getName())) {
							_root = (ViewState) v_viewState;
						}
						if ("ErrorPanel".equalsIgnoreCase(v_viewContainer.getWidget().getName())) {
							_error = (ViewState) v_viewState;
						}
					}
					if (_footer != null && _header != null && _referential != null && _root != null && _error != null)
						break;
				}
			}
		}
		return false;
	}

	public static ViewState get_headerState(Object p_object) {
		return _header;
	}

	public static ViewState get_footerState(Object p_object) {
		return _footer;
	}

	public static ViewState get_referentialState(Object p_object) {
		return _referential;
	}

	public static ViewState get_errorState(Object p_object) {
		return _error;
	}

	public static ViewState get_rootState(Object p_object) {
		return _root;
	}

	public static boolean hasReferentialContainer(Object p_object) {
		return _referential != null;
	}

	public static boolean hasHeaderContainer(Object p_object) {
		return _header != null;
	}

	public static boolean hasFooterContainer(Object p_object) {
		return _footer != null;
	}

	public static boolean hasErrorContainer(Object p_object) {
		return _error != null;
	}

	public static ViewContainer get_referentialContainer(Object p_object) {

		if (null == _referential)
			return null;

		if (null == _referential.getViewContainers() || _referential.getViewContainers().isEmpty())
			return null;

		return _referential.getViewContainers().get(0);
	}

	public static ViewContainer get_headerContainer(Object p_object) {

		if (null == _header)
			return null;

		if (null == _header.getViewContainers() || _header.getViewContainers().isEmpty())
			return null;

		return _header.getViewContainers().get(0);
	}

	public static ViewContainer get_footerContainer(Object p_object) {

		if (null == _footer)
			return null;

		if (null == _footer.getViewContainers() || _footer.getViewContainers().isEmpty())
			return null;

		return _footer.getViewContainers().get(0);
	}

	public static ViewContainer get_errorContainer(Object p_object) {

		if (null == _error)
			return null;

		if (null == _error.getViewContainers() || _error.getViewContainers().isEmpty())
			return null;

		return _error.getViewContainers().get(0);
	}

	public static Boolean insideFormLayout(EObject p_object) {
		return _formLayout;
	}

	public static void openFormLayout(EObject p_object) {
		CinematicUtils._formLayout = true;
	}

	public static void closeFormLayout(EObject p_object) {
		CinematicUtils._formLayout = false;
	}

	public static int calcNbColsForVirtualLayout(EObject p_object) {
		int nbCols = 0;
		if (null == _virtualLayout)
			return 12;
		TreeIterator<EObject> it = _virtualLayout.eAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof Layout)
				if (((Layout) obj).getViewElement() != null)
					nbCols++;
//			if (obj instanceof AbstractViewElement) {
//				nbCols++;
//			}
		}
		return nbCols > 0 ? 12 / nbCols : 12;
	}

	public static Boolean isInsideVirtualHorizontalLayout(EObject p_object) {
		if (null == _virtualLayout)
			return false;
		return _virtualLayout.getDirection() == LayoutDirection.HORIZONTAL;
	}

	public static void openVirtualLayout(Layout _virtualLayout) {
		CinematicUtils._virtualLayout = _virtualLayout;
	}

	public static void closeVirtualLayout(EObject p_object) {
		CinematicUtils._virtualLayout = null;
	}

	/**
	 * Retourne l'ensemble des AbstractViewElement pour un ViewContainer, en
	 * éliminant les doublons par nom.
	 */
	public static Set<AbstractViewElement> viewElementForImports(ViewContainer vc) {
		Map<String, AbstractViewElement> map = new LinkedHashMap<>();
		collectElements(vc, map);
		return new LinkedHashSet<>(map.values());
	}

	private static void collectElements(ViewContainer vc, Map<String, AbstractViewElement> map) {
		for (AbstractViewElement e : vc.getOwnedElements()) {
			map.putIfAbsent(e.getWidget().getName(), e);
			if (e instanceof ViewContainer)
				collectElements((ViewContainer) e, map);
		}
	}
}
