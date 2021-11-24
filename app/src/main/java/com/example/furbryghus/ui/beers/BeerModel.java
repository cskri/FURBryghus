package com.example.furbryghus.ui.beers;

public class BeerModel {
    private String Name;
    private String Type;
    private double Proof;
    private double Size;
    private String Description;
    private double CarbonEmission;
    private String ImageLink;

    private BeerModel (){};

    private BeerModel(String Name, String Type, double Proof, double Size, String Description, double CarbonEmission, String ImageLink){
        this.Name = Name;
        this.Type = Type;
        this.Proof = Proof;
        this.Size = Size;
        this.Description = Description;
        this.CarbonEmission = CarbonEmission;
        this.ImageLink = ImageLink;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public double getProof() {
        return Proof;
    }

    public void setProof(double proof) {
        Proof = proof;
    }

    public double getSize() {
        return Size;
    }

    public void setSize(double size) {
        Size = size;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public double getCarbonEmission() {
        return CarbonEmission;
    }

    public void setCarbonEmission(double carbonEmission) {
        CarbonEmission = carbonEmission;
    }

    public String getImageLink() {
        return ImageLink;
    }

    public void setImageLink(String imageLink) {
        ImageLink = imageLink;
    }
}
