package fr.pacman.front.core.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.obeonetwork.dsl.cinematic.CinematicRoot;
import org.obeonetwork.dsl.cinematic.Event;
import org.obeonetwork.dsl.cinematic.flow.Transition;
import org.obeonetwork.dsl.cinematic.flow.ViewState;
import org.obeonetwork.dsl.cinematic.view.AbstractViewElement;
import org.obeonetwork.dsl.cinematic.view.Layout;
import org.obeonetwork.dsl.cinematic.view.LayoutDirection;
import org.obeonetwork.dsl.cinematic.view.ViewContainer;
import org.obeonetwork.dsl.cinematic.view.ViewEvent;

public class CinematicUtils {

	private static List<Transition> _transitions;

	/**
	 * Conserve le temps de la génération les controleurs pour l'en-tete, le
	 * pied-de-page, le referentiel et l'index s'ils existent (sauf pour l'index qui
	 * est obligatoire).
	 * 
	 * @param p_root : le diagramme de cinématique root.
	 */
	public static boolean init(final CinematicRoot p_root) {
		_transitions = getAllTransitions(p_root);
		return false;
	}

	/**
	 * Retourne la liste des noms de transitions associées à un conteneur de vue
	 * {@link ViewContainer} dans le contexte de la génération des routes React.
	 * <p>
	 * Cette méthode parcourt l'ensemble des transitions définies dans le modèle
	 * cinématique et sélectionne celles qui :
	 * <ul>
	 * <li>ont pour cible un {@link ViewState} contenant le {@code ViewContainer}
	 * passé en paramètre,</li>
	 * <li>sont déclenchées par un événement de type {@code onClick},</li>
	 * <li>et possèdent un nom de transition valide.</li>
	 * </ul>
	 * Les transitions correspondantes sont retournées sous forme d'une liste de
	 * chaînes, correspondant à leurs noms, qui pourront ensuite être utilisées pour
	 * générer les routes dans le code React (fichier <code>page.jsx</code>).
	 * </p>
	 *
	 * @param p_view le {@link ViewContainer} pour lequel on souhaite récupérer les
	 *               routes React associées.
	 *
	 * @return une liste de noms de transitions correspondant aux routes React
	 *         accessibles depuis ce conteneur ; une liste vide si aucune transition
	 *         ne correspond.
	 *
	 * @see org.obeonetwork.dsl.cinematic.flow.Transition
	 * @see org.obeonetwork.dsl.cinematic.flow.ViewState
	 * @see org.obeonetwork.dsl.cinematic.view.ViewContainer
	 * @see org.obeonetwork.dsl.cinematic.view.ViewEvent
	 */
	public static List<String> get_urlsForReactRouter(final ViewContainer p_view) {
		return _transitions.stream()
				.filter(transition -> transition.getTo() instanceof ViewState viewState
						&& !viewState.getViewContainers().isEmpty() 
						&& viewState.getViewContainers().get(0) == p_view
						&& transition.getOn() != null
						&& transition.getOn().stream().anyMatch(
								event -> event instanceof ViewEvent viewEvent 
								&& isEligibleEventForRoute(viewEvent)))
				.map(Transition::getName).collect(Collectors.toList());
	}

	/**
	 * Vérifie si un événement de vue est éligible pour déclencher une navigation
	 * (ou "route").
	 * <p>
	 * Un événement est considéré comme éligible s'il correspond à un clic
	 * utilisateur ({@code onClick}) ou à une soumission de formulaire
	 * ({@code onSubmit}).
	 * </p>
	 *
	 * @param p_event l'événement de vue à analyser (ne doit pas être {@code null})
	 * @return {@code true} si le type de l'événement est {@code onClick} ou
	 *         {@code onSubmit}, {@code false} sinon
	 */
	private static boolean isEligibleEventForRoute(final ViewEvent p_event) {
		return p_event.getType() != null && ("onClick".equalsIgnoreCase(p_event.getType().getName())
				|| "onSubmit".equalsIgnoreCase(p_event.getType().getName()));
	}

	/**
	 * Recherche le nom de la transition associée à un événement donné dans le
	 * modèle cinématique.
	 * <p>
	 * Cette méthode parcourt l'ensemble des transitions disponibles et renvoie le
	 * nom de la première transition dont la liste d'événements
	 * {@link Transition#getOn()} contient l'événement spécifié en paramètre.
	 * </p>
	 * <p>
	 * Cette logique est utilisée pour déterminer la route React (ou le lien de
	 * navigation) correspondant à un événement déclencheur particulier, typiquement
	 * un {@link ViewEvent} de type {@code onClick}, dans le cadre de la génération
	 * d'une page React (page.jsx).
	 * </p>
	 *
	 * @param p_event l'événement {@link ViewEvent} pour lequel on souhaite
	 *                retrouver la transition associée.
	 *
	 * @return le nom de la transition correspondant à l'événement spécifié, ou la
	 *         chaîne {@code "#"} si aucune transition ne correspond.
	 *
	 * @see org.obeonetwork.dsl.cinematic.flow.Transition
	 * @see org.obeonetwork.dsl.cinematic.Event
	 * @see org.obeonetwork.dsl.cinematic.view.ViewEvent
	 */
	public static String get_urlForPageJsx(final ViewEvent p_event) {
		for (Transition transition : _transitions) {
			for (Event e : transition.getOn()) {
				if (e == p_event) {
					return transition.getName();
				}
			}
		}
		return "#";
	}

