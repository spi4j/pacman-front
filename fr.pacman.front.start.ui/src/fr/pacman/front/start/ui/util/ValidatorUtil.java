package fr.pacman.front.start.ui.util;

import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * 
 * @author MINARM.
 *
 */
public class ValidatorUtil {

	public static ValidatorUtil INSTANCE = new ValidatorUtil();

	private boolean _applicationNewOK;
	private boolean _applicationOK;
	private boolean _authorOK;
	private boolean _packageOK;

	/**
	 * Constructeur privé pour éviter l'instanciation de la classe.
	 */
	private ValidatorUtil() {
		// EMPTY.
	}

	/**
	 * Calcul de la validité des paramètres en fonction des données saisies.
	 * 
	 * @return boolean
	 */
	public boolean isValid() {

		return _applicationOK && _applicationNewOK && _packageOK && _authorOK;
	}

	/**
	 * Retourne le message à afficher à l'utilisateur en cas d'erreur de saisie.
	 * 
	 * @return String
	 */
	public String getMessage() {

		if (!_applicationNewOK)
			return "Le projet existe déjà dans l'espace de travail (ou sur le disque).";

		if (!_applicationOK)
			return "Le nom du projet n'est pas renseigné.";

		if (!_packageOK)
			return "Le package racine de l'application n'est pas renseigné.";

		if (!_authorOK)
			return "Le nom de l'auteur ou de l'organisme n'est pas renseigné.";

		return null;
	}

	/**
	 * Typologie du message à retourner (pour l'instant toujours INFO.).
	 * 
	 * @return
	 */
	public int getMessageType() {

		return IMessageProvider.INFORMATION;
	}

	public void setApplicationOK(boolean p_applicationOK) {
		_applicationOK = p_applicationOK;
	}

	public void setApplicationNewOk(boolean p_applicationNewOK) {
		_applicationNewOK = p_applicationNewOK;
	}

	public void setPackageOK(boolean p_packageOK) {
		_packageOK = p_packageOK;
	}

	public void setAuthorOK(boolean p_authorOK) {
		_authorOK = p_authorOK;
	}
}