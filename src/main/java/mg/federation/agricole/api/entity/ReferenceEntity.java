// entity/ReferenceEntity.java
package mg.federation.agricole.api.entity;

import java.time.LocalDate;

public class ReferenceEntity {
    private String candidateId;  // Long → String (C1-M1, C1-M2, etc.)
    private String sponsorId;    // Long → String (C1-M1, C1-M2, etc.)
    private String relationNature;
    private LocalDate sponsorshipDate;

    public ReferenceEntity(String candidateId, String sponsorId, String relationNature, LocalDate sponsorshipDate) {
        this.candidateId = candidateId;
        this.sponsorId = sponsorId;
        this.relationNature = relationNature;
        this.sponsorshipDate = sponsorshipDate;
    }

    public ReferenceEntity() {}

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getSponsorId() {
        return sponsorId;
    }

    public void setSponsorId(String sponsorId) {
        this.sponsorId = sponsorId;
    }

    public String getRelationNature() {
        return relationNature;
    }

    public void setRelationNature(String relationNature) {
        this.relationNature = relationNature;
    }

    public LocalDate getSponsorshipDate() {
        return sponsorshipDate;
    }

    public void setSponsorshipDate(LocalDate sponsorshipDate) {
        this.sponsorshipDate = sponsorshipDate;
    }
}