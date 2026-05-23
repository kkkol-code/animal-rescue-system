package com.animalrescue.entity;

public class Animal {

    private Long animalId;
    private String name;
    private String species;       // 猫 / 狗
    private String breed;         // 品种
    private String gender;        // 公 / 母
    private String adoptStatus;   // pending / adopted / sick / treating
    private String entryDate;

    // getter & setter
    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAdoptStatus() { return adoptStatus; }
    public void setAdoptStatus(String adoptStatus) { this.adoptStatus = adoptStatus; }
    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }
}
