package mg.federation.agricole.api.entity;

import java.time.LocalDate;

public class CollectivityEntity {
    private String id;  // Long → String
    private String location;
    private String specialiteAgricole;
    private Integer annualDuesAmount;
    private LocalDate dateCreation;
    private Boolean federationApproval;
    private String name;
    private Integer number;

    // Constructeur modifié : id de type String
    public CollectivityEntity(String id, String location, String specialiteAgricole, Integer annualDuesAmount, LocalDate dateCreation, Boolean federationApproval) {
        this.id = id;
        this.location = location;
        this.specialiteAgricole = specialiteAgricole;
        this.annualDuesAmount = annualDuesAmount;
        this.dateCreation = dateCreation;
        this.federationApproval = federationApproval;
    }

    public CollectivityEntity() {}

    // Getter et Setter modifiés
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSpecialiteAgricole() {
        return specialiteAgricole;
    }

    public void setSpecialiteAgricole(String specialiteAgricole) {
        this.specialiteAgricole = specialiteAgricole;
    }

    public Integer getAnnualDuesAmount() {
        return annualDuesAmount;
    }

    public void setAnnualDuesAmount(Integer annualDuesAmount) {
        this.annualDuesAmount = annualDuesAmount;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Boolean getFederationApproval() {
        return federationApproval;
    }

    public void setFederationApproval(Boolean federationApproval) {
        this.federationApproval = federationApproval;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}