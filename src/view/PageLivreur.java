package view;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PageLivreur extends JFrame {
    private JPanel panel;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/rapizz";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    public PageLivreur() {
        setTitle("Interface Livreur");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        rafraichirLivraisons();

        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void rafraichirLivraisons() {
        panel.removeAll();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT l.id_livraison, l.statut, l.heure_depart, l.heure_arrivee, c.id_commande " +
                         "FROM Livraison l JOIN Commande c ON l.id_commande = c.id_commande WHERE l.statut != 'Livrée'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int idLivraison = rs.getInt("id_livraison");
                String statut = rs.getString("statut");
                String idCommande = rs.getString("id_commande");

                JPanel livraisonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                livraisonPanel.add(new JLabel("Livraison #" + idLivraison + " | Statut: " + statut + " | Commande: " + idCommande));

                JButton livrerButton = new JButton("Livrer");
                livrerButton.addActionListener(e -> {
                    try (Connection conn2 = DriverManager.getConnection(DB_URL,DB_USER, DB_PASSWORD)) {
                        PreparedStatement ps2 = conn2.prepareStatement(
                            "UPDATE Livraison SET heure_arrivee = NOW(), statut = 'Livrée' WHERE id_livraison = ?"
                        );
                        ps2.setInt(1, idLivraison);
                        ps2.executeUpdate();
                        // Rafraîchir la liste après livraison
                        rafraichirLivraisons();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Erreur lors de la livraison !");
                    }
                });
                livraisonPanel.add(livrerButton);

                panel.add(livraisonPanel);
            }
        } catch (SQLException e) {
            panel.add(new JLabel("Erreur SQL"));
        }
        panel.revalidate();
        panel.repaint();
    }
}