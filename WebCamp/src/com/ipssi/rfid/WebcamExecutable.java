package com.ipssi.rfid;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;



public class WebcamExecutable extends JFrame implements ActionListener {

	private static final long serialVersionUID = -1368783325310232511L;

	private Executor executor = Executors.newSingleThreadExecutor();
	private AtomicBoolean initialized = new AtomicBoolean(false);
	private Webcam webcam = null;
	private WebcamPanel panel = null;
	private JButton button = null;

	public WebcamExecutable() {
		super();

		setTitle("Take Photograph");
		setLayout(new FlowLayout(FlowLayout.CENTER));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		webcam = Webcam.getDefault();
		webcam.setViewSize(webcam.getViewSizes()[0]);

		panel = new WebcamPanel(webcam, false);
		panel.setPreferredSize(webcam.getViewSize());
		panel.setOpaque(true);
		panel.setBackground(Color.BLACK);

//		ImageIcon icon = null;
//		try {
//			List<BufferedImage> icons = Ima.read(getClass().getResourceAsStream("/Security-Camera.ico"));
//			icon = new ImageIcon(icons.get(1));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		button = new JButton();
		button.setText("Capture");
		button.addActionListener(this);
		button.setFocusable(false);
		button.setPreferredSize(webcam.getViewSize());

		add(panel);
		add(button);

		pack();
		setVisible(true);
		if (initialized.compareAndSet(false, true)) {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					panel.start();
				}
			});
		}
	}

	boolean running = false;

	public static void getImage() throws IOException {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				new WebcamExecutable();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Image im = panel.createImage(webcam.getViewSize().width, webcam.getViewSize().height);
	}

}
