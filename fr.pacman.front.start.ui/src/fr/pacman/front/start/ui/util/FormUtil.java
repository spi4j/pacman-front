package fr.pacman.front.start.ui.util;

import java.io.File;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Classe utilitaire pour la vérification et la validation des différents champs
 * de saisie dans le cadre du wizard de création d'un projet Cali.
 * 
 * @author MINARM
 */
public class FormUtil {

	private static final String c_patternAlphaNumeric = "[a-zA-Z0-9[\b]]";
	private static final String c_patternProjectKey = "[a-zA-Z0-9[\b\\_\\-]]";
	private static final String c_patternPackageKey = "[a-z0-9[\b\\.]]";
	private static final String c_patternPackageName = "^[a-z]{2,3}(\\.([a-z0-9\\_]){1,}){1,}";

	/**
	 * Constructeur privé pour éviter l'instanciation de la classe.
	 */
	private FormUtil() {
		// EMPTY.
	}

	/**
	 * Vérification de la validité du caractère saisi pour le nommage d'un projet.
	 * 
	 * @param p_c le caractère à vérifier.
	 * @return la valeur 'true' si le caractère est validé pour la saisie.
	 */
	public static boolean checkKeyForProjectName(final Character p_c) {
		return Pattern.matches(c_patternProjectKey, p_c.toString());
	}

	/**
	 * Vérification de la validité du caractère saisi pour le nommage d'un package.
	 * 
	 * @param p_c le caractère à vérifier.
	 * @return la valeur 'true' si le caractère est validé pour la saisie.
	 */
	public static boolean checkKeyForPackageName(final Character p_c) {
		return Pattern.matches(c_patternPackageKey, p_c.toString());
	}

	/**
	 * Verification de la validite de la chaine saisie pour le nommage d'un package.
	 * 
	 * @param p_s la chaine de caractères à vérifier.
	 * @return la valeur 'true' si la chaine de caractères est validée pour la
	 *         saisie.
	 */
	public static boolean checkValueForPackageName(final String p_s) {
		return Pattern.matches(c_patternPackageName, p_s);
	}

	/**
	 * Verification de la validite du caractere saisi une valeur alphanumerique.
	 * 
	 * @param p_c le caractère à vérifier.
	 * @return la valeur 'true' si le caractère est validé pour la saisie.
	 */
	public static boolean checkKeyForAlphaNumericValue(final Character p_c) {
		return Pattern.matches(c_patternAlphaNumeric, p_c.toString());
	}

	/**
	 * Verifie si le projet existe deja. On ne se base pas sur la fonction exists()
	 * du IProject qui n'est pas complete mais directement sur la presence du
	 * fichier dans le repertoire. Cela permet ainsi de detecter les projets effaces
	 * mais toujour presents sur le disque..
	 * 
	 * @param p_appliNames
	 * @return
	 */
	public static boolean checkForNewProject(final String p_appliName) {

		IWorkspaceRoot v_root = ResourcesPlugin.getWorkspace().getRoot();

		if (null == p_appliName || p_appliName.isEmpty())
			return true;

		File v = new File(v_root.getLocationURI().getPath() + File.separator + p_appliName);

		if (v.exists())
			return false;

		return true;
	}
}
