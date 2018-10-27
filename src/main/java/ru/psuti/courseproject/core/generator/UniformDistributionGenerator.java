package ru.psuti.courseproject.core.generator;

import ru.psuti.courseproject.core.pojo.CalculatedDataObject;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.util.*;

public class UniformDistributionGenerator implements Generator {

    private static final double LeftLimit = 0.0; // Левая граница
    private static final double RightLimit = 1.0; // Правая граница
    private static final double Mean = (LeftLimit + RightLimit) / 2;
    private List<Double> generatedRandomValues;  // Список сгенерированных случайных величин
    private List<CalculatedDataObject> calculatedData;  // Список объектов рассчитанных величин
    private UniformRealDistribution uniformDistribution; // Закон распределения

    public UniformDistributionGenerator() {
        generatedRandomValues = new ArrayList<>();
        calculatedData = new ArrayList<>();
        uniformDistribution = new UniformRealDistribution(LeftLimit, RightLimit);
    }

    public List<Double> getGeneratedRandomValues(int sampleSize) {
        for (int i = 0; i < sampleSize; i++) {
            generatedRandomValues.add(Double.valueOf(
                    String.format(Locale.US, "%.3f", uniformDistribution.sample())
            ));
        }
        generatedRandomValues.sort(Double::compareTo);
        return generatedRandomValues;
    }


    public List<CalculatedDataObject> getCalculatedData() {
        double a1Theo = (LeftLimit + RightLimit) / 2; // Математическое ожидание (a1)
        double a1Stat = getMathWait(1); // Сумма частного случайной величины к общему количеству случайных величин. (a1)

        double m2Theo = Math.pow((RightLimit - LeftLimit), 2) / 12; // Второй центральный момент = Дисперсия
        double m2Stat = getStatCentralMoment(2);

        double m3Theo = 0; // Третий центральный момент
        double m3Stat = 0;

        double m4Theo = Math.pow((RightLimit - LeftLimit), 4) / 80; // Четвертый центральный момент
        double m4Stat = getStatCentralMoment(4);

        double sr1Theo = Math.sqrt(m2Theo); // Среднеквадратичное отклонение

        double AsTheo = 0.0; // Ассиметрия
        double AsStat = m3Stat / Math.pow(Math.sqrt(m2Theo), 3);

        double EkTheo = m4Theo / Math.pow(sr1Theo, 4) - 3; // Эксцесс
        double EkStat = (m4Stat / Math.pow(Math.sqrt(m2Theo), 4)) - 3;

        double chi2 = getChi2();

        calculatedData.add(new CalculatedDataObject("a1", a1Theo, a1Stat));
        calculatedData.add(new CalculatedDataObject("m2", m2Theo, m2Stat));
        calculatedData.add(new CalculatedDataObject("m3", m3Theo, m3Stat));
        calculatedData.add(new CalculatedDataObject("m4", m4Theo, m4Stat));
        calculatedData.add(new CalculatedDataObject("As", AsTheo, AsStat));
        calculatedData.add(new CalculatedDataObject("Ek", EkTheo, EkStat));
        calculatedData.add(new CalculatedDataObject("chi2", null, chi2));

        return calculatedData;
    }

    private double getMathWait(int num) {
        double mathwait = 0.0;
        int genValues=generatedRandomValues.size();
        for (double k:generatedRandomValues) {
            mathwait += Math.pow(k, num);
        }
        mathwait /= genValues;
        return mathwait;
    }

    private double getStatCentralMoment(int num) {
        double centralMoment = 0.0;
        int generatedValuesCount = generatedRandomValues.size();
        for (double d : generatedRandomValues) {
            centralMoment += Math.pow(d - Mean, num);
        }
        centralMoment /= generatedValuesCount;
        return centralMoment;
    }

    private double getChi2() {
        double chi2 = 0.0;
        for (Integer key : getHistogramData().keySet()) {
            double m = getHistogramData().get(key);
            double n = generatedRandomValues.size();
            double p = uniformDistribution.cumulativeProbability(
                    generatedRandomValues.get(getHistogramData().get(key) - 1));
            chi2 += (Math.pow(m - n * p, 2) / (n * p));
        }
        return chi2;
    }

    private Map<Integer, Integer> getHistogramData() {
        Map<Integer, Integer> histogramData = new HashMap<>(20);
        List<Double> data = generatedRandomValues;

        double boundInc = (data.get(data.size() - 1) - data.get(0))  / 20;
        double lowerBound = data.get(0);
        double upperBound = lowerBound + boundInc;
        int index = 0;
        int count = 0;
        for (double d : data) {
            if (d >= lowerBound && d < upperBound) {
                count++;
            } else {
                lowerBound += boundInc;
                upperBound += boundInc;
                histogramData.put(index, count);
                count = 0;
                index++;
                count++;
            }
        }
        return histogramData;
    }





}
