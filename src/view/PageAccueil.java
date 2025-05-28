package view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class PageAccueil extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/rapizz";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private JPanel createLivraisonsPanel() {
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
                         "WHERE l.heure_arrivee IS NULL"; // Livraisons en cours

            PreparedStatement pstmt = conn.prepareStatement(sql);
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

    private JPanel createCommandesPanel() {
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
                         "ORDER BY c.date_commande DESC";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

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

    
    public PageAccueil() throws ClassNotFoundException {
        setTitle("RaPizz - Page d'Accueil");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Titre de la page
        JLabel titleLabel = new JLabel("Bienvenue chez RaPizz !", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Panel principal avec un scroll pour les pizzas
        JPanel pizzaListPanel = new JPanel();
        pizzaListPanel.setLayout(new BoxLayout(pizzaListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(pizzaListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Panel de droite pour les livraisons et commandes
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(400, 600));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel des livraisons en cours
        JPanel livraisonsPanel = createLivraisonsPanel();
        
        // Panel des commandes passées
        JPanel commandesPanel = createCommandesPanel();

        rightPanel.add(livraisonsPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(commandesPanel);

        // Charger les pizzas et créer les cartes
        chargerPizzasDepuisBDD(pizzaListPanel);

        // Layout final
        setLayout(new BorderLayout());
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        setVisible(true);
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
                commanderButton.setBackground(Color.BLUE);
                commanderButton.setForeground(Color.WHITE);

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new PageAccueil();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }
}