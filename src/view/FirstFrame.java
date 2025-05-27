package view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.awt.event.ItemEvent;
import javax.swing.JRadioButton;
import javax.swing.JComboBox;

public class FirstFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField field1;
	private JTextField field2;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FirstFrame frame = new FirstFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FirstFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//opérations à effectuer lorsque l'on ferme la fenêtre
		setBounds(100, 100, 470, 500);// l'endroit à laquelle la fenêtre apparait sur l'écran + la taille
		contentPane = new JPanel();
		contentPane.setBackground(new Color(255, 255, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("RaPizz");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Gadugi", Font.PLAIN, 36));
		lblNewLabel.setBackground(new Color(255, 250, 240));
		lblNewLabel.setBounds(10, 11, 434, 40);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Champ 1");
		lblNewLabel_1.setBounds(56, 117, 46, 14);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Champ 2");
		lblNewLabel_2.setBounds(56, 160, 46, 14);
		contentPane.add(lblNewLabel_2);
		
		field1 = new JTextField();
		field1.setBounds(158, 114, 86, 20);
		contentPane.add(field1);
		field1.setColumns(10);
		
		field2 = new JTextField();
		field2.setBounds(158, 157, 86, 20);
		contentPane.add(field2);
		field2.setColumns(10);
		
		JButton btnNewButton = new JButton("VALIDER");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//event que je veux faire après avoir cliqué sur mon bouton
				
				int num1 = Integer.parseInt(field1.getText());
				int num2 = Integer.parseInt(field2.getText());
				lblNewLabel.setText("RESULT: " + (num1+num2));
				JOptionPane.showMessageDialog(null, "Test terminé");
			}
		});
		btnNewButton.setBounds(155, 198, 89, 23);
		contentPane.add(btnNewButton);
		
		JLabel lblNewLabel_3 = new JLabel("Choisi ta pizza");
		lblNewLabel_3.setBounds(56, 286, 120, 14);
		contentPane.add(lblNewLabel_3);
		
		ArrayList<String> selection = new ArrayList<>();
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("Magherita");
		chckbxNewCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chckbxNewCheckBox.isSelected()) {
                    // Ajoute l'élément si la case est cochée
                    selection.add("Magherita");
                } else {
                    // Retire l'élément si la case est décochée
                    selection.remove("Magherita");
                }
                System.out.println("Sélection : " + selection);
			}
		});
		chckbxNewCheckBox.setBounds(56, 316, 97, 23);
		contentPane.add(chckbxNewCheckBox);
		
		JCheckBox chckbxNewCheckBox_1 = new JCheckBox("4 fromages");
		chckbxNewCheckBox_1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chckbxNewCheckBox_1.isSelected()) { // e.getStateChanged == ItemEvent.SELECTED
                    // Ajoute l'élément si la case est cochée
                    selection.add("4 fromages");
                } else {
                    // Retire l'élément si la case est décochée
                    selection.remove("4 fromages");
                }
                System.out.println("Sélection : " + selection);
			}
		});
		chckbxNewCheckBox_1.setBounds(56, 353, 97, 23);
		contentPane.add(chckbxNewCheckBox_1);
		
		JCheckBox chckbxNewCheckBox_2 = new JCheckBox("Regina");
		chckbxNewCheckBox_2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chckbxNewCheckBox_2.isSelected()) {
                    // Ajoute l'élément si la case est cochée
                    selection.add("Regina");
                } else {
                    // Retire l'élément si la case est décochée
                    selection.remove("Regina");
                }
                System.out.println("Sélection : " + selection);
			}
		});
		chckbxNewCheckBox_2.setBounds(56, 390, 97, 23);
		contentPane.add(chckbxNewCheckBox_2);
		
		JButton btnNewButton_1 = new JButton("Valider");
		btnNewButton_1.setBounds(56, 420, 89, 23);
		contentPane.add(btnNewButton_1);
		
		textField = new JTextField();
		textField.setBounds(269, 283, 86, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		//boutons radio
		JRadioButton rdbtnNewRadioButton = new JRadioButton("Margheritta");
		rdbtnNewRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				textField.setFont(new Font("Times of Roman", Font.ITALIC,18));
				textField.setText(rdbtnNewRadioButton.getText());
			}
		});
		rdbtnNewRadioButton.setBounds(269, 316, 109, 23);
		contentPane.add(rdbtnNewRadioButton);
		
		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("4 fromages");
		rdbtnNewRadioButton_1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				textField.setFont(new Font("Times of Roman", Font.PLAIN,18));
				textField.setText(rdbtnNewRadioButton_1.getText());
			}
		});
		rdbtnNewRadioButton_1.setBounds(269, 353, 109, 23);
		contentPane.add(rdbtnNewRadioButton_1);
		
		JRadioButton rdbtnNewRadioButton_2 = new JRadioButton("Regina");
		rdbtnNewRadioButton_2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				textField.setFont(new Font("Times of Roman", Font.BOLD,18));
				textField.setText(rdbtnNewRadioButton_2.getText());
			}
		});
		rdbtnNewRadioButton_2.setBounds(269, 390, 109, 23);
		contentPane.add(rdbtnNewRadioButton_2);
		
		ButtonGroup btnGrp = new ButtonGroup();
		btnGrp.add(rdbtnNewRadioButton);
		btnGrp.add(rdbtnNewRadioButton_1);
		btnGrp.add(rdbtnNewRadioButton_2);
		
		JComboBox comboBox = new JComboBox();
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					if(!comboBox.getSelectedItem().toString().equals("Choisis une pizza")) {
						JOptionPane.showMessageDialog(null,comboBox.getSelectedItem().toString()+ " is Selected." );
					}
				}
			}
		});
		comboBox.setBounds(303, 113, 131, 22);
		contentPane.add(comboBox);
		comboBox.addItem("Choisis une pizza");
		comboBox.addItem("Margherita");
		comboBox.addItem("4 fromages");
		comboBox.addItem("Regina");
		
		JButton btnNewButton_2 = new JButton("Change layout");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SecondFrame obj = new SecondFrame();
				obj.setVisible(true);
			}
		});
		btnNewButton_2.setBounds(10, 52, 114, 23);
		contentPane.add(btnNewButton_2);
		
	}
}
