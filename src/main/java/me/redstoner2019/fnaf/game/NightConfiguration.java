package me.redstoner2019.fnaf.game;

import me.redstoner2019.fnaf.game.animatronics.Animatronic;

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

    public static NightConfiguration getNight(int night){
        NightConfiguration configuration = new NightConfiguration(0,0,0,0);
        configuration.setNightNumber(night);
        System.out.println("Getting night " + night);
        switch (night) {
            case 1 -> {
                configuration.setFreddyAI(0);
                configuration.setBonnieAI(0);
                configuration.setChicaAI(0);
                configuration.setFoxyAI(0);
            }
            case 2 -> {
                configuration.setFreddyAI(0);
                configuration.setBonnieAI(3);
                configuration.setChicaAI(1);
                configuration.setFoxyAI(1);
            }
            case 3 -> {
                configuration.setFreddyAI(1);
                configuration.setBonnieAI(0);
                configuration.setChicaAI(5);
                configuration.setFoxyAI(2);
            }
            case 4 -> {
                configuration.setFreddyAI(Math.random() > 0.5 ? 1 : 2);
                configuration.setBonnieAI(2); //5
                configuration.setChicaAI(4); //6
                configuration.setFoxyAI(6); //8
            }
            case 5 -> {
                configuration.setFreddyAI(3);
                configuration.setBonnieAI(5);
                configuration.setChicaAI(7);
                configuration.setFoxyAI(5);
            }
            case 6 -> {
                configuration.setFreddyAI(4);
                configuration.setBonnieAI(10);
                configuration.setChicaAI(12);
                configuration.setFoxyAI(16);
            }
            case 8 -> {
                configuration.setFreddyAI(20);
                configuration.setBonnieAI(20);
                configuration.setChicaAI(20);
                configuration.setFoxyAI(20);

                configuration.setIdleUsage(configuration.getIdleUsage()/2.4f);
                configuration.setNightLength(648000);

                configuration.setFreddyMovementSpeed(2920);
                configuration.setBonnieMovementSpeed(3970);
                configuration.setChicaMovementSpeed(3980);
                configuration.setFoxyMovementSpeed(4010);
            }
            default -> {
                configuration.setFreddyAI(2);
                configuration.setBonnieAI(2);
                configuration.setChicaAI(2);
                configuration.setFoxyAI(2);
            }
        }
        return configuration;
    }

    @Override
    public String toString() {
        return "NightConfiguration{" +
                "freddyAI=" + freddyAI +
                ", bonnieAI=" + bonnieAI +
                ", chicaAI=" + chicaAI +
                ", foxyAI=" + foxyAI +
                ", freddyMovementSpeed=" + freddyMovementSpeed +
                ", bonnieMovementSpeed=" + bonnieMovementSpeed +
                ", chicaMovementSpeed=" + chicaMovementSpeed +
                ", foxyMovementSpeed=" + foxyMovementSpeed +
                ", am1Increases=" + am1Increases +
                ", am2Increases=" + am2Increases +
                ", am3Increases=" + am3Increases +
                ", am4Increases=" + am4Increases +
                ", am5Increases=" + am5Increases +
                ", nightLength=" + nightLength +
                ", endlessNight=" + endlessNight +
                ", nightNumber=" + nightNumber +
                ", idleUsage=" + idleUsage +
                '}';
    }
}
