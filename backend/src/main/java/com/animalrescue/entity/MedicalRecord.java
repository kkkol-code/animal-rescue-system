package com.animalrescue.entity;

public class MedicalRecord {
    private Long recordId;
    private Long animalId;
    private String checkDate;
    private String type;
    private String vetName;
    private Double weight;
    private Double temperature;
    private String diagnosis;
    private String treatment;
    private String createdAt;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }
    public String getCheckDate() { return checkDate; }
    public void setCheckDate(String checkDate) { this.checkDate = checkDate; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getVetName() { return vetName; }
    public void setVetName(String vetName) { this.vetName = vetName; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
