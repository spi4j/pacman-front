package fr.pacman.front.core.service;

import org.eclipse.emf.ecore.EObject;
import org.obeonetwork.dsl.cinematic.view.AbstractViewElement;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.MetaData;
import org.obeonetwork.dsl.environment.MetaDataContainer;
import org.obeonetwork.dsl.environment.ObeoDSMObject;

import fr.pacman.front.core.enumeration.AnnotationEnum;

/**
 * Classe utilitaire pour la récupération des annotations dans le modèle. Les
 * annotations sont les métadonnées utilisateur éventuellement présentes dans
 * l'onglet 'METADATAS' de chaque {@link EObject} défini dans le modèle.
 * 
 * @author MINARM
 */
public final class AnnotationUtils {

	/**
	 * Constructeur privé.
	 */
	private AnnotationUtils() {
		// RAS
	}

	/**
	 * Vérifie si une annotation existe.
	 * 
	 * @param p_obj        l'objet annoté
	 * @param p_annotation l'annotation recherchée
	 * @return true si l'annotation existe, false sinon
	 */
	public static boolean is_annotationExistsRecursively(final ObeoDSMObject p_obj, final String p_annotation) {
		return isAnnotationExists(p_obj, p_annotation, true);
	}

	/**
	 * Vérifie si une annotation existe.
	 * 
	 * @param p_obj        l'objet annoté
	 * @param p_annotation l'annotation recherchée
	 * @return true si l'annotation existe, false sinon
	 */
	public static boolean is_annotationExistsRecursively(final ObeoDSMObject p_obj, final AnnotationEnum p_annotation) {
		return isAnnotationExists(p_obj, p_annotation.get_annotationName(), true);
	}

	/**
	 * Vérifie si une annotation existe.
	 * 
	 * @param p_obj        l'objet annoté
	 * @param p_annotation l'annotation recherchée
	 * @return true si l'annotation existe, false sinon
	 */

	public static boolean is_annotationExists(final ObeoDSMObject p_obj, final String p_annotation) {
		return isAnnotationExists(p_obj, p_annotation, false);
	}

	/**
	 * Vérifie si une annotation existe.
	 * 
	 * @param p_obj        l'objet annoté
	 * @param p_annotation l'annotation recherchée
	 * @return true si l'annotation existe, false sinon
	 */

	public static boolean is_annotationExists(final ObeoDSMObject p_obj, final AnnotationEnum p_annotation) {
		return isAnnotationExists(p_obj, p_annotation.get_annotationName(), false);
	}

	/**
	 * Retourne la valeur d'une annotation, en remontant récursivement dans le
	 * modèle à partir d'un objet p_obj.
	 * 
	 * @param p_obj        l'objet source de la recherche.
	 * @param p_annotation le titre de l'annotation recherchée.
	 * @return la valeur de l'annotation trouvée, sinon null
	 */
	public static String get_annotationBodyRecursively(final ObeoDSMObject p_obj, final String p_annotation) {
		return getAnnotationBody(p_obj, p_annotation, true);
	}

	/**
	 * Retourne la valeur d'une annotation, attachée à un objet p_obj.
	 * 
	 * @param p_obj        l'objet contenant l'annotation
	 * @param p_annotation le titre de l'annotation recherchée
	 * @return la valeur de l'annotation trouvée, sinon null.
	 */
	public static String get_annotationBody(final ObeoDSMObject p_obj, final String p_annotation) {
		return getAnnotationBody(p_obj, p_annotation, false);
	}

	/**
	 * Retourne la valeur d'une annotation, attachée à un objet p_obj.
	 * 
	 * @param p_obj        l'objet contenant l'annotation
	 * @param p_annotation l'annotation recherchée
	 * @return la valeur de l'annotation trouvée, sinon null.
	 */
	private static String get_annotationBody(final ObeoDSMObject p_obj, final AnnotationEnum p_annotation) {
		return getAnnotationBody(p_obj, p_annotation.get_annotationName(), false);
	}

	/**
	 * Retourne la valeur d'une annotation, récursivement ou non.
	 * 
	 * @param p_obj        l'objet source de la recherche
	 * @param p_annotation le titre de l'annotation recherchée
	 * @param p_recursive  true si la recherche doit être récursive, false si la
	 *                     recherche doit se faire seulement sur l'objet p_obj
	 * @return la valeur de l'annotation trouvée, sinon null.
	 */
	private static String getAnnotationBody(final ObeoDSMObject p_obj, final String p_annotation,
			final boolean p_recursive) {
		if (p_obj == null)
			return null;

		final MetaDataContainer metadataContainer = p_obj.getMetadatas();
		final String body = get_annotationBody(metadataContainer, p_annotation);
		if (body != null)
			return body;

		if (p_recursive) {
			final EObject containerObject = p_obj.eContainer();
			if (containerObject instanceof ObeoDSMObject) {
				return getAnnotationBody((ObeoDSMObject) containerObject, p_annotation, p_recursive);
			}
		}
		return null;
	}

	/**
	 * Retourne la valeur d'une annotation dans un MetadataContainer.
	 * 
	 * @param p_metadataContainer le container de métadonnées
	 * @param p_annotation        l'annotation recherchée
	 * @return la valeur de l'annotation trouvée, sinon null.
	 */
	public static String get_annotationBody(final MetaDataContainer p_metadataContainer,
			final AnnotationEnum p_annotation) {
		return get_annotationBody(p_metadataContainer, p_annotation.get_annotationName());
	}

