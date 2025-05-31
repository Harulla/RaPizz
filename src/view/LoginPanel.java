package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;

public class LoginPanel extends JPanel {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/rapizz";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel messageLabel;
    private MainFrame parentFrame;
    
    public LoginPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }
    
    private void initializeComponents() {
        // Titre
        JLabel titleLabel = new JLabel("RaPizz - Connexion", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(220, 20, 60)); // Rouge pizza
        
        // Champs de saisie
        JLabel loginLabel = new JLabel("Login :");
        loginLabel.setFont(new Font("Arial", Font.BOLD, 14));
        loginField = new JTextField(20);
        loginField.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JLabel passwordLabel = new JLabel("Mot de passe :");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Boutons
        loginButton = new JButton("Se connecter");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(34, 139, 34)); // Vert
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(150, 35));
        
        registerButton = new JButton("S'inscrire");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBackground(new Color(30, 144, 255)); // Bleu
        registerButton.setForeground(Color.WHITE);
        registerButton.setPreferredSize(new Dimension(150, 35));
        
        // Message d'erreur/succès
        messageLabel = new JLabel(" ", JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.ITALIC, 12));
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 245));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Panel principal avec bordure
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.insets = new Insets(15, 15, 15, 15);
        
        // Titre
        mainGbc.gridx = 0; mainGbc.gridy = 0;
        mainGbc.gridwidth = 2;
        mainGbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(new JLabel("RaPizz - Connexion") {{
            setFont(new Font("Arial", Font.BOLD, 28));
            setForeground(new Color(220, 20, 60));
        }}, mainGbc);
        
        // Login
        mainGbc.gridx = 0; mainGbc.gridy = 1;
        mainGbc.gridwidth = 1;
        mainGbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Login :") {{
            setFont(new Font("Arial", Font.BOLD, 14));
        }}, mainGbc);
        
        mainGbc.gridx = 1; mainGbc.gridy = 1;
        mainGbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(loginField, mainGbc);
        
        // Mot de passe
        mainGbc.gridx = 0; mainGbc.gridy = 2;
        mainGbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Mot de passe :") {{
            setFont(new Font("Arial", Font.BOLD, 14));
        }}, mainGbc);
        
        mainGbc.gridx = 1; mainGbc.gridy = 2;
        mainGbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(passwordField, mainGbc);
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        mainGbc.gridx = 0; mainGbc.gridy = 3;
        mainGbc.gridwidth = 2;
        mainGbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, mainGbc);
        
        // Message
        mainGbc.gridx = 0; mainGbc.gridy = 4;
        mainGbc.gridwidth = 2;
        mainPanel.add(messageLabel, mainGbc);
        
        // Ajouter le panel principal au centre
        gbc.anchor = GridBagConstraints.CENTER;
        add(mainPanel, gbc);
    }
    
    private void setupEventListeners() {
        // Action sur le bouton de connexion
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });
        
        // Action sur Entrée dans les champs
        ActionListener enterAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        };
        
        loginField.addActionListener(enterAction);
        passwordField.addActionListener(enterAction);
        
        // Action sur le bouton d'inscription
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegistrationDialog();
            }
        });
    }
    
    private void attemptLogin() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (login.isEmpty() || password.isEmpty()) {
            showMessage("Veuillez remplir tous les champs.", Color.RED);
            return;
        }
        
        // Vérification en base de données
        try {
            if (authenticateUser(login, password)) {
                showMessage("Connexion réussie !", Color.GREEN);
                // Délai pour voir le message de succès puis basculer vers la page d'accueil
                Timer timer = new Timer(1000, e -> {
                    parentFrame.showHomePage();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                showMessage("Login ou mot de passe incorrect.", Color.RED);
                passwordField.setText(""); // Vider le mot de passe pour que l'utilisataeur recommence
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Erreur de connexion à la base de données.", Color.RED);
        }
    }
    
    private boolean authenticateUser(String login, String password) throws SQLException {
        String sql = "SELECT id_compte, nom_client, prenom_client, adresse, role FROM Compte WHERE login = ? AND mot_de_passe = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, login);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int idCompte = rs.getInt("id_compte");
                    String nom = rs.getString("nom_client");
                    String prenom = rs.getString("prenom_client");
                    String adresse = rs.getString("adresse");
                    String role = rs.getString("role");
                    
                    // Créer la session
                    model.UserSession.createSession(idCompte, login, nom, prenom, adresse, role);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void showRegistrationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Inscription", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Champs d'inscription
        JTextField regLoginField = new JTextField(15);
        JPasswordField regPasswordField = new JPasswordField(15);
        JTextField nomField = new JTextField(15);
        JTextField prenomField = new JTextField(15);
        JTextField adresseField = new JTextField(15);
        
        // Layout des champs
        //gbc = grid bag constraint
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Login :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(regLoginField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(regPasswordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(nomField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Prénom :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(prenomField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Adresse :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(adresseField, gbc);
        
        // Boutons
        JButton createButton = new JButton("Créer le compte");
        JButton cancelButton = new JButton("Annuler");
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);
        
        // Actions des boutons
        createButton.addActionListener(e -> {
            try {
                if (createAccount(regLoginField.getText(), new String(regPasswordField.getPassword()),
                                nomField.getText(), prenomField.getText(), adresseField.getText())) {
                    JOptionPane.showMessageDialog(dialog, "Compte créé avec succès !");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Erreur lors de la création du compte.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Erreur de base de données.");
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private boolean createAccount(String login, String password, String nom, String prenom, String adresse) throws SQLException {
        if (login.isEmpty() || password.isEmpty() || nom.isEmpty() || prenom.isEmpty()) {
            return false;
        }
        
        String sql = "INSERT INTO Compte (login, mot_de_passe, solde, nom_client, prenom_client, adresse, nb_pizza) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, login);
            pstmt.setString(2, password); // Il faudrait que l'on utilise un hash plutot que d'avoir les MDP en dur :)
            pstmt.setFloat(3, 0);
            pstmt.setString(4, nom);
            pstmt.setString(5, prenom);
            pstmt.setString(6, adresse);
            pstmt.setFloat(7, 0);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
        
        // Effacer le message après 3 secondes
        Timer timer = new Timer(3000, e -> messageLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }
    
    public void clearFields() {
        loginField.setText("");
        passwordField.setText("");
        messageLabel.setText(" ");
    }
}