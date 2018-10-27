package ru.psuti.courseproject.core.generator;

import org.apache.commons.math3.distribution.GammaDistribution;
import ru.psuti.courseproject.core.pojo.CalculatedDataObject;

import java.util.*;

/**
 * author chillurbrain
 */
public class GammaDistributionGenerator implements Generator {
    private static final double SHAPE = 2.0; // Форма (k)
    private static final double SCALE = 0.5; // Размер (theta)
    private static final double MEAN = SHAPE * SCALE;
    private static final double SIGMA = Math.sqrt(SHAPE * Math.pow(SCALE, 2));
    private List<Double> generatedRandomValues; // Список сгенерированных случайных величин
    private List<CalculatedDataObject> calculatedData; // Список объектов рассчитанных величин
    private GammaDistribution gammaDistribution; // Закон распределения

    public GammaDistributionGenerator() {
        generatedRandomValues = new ArrayList<>();
        calculatedData = new ArrayList<>();
        gammaDistribution = new GammaDistribution(SHAPE, SCALE);
    }

    public List<Double> getGeneratedRandomValues(int sampleSize) {
        for (int i = 0; i < sampleSize; i++) {
            generatedRandomValues.add(Double.valueOf(
                    String.format(Locale.US, "%.3f", gammaDistribution.sample())
            ));
        }
        generatedRandomValues.sort(Double::compareTo);
        return generatedRandomValues;
    }


    public List<CalculatedDataObject> getCalculatedData() {
        double a1Theo = SHAPE * SCALE; // Математическое ожидание (a1)
        double a1Stat = getStatMoment(1); // Сумма частного случайной величины к общему количеству случайных величин. (a1)

        double m2Theo = SHAPE * Math.pow(SCALE, 2);
        double m2Stat = getStatCentralMoment(2);

        double m3Theo = 2.0 / Math.sqrt(SHAPE) * Math.pow(SIGMA, 3);
        double m3Stat = getStatCentralMoment(3);

        double m4Theo = (6.0 / SHAPE + 3.0) * Math.pow(SIGMA, 4);
        double m4Stat = getStatCentralMoment(4);

        double AsTheo = 2.0 / Math.sqrt(SHAPE);
        double AsStat = m3Stat / Math.pow(SIGMA, 3);

        double EkTheo = 6.0 / SHAPE;
        double EkStat = (m4Stat / Math.pow(SIGMA, 4)) - 3;

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

    private double getStatMoment(int order) {
        double moment = 0.0;
        int generatedValuesCount = generatedRandomValues.size();

        for (double d : generatedRandomValues) {
            moment += Math.pow(d, order);
        }

        moment /= generatedValuesCount;

        return moment;
    }

    private double getStatCentralMoment(int order) {
        double centralMoment = 0.0;
        int generatedValuesCount = generatedRandomValues.size();

        for (double d : generatedRandomValues) {
            centralMoment += Math.pow(d - MEAN, order);
        }

        centralMoment /= generatedValuesCount;

        return centralMoment;
    }

    private double getChi2() {
        double chi2 = 0.0;

        for (Integer key : getHistogramData().keySet()) {
            double m = getHistogramData().get(key);
            double n = generatedRandomValues.size();
            double p = gammaDistribution.cumulativeProbability(
                    generatedRandomValues.get(getHistogramData().get(key) - 1));
            chi2 += (Math.pow(m - n * p, 2) / (n * p));
        }

        return chi2;
    }

    private Map<Integer, Integer> getHistogramData() {
        Map<Integer, Integer> histogramData = new HashMap<>(20);
        List<Double> data = generatedRandomValues;

        double boundInc = (data.get(data.size() - 1) - data.get(0)) / 20;
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
