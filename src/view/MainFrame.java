package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class MainFrame extends JFrame {
    
    private LoginPanel loginPanel;
    private PageAccueil pageAccueil;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    public MainFrame() {
        initializeFrame();
        createPanels();
        setupLayout();
    }
    
    private void initializeFrame() {
        setTitle("RaPizz - Application de Commande");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrer la fenêtre
        
        try {
            setIconImage(ImageIO.read(getClass().getResourceAsStream("/images/Logo.png")));
        } catch (Exception e) {
        }
    }
    
    private void createPanels() {
        // Créer le layout à cartes pour basculer entre les vues
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        loginPanel = new LoginPanel(this);
        mainPanel.add(loginPanel, "LOGIN");
        // La page d'accueil sera créée lors de la connexion réussie
        pageAccueil = null;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        
        // Commencer par afficher la page de login
        cardLayout.show(mainPanel, "LOGIN");
    }
    
    public void showHomePage() {
        try {
            // Créer la page d'accueil si elle n'existe pas encore
            if (pageAccueil == null) {
                model.UserSession session = model.UserSession.getInstance();
                pageAccueil = new PageAccueil(session.getIdCompte(), session.getRole());

                // Créer un panel wrapper pour la page d'accueil avec un bouton de déconnexion
                JPanel homeWrapper = new JPanel(new BorderLayout());

                // Panel du header avec bouton de déconnexion
                JPanel headerPanel = new JPanel(new BorderLayout());
                headerPanel.setBackground(new Color(220, 20, 60));
                headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

                // Logo
                try {
                    InputStream is = getClass().getResourceAsStream("/images/Logo.png"); // Change le chemin si besoin
                    if (is != null) {
                        BufferedImage logoImage = ImageIO.read(is);
                        Image scaledLogo = logoImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Ajuste la taille
                        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
                        headerPanel.add(logoLabel, BorderLayout.WEST);
                    } else {
                        System.out.println("Le fichier logo.png est introuvable !");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Texte de bienvenue au centre
                String welcomeText = "Bienvenue " + session.getPrenom() + " " + session.getNom() + " sur RaPizz !";
                JLabel welcomeLabel = new JLabel(welcomeText, JLabel.CENTER);
                welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
                welcomeLabel.setForeground(Color.WHITE);
                headerPanel.add(welcomeLabel, BorderLayout.CENTER);

                // Bouton de déconnexion à droite
                JButton logoutButton = new JButton("Déconnexion");
                logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
                logoutButton.setBackground(Color.WHITE);
                logoutButton.setForeground(new Color(220, 20, 60));
                logoutButton.addActionListener(e -> showLoginPage());
                headerPanel.add(logoutButton, BorderLayout.EAST);

                // Récupérer le contenu de la page d'accueil
                JPanel homeContent = new JPanel(new BorderLayout());
                Container contentPane = pageAccueil.getContentPane();
                Component[] components = contentPane.getComponents();
                for (Component comp : components) {
                    homeContent.add(comp);
                }

                homeWrapper.add(headerPanel, BorderLayout.NORTH);
                homeWrapper.add(homeContent, BorderLayout.CENTER);

                mainPanel.add(homeWrapper, "HOME");
            }

            cardLayout.show(mainPanel, "HOME");

            // Masquer la PageAccueil originale si elle est visible
            if (pageAccueil != null) {
                pageAccueil.setVisible(false);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement de la page d'accueil.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    public void showLoginPage() {
        // Nettoyer les champs de connexion
        loginPanel.clearFields();
        
        // Afficher la page de connexion
        cardLayout.show(mainPanel, "LOGIN");
        
        // Réinitialiser la page d'accueil pour forcer un rechargement lors de la prochaine connexion
        if (pageAccueil != null) {
            pageAccueil.dispose();
            pageAccueil = null;
            // Retirer le panel HOME pour le recréer à la prochaine connexion
            Component[] components = mainPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    // Identifier le panel HOME et le retirer
                    try {
                        CardLayout cl = (CardLayout) mainPanel.getLayout();
                        // On recrée complètement le système
                        mainPanel.removeAll();
                        mainPanel.add(loginPanel, "LOGIN");
                        break;
                    } catch (Exception e) {
                       
                    }
                }
            }
        }
        model.UserSession.clearSession();
        // Forcer la mise à jour de l'affichage
        revalidate();
        repaint();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            // Utiliser le look par défaut si erreur
        }
        
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}