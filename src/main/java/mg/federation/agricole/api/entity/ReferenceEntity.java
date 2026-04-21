// entity/ReferenceEntity.java
package mg.federation.agricole.api.entity;

import java.time.LocalDate;

public class ReferenceEntity {
    private Long candidateId;
    private Long sponsorId;
    private String relationNature;
    private LocalDate sponsorshipDate;

    public ReferenceEntity(Long candidateId, Long sponsorId, String relationNature, LocalDate sponsorshipDate) {
        this.candidateId = candidateId;
        this.sponsorId = sponsorId;
        this.relationNature = relationNature;
        this.sponsorshipDate = sponsorshipDate;
    }
    public ReferenceEntity() {}

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Long getSponsorId() {
        return sponsorId;
    }

    public void setSponsorId(Long sponsorId) {
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