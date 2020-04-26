package rater.web.app.classes;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.File;
import java.util.Objects;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String description;
    private File pathToDirectory;

    public Project(){}

    public Project (String name){
        this.name = name;
    }

    public Project(String name, String description, File pathToDirectory) {
        this.name = name;
        this.description = description;
        this.pathToDirectory = pathToDirectory;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getPathToDirectory() {
        return pathToDirectory;
    }

    public void setPathToDirectory(File pathToDirectory) {
        this.pathToDirectory = pathToDirectory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return id == project.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
