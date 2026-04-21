package mg.federation.agricole.api.dto;

public class CollectivityStructure {
    private Member president;
    private Member vicePresident;
    private Member treasurer;
    private Member secretary;

    public Member getPresident() {
        return president;
    }

    public void setPresident(Member president) {
        this.president = president;
    }

    public Member getVicePresident() {
        return vicePresident;
    }

    public void setVicePresident(Member vicePresident) {
        this.vicePresident = vicePresident;
    }

    public Member getTreasurer() {
        return treasurer;
    }

    public void setTreasurer(Member treasurer) {
        this.treasurer = treasurer;
    }

    public Member getSecretary() {
        return secretary;
    }

    public void setSecretary(Member secretary) {
        this.secretary = secretary;
    }
}