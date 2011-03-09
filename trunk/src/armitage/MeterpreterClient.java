package armitage;

import console.Console;
import msf.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/* This is a rewritten client class to keep compatible with ConsoleClient but interface with the new
   MeterpreterSession class. This new class makes sure each command is executed and receives its output
   before the next one is executed. This prevents the Armitage UI from becoming confused */

public class MeterpreterClient implements ActionListener, MeterpreterSession.MeterpreterCallback {
	protected Console		window;
	protected MeterpreterSession	session;

	public Console getWindow() {
		return window;
	}

	public void commandComplete(String sid, Object token, Map response) {
		if (token == this || token == null) 
			processRead(response);
	}

       private void processRead(Map read) {
		try {
			if (! "".equals( read.get("data") )) {
				String text = new String(Base64.decode( read.get("data") + "" ), "UTF-8");
				window.append(text);
			}

			if (! "".equals( read.get("prompt") )) {
				window.updatePrompt(ConsoleClient.cleanText(new String(Base64.decode( read.get("prompt") + "" ), "UTF-8")));
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public MeterpreterClient(Console window, MeterpreterSession session) {
		this.window	= window;
		this.session	= session;
		this.session.addListener(this);

		setupListener();

		window.updatePrompt("meterpreter > ");
	}

	/* called when the associated tab is closed */
	public void actionPerformed(ActionEvent ev) {
		/* nothing we need to do for now */
	}

	protected void finalize() {
		actionPerformed(null);
	}

	public void sendString(String text) {
		window.append(window.getPromptText() + text);
		session.addCommand(this, text);
	}

	protected void setupListener() {
		window.getInput().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				String text = window.getInput().getText() + "\n";
				window.getInput().setText("");
				sendString(text);
			}
		});
	}
}