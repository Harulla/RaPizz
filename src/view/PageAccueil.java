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

    private static final String DB_URL = "jdbc:mysql://localhost:3306/cretable";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public PageAccueil() throws ClassNotFoundException {
        setTitle("RaPizz - Page d'Accueil");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Titre de la page
        JLabel titleLabel = new JLabel("Bienvenue chez RaPizz !", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Panel principal avec un scroll
        JPanel pizzaListPanel = new JPanel();
        pizzaListPanel.setLayout(new BoxLayout(pizzaListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(pizzaListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Charger les pizzas et créer les cartes
        chargerPizzasDepuisBDD(pizzaListPanel);

        // Layout final
        setLayout(new BorderLayout());
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void chargerPizzasDepuisBDD(JPanel pizzaListPanel) throws ClassNotFoundException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String sql = "SELECT p.id_pizza, p.nom AS nom_pizza, p.prix_base, i.nom AS nom_ingredient " +
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

            while (rs.next()) {
                int idPizza = rs.getInt("id_pizza");
                String nomPizza = rs.getString("nom_pizza");
                double prixBase = rs.getDouble("prix_base");
                String nomIngredient = rs.getString("nom_ingredient");

                mapNomPizza.put(idPizza, nomPizza);
                mapPrixPizza.put(idPizza, prixBase);

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
                    InputStream is = getClass().getResourceAsStream("/images/reine.png");
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

                JLabel prixLabel = new JLabel("Base: " + mapPrixPizza.get(idPizza) + "€ - Naine: 6.2€ - Ogresse: 12€");
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
