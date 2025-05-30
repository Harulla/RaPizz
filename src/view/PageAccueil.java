package view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;

import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class PageAccueil extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/rapizz";
    private static final String DB_USER = "Likian";
    private static final String DB_PASSWORD = "1234";
    private int idCompteConnecte;
    private String role;
    private JPanel rightPanel;
    
    // Commandes en cours (en train de se faire livrer par un livreur)
    private JPanel createLivraisonsPanel(int idCompteConnecte) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Livraisons en cours"));

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
        	String sql = "SELECT l.id_livraison, l.statut, l.heure_depart, c.id_commande, cl.nom_client, cl.prenom_client, v.nom AS vehicule, lv.nom AS livreur " +
        		    "FROM Livraison l " +
        		    "JOIN Commande c ON l.id_commande = c.id_commande " +
        		    "JOIN Compte cl ON c.id_compte = cl.id_compte " +
        		    "LEFT JOIN Vehicule v ON l.id_vehicule = v.id_vehicule " +
        		    "LEFT JOIN Livreur lv ON l.id_livreur = lv.id_livreur " +
        		    "WHERE l.heure_arrivee IS NULL AND c.id_compte = ?";
        	

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCompteConnecte);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Livraison #").append(rs.getInt("id_livraison"))
                  .append(" | Statut: ").append(rs.getString("statut"))
                  .append(" | Départ: ").append(rs.getString("heure_depart"))
                  .append("\nCommande: ").append(rs.getString("id_commande"))
                  .append(" | Client: ").append(rs.getString("prenom_client")).append(" ").append(rs.getString("nom_client"))
                  .append(" | Véhicule: ").append(rs.getString("vehicule") == null ? "N/A" : rs.getString("vehicule"))
                  .append(" | Livreur: ").append(rs.getString("livreur") == null ? "N/A" : rs.getString("livreur"))
                  .append("\n\n");
            }

            if (sb.length() == 0) {
                sb.append("Aucune livraison en cours.");
            }

            textArea.setText(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Erreur lors du chargement des livraisons.");
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Commandes déjà passés (historique)
    private JPanel createCommandesPanel(int idCompteConnecte) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Commandes passées"));

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
        	String sql = "SELECT c.id_commande, c.date_commande, c.est_gratuite, cl.nom_client, cl.prenom_client, t.Nom AS taille " +
        		    "FROM Commande c " +
        		    "JOIN Compte cl ON c.id_compte = cl.id_compte " +
        		    "LEFT JOIN Taille t ON c.id_taille = t.id_taille " +
        		    "WHERE c.id_compte = ? " +
        		    "ORDER BY c.date_commande DESC";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCompteConnecte);
            ResultSet rs = pstmt.executeQuery();

            //un string builder permet de concaténer plein de string entre elles
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Commande #").append(rs.getString("id_commande"))
                  .append(" | Date: ").append(rs.getString("date_commande"))
                  .append("\nClient: ").append(rs.getString("prenom_client")).append(" ").append(rs.getString("nom_client"))
                  .append(" | Taille: ").append(rs.getString("taille") == null ? "N/A" : rs.getString("taille"))
                  .append(" | Gratuite: ").append(rs.getBoolean("est_gratuite") ? "Oui" : "Non")
                  .append("\n\n");
            }

            if (sb.length() == 0) {
                sb.append("Aucune commande passée.");
            }

            textArea.setText(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Erreur lors du chargement des commandes.");
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Méthode pour créer le contenu principal de la page d'accueil qui sera ensuite ajouté dans le frame
    public JPanel createMainContent() throws ClassNotFoundException {
        JPanel mainContent = new JPanel(new BorderLayout());
        
        // Titre de la page
        JLabel titleLabel = new JLabel("Nos Délicieuses Pizzas", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Panel principal avec un scroll pour les pizzas
        JPanel pizzaListPanel = new JPanel();
        pizzaListPanel.setLayout(new BoxLayout(pizzaListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(pizzaListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Panel de droite pour les livraisons et commandes
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(400, 600));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel livraisonsPanel = createLivraisonsPanel(idCompteConnecte);
        JPanel commandesPanel = createCommandesPanel(idCompteConnecte);

        rightPanel.add(livraisonsPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(commandesPanel);

        // Charger les pizzas et créer les cartes
        chargerPizzasDepuisBDD(pizzaListPanel);

        // Layout final (TOUT)
        mainContent.add(titleLabel, BorderLayout.NORTH);
        mainContent.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(rightPanel, BorderLayout.EAST);

        // Bouton "Mon compte" visible pour les clients
        if ("client".equals(role)) {
            JButton btnMonCompte = new JButton("Mon compte");
            btnMonCompte.addActionListener(e -> {
                JFrame compteFrame = new JFrame("Mon compte");
                compteFrame.setSize(400, 250);
                compteFrame.setLocationRelativeTo(null);
                JPanel panel = new JPanel(new GridLayout(0,1));
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    PreparedStatement ps = conn.prepareStatement("SELECT nom_client, prenom_client, adresse, solde FROM Compte WHERE id_compte = ?");
                    ps.setInt(1, idCompteConnecte);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        panel.add(new JLabel("Nom : " + rs.getString("nom_client")));
                        panel.add(new JLabel("Prénom : " + rs.getString("prenom_client")));
                        panel.add(new JLabel("Adresse : " + rs.getString("adresse")));
                        panel.add(new JLabel("Solde : " + rs.getDouble("solde") + " €"));
                    }
                } catch (SQLException ex) {
                    panel.add(new JLabel("Erreur lors du chargement du compte."));
                }
                compteFrame.add(panel);
                compteFrame.setVisible(true);
            });
            mainContent.add(btnMonCompte, BorderLayout.SOUTH);
        }

        // Bouton "Statistiques" visible pour l'admin
        if ("admin".equals(role)) {
            JButton btnStats = new JButton("Statistiques");
            btnStats.addActionListener(e -> {
                JFrame statsFrame = new JFrame("Statistiques");
                statsFrame.setSize(400, 300);
                statsFrame.setLocationRelativeTo(null);
                JPanel panel = new JPanel(new GridLayout(0,1));
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT nom_client, prenom_client, COUNT(*) as nb FROM Commande c JOIN Compte co ON c.id_compte=co.id_compte GROUP BY c.id_compte ORDER BY nb DESC LIMIT 1");
                    if (rs.next()) panel.add(new JLabel("Meilleur client : " + rs.getString("prenom_client") + " " + rs.getString("nom_client") + " (" + rs.getInt("nb") + " commandes)"));
                    rs = conn.createStatement().executeQuery(
                        "SELECT p.nom, COUNT(*) as nb FROM Commande_Pizza cp JOIN Pizza p ON cp.id_pizza=p.id_pizza GROUP BY cp.id_pizza ORDER BY nb DESC LIMIT 1");
                    if (rs.next()) panel.add(new JLabel("Pizza la plus demandée : " + rs.getString("nom") + " (" + rs.getInt("nb") + " fois)"));
                } catch (SQLException ex) {
                    panel.add(new JLabel("Erreur SQL"));
                }
                statsFrame.add(panel);
                statsFrame.setVisible(true);
            });
            mainContent.add(btnStats, BorderLayout.NORTH);
        }

        // Bouton "Interface livreur" visible pour les livreurs
        if ("livreur".equals(role)) {
            JButton btnLivreur = new JButton("Interface livreur");
            btnLivreur.addActionListener(e -> {
                new PageLivreur().setVisible(true);
            });
            mainContent.add(btnLivreur, BorderLayout.WEST);
        }

        return mainContent;
    }
    
    public PageAccueil(int idCompteConnecte, String role) throws ClassNotFoundException {
        this.idCompteConnecte = idCompteConnecte;
        this.role = role;
        setTitle("RaPizz - Page d'Accueil");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(createMainContent(), BorderLayout.CENTER);
        setVisible(false);
    }

    private void chargerPizzasDepuisBDD(JPanel pizzaListPanel) throws ClassNotFoundException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Charger d'abord les tailles et leurs facteurs
            HashMap<String, Double> mapTailles = new HashMap<>();
            String sqlTailles = "SELECT Nom, facteur FROM Taille ORDER BY facteur";
            PreparedStatement pstmtTailles = conn.prepareStatement(sqlTailles);
            ResultSet rsTailles = pstmtTailles.executeQuery();
            
            while (rsTailles.next()) {
                String nomTaille = rsTailles.getString("Nom");
                double facteur = rsTailles.getDouble("facteur");
                mapTailles.put(nomTaille, facteur);
            }

            String sql = "SELECT p.id_pizza, p.nom AS nom_pizza, p.prix_base, i.nom AS nom_ingredient, p.chemin_image " +
                         "FROM Pizza p " +
                         "JOIN Contient pi ON p.id_pizza = pi.id_pizza " +
                         "JOIN Ingredient i ON pi.id_ingredient = i.id_ingredient " +
                         "ORDER BY p.id_pizza";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            // Stocker les ingrédients par pizza
            HashMap<Integer, ArrayList<String>> mapIngredients = new HashMap<>();
            HashMap<Integer, String> mapNomPizza = new HashMap<>();
            HashMap<Integer, Double> mapPrixPizza = new HashMap<>();
            HashMap<Integer, String> mapCheminPizza = new HashMap<>();
            while (rs.next()) {
                int idPizza = rs.getInt("id_pizza");
                String nomPizza = rs.getString("nom_pizza");
                double prixBase = rs.getDouble("prix_base");
                String nomIngredient = rs.getString("nom_ingredient");
                String chemin_image =  rs.getString("chemin_image");
                mapNomPizza.put(idPizza, nomPizza);
                mapPrixPizza.put(idPizza, prixBase);
                mapCheminPizza.put(idPizza, chemin_image);
                mapIngredients.computeIfAbsent(idPizza, k -> new ArrayList<>()).add(nomIngredient);
            }

            // Créer les cartes pour chaque pizza
            for (Integer idPizza : mapNomPizza.keySet()) {
                JPanel cardPanel = new JPanel(new BorderLayout());
                cardPanel.setPreferredSize(new Dimension(500, 300));
                cardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                cardPanel.setBackground(Color.WHITE);

                // Image de la pizza
                try {
                    InputStream is = getClass().getResourceAsStream("/images/"+mapCheminPizza.get(idPizza));
                    BufferedImage image = ImageIO.read(is);

                    // Redimensionnement
                    Image scaledImage = image.getScaledInstance(300, 300, Image.SCALE_SMOOTH);

                    // Appliquer à un JLabel
                    JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                    cardPanel.add(imageLabel, BorderLayout.WEST);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Infos de la pizza
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JLabel nomLabel = new JLabel(mapNomPizza.get(idPizza));
                nomLabel.setFont(new Font("Arial", Font.BOLD, 18));

                // Construire dynamiquement la chaîne des prix en fonction des tailles
                StringBuilder prixText = new StringBuilder();
                prixText.append("Base: ").append(String.format("%.2f", mapPrixPizza.get(idPizza))).append("€");
                
                for (Map.Entry<String, Double> entry : mapTailles.entrySet()) {
                    String nomTaille = entry.getKey();
                    double facteur = entry.getValue();
                    double prixTaille = mapPrixPizza.get(idPizza) * facteur;
                    prixText.append(" - ").append(nomTaille).append(": ").append(String.format("%.2f", prixTaille)).append("€");
                }

                JLabel prixLabel = new JLabel(prixText.toString());
                JLabel ingredientsLabel = new JLabel("<html>Ingrédients: " + String.join(", ", mapIngredients.get(idPizza)) + "</html>");

                JButton commanderButton = new JButton("Commander");
                commanderButton.setBackground(Color.RED);
                commanderButton.setForeground(Color.WHITE);

                commanderButton.addActionListener(e-> {
                    Object[] tailles = mapTailles.keySet().toArray();
                    String tailleChoisie = (String) JOptionPane.showInputDialog(
                        null,
                        "Choisissez la taille de la pizza :",
                        "Taille",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        tailles,
                        tailles[0]
                    );
                    if (tailleChoisie == null) return;
                    
                    double facteur = mapTailles.get(tailleChoisie);
                    double prix = mapPrixPizza.get(idPizza) * facteur;

                try (Connection conn2 = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    conn2.setAutoCommit(false);

                    PreparedStatement psSolde = conn2.prepareStatement("SELECT solde FROM Compte WHERE id_compte = ?");
                    psSolde.setInt(1, idCompteConnecte);
                    ResultSet rsSolde = psSolde.executeQuery();
                    if (!rsSolde.next() || rsSolde.getDouble("solde") < prix) {
                        JOptionPane.showMessageDialog(null, "Solde insuffisant !");
                        return;
                    }

                    String idCommande = "CMD" + System.currentTimeMillis();

                    PreparedStatement psTaille = conn2.prepareStatement("SELECT id_taille FROM Taille WHERE Nom = ?");
                    psTaille.setString(1, tailleChoisie);
                    ResultSet rsTaille = psTaille.executeQuery();
                    int idTaille = 1;
                    if (rsTaille.next()) idTaille = rsTaille.getInt("id_taille");

                    PreparedStatement psCmd = conn2.prepareStatement(
                        "INSERT INTO Commande (id_commande, date_commande, est_gratuite, id_compte, id_taille) VALUES (?, NOW(), 0, ?, ?)"
                    );
                    psCmd.setString(1, idCommande);
                    psCmd.setInt(2, idCompteConnecte);
                    psCmd.setInt(3, idTaille);
                    psCmd.executeUpdate();

                    PreparedStatement psCmdPizza = conn2.prepareStatement(
                        "INSERT INTO Commande_Pizza (id_commande, id_pizza) VALUES (?, ?)"
                    );
                    psCmdPizza.setString(1, idCommande);
                    psCmdPizza.setInt(2, idPizza);
                    psCmdPizza.executeUpdate();

                    PreparedStatement psMajSolde = conn2.prepareStatement(
                        "UPDATE Compte SET solde = solde - ? WHERE id_compte = ?"
                    );
                    psMajSolde.setDouble(1, prix);
                    psMajSolde.setInt(2, idCompteConnecte);
                    psMajSolde.executeUpdate();

                    PreparedStatement psLivreur = conn2.prepareStatement(
                    "SELECT id_livreur FROM Livreur ORDER BY RAND() LIMIT 1"
                    );
                    ResultSet rsLivreur = psLivreur.executeQuery();
                    int idLivreur = 1;
                    if (rsLivreur.next()) idLivreur = rsLivreur.getInt("id_livreur");

                    PreparedStatement psVehicule = conn2.prepareStatement(
                        "SELECT id_vehicule FROM Vehicule ORDER BY RAND() LIMIT 1"
                    );
                    ResultSet rsVehicule = psVehicule.executeQuery();
                    int idVehicule = 1;
                    if (rsVehicule.next()) idVehicule = rsVehicule.getInt("id_vehicule");

                    int idStatut = 1;

                    PreparedStatement psLivraison = conn2.prepareStatement(
                        "INSERT INTO Livraison (statut, heure_depart, id_commande, id_livreur, id_vehicule, id_statut) VALUES (?, NOW(), ?, ?, ?, ?)"
                    );
                    psLivraison.setString(1, "En cours");
                    psLivraison.setString(2, idCommande);
                    psLivraison.setInt(3, idLivreur);
                    psLivraison.setInt(4, idVehicule);
                    psLivraison.setInt(5, idStatut);
                    psLivraison.executeUpdate();
                    conn2.commit();
                    JOptionPane.showMessageDialog(null, "Commande passée avec succès !");
                    rafraichirPanels();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Erreur lors de la commande !");
                }
            });

                infoPanel.add(nomLabel);
                infoPanel.add(prixLabel);
                infoPanel.add(ingredientsLabel);
                infoPanel.add(Box.createVerticalGlue());
                infoPanel.add(commanderButton);

                cardPanel.add(infoPanel, BorderLayout.CENTER);

                pizzaListPanel.add(cardPanel);
                pizzaListPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Espace entre les cartes
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void rafraichirPanels() {
        rightPanel.removeAll();
        JPanel livraisonsPanel = createLivraisonsPanel(idCompteConnecte);
        JPanel commandesPanel = createCommandesPanel(idCompteConnecte);
        rightPanel.add(livraisonsPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(commandesPanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }
}