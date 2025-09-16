package fr.pacman.front.cinematic.react.ui.generators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.Logger;

import fr.pacman.front.cinematic.react.main.GenServer;
import fr.pacman.front.cinematic.react.main.GenValidation;
import fr.pacman.front.cinematic.react.ui.plugin.Activator;
import fr.pacman.front.core.generator.PacmanGenerator;
import fr.pacman.front.core.ui.generator.PacmanUIGenerator;

/**
 * UI Generator for Service based on Cinematic model file.
 * <p>
 * Se reporter à la classe {@link PacmanUIGenerator} pour l'explication des
 * différentes méthodes.
 * 
 * @author MINARM
 */
public class CinematicReactUIGenerators extends PacmanUIGenerator {

	/**
	 * Constructeur.
	 * 
	 * @param p_selected la ressource sélectionnée (ici obligatoirement un fichier).
	 */
	public CinematicReactUIGenerators(final IFile p_selected) {
		super(p_selected);
	}

	@Override
	protected List<PacmanGenerator> getGenerators() {
		final List<PacmanGenerator> v_generators = new ArrayList<>();
		v_generators.add(new GenValidation());
		v_generators.add(new GenServer());
		return v_generators;
	}

	@Override
	protected String getPluginId() {
		return Activator.c_pluginId;
	}

	@Override
	protected Logger getLogger() {
		return Activator.getDefault().getPluginLogger();
	}

	@Override
	protected List<String> getIncompatibleOptions() {
		return null;
	}

	@Override
	protected boolean doPostTreatments() {
		return true;
	}

	@Override
	protected boolean hasView() {
		return false;
	}
}
