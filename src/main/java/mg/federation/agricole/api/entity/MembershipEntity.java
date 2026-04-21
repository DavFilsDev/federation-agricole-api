// entity/MembershipEntity.java
package mg.federation.agricole.api.entity;

import java.time.LocalDate;

public class MembershipEntity {
    private Long memberId;
    private Long collectivityId;
    private String occupation;
    private Boolean registrationFeePaid;
    private Boolean membershipDuesPaid;
    private LocalDate dateAdhesion;
    private LocalDate paymentDate;

    public MembershipEntity() {
    }

    public MembershipEntity(Long memberId, Long collectivityId, String occupation, Boolean registrationFeePaid, Boolean membershipDuesPaid, LocalDate dateAdhesion, LocalDate paymentDate) {
        this.memberId = memberId;
        this.collectivityId = collectivityId;
        this.occupation = occupation;
        this.registrationFeePaid = registrationFeePaid;
        this.membershipDuesPaid = membershipDuesPaid;
        this.dateAdhesion = dateAdhesion;
        this.paymentDate = paymentDate;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getCollectivityId() {
        return collectivityId;
    }

    public void setCollectivityId(Long collectivityId) {
        this.collectivityId = collectivityId;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public Boolean getRegistrationFeePaid() {
        return registrationFeePaid;
    }

    public void setRegistrationFeePaid(Boolean registrationFeePaid) {
        this.registrationFeePaid = registrationFeePaid;
    }

    public Boolean getMembershipDuesPaid() {
        return membershipDuesPaid;
    }

    public void setMembershipDuesPaid(Boolean membershipDuesPaid) {
        this.membershipDuesPaid = membershipDuesPaid;
    }

    public LocalDate getDateAdhesion() {
        return dateAdhesion;
    }

    public void setDateAdhesion(LocalDate dateAdhesion) {
        this.dateAdhesion = dateAdhesion;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
}