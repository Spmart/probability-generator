package ru.psuti.courseproject.core.generator;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import ru.psuti.courseproject.core.pojo.CalculatedDataObject;

import java.util.*;

public class LogNormalDistributionGenerator implements Generator {
    private static final double SHAPE = 0.0; // Форма (k)
    private static final double SCALE = 0.5; // Размер (theta)
    private static final double MEAN = SHAPE * SCALE;
    //private static final double SIGMA = (Math.exp(Math.pow(SCALE, 2) - 1)) * Math.exp(2 * SHAPE + Math.pow(SCALE, 2));
    //private static final double SIGMA = Math.exp(SHAPE + Math.pow(SCALE, 2) / 2);
    private List<Double> generatedRandomValues; // Список сгенерированных случайных величин
    private List<CalculatedDataObject> calculatedData; // Список объектов рассчитанных величин
    private LogNormalDistribution logNormalDistribution;

    public LogNormalDistributionGenerator(){
        generatedRandomValues = new ArrayList<>();
        calculatedData = new ArrayList<>();
        logNormalDistribution = new LogNormalDistribution(SHAPE, SCALE);
    }

    public List<Double> getGeneratedRandomValues(int sampleSize) {
        for (int i = 0; i < sampleSize; i++) {
            generatedRandomValues.add(Double.valueOf(
                    String.format(Locale.US, "%.3f", logNormalDistribution .sample())
            ));
        }
        generatedRandomValues.sort(Double::compareTo);
        return generatedRandomValues;
    }

    public List<CalculatedDataObject> getCalculatedData() {

        double SCALE_P2 = Math.pow(SCALE, 2);

        double a1Theo = Math.exp(SHAPE + 0.5 *  Math.pow(SCALE, 2)); // Математическое ожидание (a1)
        double a1Stat = getStatMoment(1); // Сумма частного случайной величины к общему количеству случайных величин. (a1)

        double m2Theo = Math.exp(2 * SHAPE + ((Math.pow(2, 2) * SCALE_P2) / 2));
        double m2Stat = getStatCentralMoment(2);

        double m3Theo = Math.exp(3 * SHAPE + ((Math.pow(3, 2) * SCALE_P2) / 2));
        double m3Stat = getStatCentralMoment(3);

        double m4Theo = Math.exp(4 * SHAPE + ((Math.pow(4, 2) * SCALE_P2) / 2));
        double m4Stat = getStatCentralMoment(4);

        //double AsTheo = Math.exp(- SHAPE - SCALE_P2 / 2) * (Math.exp(SCALE_P2) + 2) * Math.sqrt(Math.exp(SCALE_P2) - 1);
        //double AsTheo = (Math.exp(SCALE_P2) + 2) * Math.sqrt(Math.exp(SCALE_P2) - 1);
        double AsTheo = m3Theo / Math.pow(SCALE, 3);
        double AsStat = m3Stat / Math.pow(SCALE, 3);

        //double EkTheo = Math.exp(4 * SCALE_P2) + 2 * Math.exp(3 * SCALE_P2) + 3 * Math.exp(2 * SCALE_P2) - 6;
        double EkTheo = (m4Theo / Math.pow(SCALE, 4)) - 3;
        double EkStat = (m4Stat / Math.pow(SCALE, 4)) - 3;

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
            double p = logNormalDistribution.cumulativeProbability(
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
