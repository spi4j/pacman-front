package fr.pacman.front.core.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
	private static ViewState _referential;

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
					}
					if (_footer != null && _header != null && _referential != null && _root != null)
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

	/**
	 * Vérifie si un layout est le premier élément d'un conteneur horizontal.
	 *
	 * @param p_layout le layout à tester
	 * @return {@code true} si le layout est le premier de son conteneur horizontal,
	 *         {@code false} sinon
	 */
	public static boolean isFirstLayoutOfHorizontalContainer(Layout p_layout) {
		return checkLayoutOfHorizontalContainer(p_layout, true);
	}

	/**
	 * Vérifie si un layout est le dernier élément d'un conteneur horizontal.
	 *
	 * @param p_layout le layout à tester
	 * @return {@code true} si le layout est le dernier de son conteneur horizontal,
	 *         {@code false} sinon
	 */
	public static boolean isLastLayoutOfHorizontalContainer(Layout p_layout) {
		return checkLayoutOfHorizontalContainer(p_layout, false);
	}

	/**
	 * Vérifie si un layout correspond au premier ou au dernier élément d'un
	 * conteneur horizontal.
	 * <p>
	 * La position attendue dépend de la valeur du paramètre {@code p_first}.
	 * </p>
	 *
	 * @param p_layout le layout à tester
	 * @param p_first  si {@code true}, vérifie si le layout est le premier élément,
	 *                 sinon vérifie s'il est le dernier
	 * @return {@code true} si le layout correspond à la position attendue (premier
	 *         ou dernier dans le conteneur horizontal), {@code false} sinon
	 */
	public static boolean checkLayoutOfHorizontalContainer(Layout p_layout, boolean p_first) {
		if (p_layout == null) {
			return false;
		}
		EObject container = p_layout.eContainer();
		if (p_layout.eContainer() instanceof Layout) {
			int length = (p_first) ? 0 : ((Layout) container).getOwnedLayouts().size() - 1;
			if (((Layout) container).getOwnedLayouts().get(length).getViewElement() == p_layout.getViewElement())
				return true;
		}
		return false;
	}

	/**
	 * Vérifie si un layout appartient à un conteneur dont la direction est
	 * horizontale.
	 *
	 * @param p_layout le layout à tester
	 * @return {@code true} si le conteneur du layout est de type horizontal,
	 *         {@code false} sinon
	 */
	public static boolean insideHorizontalLayout(Layout p_layout) {
		if (null == p_layout)
			return false;

		EObject container = p_layout.eContainer();
		if (container instanceof Layout)
			return ((Layout) container).getDirection() == LayoutDirection.HORIZONTAL;
		return false;
	}

	/**
	 * Récupère l'ensemble des éléments de vue ({@link AbstractViewElement}) d'un
	 * conteneur de vues donné, en filtrant et dédupliquant les éléments en fonction
	 * de l'implémentation de leur widget.
	 * <p>
	 * Cette méthode parcourt récursivement la hiérarchie des conteneurs de vues à
	 * partir du conteneur fourni en paramètre, collecte tous les éléments de vue
	 * dont le widget possède une implémentation non nulle et non vide, puis
	 * retourne un ensemble garantissant l'unicité et l'ordre d'insertion.
	 * </p>
	 *
	 * @param vc le conteneur de vues racine à analyser
	 * @return un ensemble d'éléments de vue uniques, collectés à partir du
	 *         conteneur et de ses sous-conteneurs
	 */
	public static Set<AbstractViewElement> viewElementForImports(ViewContainer vc) {
		Map<String, AbstractViewElement> map = new LinkedHashMap<>();
		collectElements(vc, map);
		return new LinkedHashSet<>(map.values());
	}

	/**
	 * Parcourt récursivement un conteneur de vues et ajoute dans la map les
	 * éléments de vue possédant une implémentation de widget valide.
	 * <p>
	 * L'unicité est assurée par la clé de la map, qui correspond à la chaîne
	 * d'implémentation du widget associé à l'élément.
	 * </p>
	 *
	 * @param vc  le conteneur de vues à parcourir
	 * @param map la map accumulant les éléments, indexés par l'implémentation du
	 *            widget
	 */
	private static void collectElements(ViewContainer vc, Map<String, AbstractViewElement> map) {
		for (AbstractViewElement e : vc.getOwnedElements()) {
			if (e.getWidget().getImplementation() != null && !e.getWidget().getImplementation().isEmpty())
				map.putIfAbsent(e.getWidget().getImplementation(), e);
			if (e instanceof ViewContainer)
				collectElements((ViewContainer) e, map);
		}
	}
}
