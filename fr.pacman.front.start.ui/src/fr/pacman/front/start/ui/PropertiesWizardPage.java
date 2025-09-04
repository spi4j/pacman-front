package fr.pacman.front.start.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Classe abstraite déstinée à faciliter la création du/des formulaire(s) pour
 * la création d'un nouveau projet Pacman. Définit des composants SWT plus
 * facilement pilotables pour le dévelopeur.
 * 
 * @author MINARM
 *
 * @param <T>
 */
abstract class PropertiesWizardPage<T extends Control> extends WizardPage {

	private static final String c_labelOffset = "     ";

	/**
	 * La liste de l'ensemble des composants de saisie à gérer.
	 */
	private Map<String, T> _lstWidgets = new HashMap<>();

	/**
	 * La liste des composants spécifiques à 'disposer' expressement (sinon message
	 * d'erreur dans la console, voir plus tard si solution plus élégante).
	 */
	private List<Resource> _lstResourcesToDispose = new ArrayList<>();

	/**
	 * Constructeur.
	 * 
	 * @param pageName le nom à afficher dans l'en-tête de la page.
	 */
	PropertiesWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * 
	 * @param p_resource
	 */
	void registerResourceToDispose(final Resource p_resource) {
		_lstResourcesToDispose.add(p_resource);
	}

	/**
	 * Demande d'enregistrement du composant de saisie.
	 * 
	 * @param p_key    la clé unique, identifiant du composant
	 * @param p_widget le composant
	 */
	void registerWidget(final String p_key, T p_widget) {
		_lstWidgets.put(p_key, p_widget);
	}

	/**
	 * Retourne le composant de saisie à partir de son identifiant unique.
	 * 
	 * @param p_key la clé unique pour le composant de saisie.
	 * @return le composant de saisie.
	 */
	T getWidget(final String p_key) {
		return _lstWidgets.get(p_key);
	}

	/**
	 * Création d'un conteneur de type 'group' avec une légende.
	 * 
	 * @param p_parent  le conteneur parent.
	 * @param p_title   le titre à afficher pour le groupement.
	 * @param p_tooltip la description (si fournie).
	 * @return le conteneur pour le groupe.
	 */
	Group addGroup(final Composite p_parent, final String p_title, final String p_tooltip) {
		return addGroup(p_parent, p_title, p_tooltip, new GridLayout(2, false));
	}

	/**
	 * Création d'un conteneur de type 'group' avec une légende.
	 * 
	 * @param p_parent  le conteneur parent.
	 * @param p_title   le titre à afficher pour le groupement.
	 * @param p_tooltip la description (si fournie).
	 * @return le conteneur pour le groupe.
	 */
	Group addGroup(final Composite p_parent, final String p_title, final String p_tooltip, final GridLayout p_layout) {
		Group group = new Group(p_parent, SWT.NONE);
		group.setText(" " + p_title + " ");
		group.setFont(new Font(group.getDisplay(), new FontData("Arial", 8, SWT.BOLD)));
		group.setToolTipText(p_tooltip);
		group.setLayout(p_layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		registerResourceToDispose(group.getFont());
		return group;
	}

	/**
	 * Création d'un champ de saisie de type {@link Text} avec son libellé.
	 * 
	 * @param p_labelValue   le libellé pour le champ de type 'texte.
	 * @param p_defaultValue la valeur par défaut pour le champ (optionnel).
	 * @param p_tooltip      une explication (description) pour le champ
	 *                       (optionnel).
	 * @return le composite comprenant un champ de saisie et son libellé.
	 */
	Text addText(final Composite p_parent, final String p_labelValue, final String p_defaultValue,
			final String p_tooltip) {
		Label label = new Label(p_parent, SWT.NONE);
		label.setToolTipText(p_tooltip);
		label.setText(p_labelValue + c_labelOffset);
		final Text element = new Text(p_parent, SWT.BORDER | SWT.SINGLE);
		element.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		element.setText(p_defaultValue);
		element.setData(label);
		return element;
	}

	/**
	 * Adds a Select box with a label to the container.
	 * 
	 * @param p_labelValue Label for the select box
	 * @param p_elements   List of elements to add to the list.
	 * @param p_tooltip    An explanation for the input field (optional).
	 * @return Combo
	 */
	Combo addComboBox(final Composite p_parent, final String p_labelValue, final String p_tooltip,
			final String[] p_elements, final int p_defaultSelection) {
		Label label = new Label(p_parent, SWT.NONE);
		label.setToolTipText(p_tooltip);
		label.setText(p_labelValue + c_labelOffset);
		Combo combo = new Combo(p_parent, SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setItems(p_elements);
		combo.select(p_defaultSelection);
		return combo;
	}

	/**
	 * 
	 * @param p_parent
	 * @param p_labelValue
	 * @param p_tooltip
	 * @return
	 */
	Button addCheckBox(final Composite p_parent, final String p_labelValue, final String p_tooltip) {
		Label label = new Label(p_parent, SWT.NONE);
		label.setToolTipText(p_tooltip);
		label.setText(p_labelValue + c_labelOffset);
		Composite group = new Composite(p_parent, SWT.NONE);
		FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
		fillLayout.marginHeight = 3;
		group.setLayout(fillLayout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setCursor(new Cursor(p_parent.getDisplay(), SWT.CURSOR_HAND));
		registerResourceToDispose(group.getCursor());
		Button chbox = new Button(group, SWT.CHECK);
		chbox.setData(label);
		return chbox;
	}

	/**
	 * Demande express de suppression de certaines ressources à la fermeture du
	 * formulaire, sinon apparition de messages d'erreur dans la console
	 */
	@Override
	public void dispose() {
		for (Resource resource : _lstResourcesToDispose) {
			resource.dispose();
		}
		super.dispose();
	}
}
