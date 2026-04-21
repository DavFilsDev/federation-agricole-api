package mg.federation.agricole.api.dto;

import java.util.List;

public class Member extends MemberInformation {
    private String id;      // identifiant sous forme de chaîne (conversion Long→String)
    private List<Member> referees;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Member> getReferees() {
        return referees;
    }

    public void setReferees(List<Member> referees) {
        this.referees = referees;
    }
}