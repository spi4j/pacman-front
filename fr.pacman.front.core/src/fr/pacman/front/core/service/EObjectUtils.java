package fr.pacman.front.core.service;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.obeonetwork.dsl.environment.ObeoDSMObject;

public class EObjectUtils {

	/**
	 * Retourne la racine du modèle à partir d'un élément du modèle.
	 * 
	 * @param p_obj un objet de type {@link EObject}.
	 * @return la racine du modèle
	 */
	public static EObject get_root(final EObject p_obj) {
		return EcoreUtil.getRootContainer(p_obj);
	}

	/**
	 * Vérifie si un objet a un commentaire.
	 * 
	 * @param p_obj l'objet à vérifier.
	 * @return la valeur 'true' si un commentaire a été saisi au niveau de l'objet,
	 *         sinon, retourne la valeur 'false'.
	 */
	public static boolean has_comment(final ObeoDSMObject p_obj) {
		if (p_obj.getDescription() != null && !p_obj.getDescription().trim().isEmpty())
			return true;
		return false;
	}
}
