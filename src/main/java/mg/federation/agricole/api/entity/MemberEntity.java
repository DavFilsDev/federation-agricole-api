package mg.federation.agricole.api.entity;

import java.time.LocalDate;

public class MemberEntity {
    private String id;  // Long → String
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender; // MALE, FEMALE
    private String address;
    private String profession;
    private String phoneNumber;
    private String email;
    private LocalDate dateAdhesionFederation;

    // getters/setters

    public String getId() {  // Long → String
        return id;
    }

    public void setId(String id) {  // Long → String
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateAdhesionFederation() {
        return dateAdhesionFederation;
    }

    public void setDateAdhesionFederation(LocalDate dateAdhesionFederation) {
        this.dateAdhesionFederation = dateAdhesionFederation;
    }

    // constructeurs

    public MemberEntity(String id, String firstName, String lastName, LocalDate birthDate, String gender, String address, String profession, String phoneNumber, String email, LocalDate dateAdhesionFederation) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.profession = profession;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.dateAdhesionFederation = dateAdhesionFederation;
    }

    public MemberEntity() {

    }
}