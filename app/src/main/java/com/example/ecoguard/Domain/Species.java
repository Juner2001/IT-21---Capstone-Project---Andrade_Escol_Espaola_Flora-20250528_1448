package com.example.ecoguard.Domain;

import java.io.Serializable;
import java.util.List;

public class Species implements Serializable {
    private int CategoryId;
    private String Description;
    private boolean Popular;  // Renamed from BestFood to Popular
    private int Id;


    private String ImagePath;
    private int TimeId;
    private int TimeValue;
    private String Title;

    private String FN;
    private String SN;
    private String EN;
    private String TG;
    private String CN;
    private String VN;
    private String ngo;

    private String law;        // Legal status or relevant law
    private String size;       // Size information
    private String status;     // Conservation status
    private String lifespan;   // Lifespan information

    // New field para sa multiple images
    private List<String> imagePaths;

    public Species() {
    }

    @Override
    public String toString() {
        return Title;
    }

    public int getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(int categoryId) {
        CategoryId = categoryId;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public boolean isPopular() {
        return Popular;
    }

    public void setPopular(boolean popular) {
        Popular = popular;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }



    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }



    public int getTimeId() {
        return TimeId;
    }

    public void setTimeId(int timeId) {
        TimeId = timeId;
    }

    public int getTimeValue() {
        return TimeValue;
    }

    public void setTimeValue(int timeValue) {
        TimeValue = timeValue;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }



    public String getFN() {
        return FN;
    }

    public void setFN(String fn) {
        FN = fn;
    }

    public String getSN() {
        return SN;
    }

    public void setSN(String sn) {
        SN = sn;
    }

    public String getEN() {
        return EN;
    }

    public void setEN(String en) {
        EN = en;
    }

    public String getTG() {
        return TG;
    }

    public void setTG(String tg) {
        TG = tg;
    }

    public String getCN() {
        return CN;
    }

    public void setCN(String cn) {
        CN = cn;
    }

    public String getVN() {
        return VN;
    }

    public void setVN(String vn) {
        VN = vn;
    }

    public String getNgo() {
        return ngo;
    }

    public void setNgo(String ngo) {
        this.ngo = ngo;
    }



    public String getLaw() {
        return law;
    }

    public void setLaw(String law) {
        this.law = law;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLifespan() {
        return lifespan;
    }

    public void setLifespan(String lifespan) {
        this.lifespan = lifespan;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }
}