	/**
	 * Récupère toutes les transitions présentes dans un {@link CinematicRoot}.
	 * <p>
	 * Cette méthode parcourt l'intégralité du modèle EMF contenu dans
	 * {@code p_root} (y compris tous les enfants récursivement) et collecte tous
	 * les éléments de type {@link Transition}.
	 * </p>
	 *
	 * @param p_root la racine cinématique à partir de laquelle récupérer les
	 *               transitions ; ne doit pas être {@code null}
	 * @return une {@link List} contenant toutes les instances de {@link Transition}
	 *         présentes dans le modèle de {@code p_root}. Si aucune transition
	 *         n’est trouvée, la liste renvoyée sera vide.
	 */
	private static List<Transition> getAllTransitions(CinematicRoot p_root) {
		Iterable<EObject> allContents = () -> EcoreUtil.getAllContents(p_root, true);
		return StreamSupport.stream(allContents.spliterator(), false).filter(e -> e instanceof Transition)
				.map(e -> (Transition) e).collect(Collectors.toList());
	}

	/**
	 * Vérifie si un layout donné est contenu dans un layout de type "Form".
	 * <p>
	 * La méthode parcourt récursivement les conteneurs du layout fourni en
	 * remontant la hiérarchie EMF (via {@link EObject#eContainer()}) jusqu'à
	 * trouver un parent dont le widget associé porte le nom "Form".
	 * </p>
	 *
	 * @param p_layout le layout à tester ; peut être {@code null}
	 * @return {@code true} si le layout est contenu dans un layout de type "Form",
	 *         {@code false} sinon (y compris si {@code p_layout} ou son ViewElement
	 *         est {@code null})
	 */
	public static boolean isInsideFormLayout(Layout p_layout) {
		if (p_layout == null || p_layout.getViewElement() == null) {
			return false;
		}
		EObject container = p_layout.eContainer();
		while (container instanceof Layout) {
			Layout parentLayout = (Layout) container;
			if (parentLayout.getViewElement() != null) {
				if (parentLayout.getViewElement().getWidget() != null
						&& "Form".equalsIgnoreCase(parentLayout.getViewElement().getWidget().getName())) {
					return true;
				}
			}
			container = parentLayout.eContainer();
		}
		return false;
	}

	/**
	 * Vérifie si un layout est le premier élément d'un conteneur horizontal.
	 *
	 * @param p_layout le layout à tester
	 * @return {@code true} si le layout est le premier de son conteneur horizontal,
	 *         {@code false} sinon
	 */
	public static boolean isFirstLayoutOfHorizontalContainer(Layout p_layout) {
		return isLayoutOfHorizontalContainer(p_layout, true);
	}

	/**
	 * Vérifie si un layout est le dernier élément d'un conteneur horizontal.
	 *
	 * @param p_layout le layout à tester
	 * @return {@code true} si le layout est le dernier de son conteneur horizontal,
	 *         {@code false} sinon
	 */
	public static boolean isLastLayoutOfHorizontalContainer(Layout p_layout) {
		return isLayoutOfHorizontalContainer(p_layout, false);
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
	public static boolean isLayoutOfHorizontalContainer(Layout p_layout, boolean p_first) {
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
	 * Vérifie si la direction d'un layout est horizontale.
	 *
	 * @param p_layout le layout à tester
	 * @return {@code true} si le conteneur du layout est de type horizontal,
	 *         {@code false} sinon
	 */
	public static boolean isHorizontalLayout(Layout p_layout) {
		if (null == p_layout)
			return false;
		return p_layout.getDirection() == LayoutDirection.HORIZONTAL;
	}

	/**
	 * Vérifie si un layout appartient à un conteneur dont la direction est
	 * horizontale.
	 *
	 * @param p_layout le layout à tester
	 * @return {@code true} si le conteneur du layout est de type horizontal,
	 *         {@code false} sinon
	 */
	public static boolean isInsideHorizontalLayout(Layout p_layout) {
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
	public static Set<AbstractViewElement> get_viewElementForImports(ViewContainer vc) {
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
