package org.bme.mit.iir;

import java.awt.EventQueue;

import javax.swing.JFrame;

import org.semanticweb.HermiT.Reasoner;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.JTabbedPane;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.JSplitPane;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class MainWindow {

	private JFrame mainFrame;
	private final ReasoningClass reasoner;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.mainFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		reasoner = new ReasoningClass(ReasoningClass.UNI_ONTOLOGY_FNAME);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mainFrame = new JFrame();
		mainFrame.getContentPane().setFont(new Font("Arial Narrow", Font.PLAIN, 12));
		mainFrame.setTitle("Öröklés következtető");
		mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/javax/swing/plaf/metal/icons/ocean/computer.gif")));
		mainFrame.setFont(new Font("Arial Narrow", Font.PLAIN, 12));
		mainFrame.getContentPane().setBackground(Color.WHITE);
		mainFrame.getContentPane().setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.X_AXIS));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		splitPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainFrame.getContentPane().add(splitPane);
		
		JSplitPane splitPaneInner = new JSplitPane();
		splitPaneInner.setResizeWeight(0.79);
		splitPaneInner.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setRightComponent(splitPaneInner);
		
		mainFrame.setBounds(100, 100, 750, 555);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
