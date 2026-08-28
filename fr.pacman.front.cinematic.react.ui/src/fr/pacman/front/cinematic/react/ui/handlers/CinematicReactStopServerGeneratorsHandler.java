package fr.pacman.front.cinematic.react.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import fr.pacman.front.cinematic.react.util.ReactServerManager;

/**
 * Handler Eclipse permettant d'arrêter le serveur de développement React
 * associé au projet Pacman.
 * <p>
 * L'arrêt du serveur est délégué au {@link ReactServerManager}, qui conserve la
 * référence du processus lancé lors du démarrage du serveur et assure son arrêt
 * ainsi que celui des éventuels processus enfants.
 * </p>
 * 
 * @author MINARM
 */
public class CinematicReactStopServerGeneratorsHandler extends AbstractHandler {

	/**
	 * Exécute le handler et demande l'arrêt du serveur de développement React.
	 * 
	 * @param p_event événement d'exécution du handler fourni par Eclipse
	 * @return {@code null}, le handler ne produisant aucun résultat particulier
	 * @throws ExecutionException déclaré conformément au contrat de la méthode
	 *                            {@link AbstractHandler#execute(ExecutionEvent)}
	 */
	@Override
	public Object execute(ExecutionEvent p_event) throws ExecutionException {
		ReactServerManager.getInstance().stop();
		return null;
	}
}