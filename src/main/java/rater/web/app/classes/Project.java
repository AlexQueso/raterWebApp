package rater.web.app.classes;

import javax.persistence.*;
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String description;
    private File pathToDirectory;
    @OneToMany(cascade=CascadeType.ALL)
    private List<Report> reports;
    @Lob
    @Column(length=1048576) //1MB
    private byte[] referenceFile;
    @Lob
    @Column(length=524288) //500KB
    private byte[] jplagReport;

    public Project(){}

    public Project (String name){
        this.name = name;
        reports = new LinkedList<>();
    }

    public Project(String name, String description, File pathToDirectory) {
        this.name = name;
        this.description = description;
        this.pathToDirectory = pathToDirectory;
        reports = new LinkedList<>();
    }

    public Project(String name, File pathToDirectory) {
        this.name = name;
        this.description = " ";
        this.pathToDirectory = pathToDirectory;
        reports = new LinkedList<>();
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

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public byte[] getReferenceFile() {
        return referenceFile;
    }

    public void setReferenceFile(byte[] referenceFile) {
        this.referenceFile = referenceFile;
    }

    public byte[] getJplagReport() {
        return jplagReport;
    }

    public void setJplagReport(byte[] jplagReport) {
        this.jplagReport = jplagReport;
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
