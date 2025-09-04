package fr.pacman.front.cinematic.react.ui.generators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.Logger;

import fr.pacman.front.core.generator.PacmanGenerator;
import fr.pacman.front.cinematic.react.ui.plugin.Activator;
import fr.pacman.front.cinematic.react.main.GenValidation;
import fr.pacman.front.core.ui.generator.PacmanUIGenerator;
import fr.pacman.front.core.ui.generator.PacmanUIGeneratorHelper;

/**
 * UI Generateur pour la validation des modèles.
 * 
 * Se reporter à la classe {@link PacmanUIGenerator} pour l'explication des
 * différentes méthodes.
 * 
 * @author MINARM
 */
public class CinematicValidationUIGenerators extends PacmanUIGenerator {

	/**
	 * Constructeur.
	 * 
	 * @param p_selected la ressource sélectionnée (ici obligatoirement un fichier).
	 */
	public CinematicValidationUIGenerators(final IFile p_selected) {
		super(p_selected);
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
		PacmanUIGeneratorHelper
				.displayPopUpInfo("Le fichier de modélisation pour la couche de cinématique est valide.");
		return false;
	}

	@Override
	protected List<PacmanGenerator> getGenerators() {
		final List<PacmanGenerator> v_generators = new ArrayList<>();
		v_generators.add(new GenValidation());
		return v_generators;
	}

	@Override
	protected boolean hasView() {
		return false;
	}
}
