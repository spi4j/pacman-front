package fr.pacman.front.start.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import fr.pacman.front.start.ui.activator.Activator;
import fr.pacman.front.start.ui.util.FormUtil;
import fr.pacman.front.start.ui.util.ValidatorUtil;

/**
 * 
 * @author @MINARM
 */
public class PropertiesWizardStartPage extends PropertiesWizardPage<Control> {

	/**
	 * Liste des paramètres. Pour l'instant on traite tout sous format string
	 * (erreur ou pas ?)
	 */
	private String _projectName = "";
	private String _packageName = "";
	private String _authorName = "";
	private String _typeFramework = "";

	// Pas en String car ne vas pas dans les propriétés.
	private boolean _displayReadme;

	/**
	 * Constructeur avec la définition de l'en-tête pour le panneau global de
	 * saisie.
	 */
	protected PropertiesWizardStartPage() {
		super("Propriétés du projet Cali");
		setTitle("Nouveau projet Cali (FrontEnd)");
		setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.c_pluginId, Activator.c_pluginLogo));
		setDescription("Saisir les différents paramètres pour la création du projet.");
	}

	/**
	 * 
	 * @param p_defaultSize
	 */
	void resize(final boolean p_defaultSize) {
		if (p_defaultSize)
			getShell().setSize(getShell().computeSize(720, 300));
	}

	/**
	 * Initialisation et positionnement de l'ensemble des éléments de saisie sur la
	 * page.
	 */
	@Override
	public void createControl(final Composite p_parent) {
		final Composite container = new Composite(p_parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		Group group1 = addGroup(container, "Identification",
				"Ensemble des informations de base permettant l'identification du projet.");

		Group group2 = addGroup(container, "Options",
				"Ensemble des informations structurantes pour la création du projet.");

		Group group3 = addGroup(container, "Post création",
				"Liste des opéarations à effectuer suite à la création du projet");

		registerWidget("grp_project1", group1);
		registerWidget("grp_project2", group2);
		registerWidget("txt_project", addTextApplication(group1));
		registerWidget("txt_package", addTextPackage(group1));
		registerWidget("txt_author", addTextAuthorName(group1));
		registerWidget("cb_framework", addComboFramework(group2));
		registerWidget("ck_displayReadme", addCheckBoxReadme(group3));

		setControl(container);
		setPageComplete(false);
		initWithDefault();
		resize(true);
	}

	/**
	 * Initialisation des données par défaut pour l'ensemble des valeurs de retour.
	 * Désactivation de certains composants pour l'arrivée sur le formulaire de
	 * création.
	 */
	private void initWithDefault() {
		// _typeFramework = "react";
		_displayReadme = true;
		_typeFramework = "react";
	}

	/**
	 * Champ de saisie pour le nom de l'application.
	 * 
	 * @param p_parent le composite parent sur lequel accrocher le composant.
	 */
	private Text addTextApplication(final Composite p_parent) {
		final Text txt = addText(p_parent, "Nom", "",
				"Nom du projet avec lequel la structure de l'application sera générée.");
		txt.setFocus();
		txt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(final KeyEvent p_e) {
				_projectName = txt.getText();
				completePackageName((Text) getWidget("txt_package"), txt);
				_packageName = ((Text) getWidget("txt_package")).getText();
				computeValidity();
			}

			@Override
			public void keyPressed(final KeyEvent p_e) {
				if (!FormUtil.checkKeyForProjectName(p_e.character)) {
					p_e.doit = false;
				}
			}
		});
		return txt;
	}

	/**
	 * Champ de saisie pour le package racine de l'application (tous les packages
	 * générés commenceront avec le contenu de cette saisie).
	 * 
	 * @param p_parent le composite parent sur lequel accrocher le composant.
	 */
	private Text addTextPackage(final Composite p_parent) {
		Text txt = addText(p_parent, "Package racine     ", "fr.", "Package racine du projet sous lequel"
				+ " positionner l'ensemble des classes et des autres sous-packages.");
		txt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(final KeyEvent p_e) {
				_packageName = txt.getText();
				computeValidity();
			}

			@Override
			public void keyPressed(final KeyEvent p_e) {
				if (!FormUtil.checkKeyForPackageName(p_e.character)) {
					p_e.doit = false;
				}
			}
		});
		return txt;
	}

	/**
	 * Champ de saisie pour le nom par défaut du ou des auteur(s) de l'application.
	 * 
	 * @param p_parent le composite parent sur lequel accrocher le composant.
	 */
	private Text addTextAuthorName(final Composite p_parent) {
		final Text txt = addText(p_parent, "Auteur(s)", "",
				"Auteur(s) ou organisme(s) à afficher dans l'ensemble des commentaires."
						+ "\nCes informations ne seront visibles que dans la partie infrastrucuture.");
		txt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(final KeyEvent p_e) {
				_authorName = txt.getText();
				computeValidity();
			}

			@Override
			public void keyPressed(final KeyEvent p_e) {
				// RAS.
			}
		});
		return txt;
	}

	/**
	 * Champ de saisie pour le choix du type de framework à utiliser.
	 * 
	 * @param p_parent le composite parent sur lequel accrocher le composant.
	 * @return le choix du type de framework.
	 */
	private Combo addComboFramework(final Composite p_parent) {
		Combo cbx = addComboBox(p_parent, "Framework",
				"Le framework à utiliser pour la génération des classes issues de la modélisation.",
				new String[] { "React" }, 0);

		cbx.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent p_e) {
				_typeFramework = (cbx.getItem(cbx.getSelectionIndex()).replaceAll(" +", "")).toLowerCase();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent p_e) {
				widgetSelected(p_e);
			}
		});
		return cbx;
	}

	/**
	 * 
	 * @param p_parent le composite parent sur lequel accrocher le composant.
	 * @return
	 */
	private Button addCheckBoxReadme(final Composite p_parent) {
		Button cbx = addCheckBox(p_parent, "Affichage automatique du fichier lisezmoi",
				"Lance automatiquement le navigateur pour l'affichage du fichier "
						+ " lisezmoi décrivant rapidement le projet généré.");

		cbx.setSelection(true);
		cbx.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				_displayReadme = cbx.getSelection();
			}
		});
		return cbx;
	}

	/**
	 * Construction automatique d'un nom de package a partir du nom de projet.
	 * 
	 * @param p_packageName
	 * @param p_projectName
	 */
	private void completePackageName(final Text p_packageName, final Text p_projectName) {
		p_packageName.setText(p_packageName.getText().substring(0, p_packageName.getText().indexOf(".") + 1)
				.concat(p_projectName.getText().toLowerCase().replace("-", "_")));
	}

	/**
	 * Vérification globale de la saisie.
	 */
	private void computeValidity() {

		ValidatorUtil.INSTANCE.setPackageOK(!_packageName.isEmpty());
		ValidatorUtil.INSTANCE.setApplicationNewOk(FormUtil.checkForNewProject(_projectName));
		ValidatorUtil.INSTANCE.setApplicationOK(null != _projectName && !_projectName.isEmpty());
		ValidatorUtil.INSTANCE.setAuthorOK(null != _authorName && !_authorName.isEmpty());
		setMessage(ValidatorUtil.INSTANCE.getMessage(), ValidatorUtil.INSTANCE.getMessageType());
		setPageComplete(ValidatorUtil.INSTANCE.isValid());
	}

	/**
	 * Retourne le nom pour le projet qui sera concaténé avec les différents
	 * suffixes en fonction des sous-projets à créer.
	 * 
	 * @return le nom du projet.
	 */
	public String getProjectName() {
		return _projectName;
	}

	/**
	 * Retourne le libellé du package racine pour le projet.
	 * 
	 * @return le libellé du package.
	 */
	public String getPackageName() {
		return _packageName;
	}

	/**
	 * Retourne le nom de l'auteur pour le projet.
	 * 
	 * @return le nom de l'auteur pour le projet.
	 */
	public String getAuthorName() {
		return _authorName;
	}

	/**
	 * Retourne le type de framework à utiliser pour le projet.
	 * 
	 * @return le type de framework.
	 */
	public String getTypeFramework() {
		return _typeFramework;
	}

	/**
	 * 
	 * @return
	 */
	public boolean getDisplayReadme() {
		return _displayReadme;
	}
}
