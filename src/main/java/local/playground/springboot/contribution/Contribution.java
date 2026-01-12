package local.playground.springboot.contribution;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "team_contributions")
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_name")
    private String teamName;

    private String category;

    private Integer value;

    public Contribution() {
    }

    public Contribution(Long id, String teamName, String category, Integer value) {
        this.id = id;
        this.teamName = teamName;
        this.category = category;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contribution that = (Contribution) o;
        return Objects.equals(id, that.id) && Objects.equals(teamName, that.teamName) && Objects.equals(category, that.category) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, teamName, category, value);
    }
}

