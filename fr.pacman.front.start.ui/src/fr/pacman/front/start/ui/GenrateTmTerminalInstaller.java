package fr.pacman.front.start.ui;

import java.net.URI;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import fr.pacman.front.start.ui.activator.Activator;

public class GenrateTmTerminalInstaller {

	private static final String c_update_site = "https://download.eclipse.org/releases/latest";
	private static final String c_terminal_feature = "org.eclipse.tm.terminal.feature.feature.group";

	/**
	 * Installe TM Terminal si absent et ouvre la vue Terminal.
	 */
	public static void installAndOpenTerminal() {
		Job job = new Job("Installing TM Terminal") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// Récupérer le service OSGi
					IProvisioningAgentProvider provider = Activator.getService(IProvisioningAgentProvider.class);
					if (provider == null) {
						return new Status(IStatus.ERROR, Activator.c_pluginId,
								"No provisioning agent provider available");
					}

					IProvisioningAgent agent = provider.createAgent(null);
					ProvisioningSession session = new ProvisioningSession(agent);

					// Charger le dépôt TM Terminal
					IMetadataRepositoryManager metaManager = agent.getService(IMetadataRepositoryManager.class);
					IMetadataRepository repo = metaManager.loadRepository(new URI(c_update_site), monitor);

					// Chercher l’unité installable
					IQueryResult<IInstallableUnit> units = repo.query(QueryUtil.createIUQuery(c_terminal_feature),
							monitor);

					if (units.isEmpty()) {
						return new Status(IStatus.ERROR, Activator.c_pluginId,
								"TM Terminal IU not found in repository");
					}

					// Préparer l’opération d’installation
					InstallOperation op = new InstallOperation(session, Collections.singleton(units.iterator().next()));
					IStatus status = op.resolveModal(new NullProgressMonitor());
					if (!status.isOK()) {
						return status;
					}

					// Lancer l’installation silencieuse
					ProvisioningJob pJob = op.getProvisioningJob(monitor);
					if (pJob != null) {
						IStatus result = pJob.runModal(monitor);

						if (result.getSeverity() == IStatus.ERROR) {
							return result;
						}

						// Vérifier si un redémarrage est nécessaire
						int restartPolicy = pJob.getRestartPolicy();
						if (restartPolicy == ProvisioningJob.RESTART_ONLY
								|| restartPolicy == ProvisioningJob.RESTART_OR_APPLY) {
							Display.getDefault().syncExec(() -> PlatformUI.getWorkbench().restart());
							return Status.OK_STATUS;
						}
					}
					return Status.OK_STATUS;

				} catch (Exception e) {
					return new Status(IStatus.ERROR, Activator.c_pluginId, "TM Terminal installation failed", e);
				}
			}
		};
		job.schedule();
	}
}