	/**
	 * Retourne la valeur d'une annotation dans un MetadataContainer.
	 * 
	 * @param p_metadataContainer le container de métadonnées
	 * @param p_annotation        l'annotation recherchée
	 * @return la valeur de l'annotation trouvée, sinon null.
	 */
	public static String get_annotationBody(final MetaDataContainer p_metadataContainer, final String p_annotation) {
		if (p_metadataContainer != null) {
			for (final MetaData m : p_metadataContainer.getMetadatas()) {
				if (m instanceof Annotation) {
					final Annotation anno = (Annotation) m;
					if (anno != null && anno.getTitle() != null
							&& anno.getTitle().trim().equalsIgnoreCase(p_annotation.trim()) && anno.getBody() != null
							&& !anno.getBody().trim().isEmpty()) {
						return anno.getBody();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Vérifie si une annotation existe.
	 * 
	 * @param p_obj        l'objet annoté
	 * @param p_annotation l'annotation recherchée
	 * @param p_recursive  true pour chercher récursivement parmi les parents
	 * @return true si l'annotation existe, false sinon
	 */
	private static boolean isAnnotationExists(final ObeoDSMObject p_obj, final String p_annotation,
			final boolean p_recursive) {
		if (p_obj == null) {
			return false;
		}
		final MetaDataContainer metadataContainer = p_obj.getMetadatas();
		if (metadataContainer != null) {
			for (final MetaData m : metadataContainer.getMetadatas()) {
				if (m instanceof Annotation) {
					final Annotation anno = (Annotation) m;
					if (anno != null && anno.getTitle() != null
							&& anno.getTitle().trim().equalsIgnoreCase(p_annotation.trim())) {
						return true;
					}
				}
			}
		}
		if (p_recursive) {
			final EObject containerObject = p_obj.eContainer();
			if (containerObject instanceof ObeoDSMObject) {
				return isAnnotationExists((ObeoDSMObject) containerObject, p_annotation, p_recursive);
			}
		}
		return false;
	}

	public static boolean has_metaPasswordDescription(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.PASSWORD_DESCRIPTION);
	}

	public static String get_metaPasswordDescription(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.PASSWORD_DESCRIPTION);
	}
	
	public static boolean has_metaTextInfo(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_INFO);
	}

	public static String get_metaTextInfo(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_INFO);
	}
	
	public static boolean has_metaTextSuccess(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_SUCCESS);
	}

	public static String get_metaTextSuccess(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_SUCCESS);
	}
	
	public static boolean has_metaTextPlaceHolder(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_PLACEHOLDER);
	}

	public static String get_metaTextPlaceHolder(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_PLACEHOLDER);
	}
	
	public static boolean has_metaTextError(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_ERROR);
	}

	public static String get_metaTextError(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_ERROR);
	}
	
	public static boolean has_metaRegexPattern(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_REGEX);
	}

	public static String get_metaRegexPattern(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_REGEX);
	}
	
	public static boolean has_metaIcon(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_ICON);
	}

	public static String get_metaIcon(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_ICON);
	}
	
	public static boolean has_metaButtonAddOn(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_BUTTON_ADDON);
	}

	public static String get_metaButtonAddOn(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_BUTTON_ADDON);
	}
	
	public static boolean has_metaButtonAction(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_BUTTON_ACTION);
	}

	public static String get_metaButtonAction(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_BUTTON_ACTION);
	}
	
	public static boolean has_metaMaxlength(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_MAXLENGTH);
	}

	public static String get_metaMaxlength(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_MAXLENGTH);
	}
	
	public static boolean has_metaTextarea(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_TEXTAREA);
	}

	public static String get_metaTextArea(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_TEXTAREA);
	}
	
	public static boolean has_metaMsgGroup(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_MSG_GROUP);
	}

	public static String get_metaMsgGroup(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_MSG_GROUP);
	}
	
	public static boolean has_metaHint(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_HINT);
	}

	public static String get_metaHint(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_HINT);
	}
	
	public static boolean has_metaValue(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_VALUE);
	}

	public static String get_metaValue(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_VALUE);
	}
	
	public static boolean has_metaRequired(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_REQUIRED);
	}

	public static String get_metaRequired(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_REQUIRED);
	}
	
	public static boolean has_metaValues(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_VALUES);
	}

	public static String get_metaValues(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_VALUES);
	}
	
	public static boolean has_metaHideMinMax(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_HIDE_MIN_MAX);
	}

	public static String get_metaHideMinMax(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_HIDE_MIN_MAX);
	}
	
	public static boolean has_metaMax(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_VALUE_MAX);
	}

	public static String get_metaMax(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_VALUE_MAX);
	}
	
	public static boolean has_metaMin(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_VALUE_MIN);
	}

	public static String get_metaMin(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_VALUE_MIN);
	}
	
	public static boolean has_metaSteps(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_STEPS);
	}

	public static String get_metaSteps(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_STEPS);
	}
	
	public static boolean has_metaDouble(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_DOUBLE);
	}

	public static String get_metaDouble(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_DOUBLE);
	}
	
	public static boolean has_metaPrefix(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_PREFIX);
	}

	public static String get_metaPrefix(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_PREFIX);
	}
	
	public static boolean has_metaSuffix(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_SUFFIX);
	}

	public static String get_metaSuffix(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_SUFFIX);
	}
	
	public static boolean has_metaImg(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_IMG);
	}

	public static String get_metaImg(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_IMG);
	}
	
	public static boolean has_metaLink(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_LINK);
	}

	public static String get_metaLink(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_LINK);
	}
	
	public static boolean has_metaOrientation(final AbstractViewElement p_object) {
		return is_annotationExists(p_object, AnnotationEnum.INPUT_WITH_ORIENTATION);
	}

	public static String get_metaOrientation(final AbstractViewElement p_object) {
		return get_annotationBody(p_object, AnnotationEnum.INPUT_WITH_ORIENTATION);
	}
}
