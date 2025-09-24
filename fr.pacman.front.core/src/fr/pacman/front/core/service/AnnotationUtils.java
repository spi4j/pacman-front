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
}
