// fichier UserSession.java
package model;

public class UserSession {
    private static UserSession instance;
    private int idCompte;
    private String login;
    private String nom;
    private String prenom;
    private String adresse;

    private UserSession(int idCompte, String login, String nom, String prenom, String adresse) {
        this.idCompte = idCompte;
        this.login = login;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
    }

    public static void createSession(int idCompte, String login, String nom, String prenom, String adresse) {
        instance = new UserSession(idCompte, login, nom, prenom, adresse);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static void clearSession() {
        instance = null;
    }

    public int getIdCompte() {
        return idCompte;
    }

    public String getLogin() {
        return login;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }
    
    public String getAdresse() {
        return adresse;
    }
}
