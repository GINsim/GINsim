package org.ginsim.commongui.dialog;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.ginsim.common.application.Txt;

/**
 * Wrappers to create error and message dialogs.
 * 
 * @author Lionel Spinelli
 */
public class GUIMessageUtils {

	/**
	 * Open a Message Dialog box to indicate users an exception occurred and give some feedback.
	 * 
	 * @param e the exception to display
	 * @param main the parent component
	 */
	public static void openErrorDialog( Exception e, Component main) {
		
		JOptionPane.showMessageDialog( main, e.getMessage() + "\n", e.getMessage(), JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Open a Message Dialog box to indicate users an error occurred and give some feedback.
	 * 
	 * @param message the message to show to the user
	 */
	public static void openErrorDialog( String message) {
		
		openErrorDialog( message, (Component) null);
	}
	
	/**
	 * Open a Message Dialog box to indicate users an error occurred and give some feedback.
	 * 
	 * @param message the message to show to the user
	 * @param main the parent component
	 */
	public static void openErrorDialog(String message, Component main) {

		JOptionPane.showMessageDialog(main, Txt.t(message), Txt.t("STR_error"), JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Open a Message Dialog box to indicate users a warning message.
	 * 
	 * @param message the message to show to the user
	 */
	public static void openWarningDialog(String message) {
		JOptionPane.showMessageDialog( null, message, Txt.t("STR_warning"), JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Open a confirmation Dialog box (OK/Cancel) with the given message and the given title 
	 * 
	 * @param msg the message to display in the box
	 * @param title the title of the box
	 * @return true if the user accepted, false if not
	 */
	public static boolean openConfirmationDialog(String msg, String title) {
		
		int ret = JOptionPane.showConfirmDialog(null,  Txt.t(msg), Txt.t(title), JOptionPane.OK_CANCEL_OPTION);
		return ret == JOptionPane.OK_OPTION;
	}

	public static boolean openConfirmationDeleteDialog(String msg, String title) {

		int ret = JOptionPane.showConfirmDialog(null,  Txt.t(msg), Txt.t(title), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		return ret == JOptionPane.OK_OPTION;
	}
}
