package mg.federation.agricole.api.dto;

import java.time.LocalDate;
import java.util.List;

public class CollectivityActivity extends CreateCollectivityActivity {
    private String id;

    public CollectivityActivity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}