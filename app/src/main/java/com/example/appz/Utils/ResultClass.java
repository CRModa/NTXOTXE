package com.example.appz.Utils;

public class ResultClass {
    private int indexClasse;
    private float probability;
    private String nameClass;

    public ResultClass(int indexClasse, float probability, String nameClass) {
        this.indexClasse = indexClasse;
        this.probability = probability;
        this.nameClass = nameClass;
    }

    public ResultClass() {
    }

    public int getIndexClasse() {
        return indexClasse;
    }

    public void setIndexClasse(int indexClasse) {
        this.indexClasse = indexClasse;
    }

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }

    public String getNameClass() {
        return nameClass;
    }

    public void setNameClass(String nameClass) {
        this.nameClass = nameClass;
    }

    @Override
    public String toString() {

        String formattedNumber = String.format("%.2f", probability * 100);
        return nameClass +
                ": " + formattedNumber + "%";
    }
}
