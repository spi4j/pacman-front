package fr.pacman.front.cinematic.react.ui.handlers;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.handlers.HandlerUtil;

import fr.pacman.front.cinematic.react.util.ReactServerManager;

/**
 * Handler Eclipse permettant de démarrer le serveur de développement React
 * associé à un projet Pacman.
 * <p>
 * Le handler est déclenché à partir de la sélection d'un fichier
 * {@code .cinematic}. Le projet Eclipse contenant ce fichier correspond au
 * projet de modélisation suffixé par {@code -model}.
 * </p>
 * 
 * <p>
 * Le répertoire du projet serveur React est déduit automatiquement à partir du
 * répertoire du projet de modélisation en remplaçant le suffixe {@code -model}
 * par {@code -server}.
 * </p>
 * 
 * <p>
 * Le serveur est ensuite démarré par l'intermédiaire du
 * {@link ReactServerManager}.
 * </p>
 * 
 * @author MINARM
 */
public class CinematicReactStartServerGeneratorsHandler extends AbstractHandler {

	/**
	 * Exécute le handler à partir de la sélection courante dans Eclipse.
	 * <p>
	 * Chaque élément de la sélection est parcouru. Les éléments correspondant à un
	 * {@link IFile} sont utilisés pour identifier le projet Eclipse associé et
	 * démarrer le serveur React correspondant.
	 * </p>
	 * 
	 * @param p_event événement d'exécution du handler fourni par Eclipse
	 * @return {@code null}, le handler ne produisant aucun résultat particulier
	 * @throws ExecutionException si le serveur React ne peut pas être démarré
	 */
	@Override
	public Object execute(ExecutionEvent p_event) throws ExecutionException {
		final Iterator<?> v_iterator = HandlerUtil.getCurrentStructuredSelection(p_event).iterator();
		while (v_iterator.hasNext()) {
			final Object v_selected = v_iterator.next();
			if (v_selected instanceof IFile) {
				startServer((IFile) v_selected);
			}
		}
		return null;
	}

	/**
	 * Démarre le serveur React associé au fichier sélectionné.
	 * <p>
	 * Le fichier {@code .cinematic} permet d'identifier le projet Eclipse de
	 * modélisation. Le répertoire physique de ce projet est récupéré, puis le nom
	 * du projet est transformé afin d'obtenir le répertoire du projet serveur React
	 * associé.
	 * </p>
	 * 
	 * <p>
	 * Par convention, un projet de modélisation nommé par exemple
	 * {@code ObeoTsFront-model} est associé au projet serveur
	 * {@code ObeoTsFront-server}.
	 * </p>
	 * 
	 * <p>
	 * Le serveur est ensuite démarré par {@link ReactServerManager#getInstance()}.
	 * </p>
	 * 
	 * @param file fichier sélectionné dans Eclipse, généralement le fichier
	 *             {@code .cinematic} @throws ExecutionException si une erreur
	 *             survient lors du démarrage du serveur React
	 */
	private void startServer(IFile file) throws ExecutionException {
		try {
			final File modelDirectory = file.getProject().getLocation().toFile();
			final File serverDirectory = new File(modelDirectory.getParentFile(),
					modelDirectory.getName().replace("-model", "-server"));
			ReactServerManager.getInstance().start(serverDirectory);
		} catch (IOException e) {
			throw new ExecutionException("Impossible de démarrer le serveur React.", e);
		}
	}
}
