package ru.psuti.courseproject.core.generator;

import ru.psuti.courseproject.core.pojo.CalculatedDataObject;
import org.apache.commons.math3.distribution.NormalDistribution;
import java.util.*;

public class NormalDistributionGenerator implements Generator {

    private static final double Mx = 0;  // Математическое ожидание
    private static final double SIGMA = 1;  // Дисперсия
    private static final double MEAN = Mx;  // Среднее

    private List<Double> generatedRandomValues;  // Список сгенерированных случайных величин
    private List<CalculatedDataObject> calculatedData;  // Список объектов рассчитанных величин
    private Random random = new Random();

    public NormalDistributionGenerator() {
        generatedRandomValues = new ArrayList<>();
        calculatedData = new ArrayList<>();
    }

    @Override
    public List<Double> getGeneratedRandomValues(int sampleSize) {
        /* Хак для нечетных размеров выборки. Добавляется одно дополнительное значение, т.к. в цикле добавление попарное. */
        if (sampleSize % 2 != 0) {
            generatedRandomValues.add(getPairRandomValues().get(0));
        }
        for (int i = 0; i < sampleSize / 2; i++) {
            generatedRandomValues.addAll(getPairRandomValues());
        }
        generatedRandomValues.sort(Double::compareTo);
        return generatedRandomValues;
    }

    @Override
    public List<CalculatedDataObject> getCalculatedData() {
        double a1Theo = Mx; // Математическое ожидание (a1)
        double a1Stat = getStatMoment(1); // Сумма частного случайной величины к общему количеству случайных величин. (a1)

        double m2Theo = SIGMA * SIGMA; // Дисперсия
        double m2Stat = getStatCentralMoment(2);

        double m3Theo = 0; // Коэффициент ассиметрии
        double m3Stat = getStatCentralMoment(3);

        double m4Theo = -3;  //(Mx^4/SIGMA^4)-3 но все равно 0 - 3
        double m4Stat = getStatCentralMoment(4);

        double AsTheo = 0;
        double AsStat = m3Stat / Math.pow(SIGMA, 3);

        double EkTheo = -3;
        double EkStat = (m4Stat / Math.pow(SIGMA, 4));

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

    /**
     * Метод для получения пары случайных значений по методу преобразования Бокса-Мюллера.
     * @return массив из двух элементов.
     */
    private ArrayList<Double> getPairRandomValues() {
        double firstRandomValue, secondRandomValue, sum;
        ArrayList<Double> resultRandomValues = new ArrayList<>();

        do {
            firstRandomValue = random.nextDouble() * 2 - 1; // Случайное число, равномерное распределение, [-1; 1]
            secondRandomValue = random.nextDouble() * 2 - 1;
            sum = Math.pow(firstRandomValue, 2) + Math.pow(secondRandomValue, 2);
        } while (sum >= 1 || sum == 0);  // Должно выполняться условие 0 < firstRandomValue^2 + secondRandomValue^2 <= 1

        /* Преобразование Бокса-Мюллера */
        resultRandomValues.add(firstRandomValue * Math.sqrt(-2 * Math.log(sum) / sum));
        resultRandomValues.add(secondRandomValue * Math.sqrt(-2 * Math.log(sum) / sum));

        return resultRandomValues;
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

    private double getMean(List<Double> generatedRandomValues) {
        double mean = 0.0;
        for (double randomValue : generatedRandomValues) {
            mean += randomValue;
        }
        return mean / generatedRandomValues.size();
    }

    private double getStatCentralMoment(int order) {
        double centralMoment = 0.0;
        double mean = getMean(generatedRandomValues);
        int generatedValuesCount = generatedRandomValues.size();

        for (double d : generatedRandomValues) {
            centralMoment += Math.pow(d - mean, order);
        }

        centralMoment /= generatedValuesCount;
        if (order == 4) {
            centralMoment *= -1;
        }

        return centralMoment;
    }

    private double getChi2() {
        double chi2 = 0.0;

        for (Integer key : getHistogramData().keySet()) {
            double m = getHistogramData().get(key);
            double n = generatedRandomValues.size();
            double p = new NormalDistribution().cumulativeProbability(
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