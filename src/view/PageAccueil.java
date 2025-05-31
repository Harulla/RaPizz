package view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class PageAccueil extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/rapizz";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private int idCompteConnecte;
    private String role;
    private JPanel rightPanel;
    private JButton boutonRecherche = new JButton("Rechercher");
    private JPanel tableauPanelRef;
    
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

        if ("client".equals(role)) {
            JPanel livraisonsPanel = createLivraisonsPanel(idCompteConnecte);
            JPanel commandesPanel = createCommandesPanel(idCompteConnecte);

            rightPanel.add(livraisonsPanel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rightPanel.add(commandesPanel);
        } else if ("livreur".equals(role)) {
            rightPanel.add(new JLabel("Bienvenue, cher livreur !"));
        } else if ("admin".equals(role)) {
            rightPanel.add(new JLabel("Champs de recherche tableau de livraison"));
            rightPanel.add(new JLabel("Adresse :"));
            JTextField champAdresse = new JTextField(10);
            rightPanel.add(champAdresse);

            rightPanel.add(new JLabel("Nom Pizza :"));
            JTextField champPizza = new JTextField(10);
            rightPanel.add(champPizza);

            rightPanel.add(new JLabel("Nom Livreur :"));
            JTextField champLivreur = new JTextField(10);
            rightPanel.add(champLivreur);

            rightPanel.add(new JLabel("Type Véhicule :"));
            JTextField champVehicule = new JTextField(10);
            rightPanel.add(champVehicule);

            rightPanel.add(new JLabel("Statut :"));
            JTextField champStatut = new JTextField(10);
            rightPanel.add(champStatut);

            rightPanel.add(new JLabel("Date début :"));
            JTextField champDateDebut = new JTextField(10); // format : "YYYY-MM-DD"
            rightPanel.add(champDateDebut);

            rightPanel.add(new JLabel("Date fin :"));
            JTextField champDateFin = new JTextField(10); // format : "YYYY-MM-DD"
            rightPanel.add(champDateFin);

            rightPanel.add(new JLabel("Gratuit (oui/non) :"));
            JTextField champGratuit = new JTextField(5);
            rightPanel.add(champGratuit);

            rightPanel.add(new JLabel("Retard (oui/non) :"));
            JTextField champRetard = new JTextField(5);
            rightPanel.add(champRetard);
            
            rightPanel.add(boutonRecherche);
            boutonRecherche.addActionListener(e -> {
                String adresse = champAdresse.getText().trim();
                String pizza = champPizza.getText().trim();
                String livreur = champLivreur.getText().trim();
                String vehicule = champVehicule.getText().trim();
                String statut = champStatut.getText().trim();
                String dateDebut = champDateDebut.getText().trim();
                String dateFin = champDateFin.getText().trim();
                String gratuit = champGratuit.getText().trim().toLowerCase(); // "oui" ou "non"
                String retard = champRetard.getText().trim().toLowerCase();

                if (tableauPanelRef != null) {
                    afficherTableauLivraisons(tableauPanelRef, adresse, pizza, livreur, vehicule, statut, dateDebut, dateFin, gratuit, retard);
                }
            });
        }

        // Charger les pizzas et créer les cartes
        if ("client".equals(role)) {
        	chargerPizzasDepuisBDD(pizzaListPanel);
        }else if ("admin".equals(role)) {
            afficherStats(pizzaListPanel);
        }
        
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
    
    private void afficherChiffreAffaire(JPanel panel) {
	    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
	        ResultSet rs = conn.createStatement().executeQuery(
	            "SELECT SUM(P.prix_base * T.facteur) AS chiffre_affaire " +
	            "FROM Commande C " +
	            "JOIN Commande_Pizza CP ON C.id_commande=CP.id_commande " +
	            "JOIN Pizza P ON CP.id_pizza=P.id_pizza " +
	            "JOIN Taille T ON C.id_taille=T.id_taille " +
	            "WHERE C.est_gratuite=0");
	        if (rs.next()) {
	            double chiffreAffaire = rs.getDouble("chiffre_affaire");
	            JLabel label = new JLabel("Chiffre d'affaires : " + String.format("%.2f", chiffreAffaire) + " €");
	            panel.add(label);
	        }
	    } catch (SQLException e) {
	        panel.add(new JLabel("Erreur lors du chargement du chiffre d'affaires."));
	    }
	}

    
    private void afficherMeilleurClient(JPanel panel) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT nom_client, prenom_client, COUNT(*) AS nb " +
                "FROM Commande c JOIN Compte co ON c.id_compte=co.id_compte " +
                "GROUP BY c.id_compte ORDER BY nb DESC LIMIT 1");
            if (rs.next()) {
                JLabel label = new JLabel("Meilleur client : " + rs.getString("prenom_client") + " " + rs.getString("nom_client") + " (" + rs.getInt("nb") + " commandes)");
                panel.add(label);
            }
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors du chargement du meilleur client."));
        }
    }
    
    private void afficherPireLivreur(JPanel panel) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // - temps de retard > 30 min (1800s)
            String query = 
                "SELECT l.nom, v.type, COUNT(*) AS nb_retards " +
                "FROM Livraison li " +
                "JOIN Livreur l ON li.id_livreur = l.id_livreur " +
                "JOIN Vehicule v ON li.id_vehicule = v.id_vehicule " +
                "WHERE TIMESTAMPDIFF(SECOND, li.heure_depart, li.heure_arrivee) > 1800 " +
                "GROUP BY li.id_livreur, li.id_vehicule " +
                "ORDER BY nb_retards DESC LIMIT 1";
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                JLabel label = new JLabel("Plus mauvais livreur : " + rs.getString("nom") +
                    " (" + rs.getInt("nb_retards") + " retards, véhicule : " + rs.getString("type") + ")");
                panel.add(label);
            } else {
                panel.add(new JLabel("Aucun livreur n'a eu de retard enregistré."));
            }
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors du chargement du plus mauvais livreur."));
        }
    }

    
    private void afficherPizzaPlusDemande(JPanel panel) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT p.nom, COUNT(*) AS nb " +
                "FROM Commande_Pizza cp JOIN Pizza p ON cp.id_pizza=p.id_pizza " +
                "GROUP BY cp.id_pizza ORDER BY nb DESC LIMIT 1");
            if (rs.next()) {
                JLabel label = new JLabel("Pizza la plus demandée : " + rs.getString("nom") + " (" + rs.getInt("nb") + " fois)");
                panel.add(label);
            }
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors du chargement de la pizza la plus demandée."));
        }
    }

    private void afficherPizzaMoinsDemandee(JPanel panel) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT p.nom, COUNT(*) AS nb " +
                "FROM Commande_Pizza cp JOIN Pizza p ON cp.id_pizza=p.id_pizza " +
                "GROUP BY cp.id_pizza ORDER BY nb ASC LIMIT 1");
            if (rs.next()) {
                JLabel label = new JLabel("Pizza la moins demandée : " + rs.getString("nom") + " (" + rs.getInt("nb") + " fois)");
                panel.add(label);
            }
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors du chargement de la pizza la moins demandée."));
        }
    }
    
    private void afficherIngredientFavori(JPanel panel) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT i.nom, COUNT(*) AS nb " +
                "FROM contient pi " +
                "JOIN Ingredient i ON pi.id_ingredient=i.id_ingredient " +
                "JOIN Commande_Pizza cp ON pi.id_pizza=cp.id_pizza " +
                "GROUP BY i.id_ingredient ORDER BY nb DESC LIMIT 1");
            if (rs.next()) {
                JLabel label = new JLabel("Ingrédient favori : " + rs.getString("nom") + " (" + rs.getInt("nb") + " apparitions)");
                panel.add(label);
            }
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors du chargement de l'ingrédient favori." + e.getMessage()));
        }
    }
    
    private void afficherVehiculesJamaisUtilises(JPanel panel) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT v.id_vehicule, v.type " +
                "FROM Vehicule v " +
                "WHERE NOT EXISTS (SELECT 1 FROM Livraison l WHERE l.id_vehicule = v.id_vehicule)");
            StringBuilder sb = new StringBuilder("Véhicules jamais utilisés : ");
            boolean vide = true;
            while (rs.next()) {
                vide = false;
                sb.append(rs.getString("type")).append(" (id: ").append(rs.getInt("id_vehicule")).append("), ");
            }
            if (vide) sb.append("Aucun !");
            JLabel label = new JLabel(sb.toString());
            panel.add(label);
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors du chargement des véhicules inutilisés." + e.getMessage()));
        }
    }

    private void afficherClientsAuDessusDeLaMoyenne(JPanel panel) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Moyenne
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT AVG(nb) AS moyenne " +
                "FROM (SELECT COUNT(*) AS nb FROM Commande GROUP BY id_compte) sous_req");
            double moyenne = 0.0;
            if (rs.next()) {
                moyenne = rs.getDouble("moyenne");
                JLabel labelMoy = new JLabel("Moyenne de commandes par client : " + String.format("%.2f", moyenne));
                panel.add(labelMoy);
            }

            // Clients au-dessus de la moyenne
            rs = conn.createStatement().executeQuery(
                "SELECT co.nom_client, co.prenom_client, COUNT(*) AS nb " +
                "FROM Commande c JOIN Compte co ON c.id_compte=co.id_compte " +
                "GROUP BY co.id_compte HAVING nb > " + moyenne);
            StringBuilder sb = new StringBuilder("Clients au-dessus de la moyenne : ");
            boolean vide = true;
            while (rs.next()) {
                vide = false;
                sb.append(rs.getString("prenom_client")).append(" ").append(rs.getString("nom_client")).append(" (").append(rs.getInt("nb")).append("), ");
            }
            if (vide) sb.append("Aucun !");
            JLabel labelClients = new JLabel(sb.toString());
            panel.add(labelClients);

        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors du chargement des clients au-dessus de la moyenne."));
        }
    }
    
    private void afficherNombreTotalCommandes(JPanel panel) {
        String sql = "SELECT COUNT(*) FROM Commande";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int totalCommandes = rs.getInt(1);
                panel.add(new JLabel("Nombre total de commandes : " + totalCommandes));
            }
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors de la récupération des commandes."));
        }
    }
    
    private void afficherLivraisonsEnCours(JPanel panel) {
        String sql = "SELECT COUNT(*) FROM Livraison WHERE statut = 'en cours'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int livraisonsEnCours = rs.getInt(1);
                panel.add(new JLabel("Livraisons en cours : " + livraisonsEnCours));
            }
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors de la récupération des livraisons en cours."));
        }
    }
    
    private void afficherJourSemainePlusRentable(JPanel panel) {
        String sql = """
           SELECT 
			    DAYNAME(c.date_commande) AS jour,
			    SUM(p.prix_base * t.facteur) AS totalCA
			FROM Commande c
			JOIN Commande_Pizza cp ON c.id_commande = cp.id_commande
			JOIN Pizza p ON cp.id_pizza = p.id_pizza
			JOIN Taille t ON c.id_taille = t.id_taille
			WHERE c.est_gratuite = 0
			GROUP BY jour
			ORDER BY totalCA DESC
			LIMIT 1;

        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String jour = rs.getString("jour").trim();
                double totalCA = rs.getDouble("totalCA");
                panel.add(new JLabel("Jour le plus rentable : " + jour + " (" + totalCA + " €)"));
            }
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors de la récupération du jour le plus rentable." + e.getMessage()));
        }
    }


    private void afficherTempsLivraisonMoyen(JPanel panel) {
        String sql = """
			SELECT AVG(TIMESTAMPDIFF(MINUTE, heure_depart, heure_arrivee)) AS moyenneMinutes
			FROM Livraison
			WHERE heure_arrivee IS NOT NULL AND heure_depart IS NOT NULL;
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                double moyenneMinutes = rs.getDouble("moyenneMinutes");
                panel.add(new JLabel("Temps moyen de livraison : " + String.format("%.2f", moyenneMinutes) + " min"));
            }
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur lors de la récupération du temps de livraison moyen." + e.getMessage()));
        }
    }


    private void afficherTableauLivraisons(JPanel panel, 
            String adresse, String pizza, String livreur, String vehicule,
            String statut, String dateDebut, String dateFin,
            String gratuit, String retard) {
			StringBuilder query = new StringBuilder(
			"SELECT " +
			"  li.id_livraison, c.nom_client, c.prenom_client, c.adresse, " +
			"  p.nom AS nom_pizza, t.nom AS taille, " +
			"  (p.prix_base * t.facteur) AS prix, " +
			"  co.date_commande, li.heure_depart, li.heure_arrivee, " +
			"  TIMESTAMPDIFF(MINUTE, li.heure_depart, li.heure_arrivee) AS duree_livraison, " +
			"  CASE WHEN TIMESTAMPDIFF(MINUTE, li.heure_depart, li.heure_arrivee) > 30 THEN 'oui' ELSE 'non' END AS est_retard, " +
			"  CASE WHEN co.est_gratuite THEN 'oui' ELSE 'non' END AS est_gratuite, " +
			"  l.nom AS nom_livreur, v.type AS type_vehicule, v.nom AS nom_vehicule, " +
			"  li.statut " +
			"FROM Livraison li " +
			"JOIN Commande co ON li.id_commande = co.id_commande " +
			"JOIN Compte c ON co.id_compte = c.id_compte " +
			"JOIN Commande_Pizza cp ON co.id_commande = cp.id_commande " +
			"JOIN Pizza p ON cp.id_pizza = p.id_pizza " +
			"JOIN Taille t ON co.id_taille = t.id_taille " +
			"JOIN Livreur l ON li.id_livreur = l.id_livreur " +
			"JOIN Vehicule v ON li.id_vehicule = v.id_vehicule " +
			"WHERE 1=1 ");
			
			// Ajout des filtres dynamiques
			if (!adresse.isEmpty()) query.append("AND c.adresse LIKE ? ");
			if (!pizza.isEmpty()) query.append("AND p.nom LIKE ? ");
			if (!livreur.isEmpty()) query.append("AND l.nom LIKE ? ");
			if (!vehicule.isEmpty()) query.append("AND v.type LIKE ? ");
			if (!statut.isEmpty()) query.append("AND li.statut LIKE ? ");
			if (!dateDebut.isEmpty()) query.append("AND DATE(co.date_commande) >= ? ");
			if (!dateFin.isEmpty()) query.append("AND DATE(co.date_commande) <= ? ");
			if (!gratuit.isEmpty()) query.append("AND CASE WHEN co.est_gratuite THEN 'oui' ELSE 'non' END = ? ");
			if (!retard.isEmpty()) query.append("AND CASE WHEN TIMESTAMPDIFF(MINUTE, li.heure_depart, li.heure_arrivee) > 30 THEN 'oui' ELSE 'non' END = ? ");
			
			query.append("ORDER BY li.id_livraison");
			
			try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			PreparedStatement stmt = conn.prepareStatement(query.toString());
			
			int index = 1;
			if (!adresse.isEmpty()) stmt.setString(index++, "%" + adresse + "%");
			if (!pizza.isEmpty()) stmt.setString(index++, "%" + pizza + "%");
			if (!livreur.isEmpty()) stmt.setString(index++, "%" + livreur + "%");
			if (!vehicule.isEmpty()) stmt.setString(index++, "%" + vehicule + "%");
			if (!statut.isEmpty()) stmt.setString(index++, "%" + statut + "%");
			if (!dateDebut.isEmpty()) stmt.setString(index++, dateDebut);
			if (!dateFin.isEmpty()) stmt.setString(index++, dateFin);
			if (!gratuit.isEmpty()) stmt.setString(index++, gratuit);
			if (!retard.isEmpty()) stmt.setString(index++, retard);
			
			ResultSet rs = stmt.executeQuery();
			
			// Tableau
			String[] colonnes = {
			"ID Livraison", "Client", "Adresse", "Pizza (taille)", "Prix (€)",
			"Date/Heure Commande", "Durée Livraison (min)", "Retard",
			"Gratuit", "Livreur", "Véhicule", "Statut"
			};
			DefaultTableModel model = new DefaultTableModel(colonnes, 0);
			
			while (rs.next()) {
			String idLivraison = rs.getString("id_livraison");
			String client = rs.getString("prenom_client") + " " + rs.getString("nom_client");
			String adr = rs.getString("adresse");
			String piz = rs.getString("nom_pizza") + " (" + rs.getString("taille") + ")";
			double prix = rs.getDouble("prix");
			Timestamp dateCommande = rs.getTimestamp("date_commande");
			int duree = rs.getInt("duree_livraison");
			String ret = rs.getString("est_retard");
			String gra = rs.getString("est_gratuite");
			String liv = rs.getString("nom_livreur");
			String veh = rs.getString("type_vehicule") + " (" + rs.getString("nom_vehicule") + ")";
			String stat = rs.getString("statut");
			
			Object[] ligne = {
			idLivraison, client, adr, piz, String.format("%.2f", prix),
			dateCommande, duree, ret, gra, liv, veh, stat
			};
			model.addRow(ligne);
			}
			
			JTable table = new JTable(model);
			JScrollPane scrollPane = new JScrollPane(table);
			panel.removeAll();
			panel.add(scrollPane);
			panel.revalidate();
			panel.repaint();
			
			} catch (SQLException e) {
			e.printStackTrace();
			panel.add(new JLabel("Erreur lors du chargement des livraisons."));
		}
	}


    private void afficherStats(JPanel panel) {
        panel.setLayout(new BorderLayout()); // Pour un meilleur agencement général
        
        JPanel conteneurStats = new JPanel();
        conteneurStats.setLayout(new BoxLayout(conteneurStats, BoxLayout.Y_AXIS));
        conteneurStats.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Titre principal
        JLabel titre = new JLabel("Statistiques Générales");
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);
        conteneurStats.add(titre);
        conteneurStats.add(Box.createVerticalStrut(15));

        // Panel pour les chiffres clés (regroupés)
        JPanel panelChiffresCles = new JPanel();
        panelChiffresCles.setLayout(new GridLayout(0, 1, 5, 5));
        panelChiffresCles.setBorder(BorderFactory.createTitledBorder("Chiffres Clés"));
        
        afficherChiffreAffaire(panelChiffresCles);
        afficherMeilleurClient(panelChiffresCles);
        afficherPireLivreur(panelChiffresCles);
        afficherNombreTotalCommandes(panelChiffresCles);
        afficherLivraisonsEnCours(panelChiffresCles);
        afficherJourSemainePlusRentable(panelChiffresCles);
        afficherTempsLivraisonMoyen(panelChiffresCles);
        conteneurStats.add(panelChiffresCles);
        conteneurStats.add(Box.createVerticalStrut(10));

        // Panel pour les pizzas
        JPanel panelPizzas = new JPanel();
        panelPizzas.setLayout(new GridLayout(0, 1, 5, 5));
        panelPizzas.setBorder(BorderFactory.createTitledBorder("Pizzas"));
        
        afficherPizzaPlusDemande(panelPizzas);
        afficherPizzaMoinsDemandee(panelPizzas);
        conteneurStats.add(panelPizzas);
        conteneurStats.add(Box.createVerticalStrut(10));

        // Panel pour l'ingrédient favori
        JPanel panelIngredient = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelIngredient.setBorder(BorderFactory.createTitledBorder("Ingrédient Favori"));
        afficherIngredientFavori(panelIngredient);
        conteneurStats.add(panelIngredient);
        conteneurStats.add(Box.createVerticalStrut(10));

        // Panel pour les véhicules
        JPanel panelVehicules = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelVehicules.setBorder(BorderFactory.createTitledBorder("Véhicules Jamais Utilisés"));
        afficherVehiculesJamaisUtilises(panelVehicules);
        conteneurStats.add(panelVehicules);
        conteneurStats.add(Box.createVerticalStrut(10));

        // Panel pour les clients au-dessus de la moyenne
        JPanel panelClients = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelClients.setBorder(BorderFactory.createTitledBorder("Clients Exceptionnels"));
        afficherClientsAuDessusDeLaMoyenne(panelClients);
        conteneurStats.add(panelClients);
        conteneurStats.add(Box.createVerticalStrut(10));

        // Ajout du conteneurStats en haut
        panel.add(conteneurStats, BorderLayout.NORTH);

        // Tableau des livraisons (mis dans un panneau séparé avec scroll)
        JPanel panelTableau = new JPanel(new BorderLayout());
        panelTableau.setBorder(BorderFactory.createTitledBorder("Tableau des Livraisons"));
        tableauPanelRef = panelTableau;
        afficherTableauLivraisons(tableauPanelRef, "", "", "", "", "", "", "", "", "");

        panel.add(panelTableau, BorderLayout.CENTER);
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