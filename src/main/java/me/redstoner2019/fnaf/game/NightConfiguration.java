package me.redstoner2019.fnaf.game;

import me.redstoner2019.fnaf.game.animatronics.Animatronic;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NightConfiguration {
    private int freddyAI;
    private int bonnieAI;
    private int chicaAI;
    private int foxyAI;
    private int freddyMovementSpeed = 3820;
    private int bonnieMovementSpeed = 4970;
    private int chicaMovementSpeed = 4980;
    private int foxyMovementSpeed = 5010;
    private List<Animatronic> am1Increases = new ArrayList<>();
    private List<Animatronic> am2Increases = new ArrayList<>();
    private List<Animatronic> am3Increases = new ArrayList<>();
    private List<Animatronic> am4Increases = new ArrayList<>();
    private List<Animatronic> am5Increases = new ArrayList<>();
    private long nightLength;
    private boolean endlessNight = false;
    private int nightNumber = 1;
    private float idleUsage = .0025f;
    private String challenge = null;
    private double goldenFreddyChance = 0;

    public void reset(){
        freddyMovementSpeed = 3820;
        bonnieMovementSpeed = 4970;
        chicaMovementSpeed = 4980;
        foxyMovementSpeed = 5010;
        idleUsage = .0025f;
        challenge = null;
        endlessNight = false;
        nightLength = 6 * 60000;
    }

    public NightConfiguration(JSONObject json){
        this.freddyAI = json.getInt("freddyAI");
        this.bonnieAI = json.getInt("bonnieAI");
        this.chicaAI = json.getInt("chicaAI");
        this.foxyAI = json.getInt("foxyAI");
        this.freddyMovementSpeed = json.getInt("freddyMovementSpeed");
        this.bonnieMovementSpeed = json.getInt("bonnieMovementSpeed");
        this.chicaMovementSpeed = json.getInt("chicaMovementSpeed");
        this.foxyMovementSpeed = json.getInt("foxyMovementSpeed");
        this.idleUsage = json.getInt("idleUsage");
        this.challenge = json.getString("challenge");
        this.endlessNight = json.getBoolean("endlessNight");
        this.nightNumber = json.getInt("nightNumber");
        this.nightLength = json.getLong("nightLength");
    }

    public NightConfiguration(int freddyAI, int bonnieAI, int chicaAI, int foxyAI) {
        this.freddyAI = freddyAI;
        this.bonnieAI = bonnieAI;
        this.chicaAI = chicaAI;
        this.foxyAI = foxyAI;
    }

    public NightConfiguration(int freddyAI, int bonnieAI, int chicaAI, int foxyAI, int freddyMovementSpeed, int bonnieMovementSpeed, int chicaMovementSpeed, int foxyMovementSpeed) {
        this.freddyAI = freddyAI;
        this.bonnieAI = bonnieAI;
        this.chicaAI = chicaAI;
        this.foxyAI = foxyAI;
        this.freddyMovementSpeed = freddyMovementSpeed;
        this.bonnieMovementSpeed = bonnieMovementSpeed;
        this.chicaMovementSpeed = chicaMovementSpeed;
        this.foxyMovementSpeed = foxyMovementSpeed;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public int getNightNumber() {
        return nightNumber;
    }

    public void setNightNumber(int nightNumber) {
        this.nightNumber = nightNumber;
    }

    public List<Animatronic> getAm1Increases() {
        return am1Increases;
    }

    public void setAm1Increases(List<Animatronic> am1Increases) {
        this.am1Increases = am1Increases;
    }

    public List<Animatronic> getAm2Increases() {
        return am2Increases;
    }

    public void setAm2Increases(List<Animatronic> am2Increases) {
        this.am2Increases = am2Increases;
    }

    public List<Animatronic> getAm3Increases() {
        return am3Increases;
    }

    public void setAm3Increases(List<Animatronic> am3Increases) {
        this.am3Increases = am3Increases;
    }

    public List<Animatronic> getAm4Increases() {
        return am4Increases;
    }

    public void setAm4Increases(List<Animatronic> am4Increases) {
        this.am4Increases = am4Increases;
    }

    public List<Animatronic> getAm5Increases() {
        return am5Increases;
    }

    public void setAm5Increases(List<Animatronic> am5Increases) {
        this.am5Increases = am5Increases;
    }

    public long getNightLength() {
        return nightLength;
    }

    public void setNightLength(long nightLength) {
        this.nightLength = nightLength;
    }

    public boolean isEndlessNight() {
        return endlessNight;
    }

    public void setEndlessNight(boolean endlessNight) {
        this.endlessNight = endlessNight;
    }

    public int getFreddyAI() {
        return freddyAI;
    }

    public void setFreddyAI(int freddyAI) {
        this.freddyAI = freddyAI;
    }

    public int getBonnieAI() {
        return bonnieAI;
    }

    public void setBonnieAI(int bonnieAI) {
        this.bonnieAI = bonnieAI;
    }

    public int getChicaAI() {
        return chicaAI;
    }

    public void setChicaAI(int chicaAI) {
        this.chicaAI = chicaAI;
    }

    public int getFoxyAI() {
        return foxyAI;
    }

    public void setFoxyAI(int foxyAI) {
        this.foxyAI = foxyAI;
    }

    public int getFreddyMovementSpeed() {
        return freddyMovementSpeed;
    }

    public void setFreddyMovementSpeed(int freddyMovementSpeed) {
        this.freddyMovementSpeed = freddyMovementSpeed;
    }

    public int getBonnieMovementSpeed() {
        return bonnieMovementSpeed;
    }

    public void setBonnieMovementSpeed(int bonnieMovementSpeed) {
        this.bonnieMovementSpeed = bonnieMovementSpeed;
    }

    public int getChicaMovementSpeed() {
        return chicaMovementSpeed;
    }

    public void setChicaMovementSpeed(int chicaMovementSpeed) {
        this.chicaMovementSpeed = chicaMovementSpeed;
    }

    public int getFoxyMovementSpeed() {
        return foxyMovementSpeed;
    }

    public void setFoxyMovementSpeed(int foxyMovementSpeed) {
        this.foxyMovementSpeed = foxyMovementSpeed;
    }

    public float getIdleUsage() {
        return idleUsage;
    }

    public void setIdleUsage(float idleUsage) {
        this.idleUsage = idleUsage;
    }

    public double getGoldenFreddyChance() {
        return goldenFreddyChance;
    }

    public void setGoldenFreddyChance(double goldenFreddyChance) {
        this.goldenFreddyChance = goldenFreddyChance;
    }

    public JSONObject convertToJSON(){
        JSONObject json = new JSONObject();
        json.put("freddyAI", freddyAI);
        json.put("bonnieAI", bonnieAI);
        json.put("chicaAI", chicaAI);
        json.put("foxyAI", foxyAI);
        json.put("freddyMovementSpeed", freddyMovementSpeed);
        json.put("bonnieMovementSpeed", bonnieMovementSpeed);
        json.put("chicaMovementSpeed", chicaMovementSpeed);
        json.put("foxyMovementSpeed", foxyMovementSpeed);
        json.put("idleUsage", idleUsage);
        json.put("nightLength", nightLength);
        json.put("endlessNight", endlessNight);
        json.put("nightNumber", nightNumber);
        json.put("challenge",challenge);
        return json;
    }

    public static NightConfiguration getNight(int night){
        NightConfiguration configuration = new NightConfiguration(0,0,0,0);
        configuration.setEndlessNight(false);
        configuration.setNightNumber(night);
        configuration.setNightLength(6*60000); //Length of night in ms
        System.out.println("Getting night " + night);
        System.out.println();
        switch (night) {
            case 1 -> {
                configuration.setFreddyAI(0);
                configuration.setBonnieAI(0);
                configuration.setChicaAI(0);
                configuration.setFoxyAI(0);
                configuration.setGoldenFreddyChance(0);
            }
            case 2 -> {
                configuration.setFreddyAI(0);
                configuration.setBonnieAI(3);
                configuration.setChicaAI(1);
                configuration.setFoxyAI(1);
                configuration.setGoldenFreddyChance(0);
            }
            case 3 -> {
                configuration.setFreddyAI(1);
                configuration.setBonnieAI(0);
                configuration.setChicaAI(5);
                configuration.setFoxyAI(2);
                configuration.setGoldenFreddyChance(0);
            }
            case 4 -> {
                configuration.setFreddyAI(Math.random() > 0.5 ? 1 : 2);
                configuration.setBonnieAI(2); //5
                configuration.setChicaAI(4); //6
                configuration.setFoxyAI(6); //8
                configuration.setGoldenFreddyChance(0.001);
            }
            case 5 -> {
                configuration.setFreddyAI(3);
                configuration.setBonnieAI(5);
                configuration.setChicaAI(7);
                configuration.setFoxyAI(5);
                configuration.setGoldenFreddyChance(0.002);
            }
            case 6 -> {
                configuration.setFreddyAI(4);
                configuration.setBonnieAI(10);
                configuration.setChicaAI(12);
                configuration.setFoxyAI(6);
                configuration.setChallenge("night_6");
                configuration.setGoldenFreddyChance(0.005);
            }
            case 8 -> {
                configuration.setFreddyAI(20);
                configuration.setBonnieAI(20);
                configuration.setChicaAI(20);
                configuration.setFoxyAI(20);

                configuration.setIdleUsage(configuration.getIdleUsage()/1.78f);
                configuration.setNightLength(648000);

                configuration.setFreddyMovementSpeed(2920);
                configuration.setBonnieMovementSpeed(3970);
                configuration.setChicaMovementSpeed(3980);
                configuration.setFoxyMovementSpeed(4010);

                configuration.setGoldenFreddyChance(0);
                configuration.setChallenge("ventablack");
            }
            default -> {
                configuration.setFreddyAI(2);
                configuration.setBonnieAI(2);
                configuration.setChicaAI(2);
                configuration.setFoxyAI(2);
                configuration.setGoldenFreddyChance(0);
            }
        }
        return configuration;
    }

    @Override
    public String toString() {
        System.out.println("Challenge " + challenge);
        return convertToJSON().toString();
    }
}
