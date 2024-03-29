package tvhead2tvb;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;
import util.ui.EnhancedPanelBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 * @author fjo
 */
public final class Tvhead2TVB extends Plugin implements SettingsTab {

	private final static Version VERSION = new Version(1, 1, true);
	private final static PluginInfo INFO = new PluginInfo(Tvhead2TVB.class,
		"Tvhead2TVB",
		"Exports program schedules from TV-Browser to Tvheadend",
		"Felix J. Ogris (fjo@ogris.de)",
		"AGPL-3.0",
		"https://ogris.de/tvhead2tvb");
	private boolean configured = false;
	private final JTextField tvheadUrlField = new JTextField();
	private String tvheadUrl;
	private final EnhancedPanelBuilder settingsPanel;
	private final long mainThreadId = Thread.currentThread().getId();

	public Tvhead2TVB() {
                settingsPanel = new EnhancedPanelBuilder(new FormLayout("left:pref, 23dlu, pref:grow"));
                settingsPanel.addSeparatorRowFull("Server settings");
                settingsPanel.addRow();
                settingsPanel.addLabel("URL:");
                settingsPanel.nextColumn();
                settingsPanel.nextColumn();
                settingsPanel.add(tvheadUrlField);
	}

	private void checkSettings(final boolean showDialog) {
		configured = false;

		tvheadUrl = tvheadUrlField.getText();
		try {
			new URL(tvheadUrl).toURI();
		} catch (final Exception e) {
			if (showDialog) {
				showError("Invalid Tvheadend url: " + tvheadUrl);
			}
			return;
		}

		configured = true;
	}

	private void showError(final String errMsg) {
		final Runnable run = new Runnable() {

			@Override
			final public void run() {
				JOptionPane.showMessageDialog(null, errMsg, "Tvhead2TVB", JOptionPane.ERROR_MESSAGE);
			}
		};
		if (mainThreadId == Thread.currentThread().getId()) {
			run.run();
		} else {
			SwingUtilities.invokeLater(run);
		}
	}

	private void recordProgram(final Program program) {
		String title;
		try {
			title = URLEncoder.encode(program.getTitle(), "UTF-8");
		} catch (Exception e) {
			title = program.getTitle();
		}

		util.browserlauncher.Launch.openURL(tvheadUrl + "/simple.html?s=" + title);
	}

	@Override
	public final String getTitle() {
		return null;
	}

	@Override
	public final Icon getIcon() {
		return null;
	}

	@Override
	public JPanel createSettingsPanel() {
		tvheadUrlField.setText(tvheadUrl);
		return settingsPanel.getPanel();
	}

	@Override
	public void saveSettings() {
		checkSettings(true);
	}

	@Override
	public final Properties storeSettings() {
		final Properties settings = new Properties();
		settings.setProperty("tvheadUrl", tvheadUrl);
		return settings;
	}

	@Override
	public final void loadSettings(final Properties settings) {
		tvheadUrl = settings.getProperty("tvheadUrl", "http://tvheadend.local:9981");
		tvheadUrlField.setText(tvheadUrl);
		checkSettings(false);
	}

	@Override
	public final SettingsTab getSettingsTab() {
		return this;
	}

	@Override
	protected final String getMarkIconName() {
		return "tvhead2tvb.png";
	}

	public static Version getVersion() {
		return VERSION;
	}

	@Override
	public final PluginInfo getInfo() {
		return INFO;
	}

	@Override
	public final ActionMenu getContextMenuActions(final Program program) {
		final String cmdConfigure = "Tvhead2TVB: Configure...";
		final String cmdRecord = "Tvhead2TVB: Record";
		final Plugin plugin = this;
		final AbstractAction action = new AbstractAction() {

			@Override
			public final void actionPerformed(final ActionEvent e) {
				final String cmd = e.getActionCommand();
				if (cmd.equals(cmdConfigure)) {
					Plugin.getPluginManager().showSettings(plugin);
				} else if (cmd.equals(cmdRecord)) {
					recordProgram(program);
				} else if (cmd.equals("<html>" + cmdRecord + "</html>")){
                                        // called from program side menu
                                        recordProgram(program);
                                }
			}
		};

		action.putValue(Action.SMALL_ICON, createImageIcon(getMarkIconName()));
		if (!configured) {
			action.putValue(Action.NAME, cmdConfigure);
		} else {
			action.putValue(Action.NAME, cmdRecord);
		}

		return new ActionMenu(action);
	}

        @Override
        public final String getPluginCategory() {
                return CATEGORY_OTHER;
        }
}
