package com.animalrescue.entity;

public class Adopter {

    private Long adopterId;
    private String realName;
    private String phone;
    private String idCard;
    private String houseCondition;

    public Long getAdopterId() { return adopterId; }
    public void setAdopterId(Long adopterId) { this.adopterId = adopterId; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getHouseCondition() { return houseCondition; }
    public void setHouseCondition(String houseCondition) { this.houseCondition = houseCondition; }
}
